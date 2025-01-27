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
 *
 * @param <W> the generic workbook type associated with this filetype
 */
public abstract class FileTypeWorkbook<W extends Workbook> extends FileType<W> {
    /**
     * Constructor.
     *
     * @param name          the name of the file type (displayed in dialogs)
     * @param mode          the {@link OpenMode}
     * @param cls           the class belonging to this file type that implements the {@link Workbook} interface
     * @param extensions    the file name extensions belonging to this file type
     */
    protected FileTypeWorkbook(String name, OpenMode mode, Class<? extends W> cls, String... extensions) {
        super(name, mode, cls, extensions);
    }

    /**
     * Returns a WorkbookWriter object.
     *
     * @return A WorkbookWriter object that can be used to write out a workbook.
     */
    public abstract WorkbookWriter getWorkbookWriter();

    /**
     * Returns a WorkbookFactory object.
     *
     * @return A WorkbookFactory object that can be used to create a workbook.
     */
    public abstract WorkbookFactory<? extends W> getWorkbookFactory();

    @Override
    public void write(URI uri, W document, Function<FileType<? super W>, Arguments> options) throws IOException {
        getWorkbookWriter().write(document, uri);
    }
}
