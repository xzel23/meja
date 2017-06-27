package com.dua3.meja.model;

public abstract class AbstractRow implements Row {

    private final AbstractSheet sheet;

    public AbstractRow(AbstractSheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public abstract AbstractCell getCell(int col);

    @Override
    public AbstractSheet getSheet() {
        return sheet;
    }

    @Override
    public abstract AbstractWorkbook getWorkbook();

}
