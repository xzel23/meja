package com.dua3.meja.model.poi.io;

import com.dua3.meja.io.FileTypeWorkbook;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.options.OptionValues;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * FileType for <em>reading</em> Excel files.
 *
 * There are different file types for reading and writing because
 * when reading, the user normally wants to have a filter that shows <em>all</em>
 * Excel files.
 */
public class FileTypeExcel extends FileTypeWorkbook<PoiWorkbook> {
    private static final FileType INSTANCE = new FileTypeExcel();

    public static FileType instance() {
        return INSTANCE;
    }

    public FileTypeExcel() {
        super("All Excel files", OpenMode.READ, PoiWorkbook.class, "xlsx", "xls", "xlsm");
    }

    @Override
    public WorkbookFactory getWorkbookFactory() {
        return PoiWorkbookFactory.instance();
    }

    @Override
    public WorkbookWriter getWorkbookWriter() {
        return XlsxWorkbookWriter.instance();
    }

    @Override
    protected void init() {
        // register this type
        super.init();
        // also register the other types provided by the POI implementation
        FileTypeXlsx.instance().init();
        FileTypeXls.instance().init();
    }

    @Override
    public PoiWorkbook read(URI uri, Function<FileType, OptionValues> options) throws IOException {
        if (FileTypeXlsx.instance().matches(uri.getSchemeSpecificPart())) {
            return FileTypeXlsx.instance().read(uri, options);
        }
        if (FileTypeXls.instance().matches(uri.getSchemeSpecificPart())) {
            return FileTypeXls.instance().read(uri, options);
        }
        throw new IllegalArgumentException("cannot determine file type for reading");
    }
}
