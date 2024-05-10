package com.dua3.meja.model;

import com.dua3.cabe.annotations.Nullable;

import java.net.URI;

public interface WorkbookEvent extends Event<Workbook> {
    String ACTIVE_SHEET_CHANGED = "ACTIVE_SHEET_CHANGED";
    String SHEET_ADDED = "SHEET_ADDED";
    String SHEET_REMOVED = "SHEET_REMOVED";
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
