/*
 * Copyright 2016 axel.
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
package com.dua3.meja.ui;

import com.dua3.meja.model.Workbook;

/**
 *
 * @author axel
 */
public interface WorkbookView {

    /**
     * Get the {@link SheetView} that is currently visible.
     *
     * @return the {@link SheetView} displayed on the visible tab of this view
     */
    SheetView getCurrentView();

    /**
     * Get view for sheet.
     *
     * @param sheetName name of the sheet
     * @return the view for the requested sheet or {@code null} if not found
     */
    SheetView getViewForSheet(String sheetName);

    /**
     * Get Workbook.
     *
     * @return the workbook displayed
     */
    Workbook getWorkbook();

    /**
     * Set editable state.
     *
     * @param editable set to {@code true} to allow editing of the displayed
     *                 workbook
     */
    void setEditable(boolean editable);

    /**
     * Set the workbook.
     *
     * @param workbook the workbook to display
     */
    void setWorkbook(Workbook workbook);

}
