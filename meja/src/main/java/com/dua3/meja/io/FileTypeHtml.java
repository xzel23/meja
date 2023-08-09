package com.dua3.meja.io;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
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

    /**
     * Returns the singleton instance of FileType for Workbooks in HTML format.
     *
     * @return The singleton instance of FileType for Workbooks in HTML format.
     */
    public static FileType<Workbook> instance() {
        return INSTANCE;
    }

    /**
     * Constructs a new FileTypeHtml instance.
     *
     * Initializes the FileType with the name "HTML", open mode as "WRITE", associated class as Workbook,
     * and file extensions "html" and "htm".
     */
    public FileTypeHtml() {
        super("HTML", OpenMode.WRITE, Workbook.class, "html", "htm");
    }

    @Override
    public WorkbookWriter getWorkbookWriter() {
        return HtmlWorkbookWriter.create();
    }

    @Override
    public WorkbookFactory<? extends Workbook> getWorkbookFactory() {
        throw new UnsupportedOperationException("not implemented: factory for HTML workbooks");
    }

    @Override
    public Workbook read(URI uri, Function<FileType<? extends Workbook>, Arguments> options) throws IOException {
        throw new UnsupportedOperationException("not implemented: reading workbook from HTML file");
    }

    @Override
    public Collection<Option<?>> getSettings() {
        return Collections.emptyList();
    }
}
