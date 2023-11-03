package com.dua3.meja.model.poi.io;

import com.dua3.meja.io.FileTypeWorkbook;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Arguments;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

/**
 * File type for Excel 97-2004 .XLS files.
 */
public final class FileTypeXls extends FileTypeWorkbook<PoiWorkbook> {
    private static final FileTypeXls INSTANCE = new FileTypeXls();

    /**
     * Get XLS file type instance.
     *
     * @return instance of this file type
     */
    public static FileTypeXls instance() {
        return INSTANCE;
    }

    private FileTypeXls() {
        super("Excel 97-2003", OpenMode.READ_AND_WRITE, PoiHssfWorkbook.class, "xls");
    }

    // loosen access to make init() callable by FileTypeExcel
    @Override
    public void init() {
        super.init();
    }

    @Override
    public PoiHssfWorkbook read(URI uri, Function<FileType<? extends PoiWorkbook>, Arguments> options) throws IOException {
        PoiWorkbook wb = PoiWorkbookFactory.instance().open(uri);
        LangUtil.check(wb instanceof PoiHssfWorkbook, "internal error: expected an instance of PoiWorkbook but got %s", wb.getClass());
        return (PoiHssfWorkbook) wb;
    }

    @Override
    public WorkbookFactory<PoiWorkbook> getWorkbookFactory() {
        return PoiWorkbookFactory.instance();
    }

    @Override
    public WorkbookWriter getWorkbookWriter() {
        return XlsWorkbookWriter.instance();
    }

}
