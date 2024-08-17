package com.dua3.meja.ui.fx;

import com.dua3.cabe.annotations.Nullable;
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
import javafx.collections.FXCollections;
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
import java.util.function.Function;

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

    public FxSheetView() {
        this(null);
    }

    public FxSheetView(@Nullable Sheet sheet) {
        LOG.debug("FxSheetView({})", sheet);

        this.delegate = new FxSheetViewDelegate(this);
        delegate.setSheet(sheet);

        // Initialize GridPane
        gridPane = new GridPane();

        // Create quadrants
        ObservableList<Row> rows = delegate.getSheet().map(ObservableSheet::new)
                .map(s -> (ObservableList<Row>) s)
                .orElse(FXCollections.emptyObservableList());
        delegate.updateLayout();
        topLeftQuadrant = new FxSegmentView(delegate, Quadrant.TOP_LEFT, rows);
        topRightQuadrant = new FxSegmentView(delegate, Quadrant.TOP_RIGHT, rows);
        bottomLeftQuadrant = new FxSegmentView(delegate, Quadrant.BOTTOM_LEFT, rows);
        bottomRightQuadrant = new FxSegmentView(delegate, Quadrant.BOTTOM_RIGHT, rows);

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
        entangleScrollBars(hScrollbar, topRightQuadrant.flow.getHScrollbar(), bottomRightQuadrant.flow.getHScrollbar());
        entangleScrollBars(vScrollbar, bottomLeftQuadrant.flow.getVScrollbar(), bottomRightQuadrant.flow.getVScrollbar());

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

    private void entangleScrollBars(ScrollBar mainScrollBar, ScrollBar... dependentScrollbars) {
        for (ScrollBar scrollbar : dependentScrollbars) {
            entangle(ScrollBar::visibleAmountProperty, mainScrollBar, scrollbar);
            entangle(ScrollBar::unitIncrementProperty, mainScrollBar, scrollbar);
            entangle(ScrollBar::minProperty, mainScrollBar, scrollbar);
            entangle(ScrollBar::maxProperty, mainScrollBar, scrollbar);
            entangle(ScrollBar::valueProperty, mainScrollBar, scrollbar);
        }
    }

    private <T, U, P extends Property<U>> void entangle(Function<T, P> s, T a, T b) {
        s.apply(b).bindBidirectional(s.apply(a));
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

        Platform.runLater( () -> {
            delegate.getCurrentLogicalCell().ifPresent(cell -> {
                Sheet sheet = delegate.getSheet().orElseThrow();
                int i = cell.getRowNumber();
                int j = cell.getColumnNumber();
                int splitRow = sheet.getSplitRow();
                if (i >= splitRow) {
                    i -= splitRow;
                    // at least part of the (possibly merged) cell is below the split => scroll row into view
                    FxSegmentView.VirtualFlowWithHiddenScrollBars<FxRow> flow = bottomRightQuadrant.flow;
                    FxRow fxRow = flow.getCell(i);
                    LOG.trace("scrolling row {} into view", i);
                    Platform.runLater(() -> flow.scrollTo(fxRow));
                }
                int splitColumn = sheet.getSplitColumn();
                if (j >= splitColumn) {
                    // at least part of the (possibly merged) cell is to the right of the split => scroll column into view
                    Scale2f s = delegate.getScale();
                    float split = delegate.getSplitX();
                    float xMin = (delegate.getColumnPos(j) - split) * s.sx();
                    float xMax = (delegate.getColumnPos(j+1) - split) * s.sy();
                    float width = xMax - xMin;
                    if (xMin < hScrollbar.getValue()) {
                        LOG.trace("scrolling to leftmost column");
                        hScrollbar.setValue(xMin);
                    } else if (xMin > hScrollbar.getValue() + hScrollbar.getVisibleAmount() * s.sx()) {
                        LOG.trace("scrolling column {} into view", j);
                        hScrollbar.setValue(xMin - hScrollbar.getVisibleAmount() * s.sx());
                    }
                }
            });
        });
    }

    @Override
    public void repaintCell(Cell cell) {
        LOG.trace("repaintCell({})", cell);

        Cell lc = cell.getLogicalCell();
        int startRow = lc.getRowNumber();
        int endRow = startRow + lc.getVerticalSpan();

        PlatformHelper.runLater(() ->{
            for (int i = startRow; i< endRow; i++) {
                topLeftQuadrant.flow.getCell(i).requestLayout();
                topRightQuadrant.flow.getCell(i).requestLayout();
                bottomLeftQuadrant.flow.getCell(i).requestLayout();
                bottomRightQuadrant.flow.getCell(i).requestLayout();
            }
        });
    }

    @Override
    public void updateContent() {
        LOG.debug("updateContent()");

        getSheet().ifPresent(sheet -> {
            Lock lock = delegate.writeLock();
            lock.lock();
            try {
                int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
                delegate.setDisplayScale(getDisplayScale());
                delegate.setScale(new Scale2f(sheet.getZoom() * dpi / 72f));
                delegate.updateLayout();
                if (topLeftQuadrant != null) {
                    topLeftQuadrant.refresh();
                }
                if (topRightQuadrant != null) {
                    topRightQuadrant.refresh();
                }
                if (bottomLeftQuadrant != null) {
                    bottomLeftQuadrant.refresh();
                }
                if (bottomRightQuadrant != null) {
                    bottomRightQuadrant.refresh();
                }
            } finally {
                lock.unlock();
            }
        });
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