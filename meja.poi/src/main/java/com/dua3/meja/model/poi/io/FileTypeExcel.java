package com.dua3.meja.model.poi.io;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.poi.PoiWorkbookFactory;

/**
 * FileType for <em>reading</em> Excel files.
 * 
 * There are different file types for reading and writing because
 * when reading, the user normally wants to have a filter that shows <em>all</em>
 * Excel files.
 */
public class FileTypeExcel extends FileType {
    private static final FileType INSTANCE = new FileTypeExcel();
    
    public static FileType instance() {
        return INSTANCE;
    }

    private FileTypeExcel() {
        super("All Excel files", OpenMode.READ, "xls", "xlsx", "xlsm");
        addType(this);
    }
    
    @Override
    public PoiWorkbookFactory factory() {
        return PoiWorkbookFactory.instance();
    }

    @Override
    public WorkbookWriter getWriter() {
        return XlsxWorkbookWriter.instance();
    }
}