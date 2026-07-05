package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.util.CellValueHelper;
import com.dua3.utility.fx.controls.TextEditorPane;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PlatformHelper;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.ui.DetachableNode;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.awt.Toolkit;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Objects;

/**
 * The FxSheetView class is responsible for rendering a spreadsheet-like UI component,
 * using four quadrants to manage visible data sections efficiently.
 * This class incorporates scrollbars and quadrants to manage the display of data and handle
 * keyboard inputs for navigation. It also entangles scrollbar movements to ensure synchronized scrolling
 * across different quadrants.
 */
public final class FxSheetView extends StackPane implements SheetView {
    private static final Logger LOG = LogManager.getLogger(FxSheetView.class);
    private final ObservableSheet observableSheet;
    private final FxSheetViewDelegate delegate;
    private final FxSegmentView topSegment;
    private final FxSegmentView bottomSegment;

    private final ScrollBar hScrollbar = new ScrollBar();
    private final ScrollBar vScrollbar = new ScrollBar();
    private final TextEditorPane editor = new TextEditorPane();

    private final Region leftSpacer = new Region();

    private final DoubleProperty sheetScaleXProperty = new SimpleDoubleProperty(1.0);
    private final DoubleProperty sheetScaleYProperty = new SimpleDoubleProperty(1.0);

    private final BooleanProperty editableProperty = new SimpleBooleanProperty(false);

    private boolean updating = false;
    private boolean forwardingNavigationKey = false;
    private final EventHandler<KeyEvent> editingNavigationKeyFilter = this::handleEditingNavigationKey;

    private final ObjectProperty<@Nullable Pane> toolbarParentProperty = new SimpleObjectProperty<>(null);

