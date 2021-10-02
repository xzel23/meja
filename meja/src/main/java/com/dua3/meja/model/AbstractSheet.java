package com.dua3.meja.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dua3.meja.util.RectangularRegion;
import com.dua3.utility.lang.LangUtil;

/**
 * Abstract base class for implementations of the {@link Sheet} interface.
 */
public abstract class AbstractSheet implements Sheet {

    private static final Logger LOG = Logger.getLogger(AbstractSheet.class.getName());

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<RectangularRegion> mergedRegions = new ArrayList<>();

    protected AbstractSheet() {
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    protected void cellStyleChanged(AbstractCell cell, Object old, Object arg) {
        PropertyChangeEvent evt = new PropertyChangeEvent(cell, PROPERTY_CELL_STYLE, old, arg);
        pcs.firePropertyChange(evt);
    }

    void cellValueChanged(AbstractCell cell, Object old, Object arg) {
        PropertyChangeEvent evt = new PropertyChangeEvent(cell, PROPERTY_CELL_CONTENT, old, arg);
        pcs.firePropertyChange(evt);
    }

    @Override
    public Lock readLock() {
        return lock.readLock();
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public Lock writeLock() {
        return lock.writeLock();
    }

    protected <T> void firePropertyChange(String property, T oldValue, T newValue) {
        pcs.firePropertyChange(property, oldValue, newValue);
    }

    @Override
    public void addMergedRegion(RectangularRegion cells) {
        // check that all cells are unmerged
        for (RectangularRegion rr : mergedRegions) {
            LangUtil.check(!rr.intersects(cells), "New merged region overlaps with an existing one.");
        }

        // update cell data
        int spanX = cells.lastColumn() - cells.firstColumn() + 1;
        int spanY = cells.lastRow() - cells.firstRow() + 1;
        AbstractCell topLeftCell = getCell(cells.firstRow(), cells.firstColumn());
        for (int i = 0; i < spanY; i++) {
            for (int j = 0; j < spanX; j++) {
                AbstractCell cell = getCell(cells.firstRow() + i, cells.firstColumn() + j);
                cell.addedToMergedRegion(topLeftCell, spanX, spanY);
            }
        }

        // add to list
        mergedRegions.add(cells);

        LOG.log(Level.FINE, "added merged region: {0}", cells);
    }

    @Override
    public List<RectangularRegion> getMergedRegions() {
        return Collections.unmodifiableList(mergedRegions);
    }

    @Override
    public RectangularRegion getMergedRegion(int rowNum, int colNum) {
        return mergedRegions.stream().filter(rr -> rr.contains(rowNum, colNum)).findFirst().orElse(null);
    }

    protected void removeMergedRegion(int rowNumber, int columnNumber) {
        for (int idx = 0; idx < mergedRegions.size(); idx++) {
            RectangularRegion rr = mergedRegions.get(idx);
            if (rr.firstRow() == rowNumber && rr.firstColumn() == columnNumber) {
                mergedRegions.remove(idx--);
                for (int i = rr.firstRow(); i <= rr.lastRow(); i++) {
                    Row row = getRow(i);
                    for (int j = rr.firstColumn(); j <= rr.lastColumn(); j++) {
                        AbstractCell cell = (AbstractCell) row.getCellIfExists(j);
                        if (cell != null) {
                            cell.removedFromMergedRegion();
                        }
                    }
                }
            }
        }
        LOG.log(Level.FINE, "removed merged region at [{0}]", rowNumber + "," + columnNumber);
    }

    @Override
    public abstract AbstractCell getCell(int i, int j);

    @Override
    public abstract AbstractWorkbook getWorkbook();
}
