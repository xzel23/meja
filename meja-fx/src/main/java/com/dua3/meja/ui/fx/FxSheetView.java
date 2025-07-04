package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PlatformHelper;
import com.dua3.utility.math.geometry.Scale2f;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Toolkit;
import java.util.Locale;
import java.util.Objects;

/**
 * The FxSheetView class is responsible for rendering a spreadsheet-like UI component,
 * using four quadrants to manage visible data sections efficiently.
 * This class incorporates scrollbars and quadrants to manage the display of data and handle
 * keyboard inputs for navigation. It also entangles scrollbar movements to ensure synchronized scrolling
 * across different quadrants.
 */
public class FxSheetView extends StackPane implements SheetView {
    private static final Logger LOG = LogManager.getLogger(FxSheetView.class);
    private final ObservableSheet observableSheet;
    private final FxSheetViewDelegate delegate;
    private final FxSegmentView topSegment;
    private final FxSegmentView bottomSegment;
    private final GridPane gridPane;

    private final ScrollBar hScrollbar = new ScrollBar();
    private final ScrollBar vScrollbar = new ScrollBar();

    private final Region leftSpacer = new Region();
    private final Region topSpacer = new Region();
    private final Region bottomRightCorner = new Region();

    private final DoubleProperty sheetScaleXProperty = new SimpleDoubleProperty(1.0);
    private final DoubleProperty sheetScaleYProperty = new SimpleDoubleProperty(1.0);

    private boolean updating = false;

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
        this.gridPane = new GridPane();

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

        // set up horizontal scrollbar
        leftSpacer.prefHeightProperty().bind(hScrollbar.heightProperty());
        observableSheet.splitColumnProperty().addListener((v, o, n) -> updateLeftSpacer());
        updateLeftSpacer();

        bottomRightCorner.prefWidthProperty().bind(vScrollbar.widthProperty());
        bottomRightCorner.prefHeightProperty().bind(hScrollbar.heightProperty());

        HBox hScrollbarContainer = new HBox(0.0, leftSpacer, hScrollbar, bottomRightCorner);
        HBox.setHgrow(leftSpacer, Priority.NEVER);
        HBox.setHgrow(hScrollbar, Priority.ALWAYS);
        HBox.setHgrow(bottomRightCorner, Priority.NEVER);

        // set up vertical scrollbar
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
        vScrollbar.valueProperty().addListener((v, o, n) -> {
            LOG.trace("set vscrollbar value to {}", n);
            double position = n.doubleValue() / Math.max(1.0, vScrollbar.getMax() - vScrollbar.getMin());
            bottomSegment.getFlow().setPosition(position);
        });
        bottomSegment.getFlow().positionProperty().addListener((v, o, n) -> {
            double position = n.doubleValue() * (vScrollbar.getMax() - vScrollbar.getMin());
            updateVScrollbar();
            vScrollbar.setValue(position);
        });

        // Add the GridPane to the StackPane
        getChildren().add(gridPane);

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
                    double xMax = delegate.getColumnPos(j + 1);
                    double sheetWidth = delegate.getSheetWidthInPoints();
                    double segmentWidth = sheetWidth - splitX;

                    double sbMax = hScrollbar.getMax();
                    double sbMin = hScrollbar.getMin();
                    double sbValue = hScrollbar.getValue();
                    double sbRange = sbMax - sbMin;
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
        LOG.warn("startEditing() not implemented");
    }

    @Override
    public void stopEditing(boolean commit) {
        LOG.warn("stopEditing() not implemented");
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
}
