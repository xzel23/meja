/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.SheetEvent;
import com.dua3.meja.ui.CellRenderer;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.util.CellValueHelper;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.ui.DetachableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Flow;

/**
 * Swing component for displaying instances of {@link Sheet}.
 */
public final class SwingSheetView extends JPanel implements SheetView {
    private static final Logger LOG = LogManager.getLogger(SwingSheetView.class);
    private static final double FLOATING_TOOLBAR_GAP = 4.0;

    private static final String ACTION_EDITOR_COMMIT = "editor.commit";
    private static final String ACTION_EDITOR_NEWLINE = "editor.newline";
    private static final String ACTION_EDITOR_ABORT = "editor.abort";

    private final transient SwingSheetViewDelegate delegate;
    private final transient SwingSheetPane sheetPane;
    private final transient SwingSearchDialog searchDialog = new SwingSearchDialog(this);
    private final transient ScaledTextEditorPane editor = new ScaledTextEditorPane();
    private final transient JLayeredPane layeredPane;
    private final transient KeyEventDispatcher editingKeyEventDispatcher = this::dispatchEditingKeyEvent;

    private final transient Flow.Subscriber<SheetEvent> sheetEventSubscriber = new Flow.Subscriber<>() {
        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            if (sheetSubscription != null) {
                sheetSubscription.cancel();
            }
            sheetSubscription = subscription;
            sheetSubscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(SheetEvent item) {
            if (Objects.equals(item.type(), SheetEvent.ACTIVE_CELL_CHANGED)) {
                SheetEvent.ActiveCellChanged evt = (SheetEvent.ActiveCellChanged) item;
                if (!isSameLogicalCell(delegate.getEditingCell().orElse(null), evt.newValue())) {
                    runOnEdt(() -> stopEditing(true));
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            LOG.error("error with subscription", throwable);
        }

        @Override
        public void onComplete() {
            sheetSubscription = null;
        }
    };

    private transient Flow.@Nullable Subscription sheetSubscription;

    private boolean editable;
    private boolean updating;
    private boolean editingDispatcherInstalled;
    private transient @Nullable Container toolbarParent;

    /**
     * Constructor.
     *
     * @param sheet the {@link Sheet}
     */
    public SwingSheetView(Sheet sheet) {
        super(new BorderLayout());

        this.delegate = new SwingSheetViewDelegate(sheet, this, CellRenderer::new);
        this.sheetPane = new SwingSheetPane(delegate);
        this.layeredPane = createLayeredPane();

        init();
        getSheet().subscribe(sheetEventSubscriber);
    }

    private JLayeredPane createLayeredPane() {
        return new JLayeredPane() {
            @Override
            public void doLayout() {
                sheetPane.setBounds(0, 0, getWidth(), getHeight());
                updateEditorBounds();
            }
        };
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (!editingDispatcherInstalled) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(editingKeyEventDispatcher);
            editingDispatcherInstalled = true;
        }
    }

    @Override
    public void removeNotify() {
        if (editingDispatcherInstalled) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(editingKeyEventDispatcher);
            editingDispatcherInstalled = false;
        }
        stopEditing(false);
        if (sheetSubscription != null) {
            sheetSubscription.cancel();
            sheetSubscription = null;
        }
        searchDialog.dispose();
        super.removeNotify();
    }

    @Override
    public void repaintCell(Cell cell) {
        runOnEdt(() -> {
            Cell logicalCell = cell.getLogicalCell();
            Rectangle2f r = delegate.getCellRect(logicalCell);
            float m = getDelegate().getSelectionStrokeWidth() / 2.0f;
            CellStyle cs = logicalCell.getCellStyle();
            r = r.addMargin(
                    Math.max(m, cs.getBorderStyle(Direction.WEST).width()),
                    Math.max(m, cs.getBorderStyle(Direction.NORTH).width()),
                    Math.max(m, cs.getBorderStyle(Direction.EAST).width()),
                    Math.max(m, cs.getBorderStyle(Direction.SOUTH).width())
            );
            sheetPane.repaintSheet(r);
            if (isSameLogicalCell(delegate.getEditingCell().orElse(null), logicalCell)) {
                updateEditorBounds();
            }
        });
    }

