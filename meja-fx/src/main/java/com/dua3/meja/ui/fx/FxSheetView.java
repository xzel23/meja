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

    public FxSheetView() {
        this(null);
    }

    public FxSheetView(@Nullable Sheet sheet) {
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
        ScrollBar hScrollbar = new ScrollBar();
        hScrollbar.setOrientation(Orientation.HORIZONTAL);

        ScrollBar vScrollbar = new ScrollBar();
        vScrollbar.setOrientation(Orientation.VERTICAL);

        entangleScrollBars(hScrollbar, topRightQuadrant.flow.getHScrollbar(), bottomRightQuadrant.flow.getHScrollbar());
        entangleScrollBars(vScrollbar, bottomLeftQuadrant.flow.getVScrollbar(), bottomRightQuadrant.flow.getVScrollbar());

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
            entangle(ScrollBar::minProperty, mainScrollBar, scrollbar);
            entangle(ScrollBar::maxProperty, mainScrollBar, scrollbar);
            entangle(ScrollBar::visibleAmountProperty, mainScrollBar, scrollbar);
            entangle(ScrollBar::valueProperty, mainScrollBar, scrollbar);
        }
    }

    private <T, U, P extends Property<U>> void entangle(Function<T, P> s, T a, T b) {
        entangleProperties(s.apply(a), s.apply(b));
    }

    @SafeVarargs
    private <T, P extends Property<T>> void entangleProperties(P pMain, P... pDependent) {
        for (P p : pDependent) {
            p.bindBidirectional(pMain);
        }
    }

    void onKeyPressed(KeyEvent event) {
        if (event.isConsumed()) {
            LOG.trace("onKeyPressed(): KeyEvent already consumed: {}", event);
            return;
        }

        LOG.trace("onKeyPressed(): event = {}, focusOwner = {}", event, getScene().getFocusOwner());
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

    private boolean isTabButtonFocused() {
        Scene scene = getScene();
        if (scene != null && scene.getFocusOwner() != null) {
            return scene.getFocusOwner().getParent() instanceof javafx.scene.control.TabPane;
        }
        return false;
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
    }

    @Override
    public void stopEditing(boolean commit) {
    }

    @Override
    public void repaintCell(Cell cell) {
        Cell lc = cell.getLogicalCell();
        int startRow = lc.getRowNumber();
        int endRow = startRow + lc.getVerticalSpan() -1;

        PlatformHelper.runLater(() ->{
            for (int i=startRow; i<=endRow; i++) {
                topLeftQuadrant.flow.getCell(i).requestLayout();
                topRightQuadrant.flow.getCell(i).requestLayout();
                bottomLeftQuadrant.flow.getCell(i).requestLayout();
                bottomRightQuadrant.flow.getCell(i).requestLayout();
            }
        });
    }

    @Override
    public void updateContent() {
        LOG.debug("updating content");
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
        requestFocus();
    }

    @Override
    public void copyToClipboard() {
        delegate.getCurrentLogicalCell().ifPresent(cell -> {
            RichText text = cell.getAsText(getLocale());
            FxUtil.copyToClipboard(text.toString());
        });
    }

    @Override
    public void showSearchDialog() {
    }

    @Override
    public void startEditing() {
    }

    /**
     * Scroll cell into view.
     *
     * @param cell the cell to scroll to
     */
    public void ensureCellIsVisible(@Nullable Cell cell) {
    }
}