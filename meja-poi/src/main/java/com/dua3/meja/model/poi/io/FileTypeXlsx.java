package com.dua3.meja.model.poi.io;

import com.dua3.meja.io.FileTypeWorkbook;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Arguments;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

/**
 * File type for Excel .XLSX files.
 */
public final class FileTypeXlsx extends FileTypeWorkbook<PoiWorkbook> {
    private static final FileTypeXlsx INSTANCE = new FileTypeXlsx();

    /**
     * Get XLSX file type instance.
     *
     * @return instance of this file type
     */
    public static FileTypeXlsx instance() {
        return INSTANCE;
    }

    private FileTypeXlsx() {
        super("Excel", OpenMode.READ_AND_WRITE, PoiXssfWorkbook.class, "xlsx", "xlsm");
    }

    // loosen access to make init() callable by FileTypeExcel
    @Override
    public void init() {
        super.init();
    }

    @Override
    public PoiWorkbook read(URI uri, Function<FileType<? extends PoiWorkbook>, Arguments> options) throws IOException {
        PoiWorkbook wb = PoiWorkbookFactory.instance().open(uri);
        LangUtil.check(wb instanceof PoiXssfWorkbook, "internal error: expected an instance of PoiXssfWorkbook but got %s", wb.getClass());
        return wb;
    }

    @Override
    public WorkbookFactory<PoiWorkbook> getWorkbookFactory() {
        return PoiWorkbookFactory.instance();
    }

    @Override
    public WorkbookWriter getWorkbookWriter() {
        return XlsxWorkbookWriter.instance();
    }
}