    /**
     * Scroll the currently selected cell into view.
     */
    @Override
    public void scrollToCurrentCell() {
        runOnEdt(() -> {
            try (var __ = delegate.readLock("SwingSheetView.scrollToCurrentCell()")) {
                sheetPane.ensureCellIsVisible(delegate.getCurrentLogicalCell());
            }
            updateEditorBounds();
        });
    }

    /**
     * Set the current row and column.
     *
     * @param rowNum number of row to be set
     * @param colNum number of column to be set
     * @return true if the current logical cell changed
     */
    @Override
    public boolean setCurrentCell(int rowNum, int colNum) {
        return delegate.setCurrentCell(rowNum, colNum);
    }

    /**
     * End edit mode for the current cell.
     *
     * @param commit true if the content of the edited cell is to be updated
     */
    @Override
    public void stopEditing(boolean commit) {
        runOnEdt(() -> delegate.stopEditing().ifPresent(cell -> {
            if (commit) {
                LOG.debug("committing cell content: {}", cell);
                updateCellContent(cell);
                repaintCell(cell);
            }

            editor.setEditable(false);
            editor.setToolbarApplicationParent(null);
            editor.setToolbarLocation(DetachableNode.Location.HIDDEN);
            editor.setVisible(false);
            editor.setText("");
            requestFocusInWindow();
        }));
    }

    /**
     * Backward-compatible method previously used by the old {@link CellEditor} path.
     */
    public void stoppedEditing() {
        stopEditing(false);
    }

    @Override
    public void copyToClipboard() {
        SwingUtil.copyToClipboard(delegate.getCurrentLogicalCell().getAsFormattedText(getLocale()));
    }

