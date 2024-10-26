package com.dua3.meja.model;

import org.jspecify.annotations.Nullable;

import java.net.URI;

/**
 * Interface representing various events that can occur within a workbook.
 * These events include changes to the active sheet, adding or removing sheets,
 * and changes to the workbook's URI.
 */
public interface WorkbookEvent extends Event<Workbook> {
    /**
     * This constant signifies that the currently active sheet has changed.
     */
    String ACTIVE_SHEET_CHANGED = "ACTIVE_SHEET_CHANGED";
    /**
     * This constant signifies that a sheet has been added.
     */
    String SHEET_ADDED = "SHEET_ADDED";
    /**
     * This constant signifies that a sheet has been removed.
     */
    String SHEET_REMOVED = "SHEET_REMOVED";
    /**
     * This constant signifies that the workbook URI has changed.
     */
    String URI_CHANGED = "URI_CHANGED";

    /**
     * Event sent when the active sheet changes.
     */
    class ActiveSheetChanged extends EventIndexChanged<Workbook> implements WorkbookEvent {
        ActiveSheetChanged(Workbook workbook, int idxOld, int idxNew) {
            super(workbook, ACTIVE_SHEET_CHANGED, idxOld, idxNew);
        }
    }

    /**
     * Event sent when a sheet is added to the workbook.
     */
    class SheetAdded extends EventWithIndex<Workbook> implements WorkbookEvent {
        SheetAdded(Workbook workbook, int idx) {
            super(workbook, SHEET_ADDED, idx);
        }
    }

    /**
     * Event sent when a sheet is removed from the workbook.
     */
    class SheetRemoved extends EventWithIndex<Workbook> implements WorkbookEvent {
        SheetRemoved(Workbook workbook, int idx) {
            super(workbook, SHEET_REMOVED, idx);
        }
    }

    /**
     * Event sent when the workbook URI changes.
     */
    class UriChanged extends EventValueChanged<Workbook, URI> implements WorkbookEvent {
        UriChanged(Workbook workbook, @Nullable URI uriOld, @Nullable URI uriNew) {
            super(workbook, URI_CHANGED, uriOld, uriNew);
        }
    }
}
