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

import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;
import com.dua3.utility.options.Arguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.DoubleConsumer;

/**
 * Implementation of {@link WorkbookWriter} for Excel files in the old
 * ".xls"-format.
 */
public final class XlsWorkbookWriter implements WorkbookWriter {

    private static final Logger LOGGER = LogManager.getLogger(XlsWorkbookWriter.class);

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
        if (workbook instanceof PoiHssfWorkbook) {
            LOGGER.debug("writing XLS workbook using POI");
            workbook.write(FileTypeXls.instance(), out, Arguments.empty(), updateProgress);
        } else {
            try (Workbook xlsWorkbook = PoiWorkbookFactory.instance().createXls()) {
                LOGGER.debug("copying workbook data");
                xlsWorkbook.copy(workbook);
                LOGGER.debug("writing workbook");
                xlsWorkbook.write(FileTypeXls.instance(), out, Arguments.empty(), updateProgress);
                LOGGER.debug("flushing buffers");
                out.flush();
            }
        }
    }

}
