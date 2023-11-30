package com.dua3.meja.util;

/**
 * The TableOptions enum represents the options that can be used when creating a table model.
 * These options control the behavior of the table, such as whether the first row is treated as a header row,
 * and whether the table is editable or not.
 */
public enum TableOptions {
    /**
     * Indicates whether the first row in a table is a header row.
     * <p>
     * If the value is set to true, it means that the first row of the table contains
     * header information, such as column names. If set to false, the first row is treated
     * as a regular data row.
     */
    FIRST_ROW_IS_HEADER,
    /**
     * Represents a flag indicating whether a table is editable.
     * This variable is used to control whether the user is allowed to make changes to the table or not.
     */
    EDITABLE
}
