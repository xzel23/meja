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
import com.dua3.utility.options.OptionValues;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

public class FileTypeXls extends FileTypeWorkbook<PoiHssfWorkbook> {
    private static final FileTypeXls INSTANCE = new FileTypeXls();

    public static FileTypeXls instance() {
        return INSTANCE;
    }

    private FileTypeXls() {
        super("Excel 97-2003", OpenMode.READ_AND_WRITE, PoiHssfWorkbook.class,"xls");
    }

    // loosen access to make init() callable by FileTypeExcel
    @Override
    public void init() {
        super.init();
    }

    @Override
    public PoiHssfWorkbook read(URI uri, Function<FileType, OptionValues> options) throws IOException {
        PoiWorkbook wb = PoiWorkbookFactory.instance().open(uri);
        LangUtil.check(wb instanceof PoiHssfWorkbook);
        return (PoiHssfWorkbook) wb;
    }

    @Override
    public WorkbookFactory getWorkbookFactory() {
        return PoiWorkbookFactory.instance();
    }

    @Override
    public WorkbookWriter getWorkbookWriter() {
        return XlsWorkbookWriter.instance();
    }

}
