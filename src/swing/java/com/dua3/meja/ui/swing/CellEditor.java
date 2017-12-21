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
package com.dua3.meja.ui.swing;

import javax.swing.JComponent;

import com.dua3.meja.model.Cell;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public interface CellEditor {

    /**
     * Check editing state.
     *
     * @return true if CellEditor is currently used to edit a cell
     */
    boolean isEditing();

    /**
     * Start editing.
     *
     * @param cell
     *            the cell to be edited
     * @return the component that is used for editing
     */
    JComponent startEditing(Cell cell);

    /**
     * Stop editing.
     *
     * @param commit
     *            if true, changes will be applied to the edited cell
     */
    void stopEditing(boolean commit);

}
