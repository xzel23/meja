package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetViewDelegate;

/**
 * Represents a delegate specific to the FxSheetView, extending the general functionality provided by the
 * SheetViewDelegate class. This class is designed to interface with an FxSheetView, allowing for behavior
 * and operations tailored to the FxSheetView's requirements and attributes.
 */
public class FxSheetViewDelegate extends SheetViewDelegate {

    /**
     * Constructs an FxSheetViewDelegate instance linked to the specified FxSheetView.
     * This delegate is responsible for managing the functions and interactions
     * specific to the associated FxSheetView.
     *
     * @param owner The FxSheetView instance that this delegate will be associated with and
     *              responsible for managing.
     */
    public FxSheetViewDelegate(
            Sheet sheet,
            FxSheetView owner
    ) {
        super(sheet, owner);
    }

}