    /**
     * Constructs a new instance of FxSheetView using the specified Sheet object.
     * Initializes the layout and scrollbars, and sets up the view quadrants based on the provided sheet.
     *
     * @param sheet the Sheet object to be used as a model for the FxSheetView; can be null, in which case
     *              an empty view is initialized.
     */
    public FxSheetView(Sheet sheet) {
        LOG.debug("FxSheetView({})", sheet);

        this.delegate = new FxSheetViewDelegate(sheet, this, getDpi());
        GridPane gridPane = new GridPane();

        // Create quadrants
        this.observableSheet = new ObservableSheet(sheet);

        ObservableList<Row> topRows = new FilteredList<>(observableSheet, row -> row.getRowNumber() < delegate.getSplitRow());
        ObservableList<Row> bottomRows = new FilteredList<>(observableSheet, row -> row.getRowNumber() >= delegate.getSplitRow());

        topSegment = new FxSegmentView(this, Side.TOP, delegate, topRows);
        bottomSegment = new FxSegmentView(this, Side.BOTTOM, delegate, bottomRows);

        // Create scrollbars
        hScrollbar.setOrientation(Orientation.HORIZONTAL);
        observableSheet.splitColumnProperty().addListener((v, o, n) -> hScrollbar.setMin(delegate.getSplitXInPoints()));
        widthProperty().addListener((v, o, n) -> updateHScrollbar());
        updateHScrollbar();

        vScrollbar.setOrientation(Orientation.VERTICAL);
        observableSheet.splitRowProperty().addListener((v, o, n) -> vScrollbar.setMin(delegate.getSplitYInPoints()));
        heightProperty().addListener((v, o, n) -> updateVScrollbar());
        updateVScrollbar();

        observableSheet.addLayoutListener(s -> {
            updateHScrollbar();
            updateVScrollbar();
        });
        observableSheet.currentCellProperty().addListener((v, o, n) -> {
            if (!isSameLogicalCell(delegate.getEditingCell().orElse(null), n)) {
                stopEditing(true);
            }
        });

        // set up horizontal scrollbar
        leftSpacer.prefHeightProperty().bind(hScrollbar.heightProperty());
        observableSheet.splitColumnProperty().addListener((v, o, n) -> updateLeftSpacer());
        updateLeftSpacer();

        Region bottomRightCorner = new Region();
        bottomRightCorner.prefWidthProperty().bind(vScrollbar.widthProperty());
        bottomRightCorner.prefHeightProperty().bind(hScrollbar.heightProperty());

        HBox hScrollbarContainer = new HBox(0.0, leftSpacer, hScrollbar, bottomRightCorner);
        HBox.setHgrow(leftSpacer, Priority.NEVER);
        HBox.setHgrow(hScrollbar, Priority.ALWAYS);
        HBox.setHgrow(bottomRightCorner, Priority.NEVER);

        // set up vertical scrollbar
        Region topSpacer = new Region();
        topSpacer.prefWidthProperty().bind(vScrollbar.widthProperty());
        topSpacer.prefHeightProperty().bind(topSegment.heightProperty());

        VBox vScrollbarContainer = new VBox(0.0, topSpacer, vScrollbar);
        VBox.setVgrow(topSpacer, Priority.NEVER);
        VBox.setVgrow(vScrollbar, Priority.ALWAYS);

        // Set layout constraints
        RowConstraints[] rc = {
                rowConstraints(Priority.NEVER),
                rowConstraints(Priority.ALWAYS),
                rowConstraints(Priority.NEVER)
        };
        gridPane.getRowConstraints().setAll(rc);
        GridPane.setHgrow(bottomSegment, Priority.ALWAYS);
        GridPane.setVgrow(bottomSegment, Priority.ALWAYS);

        ColumnConstraints[] cc = {
                columnConstraints(Priority.ALWAYS),
                columnConstraints(Priority.NEVER)
        };
        gridPane.getColumnConstraints().setAll(cc);

        // Add segments and scrollbar to GridPane
        gridPane.add(topSegment, 0, 0);
        gridPane.add(bottomSegment, 0, 1);
        gridPane.add(vScrollbarContainer, 1, 0, 1, 2);
        gridPane.add(hScrollbarContainer, 0, 2, 2, 1);

        // bind vertical scrollbar
        hScrollbar.valueProperty().addListener((v, o, n) -> updateEditorBounds());
        vScrollbar.valueProperty().addListener((v, o, n) -> {
            LOG.trace("set vscrollbar value to {}", n);
            double position = n.doubleValue() / Math.max(1.0, vScrollbar.getMax() - vScrollbar.getMin());
            bottomSegment.getFlow().setPosition(position);
            updateEditorBounds();
        });
        bottomSegment.getFlow().positionProperty().addListener((v, o, n) -> {
            double position = n.doubleValue() * (vScrollbar.getMax() - vScrollbar.getMin());
            updateVScrollbar();
            vScrollbar.setValue(position);
        });

        // Add the GridPane to the StackPane
        getChildren().add(gridPane);
        initEditor();
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, editingNavigationKeyFilter);
            }
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, editingNavigationKeyFilter);
            }
        });

        hScrollbar.setValue(0);
        vScrollbar.setValue(0);

        setFocusTraversable(true);
        setOnKeyPressed(this::onKeyPressed);

        updateContent();

        bottomSegment.getFlow().setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            double deltaX = event.getDeltaX();
            hScrollbar.setValue(Math.clamp(hScrollbar.getValue() - deltaX * delegate.getScale().sx(), hScrollbar.getMin(), hScrollbar.getMax()));
            vScrollbar.setValue(Math.clamp(vScrollbar.getValue() - deltaY * delegate.getScale().sy(), vScrollbar.getMin(), vScrollbar.getMax()));
        });

        observableSheet.zoomProperty().addListener((v, o, n) -> {
            if (!Objects.equals(n, o)) {
                PlatformHelper.runLater(() -> {
                    if (Objects.equals(observableSheet.zoomProperty().get(), n)) {
                        updateLayout();
                    }
                });
            }
        });
    }

    private void updateLeftSpacer() {
        int j = delegate.getSplitColumn();
        leftSpacer.setPrefWidth(delegate.getColumnPos(j) * delegate.getScale().sx() + delegate.getRowLabelWidthInPixels());
    }

    private void updateHScrollbar() {
        updateLeftSpacer();
        double scrollableSize = delegate.getSheetWidthInPoints() - delegate.getSplitXInPoints();
        double viewableSize = Math.max(0.0, (getWidth() - vScrollbar.getWidth()) / delegate.getScale().sx() - delegate.getSplitXInPoints());
        double hScrollbarMax = Math.max(0.0, scrollableSize - viewableSize + vScrollbar.getWidth() / delegate.getScale().sx());
        double visibleAmount = hScrollbarMax * viewableSize / scrollableSize;
        hScrollbar.setMin(0.0);
        hScrollbar.setMax(hScrollbarMax);
        hScrollbar.setVisibleAmount(visibleAmount);
        hScrollbar.setUnitIncrement(delegate.getDefaultColumnWidthInPixels() * delegate.getScale().sx() / 8);
        hScrollbar.setBlockIncrement(delegate.getDefaultColumnWidthInPixels() * delegate.getScale().sx());
        updateEditorBounds();
    }

    private void updateVScrollbar() {
        double scrollableSize = delegate.getSheetHeightInPoints() - delegate.getSplitYInPoints();
        double viewableSize = Math.max(0.0, (getHeight() - hScrollbar.getHeight()) / delegate.getScale().sy() - delegate.getSplitYInPoints());
        double vScrollbarMax = Math.max(0.0, scrollableSize - viewableSize + hScrollbar.getHeight() / delegate.getScale().sy());
        double visibleAmount = vScrollbarMax * viewableSize / scrollableSize;
        vScrollbar.setMin(0.0);
        vScrollbar.setMax(vScrollbarMax);
        vScrollbar.setVisibleAmount(visibleAmount);
        vScrollbar.setUnitIncrement(delegate.getDefaultRowHeightInPixels() * delegate.getScale().sy());
        vScrollbar.setBlockIncrement(delegate.getDefaultRowHeightInPixels() * delegate.getScale().sy() * 8);
        updateEditorBounds();
    }

    private static ColumnConstraints columnConstraints(Priority prio) {
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(prio);
        return cc;
    }

    private static RowConstraints rowConstraints(Priority prio) {
        RowConstraints rc = new RowConstraints();
        rc.setVgrow(prio);
        return rc;
    }

    /**
     * Returns the screen resolution in dots per inch (DPI).
     *
     * @return the screen resolution in DPI
     */
    static int getDpi() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    private void initEditor() {
        editor.setManaged(false);
        editor.setVisible(false);
        editor.setMinSize(0.0, 0.0);
        editor.setWrapText(false);
        editor.setEditable(false);
        editor.setEnterKeyInsertsNewline(true);
        editor.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER && !event.isShiftDown()) {
                stopEditing(true);
                event.consume();
            }
        });
        editor.documentVersionProperty().addListener((v, o, n) -> updateEditorBounds());
        editor.skinProperty().addListener((v, o, n) -> Platform.runLater(() -> {
            configureEditorScrollPane();
            hideEditorScrollbars();
        }));
        getChildren().add(editor);
    }

    private void handleEditingNavigationKey(KeyEvent event) {
        if (event.isConsumed() || !delegate.isEditing() || forwardingNavigationKey) {
            return;
        }

        switch (event.getCode()) {
            case LEFT, RIGHT, UP, DOWN -> {
                forwardingNavigationKey = true;
                try {
                    editor.requestFocus();
                    Event.fireEvent(editor, event.copyFor(editor, editor));
                } finally {
                    forwardingNavigationKey = false;
                }
                event.consume();
            }
            default -> {
                // no-op
            }
        }
    }

    void onKeyPressed(KeyEvent event) {
        LOG.trace("onKeyPressed(): event = {}, focusOwner = {}", () -> event, () -> getScene().getFocusOwner());

        if (event.isConsumed()) {
            LOG.trace("KeyEvent already consumed");
            return;
        }

        if (isFocused()) {
            switch (event.getCode()) {
                case UP -> {
                    move(Direction.NORTH);
                    event.consume();
                }
                case LEFT -> {
                    move(Direction.WEST);
                    event.consume();
                }
                case DOWN -> {
                    move(Direction.SOUTH);
                    event.consume();
                }
                case RIGHT -> {
                    move(Direction.EAST);
                    event.consume();
                }
                case PAGE_UP -> {
                    movePage(Direction.NORTH);
                    event.consume();
                }
                case PAGE_DOWN -> {
                    movePage(Direction.SOUTH);
                    event.consume();
                }
                case F2 -> {
                    startEditing();
                    event.consume();
                }
                case C -> {
                    if (event.isShortcutDown()) {
                        copyToClipboard();
                    }
                }
                default -> {
                    // No action for other keys
                }
            }
        }
    }

    @Override
    public FxSheetViewDelegate getDelegate() {
        return delegate;
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public Scale2f getDisplayScale() {
        Scene scene = getScene();
        Window window = scene == null ? null : scene.getWindow();
        Screen screen = window == null ? Screen.getPrimary() : FxUtil.getScreen(window);
        return FxUtil.getDisplayScale(screen);
    }

    @Override
    public void scrollToCurrentCell() {
        LOG.trace("scrollToCurrentCell()");

        Platform.runLater(() -> {
            try (var __ = delegate.readLock("FxSheetView.scrollToCurrentCell()")) {
                Cell cell = delegate.getCurrentLogicalCell();
                int i = cell.getRowNumber();
                int j = cell.getColumnNumber();
                int splitRow = delegate.getSplitRow();

                // vertical scroll
                if (i >= splitRow) {
                    // at least part of the (possibly merged) cell is below the split => scroll row into view
                    VirtualFlowWithHiddenScrollBars<FxRow> flow = bottomSegment.getFlow();
                    LOG.trace("scrolling row {} into view", i);
                    flow.scrollTo(i - splitRow);
                }

                // horizontal scroll
                int splitColumn = delegate.getSplitColumn();
                if (j >= splitColumn) {
                    // at least part of the (possibly merged) cell is to the right of the split => scroll column into view
                    double sx = delegate.getScale().sx();
                    double splitX = delegate.getSplitXInPoints();
                    double xMin = delegate.getColumnPos(j);
                    double xMax = delegate.getColumnPos(j + cell.getHorizontalSpan());

                    double sbMin = hScrollbar.getMin();
                    double sbValue = hScrollbar.getValue();
                    double visibleWidth = (getWidth() - vScrollbar.getWidth() - getDelegate().getColumnLabelHeightInPixels() - delegate.getSplitLineWidth()) / sx - splitX;
                    double visibleMaxX = splitX + sbValue + visibleWidth;

                    if (xMin < splitX + sbValue) {
                        LOG.trace("scrolling to leftmost column");
                        hScrollbar.setValue(xMin - splitX);
                    } else if (xMax > visibleMaxX) {
                        LOG.trace("scrolling column {} into view", j);
                        double value = xMax - visibleWidth - sbMin - splitX;
                        hScrollbar.setValue(value);
                    }
                }
            }
        });
    }

    @Override
    public void repaintCell(Cell cell) {
        LOG.trace("repaintCell({})", cell);
        PlatformHelper.checkApplicationThread();

        try (var __ = delegate.readLock("FxSheetView.repaintCell()")) {
            Cell lc = cell.getLogicalCell();
            int startRow = lc.getRowNumber();
            int endRow = startRow + lc.getVerticalSpan();
            for (int i = startRow; i < endRow; i++) {
                topSegment.requestLayoutForRow(i);
                bottomSegment.requestLayoutForRow(i);
            }
        }
    }

    /**
     * Updates the layout of the FxSheetView instance and its associated components.
     * <p>
     * This method performs the following operations:
     * <ul>
     *   <li>Logs the start of the layout update process for debugging purposes.</li>
     *   <li>Ensures that the logic executes on the UI application thread by invoking
     *       {@code PlatformHelper.checkApplicationThread()}.</li>
     *   <li>Acquires a write lock for thread-safe operations using the delegate's
     *       {@code automaticWriteLock()} method.</li>
     *   <li>Calls {@code updateDelegate()} to update the internal state of the
     *       delegate with the current sheet's properties.</li>
     *   <li>Updates the layout of all four quadrants of the view: top-left, top-right,
     *       bottom-left, and bottom-right, by invoking their {@code updateLayout} methods.</li>
     * </ul>
     *
     * This operation is vital for reflecting changes in layout, scaling, or other graphical
     * attributes in the view, particularly after modifications to the underlying {@code Sheet}
     * or its configurations.
     */
    private void updateLayout() {
        LOG.debug("updateLayout()");
        PlatformHelper.checkApplicationThread();
        try (var __ = delegate.writeLock("FxSheetView.updateLayout()")) {
            delegate.update(getDpi());
            topSegment.updateLayout();
            bottomSegment.updateLayout();
            updateHScrollbar();
            updateVScrollbar();
            updateEditorBounds();
        }
    }

    @Override
    public void updateContent() {
        LOG.debug("updateContent()");
        PlatformHelper.checkApplicationThread();

        if (updating) {
            return;
        }

        try (var __ = getSheet().readLock("FxSheetView.updateContent()")) {
            updating = true;
            updateLayout();
            sheetScaleXProperty.set(delegate.getScale().sx());
            sheetScaleYProperty.set(delegate.getScale().sy());
        } finally {
            updating = false;
        }
    }

    @Override
    public void focusView() {
        LOG.debug("focusView()");
        requestFocus();
    }

    @Override
    public void copyToClipboard() {
        LOG.debug("copyToClipboard()");
        FxUtil.copyToClipboard(delegate.getCurrentLogicalCell().getAsFormattedText(getLocale()));
    }

    @Override
    public void showSearchDialog() {
        LOG.warn("showSearchDialog() not implemented");
    }

    @Override
    public void startEditing() {
        if (!isEditable()) {
            return;
        }

        delegate.startEditing().ifPresent(cell -> {
            scrollToCurrentCell();
            Platform.runLater(() -> {
                CellStyle cellStyle = cell.getCellStyle();
                editor.setTextFont(cellStyle.getFont().scaled(delegate.getScale().sy()));
                editor.setText(cell.getCellType() == CellType.FORMULA ? "=" + cell.getFormula() : cell.getAsText(getLocale()).toString());
                editor.selectAll();
                editor.setToolbarLocation(DetachableNode.Location.HIDDEN);
                editor.setEditable(true);

                editor.setVisible(true);
                editor.toFront();
                updateEditorBounds();
                configureEditorScrollPane();
                hideEditorScrollbars();
                editor.requestFocus();
            });
        });
    }

    @Override
    public void stopEditing(boolean commit) {
        delegate.stopEditing().ifPresent(cell ->
                PlatformHelper.runAndWait(() -> {
                    if (commit) {
                        LOG.debug("committing cell content: {}", cell);
                        updateCellContent(cell);
                        repaintCell(cell);
                    }

                    editor.setEditable(false);
                    editor.setWrapText(false);
                    editor.setToolbarLocation(DetachableNode.Location.HIDDEN);
                    editor.setVisible(false);
                    editor.setText("");

                    requestFocus();
                })
        );
    }

    private void updateCellContent(Cell cell) {
        String text = editor.getText().toString();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        NumberFormat numberFormat = NumberFormat.getInstance(getLocale());
        new CellValueHelper(numberFormat, dateFormatter).setCellValue(cell, text);
    }

    private void updateEditorBounds() {
        delegate.getEditingCell().ifPresent(cell -> {
            Rectangle2f cellRectInLocal = getCellRectInLocal(cell);
            double x = cellRectInLocal.x() + 1;
            double y = cellRectInLocal.y() + 1;
            double minWidth = Math.max(1.0, cellRectInLocal.width() - 2);
            double minHeight = Math.max(1.0, cellRectInLocal.height() - 2);

            EditorSize editorSize = computeEditorSize(x, y, minWidth, minHeight);
            editor.setWrapText(editorSize.wrapText());
            editor.resizeRelocate(x, y, editorSize.width(), editorSize.height());
            configureEditorScrollPane();
            hideEditorScrollbars();
        });
    }

    private EditorSize computeEditorSize(double x, double y, double minWidth, double minHeight) {
        String text = editor.getText().toString();
        String displayText = text.isEmpty() ? " " : text;

        double hPadding = Math.max(4.0, 2.0 * delegate.getPaddingX() * delegate.getScale().sx());
        double vPadding = Math.max(2.0, 2.0 * delegate.getPaddingY() * delegate.getScale().sy());

        double naturalWidth = measureLongestLineWidth(displayText) + hPadding;
        double maxWidth = Math.max(minWidth, getEditorRightLimitX() - x);
        boolean wrapText = naturalWidth > maxWidth + 0.5;
        double width = wrapText ? maxWidth : Math.max(minWidth, naturalWidth);

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
        double verticalScrollBarWidth = vScrollbar.isVisible() ? vScrollbar.getWidth() : 0.0;
        return Math.max(1.0, getWidth() - verticalScrollBarWidth);
    }

    private double measureLongestLineWidth(String text) {
        Text measurement = new Text();
        measurement.setFont(editor.getFxFont());

        double width = 0.0;
        for (String line : text.split("\n", -1)) {
            measurement.setText(line.isEmpty() ? " " : line);
            width = Math.max(width, measurement.getLayoutBounds().getWidth());
        }
        return width;
    }

    private double measureTextHeight(String text, double wrappingWidth) {
        Text measurement = new Text(text.isEmpty() ? " " : text);
        measurement.setFont(editor.getFxFont());
        if (wrappingWidth > 0.0) {
            measurement.setWrappingWidth(wrappingWidth);
        }
        return measurement.getLayoutBounds().getHeight();
    }

    private void configureEditorScrollPane() {
        Node direct = editor.lookup(".scroll-pane");
        if (direct instanceof ScrollPane scrollPane) {
            configureEditorScrollPane(scrollPane);
            return;
        }

        for (Node node : editor.lookupAll(".scroll-pane")) {
            if (node instanceof ScrollPane scrollPane) {
                configureEditorScrollPane(scrollPane);
                return;
            }
        }
    }

    private void configureEditorScrollPane(ScrollPane scrollPane) {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(false);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
    }

    private void hideEditorScrollbars() {
        for (Node node : editor.lookupAll(".scroll-bar")) {
            node.setManaged(false);
            node.setVisible(false);
            node.setMouseTransparent(true);
            if (node instanceof Region region) {
                region.setMinSize(0.0, 0.0);
                region.setPrefSize(0.0, 0.0);
                region.setMaxSize(0.0, 0.0);
            }
        }
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

    private record EditorSize(double width, double height, boolean wrapText) {}

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
            x -= hScrollbar.getValue() * delegate.getScale().sx();
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
            y -= vScrollbar.getValue() * delegate.getScale().sy();
            if (delegate.getSplitRow() > 0) {
                y += delegate.getSplitLineHeight();
            }
        }
        return y;
    }

    /**
     * Returns the horizontal scrollbar of the sheet view.
     *
     * @return the horizontal ScrollBar instance
     */
    public ScrollBar getHScrollbar() {
        return hScrollbar;
    }

    /**
     * Returns the vertical scrollbar of the sheet view.
     *
     * @return the vertical ScrollBar instance
     */
    public ScrollBar getVScrollbar() {
        return vScrollbar;
    }

    DoubleProperty sheetScaleXProperty() {
        return sheetScaleXProperty;
    }

    DoubleProperty sheetScaleYProperty() {
        return sheetScaleYProperty;
    }

    ObservableSheet getObservableSheet() {
        return observableSheet;
    }

    public BooleanProperty editableProperty() {
        return editableProperty;
    }

    record SheetPosition(int row, int column, float xSheet, float ySheet) {}

    /**
     * Converts an X coordinate in points to a column number.
     *
     * @param x the X coordinate in points
     * @return the column number at the given X coordinate
     */
    public int getColumnFromXInPoints(double x) {
        x -= delegate.getRowLabelWidthInPoints();
        boolean isRight = x > delegate.getSplitXInPoints();
        if (isRight) {
            x += hScrollbar.getValue();
        }
        return delegate.getColumnNumberFromX((float) x, false);
    }

    /**
     * Converts a Y coordinate in points to a row number.
     *
     * @param y the Y coordinate in points
     * @return the row number at the given Y coordinate
     */
    public int getRowFromYInPoints(double y) {
        y -= delegate.getColumnLabelHeightInPoints();
        boolean isBottom = y > delegate.getSplitYInPoints();
        if (isBottom) {
            y += vScrollbar.getValue();
        }
        return delegate.getRowNumberFromY((float) y, false);
    }

    /**
     * Converts an X coordinate in pixels to a column number.
     *
     * @param x the X coordinate in pixels
     * @return the column number at the given X coordinate
     */
    public int getColumnFromXInPixels(double x) {
        x -= delegate.getRowLabelWidthInPixels();
        boolean isRight = x > delegate.getSplitXInPixels();
        x /= delegate.getScale().sx();
        if (isRight) {
            x += hScrollbar.getValue();
        }
        return delegate.getColumnNumberFromX((float) x, false);
    }

    /**
     * Converts a Y coordinate in pixels to a row number.
     *
     * @param y the Y coordinate in pixels
     * @return the row number at the given Y coordinate
     */
    public int getRowFromYInPixels(double y) {
        y -= delegate.getColumnLabelHeightInPixels();
        boolean isBottom = y > delegate.getSplitYInPixels();
        y /= delegate.getScale().sy();
        if (isBottom) {
            y += vScrollbar.getValue();
        }
        return delegate.getRowNumberFromY((float) y, false);
    }

    public void setEditable(boolean editable) {
        editableProperty.set(editable);
    }

    @Override
    public boolean isEditable() {
        return editableProperty.get();
    }

    /**
     * Returns the property that holds the parent Pane for the toolbar.
     * This property allows the association of a toolbar's parent container
     * in the FxSheetView.
     *
     * @return an ObjectProperty containing the parent Pane for the toolbar,
     *         or null if no parent is defined
     */
    public ObjectProperty<@Nullable Pane> toolbarParentProperty() {
        return toolbarParentProperty;
    }
}
