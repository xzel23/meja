/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.meja.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.PoiWorkbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;

/**
 * Implementation of {@link WorkbookWriter} for Excel files in the new
 * ".xlsx"-format.
 */
public class XlsxWorkbookWriter extends WorkbookWriter {

    private static final Logger LOGGER = Logger.getLogger(XlsxWorkbookWriter.class.getName());

    private static final XlsxWorkbookWriter INSTANCE = new XlsxWorkbookWriter();

    /**
     * Get the singleton instance.
     *
     * @return the singleton instance of {@code XlsxWorkbookWriter}
     */
    public static XlsxWorkbookWriter instance() {
        return INSTANCE;
    }

    private XlsxWorkbookWriter() {
    }

    @Override
    public void write(Workbook workbook, OutputStream out) throws IOException {
        if (workbook instanceof PoiWorkbook.PoiXssfWorkbook) {
            LOGGER.log(Level.FINE, "writing XLSX workbook using POI.");
            workbook.write(FileType.XLSX, out);
        } else {
            LOGGER.log(Level.FINE, "writing {0} using streaming API in XLSX format.", workbook.getClass().getSimpleName());
            try (Workbook xlsxWorkbook = PoiWorkbookFactory.instance().createXlsxStreaming()) {
                LOGGER.log(Level.FINE, "copying workbook data ...");
                xlsxWorkbook.copy(workbook);
                LOGGER.log(Level.FINE, "writing workbook ...");
                xlsxWorkbook.write(FileType.XLSX, out);
                LOGGER.log(Level.FINE, "flushing buffers ...");
                out.flush();
                LOGGER.log(Level.FINE, "done.");
            }
        }
    }

}
