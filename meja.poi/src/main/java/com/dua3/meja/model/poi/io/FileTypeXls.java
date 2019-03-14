package com.dua3.meja.model.poi.io;

import com.dua3.meja.io.FileTypeWorkbook;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.OptionValues;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

public class FileTypeXls extends FileTypeWorkbook<PoiWorkbook.PoiHssfWorkbook> {
    private static final FileTypeXls INSTANCE = new FileTypeXls();
    
    public static FileTypeXls instance() {
        return INSTANCE;
    }

    private FileTypeXls() {
        super("Excel 97-2003", OpenMode.READ_AND_WRITE, PoiWorkbook.PoiHssfWorkbook.class,"xls");
    }

    // loosen access to make init() callable by FileTypeExcel
    @Override
    public void init() {
        super.init();
    }

    @Override
    public PoiWorkbook.PoiHssfWorkbook read(Path path, Function<FileType, OptionValues> options) throws IOException {
        PoiWorkbook wb = PoiWorkbookFactory.instance().open(path);
        LangUtil.check(wb instanceof PoiWorkbook.PoiHssfWorkbook);
        return (PoiWorkbook.PoiHssfWorkbook) wb;
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