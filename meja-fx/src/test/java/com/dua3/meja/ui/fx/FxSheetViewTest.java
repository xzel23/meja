package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Direction;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.utility.fx.PlatformHelper;
import com.dua3.utility.math.geometry.Rectangle2f;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FxSheetViewTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Already started
        }
    }

    @Test
    @Order(2)
    void testFxSheetViewLifecycle() {
        PlatformHelper.runAndWait(() -> {
            GenericWorkbook wb = GenericWorkbookFactory.instance().create();
            GenericSheet sheet = wb.createSheet("FxTestSheet");
            sheet.getCell(0, 0).set("Start");
            sheet.getCell(5, 5).set("End");

            FxSheetView view = new FxSheetView(sheet);

            assertSame(sheet, view.getSheet());
            assertNotNull(view.getDelegate());
            assertNotNull(view.getObservableSheet());
            assertNotNull(view.getDisplayScale());
            assertNotNull(view.getLocale());
            assertNotNull(view.sheetScaleXProperty());
            assertNotNull(view.sheetScaleYProperty());

            // Editable
            view.setEditable(true);
            assertTrue(view.isEditable());
            assertTrue(view.editableProperty().get());
            view.setEditable(false);
            assertFalse(view.isEditable());

            // Allow open links
            view.setAllowOpenLinks(true);
            assertTrue(view.getAllowOpenLinks());
            view.setAllowOpenLinks(false);
            assertFalse(view.getAllowOpenLinks());

            // Toolbar parent
            Pane toolbarPane = new Pane();
            view.toolbarParentProperty().set(toolbarPane);
            assertSame(toolbarPane, view.toolbarParentProperty().get());

            // Navigation
            assertTrue(view.setCurrentCell(2, 3));
            assertEquals(2, sheet.getCurrentCell().getRowNumber());
            assertEquals(3, sheet.getCurrentCell().getColumnNumber());

            view.moveHome();
            assertEquals(0, sheet.getCurrentCell().getRowNumber());
            assertEquals(0, sheet.getCurrentCell().getColumnNumber());

            view.moveEnd();
            assertEquals(5, sheet.getCurrentCell().getRowNumber());
            assertEquals(5, sheet.getCurrentCell().getColumnNumber());

            view.move(Direction.NORTH);
            assertEquals(4, sheet.getCurrentCell().getRowNumber());

            view.movePage(Direction.SOUTH);

            // Repaint, focus, scroll
            assertDoesNotThrow(() -> view.repaintCell(sheet.getCell(0, 0)));
            assertDoesNotThrow(view::scrollToCurrentCell);
            assertDoesNotThrow(view::focusView);
            assertDoesNotThrow(view::copyToClipboard);
            assertDoesNotThrow(view::updateContent);
        });
    }

    @Test
    @Order(1)
    void cellBoundsTrackRenderedRowAfterScrollingToHighRow() throws InterruptedException {
        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                GenericWorkbook wb = GenericWorkbookFactory.instance().create();
                GenericSheet sheet = wb.createSheet("FxHighRowTest");
                sheet.getCell(700, 0).set("editable");

                FxSheetView view = new FxSheetView(sheet);
                view.resize(800, 600);
                view.layout();
                view.setCurrentCell(700, 0);
                view.scrollToCurrentCell();

                Platform.runLater(() -> Platform.runLater(() -> {
                    try {
                        view.layout();
                        FxRow row = findRow(view, 700);
                        assertNotNull(row, "the edited row must be rendered after scrolling");
                        Bounds rowBounds = view.sceneToLocal(row.localToScene(row.getBoundsInLocal()));
                        Rectangle2f cellBounds = view.getCellRectInLocal(sheet.getCell(700, 0));
                        assertEquals(rowBounds.getMinY(), cellBounds.y(), 1.0);
                    } catch (Throwable t) {
                        failure.set(t);
                    } finally {
                        finished.countDown();
                    }
                }));
            } catch (Throwable t) {
                failure.set(t);
                finished.countDown();
            }
        });

        assertTrue(finished.await(10, TimeUnit.SECONDS), "JavaFX test did not complete");
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }

    private static FxRow findRow(Node node, int rowNumber) {
        if (node instanceof FxRow row && row.getItem().rowNumber() == rowNumber) {
            return row;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                FxRow row = findRow(child, rowNumber);
                if (row != null) {
                    return row;
                }
            }
        }
        return null;
    }

}
