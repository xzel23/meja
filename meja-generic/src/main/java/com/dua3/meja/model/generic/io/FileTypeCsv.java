package com.dua3.meja.model.generic.io;

import com.dua3.meja.io.FileTypeWorkbook;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.utility.io.CsvIo;
import com.dua3.meja.io.CsvWorkbookWriter;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.options.OptionSet;
import com.dua3.utility.options.OptionValues;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

/**
 * File type for CSV files.
 */
public class FileTypeCsv extends FileTypeWorkbook<GenericWorkbook> {
    private static final FileType<GenericWorkbook> INSTANCE = new FileTypeCsv();

    /**
     * Return instance of this file type.
     * @return instance of file type
     */
    public static FileType<GenericWorkbook> instance() {
        return INSTANCE;
    }

    public FileTypeCsv() {
        super("CSV", OpenMode.READ_AND_WRITE, GenericWorkbook.class, "csv", "txt");
    }

    @Override
    public GenericWorkbook read(URI uri, Function<FileType<? extends GenericWorkbook>, OptionValues> options) throws IOException {
        return GenericWorkbookFactory.instance().open(uri);
    }

    @Override
    public GenericWorkbookFactory getWorkbookFactory() {
        return GenericWorkbookFactory.instance();
    }

    @Override
    public WorkbookWriter getWorkbookWriter() {
        return CsvWorkbookWriter.create();
    }

    @Override
    public OptionSet getSettings() {
        return CsvIo.getOptions();
    }
}
