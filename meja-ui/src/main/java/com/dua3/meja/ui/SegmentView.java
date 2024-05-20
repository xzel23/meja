/*
 *
 */
package com.dua3.meja.ui;

import com.dua3.meja.model.Sheet;

/**
 * @author Axel Howind
 */
public interface SegmentView {

    SegmentViewDelegate getDelegate();

    Sheet getSheet();

    void setViewSizeOnDisplay(float w, float h);

}
