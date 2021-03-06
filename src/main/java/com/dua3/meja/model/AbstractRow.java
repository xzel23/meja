package com.dua3.meja.model;

public abstract class AbstractRow implements Row {

    private final AbstractSheet sheet;
    protected final int rowNumber;

    public AbstractRow(AbstractSheet sheet, int rowNumber) {
        this.sheet = sheet;
        this.rowNumber = rowNumber;
    }

    @Override
    public abstract AbstractCell getCell(int col);

    @Override
    public AbstractSheet getSheet() {
        return sheet;
    }

    @Override
    public abstract AbstractWorkbook getWorkbook();

    @Override
    public abstract AbstractCell getCellIfExists(int j);

    @Override
    public int getRowNumber() {
        return rowNumber;
    }
}
