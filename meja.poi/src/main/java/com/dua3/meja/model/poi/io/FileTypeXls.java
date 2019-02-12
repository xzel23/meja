package com.dua3.meja.model.poi.io;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.poi.PoiWorkbookFactory;

public class FileTypeXls extends FileType {
    private static final FileType INSTANCE = new FileTypeXls();
    
    public static FileType instance() {
        return INSTANCE;
    }

    private FileTypeXls() {
        super("Excel 97-2003", OpenMode.READ_AND_WRITE, "xls");
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