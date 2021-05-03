package com.dua3.meja.io;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.options.Arguments;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

/**
 * Abstract base class for workbook filetypes.
 * @param <W> the generic workbook type associated with this filetype
 */
public abstract class FileTypeWorkbook<W extends Workbook> extends FileType<W> {
    protected FileTypeWorkbook(String name, OpenMode mode, Class<? extends W> cls, String... extensions) {
        super(name, mode, cls, extensions);
    }

    public abstract WorkbookWriter getWorkbookWriter();
    public abstract WorkbookFactory<? extends W> getWorkbookFactory();

    @Override
    public void write(URI uri, W document, Function<FileType<? super W>, Arguments> options) throws IOException {
        getWorkbookWriter().write(document, uri);
    }
}
