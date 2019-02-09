package com.dua3.meja.model.poi.io;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.poi.PoiWorkbookFactory;

public class FileTypeXlsx extends FileType {
    private static final FileType INSTANCE = new FileTypeXlsx();
    
    public static FileType instance() {
        return INSTANCE;
    }
    
    private FileTypeXlsx() {
        super("Excel", "Excel files", OpenMode.READ_AND_WRITE, "xlsx");
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
