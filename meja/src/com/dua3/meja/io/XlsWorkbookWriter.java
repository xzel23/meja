/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.io;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.PoiWorkbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class XlsWorkbookWriter extends WorkbookWriter {

    private static final XlsWorkbookWriter INSTANCE = new XlsWorkbookWriter();

    public static XlsWorkbookWriter instance() {
        return INSTANCE;
    }

    private XlsWorkbookWriter() {
    }

    @Override
    public void write(Workbook workbook, OutputStream out) throws IOException {
        if (workbook instanceof PoiWorkbook.PoiHssfWorkbook) {
            workbook.write(FileType.XLS, out);
        } else {
            Workbook xlsWorkbook = PoiWorkbookFactory.instance().createXls(workbook.getLocale());
            try {
                xlsWorkbook.copy(workbook);
                xlsWorkbook.write(FileType.XLS, out);
            } finally {
                out.flush();
                xlsWorkbook.close();
            }
        }
    }

}
