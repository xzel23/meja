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

import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * The WorkbookView interface represents a user interface view for a workbook.
 * It provides methods for accessing and modifying the displayed workbook and its sheets.
 * The generic type parameter SV represents the type of sheet views used in this workbook view.
 *
 * @param <SV> the generic type of the {@link SheetView} implementation
 */
public interface WorkbookView<SV extends SheetView> {

    /**
     * Get the {@link SheetView} that is currently visible.
     *
     * @return the {@link SheetView} displayed on the visible tab of this view
     */
    Optional<SV> getCurrentView();

    /**
     * Get view for sheet.
     *
     * @param sheet the sheet
     * @return the view for the requested sheet
     */
    Optional<SV> getViewForSheet(Sheet sheet);

    /**
     * Get Workbook.
     *
     * @return the workbook displayed
     */
    Optional<Workbook> getWorkbook();

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
    void setWorkbook(@Nullable Workbook workbook);

}
