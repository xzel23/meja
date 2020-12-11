package com.dua3.meja.util.fx;

import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser.ExtensionFilter;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for integrating Meja with JavaFX.
 */
public final class FxMejaUtil {

    private FxMejaUtil() {
        // utility class
    }

    /**
     * Copy data from TableView to Sheet.
     *
     * @param table the TableView to copy from
     * @param sheet the Sheet to copy to
     */
    public static void copyToSheet(TableView<? extends Iterable<?>> table, Sheet sheet) {
        copyToSheet(table, sheet, 0, 0);
    }

    /**
     * Copy data from TableView to Sheet.
     *
     * @param table the TableView to copy from
     * @param sheet the Sheet to copy to
     * @param posI  the row number where to insert the data
     * @param posJ  the column number where to insert the data
     */
    public static void copyToSheet(TableView<? extends Iterable<?>> table, Sheet sheet, int posI, int posJ) {
        ObservableList<? extends TableColumn<? extends Iterable<?>, ?>> columns = table.getColumns();
        int m = columns.size();

        // table header
        int i = 0;
        Row row = sheet.getRow(posI + i);
        for (int j = 0; j < m; j++) {
            row.getCell(posJ + j).set(columns.get(j).getText());
        }
        sheet.splitAt(1, 0);

        // copy data
        for (Iterable<?> rowData : table.getItems()) {
            row = sheet.getRow(posI + (++i));
            int j = 0;
            for (Object obj : rowData) {
                row.getCell(posJ + (j++)).set(obj);
            }
        }
    }

    /**
     * Get list of ExtensionFilters for supported file types.
     *
     * @param mode the mode requested (read/write)
     * @return list of ExtensionFilters
     */
    public static List<ExtensionFilter> getExtensionFilters(OpenMode mode) {
        return FileType.getFileTypes(mode, Workbook.class).stream().map(t -> new ExtensionFilter(
                t.getName(),
                t.getExtensions()
                        .stream()
                        .map(ext -> "*." + ext)
                        .collect(Collectors.toList()).toArray(new String[0]))).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get list of ExtensionFilters for supported file types.
     *
     * @return list of ExtensionFilters
     */
    public static List<ExtensionFilter> getExtensionFilters() {
        return FileType.fileTypes().stream().map(t -> new ExtensionFilter(t.getName(), t.getExtensions())).collect(Collectors.toCollection(LinkedList::new));
    }
}
