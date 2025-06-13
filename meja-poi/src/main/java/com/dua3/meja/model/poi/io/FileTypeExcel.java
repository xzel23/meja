package com.dua3.meja.model.poi.io;

import com.dua3.meja.io.FileTypeWorkbook;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.options.Arguments;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

/**
 * FileType for <em>reading</em> Excel files.
 * <p>
 * There are different file types for reading and writing because
 * when reading, the user normally wants to have a filter that shows <em>all</em>
 * Excel files.
 */
public class FileTypeExcel extends FileTypeWorkbook<PoiWorkbook> {

    private static final FileTypeXlsx FILETYPE_XLSX = FileTypeXlsx.instance();
    private static final FileTypeXls FILETYPE_XLS = FileTypeXls.instance();
    private static final FileTypeExcel INSTANCE;

    static {
        FileType.addType(INSTANCE = new FileTypeExcel());
    }

    /**
     * Returns the instance of FileType for PoiWorkbook.
     *
     * @return the instance of FileType for PoiWorkbook
     */
    public static FileType<PoiWorkbook> instance() {
        return INSTANCE;
    }

    /**
     * Constructs a new FileTypeExcel instance.
     * <p>
     * The FileTypeExcel class represents the file type for Excel files.
     * It supports reading Excel files in the .xlsx, .xls, and .xlsm formats.
     * <p>
     * The FileTypeExcel instance is used to specify the file type when opening Excel files with the specified extensions.
     * The file type is also used for identifying supported file types and filtering files in file selection dialogs.
     */
    public FileTypeExcel() {
        super("All Excel files", OpenMode.READ, PoiWorkbook.class, Workbook.class, "xlsx", "xls", "xlsm");
    }

    @Override
    public WorkbookFactory<PoiWorkbook> getWorkbookFactory() {
        return PoiWorkbookFactory.instance();
    }

    @Override
    public WorkbookWriter getWorkbookWriter() {
        return XlsxWorkbookWriter.instance();
    }

    @Override
    public PoiWorkbook read(URI uri, Function<FileType<? extends PoiWorkbook>, Arguments> options) throws IOException {
        if (FILETYPE_XLSX.matches(uri.getSchemeSpecificPart())) {
            return FileTypeXlsx.instance().read(uri, options);
        }
        if (FILETYPE_XLS.matches(uri.getSchemeSpecificPart())) {
            return FileTypeXls.instance().read(uri, options);
        }
        throw new IllegalArgumentException("cannot determine file type for reading");
    }

    @Override
    public boolean isCompound() {
        return true;
    }
}
