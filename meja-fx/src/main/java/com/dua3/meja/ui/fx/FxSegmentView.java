package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SegmentViewDelegate;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.function.IntSupplier;
import java.util.stream.IntStream;

public class FxSegmentView extends TableView<Row> implements SegmentView {
    private final FxSheetViewDelegate svDelegate;
    private final FxSegmentViewDelegate fsvDelegate;

    FxSegmentView(
            FxSheetViewDelegate sheetViewDelegate,
            IntSupplier startRow,
            IntSupplier endRow,
            IntSupplier startColumn,
            IntSupplier endColumn
    ) {
        super(sheetViewDelegate.getSheet()
                .<ObservableList<Row>>map(ObservableSheet::new)
                .orElse(FXCollections.emptyObservableList())
        );

        TableColumn<Row, Integer> colRowNumber = new TableColumn<>("");
        colRowNumber.setCellValueFactory(cdf -> new SimpleObjectProperty<>(cdf.getValue().getRowNumber()));
        getColumns().setAll(colRowNumber);
        sheetViewDelegate.getSheet().ifPresent(sheet -> {
            getColumns().addAll(
                IntStream.range(0, sheet.getColumnCount())
                        .mapToObj(j -> {
                            TableColumn<Row, Cell> col = new TableColumn<>(Sheet.getColumnName(j));
                            col.setCellValueFactory(cdf -> new SimpleObjectProperty<>(cdf.getValue().getCell(j)));
                            return col;
                        })
                        .toList()
            );
        });

        this.svDelegate = sheetViewDelegate;
        this.fsvDelegate = new FxSegmentViewDelegate(this, svDelegate, startRow, endRow, startColumn, endColumn);
    }

    private void init() {
        // TODO listen to mouse events
        /*
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                translateMousePosition(p);
                svDelegate.onMousePressed(p.x, p.y);
            }
        });
         */
    }

    @Override
    public SegmentViewDelegate getDelegate() {
        return null;
    }

    @Override
    public Sheet getSheet() {
        return null;
    }

    @Override
    public void setViewSizeOnDisplay(float w, float h) {

    }
}
