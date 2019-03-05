package com.dua3.meja.model.generic;

import com.dua3.utility.io.CsvIo;
import com.dua3.meja.io.CsvWorkbookWriter;
import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.utility.options.OptionSet;

public class FileTypeCsv extends FileType {
    private static final FileType INSTANCE = new FileTypeCsv();
    
    public static FileType instance() {
        return INSTANCE;
    }
    
    private FileTypeCsv() {
        super("CSV", OpenMode.READ_AND_WRITE, "csv", "txt");
        addType(this);
    }
    
    @Override
    public GenericWorkbookFactory factory() {
        return GenericWorkbookFactory.instance();
    }
    @Override
    public WorkbookWriter getWriter() {
        return CsvWorkbookWriter.create();
    }
    @Override
    public OptionSet getSettings() {
        return CsvIo.getOptions();
    }
}