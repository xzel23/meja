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
package com.dua3.meja.model.poi.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.DoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.PoiWorkbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;
import com.dua3.utility.options.OptionValues;

/**
 * Implementation of {@link WorkbookWriter} for Excel files in the old
 * ".xls"-format.
 */
public class XlsWorkbookWriter implements WorkbookWriter {

    private static final Logger LOGGER = Logger.getLogger(XlsWorkbookWriter.class.getName());

    private static final XlsWorkbookWriter INSTANCE = new XlsWorkbookWriter();

    /**
     * Get the singleton instance.
     *
     * @return the singleton instance of {@code XlsWorkbookWriter}
     */
    public static XlsWorkbookWriter instance() {
        return INSTANCE;
    }

    private XlsWorkbookWriter() {
    }

    @Override
    public void write(Workbook workbook, OutputStream out, DoubleConsumer updateProgress) throws IOException {
        if (workbook instanceof PoiWorkbook.PoiHssfWorkbook) {
            LOGGER.log(Level.FINE, "writing XLS workbook using POI.");
            workbook.write(FileTypeXls.instance(), out, OptionValues.empty(), updateProgress);
        } else {
            try (Workbook xlsWorkbook = PoiWorkbookFactory.instance().createXls()) {
                LOGGER.log(Level.FINE, "copying workbook data ...");
                xlsWorkbook.copy(workbook);
                LOGGER.log(Level.FINE, "writing workbook ...");
                xlsWorkbook.write(FileTypeXls.instance(), out, OptionValues.empty(), updateProgress);
                LOGGER.log(Level.FINE, "flushing buffers ...");
                out.flush();
                LOGGER.log(Level.FINE, "done.");
            }
        }
    }

}
