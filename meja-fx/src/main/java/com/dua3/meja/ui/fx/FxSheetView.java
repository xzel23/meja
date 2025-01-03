package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PlatformHelper;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.text.RichText;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Toolkit;
import java.util.Locale;
import java.util.concurrent.locks.Lock;

/**
 * The FxSheetView class is responsible for rendering a spreadsheet-like UI component,
 * using four quadrants to manage visible data sections efficiently.
 * This class incorporates scrollbars and quadrants to manage the display of data and handle
 * keyboard inputs for navigation. It also entangles scrollbar movements to ensure synchronized scrolling
 * across different quadrants.
 */
public class FxSheetView extends StackPane implements SheetView {
    private static final Logger LOG = LogManager.getLogger(FxSheetView.class);
    private final FxSheetViewDelegate delegate;
    private final FxSegmentView topLeftQuadrant;
    private final FxSegmentView topRightQuadrant;
    private final FxSegmentView bottomLeftQuadrant;
    private final FxSegmentView bottomRightQuadrant;
    private final GridPane gridPane;
    private final ScrollBar hScrollbar;
    private final ScrollBar vScrollbar;

    /**
     * Constructs a new instance of FxSheetView using the specified Sheet object.
     * Initializes the layout and scrollbars, and sets up the view quadrants based on the provided sheet.
     *
     * @param sheet the Sheet object to be used as a model for the FxSheetView; can be null, in which case
     *              an empty view is initialized.
     */
    public FxSheetView(Sheet sheet) {
        LOG.debug("FxSheetView({})", sheet);

        this.delegate = new FxSheetViewDelegate(sheet, this);
        this.gridPane = new GridPane();

        updateDelegate(sheet);

        // Create quadrants
        ObservableList<Row> rows = new ObservableSheet(sheet);

        topLeftQuadrant = new FxSegmentView(delegate, Quadrant.TOP_LEFT, rows);
        topRightQuadrant = new FxSegmentView(delegate, Quadrant.TOP_RIGHT, rows);
        bottomLeftQuadrant = new FxSegmentView(delegate, Quadrant.BOTTOM_LEFT, rows);
        bottomRightQuadrant = new FxSegmentView(delegate, Quadrant.BOTTOM_RIGHT, rows);

        updateContent();

        // Create scrollbars
        hScrollbar = new ScrollBar();
        hScrollbar.setOrientation(Orientation.HORIZONTAL);

        vScrollbar = new ScrollBar();
        vScrollbar.setOrientation(Orientation.VERTICAL);

        // Set layout constraints
        RowConstraints[] rc = {
                rowConstraints(Priority.NEVER),
                rowConstraints(Priority.ALWAYS),
                rowConstraints(Priority.NEVER)
        };
        gridPane.getRowConstraints().setAll(rc);

        ColumnConstraints[] cc = {
                columnConstraints(Priority.NEVER),
                columnConstraints(Priority.ALWAYS),
                columnConstraints(Priority.NEVER)
        };
        gridPane.getColumnConstraints().setAll(cc);

        // Add quadrants to GridPane
        gridPane.add(topLeftQuadrant, 0, 0);
        gridPane.add(topRightQuadrant, 1, 0);
        gridPane.add(bottomLeftQuadrant, 0, 1);
        gridPane.add(bottomRightQuadrant, 1, 1);

        // Add the GridPane to the StackPane
        getChildren().add(gridPane);

        // Add scrollbars directly to the StackPane
        getChildren().addAll(hScrollbar, vScrollbar);

        // Bind visibility of scrollbars
        hScrollbar.visibleProperty().bind(bottomRightQuadrant.flow.getHScrollbar().visibleProperty());
        vScrollbar.visibleProperty().bind(bottomRightQuadrant.flow.getVScrollbar().visibleProperty());

        // Position scrollbars
        StackPane.setAlignment(hScrollbar, javafx.geometry.Pos.BOTTOM_CENTER);
        StackPane.setAlignment(vScrollbar, javafx.geometry.Pos.CENTER_RIGHT);

        // entangle scrollbars
        entangleScrollBars(hScrollbar, bottomRightQuadrant.flow.getHScrollbar(), topRightQuadrant.flow.getHScrollbar());
        entangleScrollBars(vScrollbar, bottomRightQuadrant.flow.getVScrollbar(), bottomLeftQuadrant.flow.getVScrollbar());

        hScrollbar.setValue(0);
        vScrollbar.setValue(0);

        setFocusTraversable(true);
        setOnKeyPressed(this::onKeyPressed);

        updateContent();
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

    private void entangleScrollBars(ScrollBar visibleScrollBar, ScrollBar controllingScrollBar, ScrollBar dependentScrollbar) {
        entangleFollowing(visibleScrollBar.visibleAmountProperty(), controllingScrollBar.visibleAmountProperty(), dependentScrollbar.visibleAmountProperty());
        entangleFollowing(visibleScrollBar.unitIncrementProperty(), controllingScrollBar.unitIncrementProperty(), dependentScrollbar.unitIncrementProperty());
        entangleFollowing(visibleScrollBar.minProperty(), controllingScrollBar.minProperty(), dependentScrollbar.minProperty());
        entangleFollowing(visibleScrollBar.maxProperty(), controllingScrollBar.maxProperty(), dependentScrollbar.maxProperty());
        entangleBinding(visibleScrollBar.valueProperty(), controllingScrollBar.valueProperty(), dependentScrollbar.valueProperty());
    }

    private <V> void entangleFollowing(Property<V> visibleProperty, Property<V> controllingProperty, Property<V> dependentProperty) {
        V value = controllingProperty.getValue();
        visibleProperty.setValue(value);
        dependentProperty.setValue(value);

        dependentProperty.addListener(v -> dependentProperty.setValue(controllingProperty.getValue()));
        visibleProperty.addListener(v -> visibleProperty.setValue(controllingProperty.getValue()));

        controllingProperty.addListener((v, o, n) -> {
            visibleProperty.setValue(n);
            dependentProperty.setValue(n);
        });
    }

    private <V> void entangleBinding(Property<V> visibleProperty, Property<V> controllingProperty, Property<V> dependentProperty) {
        V value = controllingProperty.getValue();
        visibleProperty.setValue(value);
        dependentProperty.setValue(value);

        controllingProperty.bindBidirectional(visibleProperty);
        visibleProperty.bindBidirectional(dependentProperty);
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
        Scene scene = this.getScene();
        Window window = scene == null ? null : scene.getWindow();
        Screen screen = window == null ? Screen.getPrimary() : FxUtil.getScreen(window);
        return FxUtil.getDisplayScale(screen);
    }

    @Override
    public void scrollToCurrentCell() {
        LOG.trace("scrollToCurrentCell()");

        Platform.runLater(() -> delegate.getCurrentLogicalCell().ifPresent(cell -> {
            Sheet sheet = delegate.getSheet();
            int i = cell.getRowNumber();
            int j = cell.getColumnNumber();
            int splitRow = sheet.getSplitRow();
            if (i >= splitRow) {
                i -= splitRow;
                // at least part of the (possibly merged) cell is below the split => scroll row into view
                FxSegmentView.VirtualFlowWithHiddenScrollBars<FxRow> flow = bottomRightQuadrant.flow;
                LOG.trace("scrolling row {} into view", i);
                flow.scrollTo(i);
            }
            int splitColumn = sheet.getSplitColumn();
            if (j >= splitColumn) {
                // at least part of the (possibly merged) cell is to the right of the split => scroll column into view
                Scale2f s = delegate.getScale();
                double split = delegate.getSplitX() * s.sx();
                double xMin = delegate.getColumnPos(j) * s.sx() - split;
                double xMax = delegate.getColumnPos(j + 1) * s.sx() - split;
                double width = delegate.getColumnPos(delegate.getColumnCount()) * s.sx() - split;

                double sbVisibleAmount = hScrollbar.getVisibleAmount();
                double sbMax = hScrollbar.getMax();
                double sbMin = hScrollbar.getMin();
                double sbValue = hScrollbar.getValue();
                double sbRange = sbMax - sbMin;
                double sbWidth = sbVisibleAmount + sbRange;
                double visiblePercent = sbVisibleAmount / sbWidth;

                double visibleMaxX = split + sbValue + width * visiblePercent;

                if (xMin < sbValue) {
                    LOG.trace("scrolling to leftmost column");
                    hScrollbar.setValue(xMin);
                } else if (xMax > visibleMaxX) {
                    LOG.trace("scrolling column {} into view", j);
                    hScrollbar.setValue(Math.min(sbMax, xMax - width * visiblePercent - split));
                }
            }
        }));
    }

    @Override
    public void repaintCell(Cell cell) {
        LOG.trace("repaintCell({})", cell);

        Cell lc = cell.getLogicalCell();
        int startRow = lc.getRowNumber();
        int endRow = startRow + lc.getVerticalSpan();

        PlatformHelper.runLater(() -> {
            for (int i = startRow; i < endRow; i++) {
                topLeftQuadrant.requestLayoutForRow(i);
                topRightQuadrant.requestLayoutForRow(i);
                bottomLeftQuadrant.requestLayoutForRow(i);
                bottomRightQuadrant.requestLayoutForRow(i);
            }
        });
    }

    @Override
    public void updateContent() {
        LOG.debug("updateContent()");

        PlatformHelper.runAndWait(() -> {
            Lock lock = delegate.writeLock();
            lock.lock();
            try {
                updateDelegate(getSheet());
                topLeftQuadrant.refresh();
                topRightQuadrant.refresh();
                bottomLeftQuadrant.refresh();
                bottomRightQuadrant.refresh();
            } finally {
                lock.unlock();
            }
        });
    }

    private void updateDelegate(Sheet sheet) {
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        delegate.setDisplayScale(getDisplayScale());
        delegate.setScale(new Scale2f(sheet.getZoom() * dpi / 72.0f));
        delegate.updateLayout();
    }

    @Override
    public void focusView() {
        LOG.debug("focusView()");

        requestFocus();
    }

    @Override
    public void copyToClipboard() {
        LOG.debug("copyToClipboard()");

        delegate.getCurrentLogicalCell().ifPresent(cell -> {
            RichText text = cell.getAsText(getLocale());
            FxUtil.copyToClipboard(text.toString());
        });
    }

    @Override
    public void showSearchDialog() {
        LOG.warn("showSearchDialog() not implemented");
    }

    @Override
    public void startEditing() {
        LOG.warn("startEditing() not implemented");
    }

    @Override
    public void stopEditing(boolean commit) {
        LOG.warn("stopEditing() not implemented");
    }

}