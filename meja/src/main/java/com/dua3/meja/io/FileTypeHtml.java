package com.dua3.meja.io;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.utility.io.CsvIo;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 * A FileType instance for HTML files.
 */
public class FileTypeHtml extends FileTypeWorkbook<Workbook> {
    private static final FileType<Workbook> INSTANCE = new FileTypeHtml();

    public static FileType<Workbook> instance() {
        return INSTANCE;
    }

    public FileTypeHtml() {
        super("HTML", OpenMode.WRITE, Workbook.class, "html", "htm");
    }

    @Override
    public WorkbookWriter getWorkbookWriter() {
        return HtmlWorkbookWriter.create();
    }

    @Override
    public WorkbookFactory<? extends Workbook> getWorkbookFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Workbook read(URI uri, Function<FileType<? extends Workbook>, Arguments> options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Option<?>> getSettings() {
        return Collections.emptyList();
    }
}