    private void init() {
        layeredPane.add(sheetPane, JLayeredPane.DEFAULT_LAYER);

        initEditor();
        layeredPane.add(editor, JLayeredPane.PALETTE_LAYER);

        add(layeredPane, BorderLayout.CENTER);

        initKeyBindings();

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                onKeyTyped(event);
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateEditorBounds();
            }
        });
        sheetPane.getViewport().addChangeListener(evt -> updateEditorBounds());
        sheetPane.getHorizontalScrollBar().addAdjustmentListener(e -> updateEditorBounds());
        sheetPane.getVerticalScrollBar().addAdjustmentListener(e -> updateEditorBounds());

        SwingUtilities.invokeLater(this::focusView);
    }

    private void initEditor() {
        editor.setVisible(false);
        editor.setEditable(false);
        editor.setWrapText(false);
        editor.setEnterKeyInsertsNewline(true);
        editor.setBorder(BorderFactory.createEmptyBorder());
        editor.setViewportBorder(BorderFactory.createEmptyBorder());
        editor.getTextComponent().setBorder(BorderFactory.createEmptyBorder());
        editor.addPropertyChangeListener("text", evt -> updateEditorBounds());

        InputMap inputMap = editor.getTextComponent().getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ACTION_EDITOR_COMMIT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), ACTION_EDITOR_NEWLINE);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACTION_EDITOR_ABORT);

        ActionMap actionMap = editor.getTextComponent().getActionMap();
        actionMap.put(ACTION_EDITOR_COMMIT, SwingUtil.createAction("CommitCellEdit", () -> {
            boolean wasEditing = delegate.isEditing();
            stopEditing(true);
            if (wasEditing) {
                move(Direction.SOUTH);
            }
        }));
        actionMap.put(ACTION_EDITOR_NEWLINE, SwingUtil.createAction("InsertEditorNewLine", () -> editor.replaceSelection("\n")));
        actionMap.put(ACTION_EDITOR_ABORT, SwingUtil.createAction("AbortCellEdit", () -> stopEditing(false)));
    }

    private void initKeyBindings() {
        final InputMap inputMap = getInputMap(WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), Actions.MOVE_UP);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), Actions.MOVE_UP);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), Actions.MOVE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), Actions.MOVE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), Actions.MOVE_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), Actions.MOVE_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), Actions.MOVE_RIGHT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), Actions.MOVE_RIGHT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), Actions.PAGE_UP);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), Actions.PAGE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_DOWN_MASK), Actions.MOVE_HOME);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_DOWN_MASK), Actions.MOVE_END);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), Actions.START_EDITING);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), Actions.SHOW_SEARCH_DIALOG);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), Actions.COPY);

        final ActionMap actionMap = getActionMap();
        for (Actions action : Actions.values()) {
            actionMap.put(action, SwingUtil.createAction(action.name(), () -> action.action().accept(this)));
        }
    }

    @Override
    public void focusView() {
        requestFocusInWindow();
    }

    /**
     * Show the search dialog.
     */
    @Override
    public void showSearchDialog() {
        searchDialog.setVisible(true);
    }

    /**
     * Enter edit mode for the current cell.
     */
    @Override
    public void startEditing() {
        if (!isEditable()) {
            return;
        }

        runOnEdt(() -> delegate.startEditing().ifPresent(cell -> {
            scrollToCurrentCell();
            CellStyle cellStyle = cell.getCellStyle();
            editor.setTextFont(cellStyle.getFont().scaled(delegate.getScale().sy()));
            editor.setFontScale(delegate.getScale().sy());
            editor.setWrapText(cellStyle.isStyleWrapping());
            editor.setCellText(cell.getCellType() == CellType.FORMULA ? RichText.valueOf("=" + cell.getFormula()) : cell.getAsText(getLocale()));
            editor.selectAll();
            editor.setToolbarApplicationParent(toolbarParent);
            editor.setToolbarLocation(toolbarParent == null ? DetachableNode.Location.FLOATING : DetachableNode.Location.APPLICATION);
            editor.setEditable(true);
            editor.setVisible(true);
            layeredPane.moveToFront(editor);
            updateEditorBounds();
            requestEditorFocusWithRetry();
            SwingUtilities.invokeLater(() -> {
                updateEditorBounds();
                requestEditorFocusWithRetry();
            });
        }));
    }

    private void requestEditorFocusWithRetry() {
        requestEditorFocus();
        scheduleEditorFocusRetry(50);
        scheduleEditorFocusRetry(150);
    }

    private void scheduleEditorFocusRetry(int delayMs) {
        javax.swing.Timer timer = new javax.swing.Timer(delayMs, evt -> {
            ((javax.swing.Timer) evt.getSource()).stop();
            if (delegate.isEditing() && editor.isVisible()) {
                requestEditorFocus();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void requestEditorFocus() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.toFront();
        }
        if (window != null && !window.isFocused()) {
            window.requestFocus();
        }
        editor.getTextComponent().requestFocusInWindow();
        editor.getTextComponent().requestFocus();
        editor.requestFocusInWindow();
    }

    private boolean dispatchEditingKeyEvent(KeyEvent event) {
        if (!delegate.isEditing() || !editor.isVisible() || !editor.isEditable() || event.isConsumed()) {
            return false;
        }

        if (event.getID() != KeyEvent.KEY_PRESSED
                && event.getID() != KeyEvent.KEY_RELEASED
                && event.getID() != KeyEvent.KEY_TYPED) {
            return false;
        }

        Component textComponent = editor.getTextComponent();
        Object source = event.getSource();
        if (source == textComponent || source instanceof Component c && SwingUtilities.isDescendingFrom(c, textComponent)) {
            return false;
        }

        KeyEvent forwarded = new KeyEvent(
                textComponent,
                event.getID(),
                event.getWhen(),
                event.getModifiersEx(),
                event.getKeyCode(),
                event.getKeyChar(),
                event.getKeyLocation()
        );
        textComponent.dispatchEvent(forwarded);
        event.consume();
        return true;
    }

    @Override
    public void updateContent() {
        LOG.trace("updateContent()");

        runOnEdt(() -> {
            if (updating) {
                return;
            }

            try (var __ = getSheet().readLock("SwingSheetView.updateContent()")) {
                updating = true;
                Sheet sheet = getSheet();
                delegate.update(getDpi());
                delegate.getSheetPainter().update(sheet);
                revalidate();
                repaint();
                updateEditorBounds();
            } finally {
                updating = false;
            }
        });
    }

    @Override
    public Scale2f getDisplayScale() {
        return SwingUtil.getDisplayScale(this);
    }

    Scale2f getScale() {
        return delegate.getScale();
    }

    @Override
    public SwingSheetViewDelegate getDelegate() {
        return delegate;
    }

    @Override
    public Locale getLocale() {
        Locale locale = super.getLocale();
        assert locale != null;
        return locale;
    }

    @Override
    public void validate() {
        super.validate();
        delegate.updateLayout();
        updateEditorBounds();
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    /**
     * Returns the container used as toolbar application parent.
     *
     * @return the toolbar parent container or {@code null}
     */
    public @Nullable Container getToolbarParent() {
        return toolbarParent;
    }

    /**
     * Sets the container used as toolbar application parent.
     * A {@code null} parent causes the toolbar to be shown as floating while editing.
     *
     * @param toolbarParent the toolbar parent container or {@code null}
     */
    public void setToolbarParent(@Nullable Container toolbarParent) {
        runOnEdt(() -> {
            this.toolbarParent = toolbarParent;
            if (delegate.isEditing() && editor.isVisible()) {
                editor.setToolbarApplicationParent(toolbarParent);
                editor.setToolbarLocation(toolbarParent == null ? DetachableNode.Location.FLOATING : DetachableNode.Location.APPLICATION);
                updateEditorBounds();
            }
        });
    }

    /**
     * Returns the screen resolution in dots per inch (DPI).
     *
     * @return the screen resolution in DPI
     */
    static int getDpi() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    private void updateCellContent(Cell cell) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        NumberFormat numberFormat = NumberFormat.getInstance(getLocale());
        new CellValueHelper(numberFormat, dateFormatter).setCellValue(cell, editor.getText());
    }

    private void onKeyTyped(KeyEvent event) {
        if (event.isConsumed() || delegate.isEditing() || !isFocusOwner() || !isEditable()) {
            return;
        }

        if (event.isAltDown() || event.isControlDown() || event.isMetaDown()) {
            return;
        }

        char c = event.getKeyChar();
        if (c == KeyEvent.CHAR_UNDEFINED || Character.isISOControl(c)) {
            return;
        }

        startEditing();
        SwingUtilities.invokeLater(() -> {
            if (delegate.isEditing() && editor.isVisible() && editor.isEditable()) {
                editor.replaceSelection(String.valueOf(c));
            }
        });
        event.consume();
    }

    private void updateEditorBounds() {
        runOnEdt(() -> delegate.getEditingCell().ifPresent(cell -> {
            if (!editor.isVisible()) {
                return;
            }

            Rectangle2f cellRectInLocal = getCellRectInLocal(cell);
            double x = cellRectInLocal.x() + 1;
            double y = cellRectInLocal.y() + 1;
            double minWidth = Math.max(1.0, cellRectInLocal.width() - 2);
            double minHeight = Math.max(1.0, cellRectInLocal.height() - 2);
            boolean styleWrapping = cell.getCellStyle().isStyleWrapping();

            EditorSize editorSize = computeEditorSize(x, y, minWidth, minHeight, styleWrapping);
            editor.setWrapText(editorSize.wrapText());
            int left = (int) Math.round(x + 1);
            int top = (int) Math.round(y + 1);
            int right = (int) Math.round(x + editorSize.width());
            int bottom = (int) Math.round(y + editorSize.height());
            editor.setBounds(
                    left,
                    top,
                    Math.max(1, right - left + 1),
                    Math.max(1, bottom - top + 1)
            );
            editor.revalidate();
            editor.repaint();
            updateFloatingToolbarPosition(x, y, editorSize.width(), editorSize.height());
        }));
    }

    private EditorSize computeEditorSize(double x, double y, double minWidth, double minHeight, boolean styleWrapping) {
        String text = editor.getText().toString();
        String displayText = text.isEmpty() ? " " : text;

        double hPadding = Math.max(4.0, 2.0 * delegate.getPaddingX() * delegate.getScale().sx());
        double vPadding = Math.max(2.0, 2.0 * delegate.getPaddingY() * delegate.getScale().sy());

        double width;
        boolean wrapText;
        if (styleWrapping) {
            wrapText = true;
            width = minWidth;
        } else {
            double naturalWidth = measureLongestLineWidth(displayText) + hPadding;
            double maxWidth = Math.max(minWidth, getEditorRightLimitX() - x);
            wrapText = naturalWidth > maxWidth + 0.5;
            width = wrapText ? maxWidth : Math.max(minWidth, naturalWidth);
        }

        boolean multiline = wrapText || text.indexOf('\n') >= 0;
        double height = minHeight;
        if (multiline) {
            double contentWidth = Math.max(1.0, width - hPadding);
            double naturalHeight = measureTextHeight(displayText, wrapText ? contentWidth : 0.0) + vPadding;
            height = Math.max(minHeight, naturalHeight);
        }

        return new EditorSize(Math.rint(width), Math.rint(height), wrapText);
    }

    private double getEditorRightLimitX() {
        double verticalScrollBarWidth = sheetPane.getVerticalScrollBar().isVisible()
                ? sheetPane.getVerticalScrollBar().getWidth()
                : 0.0;
        return Math.max(1.0, getWidth() - verticalScrollBarWidth);
    }

    private double measureLongestLineWidth(String text) {
        FontMetrics fm = editor.getTextComponent().getFontMetrics(editor.getTextComponent().getFont());
        double width = 0.0;
        for (String line : text.split("\n", -1)) {
            width = Math.max(width, fm.stringWidth(line.isEmpty() ? " " : line));
        }
        return width;
    }

    private double measureTextHeight(String text, double wrappingWidth) {
        FontMetrics fm = editor.getTextComponent().getFontMetrics(editor.getTextComponent().getFont());
        if (wrappingWidth <= 0.0) {
            int lineCount = Math.max(1, text.split("\n", -1).length);
            return lineCount * (double) fm.getHeight();
        }

        FontRenderContext frc = new FontRenderContext(null, true, true);
        float width = (float) Math.max(1.0, wrappingWidth);
        double totalHeight = 0.0;

        for (String line : text.split("\n", -1)) {
            String content = line.isEmpty() ? " " : line;
            AttributedString attributedText = new AttributedString(content);
            attributedText.addAttribute(TextAttribute.FONT, editor.getTextComponent().getFont());
            AttributedCharacterIterator it = attributedText.getIterator();
            LineBreakMeasurer lbm = new LineBreakMeasurer(it, frc);
            int end = it.getEndIndex();

            if (lbm.getPosition() >= end) {
                totalHeight += fm.getHeight();
                continue;
            }

            while (lbm.getPosition() < end) {
                TextLayout layout = lbm.nextLayout(width);
                if (layout == null) {
                    break;
                }
                totalHeight += layout.getAscent() + layout.getDescent() + layout.getLeading();
            }
        }

        return Math.max(totalHeight, fm.getHeight());
    }

    private void updateFloatingToolbarPosition(double editorX, double editorY, double editorWidth, double editorHeight) {
        if (editor.getToolbarLocation() != DetachableNode.Location.FLOATING || !editor.isVisible()) {
            return;
        }

        Window toolbarWindow = findFloatingToolbarWindow();
        if (toolbarWindow == null) {
            return;
        }

        Point editorTopLeftOnScreen = new Point((int) Math.round(editorX), (int) Math.round(editorY));
        SwingUtilities.convertPointToScreen(editorTopLeftOnScreen, this);
        Rectangle editorBoundsOnScreen = new Rectangle(
                editorTopLeftOnScreen.x,
                editorTopLeftOnScreen.y,
                Math.max(1, (int) Math.round(editorWidth)),
                Math.max(1, (int) Math.round(editorHeight))
        );

        Rectangle visualBounds = getVisualBounds(editorBoundsOnScreen);
        double toolbarWidth = Math.max(1.0, toolbarWindow.getWidth());
        double toolbarHeight = Math.max(1.0, toolbarWindow.getHeight());

        double x = clamp(
                editorBoundsOnScreen.getMinX(),
                visualBounds.getMinX(),
                Math.max(visualBounds.getMinX(), visualBounds.getMaxX() - toolbarWidth)
        );

        double aboveY = editorBoundsOnScreen.getMinY() - toolbarHeight - FLOATING_TOOLBAR_GAP;
        double belowY = editorBoundsOnScreen.getMaxY() + FLOATING_TOOLBAR_GAP;
        double y = aboveY >= visualBounds.getMinY()
                ? aboveY
                : clamp(
                belowY,
                visualBounds.getMinY(),
                Math.max(visualBounds.getMinY(), visualBounds.getMaxY() - toolbarHeight)
        );

        toolbarWindow.setLocation((int) Math.round(x), (int) Math.round(y));
    }

    private Rectangle getVisualBounds(Rectangle editorBoundsOnScreen) {
        GraphicsConfiguration gc = findGraphicsConfiguration(editorBoundsOnScreen);
        Rectangle bounds = gc.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        return new Rectangle(
                bounds.x + insets.left,
                bounds.y + insets.top,
                Math.max(1, bounds.width - insets.left - insets.right),
                Math.max(1, bounds.height - insets.top - insets.bottom)
        );
    }

    private GraphicsConfiguration findGraphicsConfiguration(Rectangle editorBoundsOnScreen) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point probe = new Point(
                (int) Math.round(editorBoundsOnScreen.getCenterX()),
                (int) Math.round(editorBoundsOnScreen.getCenterY())
        );

        for (GraphicsDevice device : ge.getScreenDevices()) {
            GraphicsConfiguration gc = device.getDefaultConfiguration();
            if (gc.getBounds().contains(probe)) {
                return gc;
            }
        }

        GraphicsConfiguration ownConfig = getGraphicsConfiguration();
        if (ownConfig != null) {
            return ownConfig;
        }

        return ge.getDefaultScreenDevice().getDefaultConfiguration();
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private @Nullable Window findFloatingToolbarWindow() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner == null) {
            return null;
        }

        for (Window window : Window.getWindows()) {
            if (!(window instanceof JDialog dialog) || !dialog.isShowing() || dialog.getOwner() != owner) {
                continue;
            }

            if (containsToolbar(dialog.getContentPane())) {
                return dialog;
            }
        }

        return null;
    }

    private static boolean containsToolbar(Component component) {
        if (component instanceof JToolBar) {
            return true;
        }
        if (!(component instanceof Container container)) {
            return false;
        }
        for (Component child : container.getComponents()) {
            if (containsToolbar(child)) {
                return true;
            }
        }
        return false;
    }

    private Rectangle2f getCellRectInLocal(Cell cell) {
        Rectangle2f cellRectInSheet = delegate.getCellRect(cell.getLogicalCell());
        double xMin = toLocalX(cellRectInSheet.xMin(), true);
        double xMax = toLocalX(cellRectInSheet.xMax(), false);
        double yMin = toLocalY(cellRectInSheet.yMin(), true);
        double yMax = toLocalY(cellRectInSheet.yMax(), false);
        return Rectangle2f.of(
                (float) xMin,
                (float) yMin + 1,
                (float) Math.max(1.0, xMax - xMin),
                (float) Math.max(1.0, yMax - yMin + 1)
        );
    }

    private double toLocalX(float xInPoints, boolean leadingEdge) {
        double splitX = delegate.getSplitXInPoints();
        double x = delegate.getRowLabelWidthInPixels() + xInPoints * delegate.getScale().sx();
        boolean rightPane = leadingEdge ? xInPoints >= splitX : xInPoints > splitX;
        if (rightPane) {
            x -= sheetPane.getHorizontalScrollBar().getValue();
            if (delegate.getSplitColumn() > 0) {
                x += delegate.getSplitLineWidth();
            }
        }
        return x;
    }

    private double toLocalY(float yInPoints, boolean leadingEdge) {
        double splitY = delegate.getSplitYInPoints();
        double y = delegate.getColumnLabelHeightInPixels() + yInPoints * delegate.getScale().sy();
        boolean bottomPane = leadingEdge ? yInPoints >= splitY : yInPoints > splitY;
        if (bottomPane) {
            y -= sheetPane.getVerticalScrollBar().getValue();
            if (delegate.getSplitRow() > 0) {
                y += delegate.getSplitLineHeight();
            }
        }
        return y;
    }

    private static boolean isSameLogicalCell(@Nullable Cell c1, @Nullable Cell c2) {
        if (c1 == c2) {
            return true;
        }
        if (c1 == null || c2 == null) {
            return false;
        }
        Cell logical1 = c1.getLogicalCell();
        Cell logical2 = c2.getLogicalCell();
        return logical1.getSheet() == logical2.getSheet()
                && logical1.getRowNumber() == logical2.getRowNumber()
                && logical1.getColumnNumber() == logical2.getColumnNumber();
    }

    private static void runOnEdt(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    private record EditorSize(double width, double height, boolean wrapText) {}
}
