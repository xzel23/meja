package com.dua3.meja.io;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.options.OptionValues;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

public abstract class FileTypeWorkbook<W extends Workbook> extends FileType<W> {
    protected FileTypeWorkbook(String name, OpenMode mode, Class<W> cls, String... extensions) {
        super(name, mode, cls, extensions);
    }

    public abstract WorkbookWriter getWorkbookWriter();
    public abstract WorkbookFactory getWorkbookFactory();

    @Override
    public void write(Path path, W document, Function<FileType, OptionValues> options) throws IOException {
        getWorkbookWriter().write(document, path);
    }
}
