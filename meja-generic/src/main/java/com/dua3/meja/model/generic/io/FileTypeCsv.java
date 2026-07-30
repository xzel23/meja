package com.dua3.meja.model.generic.io;

import com.dua3.meja.io.CsvWorkbookWriter;
import com.dua3.meja.io.FileTypeWorkbook;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.utility.io.CsvIo;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.function.Function;

/**
 * File type for CSV files.
 */
public class FileTypeCsv extends FileTypeWorkbook<Workbook> {
    private static final FileTypeCsv INSTANCE = new FileTypeCsv();

    /**
     * Return instance of this file type.
     *
     * @return instance of file type
     */
    public static FileTypeCsv instance() {
        return INSTANCE;
    }

    /**
     * CSV-file type constructor. Not intended for user code, but required by SPI.
     */
    public FileTypeCsv() {
        super("CSV", OpenMode.READ_AND_WRITE, Workbook.class, Workbook.class, "csv", "txt");
    }

    @Override
    public GenericWorkbook read(URI uri, InputStream in, Function<FileType<? extends Workbook>, Arguments> options) throws IOException {
        return GenericWorkbookFactory.instance().open(uri, options.apply(this), in);
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
    public Collection<Option<?>> getSettings() {
        return CsvIo.getOptions();
    }
}
