package com.dua3.meja.util.fx;

import com.dua3.meja.model.Workbook;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import com.dua3.meja.model.Sheet;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;

import com.dua3.meja.model.Row;

public class FxMejaUtil {

    /**
     * Copy data from TableView to Sheet.
     * 
     * @param table
     *  the TableView to copy from
     * @param sheet
     *  the Sheet to copy to
     */
	public static void copyToSheet(TableView<? extends Iterable<?>> table, Sheet sheet) {
		copyToSheet(table, sheet, 0, 0);
	}

    /**
     * Copy data from TableView to Sheet.
     * 
     * @param table
     *  the TableView to copy from
     * @param sheet
     *  the Sheet to copy to
     * @param posI
     *  the row number where to insert the data
     * @param posJ
     *  the column number where to insert the data
     */
	public static void copyToSheet(TableView<? extends Iterable<?>> table, Sheet sheet, int posI, int posJ) {
		var columns = table.getColumns();
		int m = columns.size();

		// table header
		int i = 0;
		Row row = sheet.getRow(posI+i);
        for (int j=0; j<m; j++) {
			row.getCell(posJ+j).set(columns.get(j).getText());
		}
        sheet.splitAt(1, 0);

		// copy data
        for (var rowData: table.getItems()) {
        	row = sheet.getRow(posI+(++i));
        	int j = 0;
        	for (var obj: rowData) {
    			row.getCell(posJ+(j++)).set(obj);        		
        	}
        }
    }
    
    /**
     * Get list of ExtensionFilters for supported file types.
     * 
     * @param mode
     *  the mode requested (read/write)
     * @return 
     *  list of ExtensionFilters
     */
    public static List<FileChooser.ExtensionFilter> getExtensionFilters(OpenMode mode) {
        List<FileChooser.ExtensionFilter> filters = new LinkedList<>();
        FileType.getFileTypes(mode, Workbook.class)
            .forEach(t ->
                filters.add(
                    new FileChooser.ExtensionFilter(
                        t.getName(), 
                        t.getExtensions()
                            .stream()
                            .map(ext -> "*."+ext)
                            .collect(Collectors.toList()).toArray(String[]::new))));
        return filters;
    }

    /**
     * Get list of ExtensionFilters for supported file types.
     * 
     * @return 
     *  list of ExtensionFilters
     */
    public static List<FileChooser.ExtensionFilter> getExtensionFilters() {
        List<FileChooser.ExtensionFilter> filters = new LinkedList<>();
        FileType.filetypes()
            .forEach(t -> filters.add(new FileChooser.ExtensionFilter(t.getName(), t.getExtensions())));
        return filters;
    }

    private FxMejaUtil() {
        // utility class
    }
}
