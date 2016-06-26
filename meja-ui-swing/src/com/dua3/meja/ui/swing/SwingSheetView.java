/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.SearchOptions;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.Rectangle;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.util.MejaHelper;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumSet;
import java.util.concurrent.locks.Lock;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Swing component for displaying instances of {@link Sheet}.
 */
public class SwingSheetView extends JPanel implements SheetView, PropertyChangeListener {

    private static final long serialVersionUID = 1L;

    static final int MAX_COLUMN_WIDTH = 800;

    /**
     * Test whether style uses text wrapping. While there is a property for text
     * wrapping, the alignment settings have to be taken into account too.
     *
     * @param style style
     * @return true if cell content should be displayed with text wrapping
     */
    static boolean isWrapping(CellStyle style) {
        return style.isWrap() || style.getHAlign().isWrap() || style.getVAlign().isWrap();
    }

    private final SwingSheetPainter sheetPainter;
    private final CellEditor editor;
    private final SheetPane sheetPane;
    private SearchDialog searchDialog = null;

    /**
     * The scale used to calculate screen sizes dependent of display resolution.
     */
    private float scaleDpi = 1f;

    /**
     * The scaling factor used to draw the sheet according to current zoom.
     */
    private float scale = 1f;

    /**
     * Height of the sheet in points.
     */
    private float sheetWidthInPoints;

    /**
     * Width of the sheet in points.
     */
    private float sheetHeightInPoints;

    /**
     * The sheet displayed.
     */
    private Sheet sheet;

    /**
     * The color to use for the grid lines.
     */
    private Color gridColor = Color.LIGHT_GRAY;

    /**
     * Read-only mode.
     */
    private boolean editable = false;

    /**
     * Editing state.
     */
    private boolean editing = false;

    /**
     * Constructor.
     *
     * No sheet is set.
     */
    public SwingSheetView() {
        this(null);
    }

    int xS2D(double x) {
        return (int) Math.round(x);
    }

    int yS2D(double y) {
        return (int) Math.round(y);
    }

    int wS2D(double w) {
        return (int) Math.round(w);
    }

    int hS2D(double h) {
        return (int) Math.round(h);
    }

    java.awt.Rectangle rectS2D(Rectangle r) {
        return new java.awt.Rectangle(
                xS2D(r.getX()),
                yS2D(r.getY()),
                wS2D(r.getW()),
                hS2D(r.getH())
        );
    }

    /**
     * Construct a new SheetView for the given sheet.
     *
     * @param sheet the sheet to display
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public SwingSheetView(Sheet sheet) {
        super(new GridLayout(1, 1));

        sheetPainter = new SwingSheetPainter(this, new DefaultCellRenderer());
        editor = new DefaultCellEditor(this);
        sheetPane = new SheetPane();
        searchDialog = new SearchDialog();

        init(sheet);
    }

    private void init(Sheet sheet1) {
        add(sheetPane);
        // setup input map for keyboard navigation
        final InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0), Actions.MOVE_UP);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_UP, 0), Actions.MOVE_UP);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0), Actions.MOVE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_DOWN, 0), Actions.MOVE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), Actions.MOVE_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_LEFT, 0), Actions.MOVE_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), Actions.MOVE_RIGHT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_RIGHT, 0), Actions.MOVE_RIGHT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_UP, 0), Actions.PAGE_UP);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_DOWN, 0), Actions.PAGE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_HOME, InputEvent.CTRL_DOWN_MASK), Actions.MOVE_HOME);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_END, InputEvent.CTRL_DOWN_MASK), Actions.MOVE_END);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0), Actions.START_EDITING);
        inputMap.put(KeyStroke.getKeyStroke('F', java.awt.event.InputEvent.CTRL_DOWN_MASK), Actions.SHOW_SEARCH_DIALOG);
        final ActionMap actionMap = getActionMap();
        for (Actions action : Actions.values()) {
            actionMap.put(action, action.getAction(this));
        }
        // listen to mouse events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onMousePressed(e.getX() + xS2D(getSplitX()), e.getY() + yS2D(getSplitY()));
            }
        });
        // make focusable
        setFocusable(true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                requestFocusInWindow();
            }
        });
        setSheet(sheet1);
    }

    /**
     * Check whether editing is enabled.
     *
     * @return true if this SwingSheetView allows editing.
     */
    @Override
    public boolean isEditable() {
        return editable;
    }

    /**
     * Enable/disable sheet editing.
     *
     * @param editable true to allow editing
     */
    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Check editing state.
     *
     * @return true, if a cell is being edited.
     */
    @Override
    public boolean isEditing() {
        return editing;
    }

    /**
     * Move the selection rectangle to an adjacent cell.
     *
     * @param d direction
     */
    private void move(Direction d) {
        Cell cell = getCurrentCell().getLogicalCell();

        switch (d) {
            case NORTH:
                setCurrentRowNum(cell.getRowNumber() - 1);
                break;
            case SOUTH:
                setCurrentRowNum(cell.getRowNumber() + cell.getVerticalSpan());
                break;
            case WEST:
                setCurrentColNum(cell.getColumnNumber() - 1);
                break;
            case EAST:
                setCurrentColNum(cell.getColumnNumber() + cell.getHorizontalSpan());
                break;
        }
    }

    /**
     * Move the selection rectangle to an adjacent cell.
     *
     * @param d direction
     */
    private void movePage(Direction d) {
        Cell cell = getCurrentCell().getLogicalCell();
        Rectangle cellRect = sheetPainter.getCellRect(cell);
        double x = cellRect.getX();
        double y = cellRect.getY();
        switch (d) {
            case NORTH:
                y = Math.max(0, y - getVisibleRect().height);
                break;
            case SOUTH:
                y = Math.min(getSheetHeight() - 1, y + getVisibleRect().height);
                break;
        }

        int row = sheetPainter.getRowNumberFromY(y);
        int col = sheetPainter.getColumnNumberFromX(x);
        setCurrentCell(row, col);
    }

    /**
     * Move the selection rectangle to the top left cell.
     */
    private void moveHome() {
        int row = sheet.getFirstRowNum();
        int col = sheet.getFirstColNum();
        setCurrentCell(row, col);
    }

    /**
     * Move the selection rectangle to the bottom right cell.
     */
    private void moveEnd() {
        int row = sheet.getLastRowNum();
        int col = sheet.getLastColNum();
        setCurrentCell(row, col);
    }

    /**
     * Scroll the currently selected cell into view.
     */
    @Override
    public void scrollToCurrentCell() {
        sheetPane.ensureCellIsVisibile(getCurrentCell().getLogicalCell());
    }

    public void repaintCell(Cell cell) {
        sheetPane.repaintSheet(sheetPainter.getSelectionRect(cell));
    }

    /**
     * Get the current row number.
     *
     * @return row number of the selected cell
     */
    public int getCurrentRowNum() {
        return sheet.getCurrentCell().getRowNumber();
    }

    /**
     * Set the current row number.
     *
     * @param rowNum number of row to be set
     */
    public void setCurrentRowNum(int rowNum) {
        setCurrentCell(rowNum, getCurrentColNum());
    }

    /**
     * Get the current column number.
     *
     * @return column number of the selected cell
     */
    public int getCurrentColNum() {
        return sheet.getCurrentCell().getColumnNumber();
    }

    /**
     * Set the current column number.
     *
     * @param colNum number of column to be set
     */
    public void setCurrentColNum(int colNum) {
        setCurrentCell(getCurrentRowNum(), colNum);
    }

    /**
     * Set current row and column.
     *
     * @param rowNum number of row to be set
     * @param colNum number of column to be set
     * @return true if the current logical cell changed
     */
    @Override
    public boolean setCurrentCell(int rowNum, int colNum) {
        Cell oldCell = sheet.getCurrentCell();
        int newRowNum = Math.max(sheet.getFirstRowNum(), Math.min(sheet.getLastRowNum(), rowNum));
        int newColNum = Math.max(sheet.getFirstColNum(), Math.min(sheet.getLastColNum(), colNum));
        sheet.setCurrentCell(newRowNum, newColNum);
        return getCurrentCell() != oldCell;
    }

    /**
     * Enter edit mode for the current cell.
     */
    private void startEditing() {
        if (!isEditable() || isEditing()) {
            return;
        }

        final Cell cell = getCurrentCell().getLogicalCell();

        sheetPane.ensureCellIsVisibile(cell);
        sheetPane.setScrollable(false);

        final JComponent editorComp = editor.startEditing(cell);
        final Rectangle cellRect = sheetPane.getCellRectInViewCoordinates(cell);

        JComponent editorParent;
        editorParent = sheetPane;
        editorComp.setBounds(rectS2D(cellRect));
        editorParent.add(editorComp);
        editorComp.validate();
        editorComp.setVisible(true);
        editorComp.repaint();
        editing = true;
    }

    /**
     * End edit mode for the current cell.
     *
     * @param commit true if the content of the edited cell is to be updated
     */
    @Override
    public void stopEditing(boolean commit) {
        editor.stopEditing(commit);
    }

    /**
     * Reset editing state when finished editing. This method should only be
     * called from the {@link CellEditor#stopEditing} method of
     * {@link CellEditor} subclasses.
     */
    public void stoppedEditing() {
        editing = false;
        sheetPane.setScrollable(true);
    }

    float getScale() {
        return scale;
    }

    /**
     * @return the sheetWidth
     */
    public int getSheetWidth() {
        return Math.round(sheetWidthInPoints * getScale());
    }

    /**
     * @return the sheetHeight
     */
    public int getSheetHeight() {
        return Math.round(sheetHeightInPoints * getScale());
    }

    /**
     * Set sheet to display.
     *
     * @param sheet the sheet to display
     */
    @Override
    public final void setSheet(Sheet sheet) {
        if (this.sheet != null) {
            this.sheet.removePropertyChangeListener(this);
        }

        if (sheet != this.sheet) {
            Sheet oldSheet = this.sheet;
            this.sheet = sheet;

            if (this.sheet != null) {
                sheet.addPropertyChangeListener(this);
            }

            updateContent();

            firePropertyChange(PROPERTY_SHEET, oldSheet, sheet);
        }
    }

    @Override
    public Sheet getSheet() {
        return sheet;
    }

    @Override
    public void removeNotify() {
        searchDialog.dispose();
        super.removeNotify();
    }

    /**
     * Show the search dialog.
     */
    private void showSearchDialog() {
        searchDialog.setVisible(true);
    }

    void onMousePressed(int x, int y) {
        // make the cell under pointer the current cell
        int row = sheetPainter.getRowNumberFromY(y);
        int col = sheetPainter.getColumnNumberFromX(x);
        boolean currentCellChanged = setCurrentCell(row, col);
        requestFocusInWindow();

        if (!currentCellChanged) {
            // if it already was the current cell, start cell editing
            if (isEditable()) {
                startEditing();
                editing = true;
            }
        } else // otherwise stop cell editing
        {
            if (editing) {
                stopEditing(true);
                editing = false;
            }
        }
    }

    /**
     * Set the grid color.
     *
     * @param gridColor the color for th grid
     */
    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    /**
     * Get the grid color.
     *
     * @return color of grid
     */
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * Get y-coordinate of split.
     *
     * @return y coordinate of split
     */
    double getSplitY() {
        return sheet == null ? 0 : sheetPainter.getRowPos(sheet.getSplitRow());
    }

    /**
     * Get x-coordinate of split.
     *
     * @return x coordinate of split
     */
    double getSplitX() {
        return sheet == null ? 0 : sheetPainter.getColumnPos(sheet.getSplitColumn());
    }

    public Dimension getSheetSize() {
        return new Dimension(getSheetWidth() + 1, getSheetHeight() + 1);
    }

    /**
     * Get column name.
     *
     * @param j the column number
     * @return name of column
     */
    public String getColumnName(int j) {
        return MejaHelper.getColumnName(j);
    }

    /**
     * Get row name.
     *
     * @param i the row number
     * @return name of row
     */
    public String getRowName(int i) {
        return Integer.toString(i + 1);
    }

    private Cell getCurrentCell() {
        return sheet.getCurrentCell();
    }

    private void updateContent() {
        // scale according to screen resolution
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        scaleDpi = dpi / 72f;

        sheetPainter.update(sheet);

        revalidate();
        repaint();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case Sheet.PROPERTY_ZOOM:
            case Sheet.PROPERTY_LAYOUT:
                updateContent();
                break;
            case Sheet.PROPERTY_FREEZE:
                updateContent();
                scrollToCurrentCell();
                break;
            case Sheet.PROPERTY_ACTIVE_CELL:
                repaintCell((Cell) evt.getOldValue());
                repaintCell((Cell) evt.getNewValue());
                scrollToCurrentCell();
                break;
            case Sheet.PROPERTY_CELL_CONTENT:
            case Sheet.PROPERTY_CELL_STYLE:
                repaintCell((Cell) evt.getSource());
                break;
            default:
                // nop
                break;
        }
    }

    /**
     * Actions for key bindings.
     */
    @SuppressWarnings("serial")
    static enum Actions {
        MOVE_UP {
            @Override
            public Action getAction(final SwingSheetView view) {
                return new AbstractAction(name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        view.move(Direction.NORTH);
                    }
                };
            }
        }, MOVE_DOWN {
            @Override
            public Action getAction(final SwingSheetView view) {
                return new AbstractAction(name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        view.move(Direction.SOUTH);
                    }
                };
            }
        }, MOVE_LEFT {
            @Override
            public Action getAction(final SwingSheetView view) {
                return new AbstractAction(name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        view.move(Direction.WEST);
                    }
                };
            }
        }, MOVE_RIGHT {
            @Override
            public Action getAction(final SwingSheetView view) {
                return new AbstractAction(name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        view.move(Direction.EAST);
                    }
                };
            }
        }, PAGE_UP {
            @Override
            public Action getAction(final SwingSheetView view) {
                return new AbstractAction(name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        view.movePage(Direction.NORTH);
                    }
                };
            }
        }, PAGE_DOWN {
            @Override
            public Action getAction(final SwingSheetView view) {
                return new AbstractAction(name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        view.movePage(Direction.SOUTH);
                    }
                };
            }
        }, MOVE_HOME {
            @Override
            public Action getAction(final SwingSheetView view) {
                return new AbstractAction(name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        view.moveHome();
                    }
                };
            }
        }, MOVE_END {
            @Override
            public Action getAction(final SwingSheetView view) {
                return new AbstractAction(name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        view.moveEnd();
                    }
                };
            }
        }, START_EDITING {
            @Override
            public Action getAction(final SwingSheetView view) {
                return new AbstractAction(name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        view.startEditing();
                    }
                };
            }
        }, SHOW_SEARCH_DIALOG {
            @Override
            public Action getAction(final SwingSheetView view) {
                return new AbstractAction(name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        view.showSearchDialog();
                    }
                };
            }
        };

        abstract Action getAction(SwingSheetView view);
    }

    private class SearchDialog extends JDialog {

        private static final long serialVersionUID = 1L;

        private final JTextField jtfText = new JTextField(40);
        private final JCheckBox jcbIgnoreCase = new JCheckBox("ignore case", true);
        private final JCheckBox jcbMatchCompleteText = new JCheckBox("match complete text", false);

        SearchDialog() {
            init();
        }

        private void init() {
            setTitle("Search");
            setModal(true);
            setResizable(false);

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            getRootPane().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            // text label
            c.gridx = 1;
            c.gridy = 1;
            c.gridwidth = 1;
            c.gridheight = 1;
            add(new JLabel("Text:"), c);

            // text input
            c.gridx = 2;
            c.gridy = 1;
            c.gridwidth = 4;
            c.gridheight = 1;
            add(jtfText, c);

            // options
            c.gridx = 1;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            add(new JLabel("Options:"), c);

            c.gridx = 2;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            add(jcbIgnoreCase, c);

            c.gridx = 3;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            add(jcbMatchCompleteText, c);

            // submit button
            c.gridx = 4;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            final JButton submitButton = new JButton(new AbstractAction("Search") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doSearch();
                }

            });
            add(submitButton, c);

            // close button
            c.gridx = 5;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            add(new JButton(new AbstractAction("Close") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            }), c);

            // Enter starts search
            SwingUtilities.getRootPane(submitButton).setDefaultButton(submitButton);

            // Escape closes dialog
            final AbstractAction escapeAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    setVisible(false);
                }
            };

            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE_KEY");
            rootPane.getActionMap().put("ESCAPE_KEY", escapeAction);

            // pack layout
            pack();
        }

        String getText() {
            return jtfText.getText();
        }

        void doSearch() {
            EnumSet<SearchOptions> options = EnumSet.of(SearchOptions.SEARCH_FROM_CURRENT);

            if (jcbIgnoreCase.isSelected()) {
                options.add(SearchOptions.IGNORE_CASE);
            }

            if (jcbMatchCompleteText.isSelected()) {
                options.add(SearchOptions.MATCH_COMPLETE_TEXT);
            }

            Lock readLock = sheet.readLock();
            Cell cell = null;
            try {
                readLock.lock();
                cell = MejaHelper.find(sheet, getText(), options);
            } finally {
                readLock.unlock();
            }

            if (cell == null) {
                JOptionPane.showMessageDialog(this, "Text was not found.");
            } else {
                setCurrentCell(cell.getRowNumber(), cell.getColumnNumber());
            }
        }

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);

            if (visible) {
                jtfText.requestFocusInWindow();
                jtfText.selectAll();
            }
        }

    }

    private class SheetPane extends JScrollPane {

        private final JLabel painter = new JLabel();
        private int labelHeight = 0;
        private int labelWidth = 0;
        private final TopLeftQuadrant topLeftQuadrant = new TopLeftQuadrant();
        private final TopRightQuadrant topRightQuadrant = new TopRightQuadrant();
        private final BottomLeftQuadrant bottomLeftQuadrant = new BottomLeftQuadrant();
        private final BottomRightQuadrant bottomRightQuadrant = new BottomRightQuadrant();

        SheetPane() {
            init();
        }

        private void init() {
            setDoubleBuffered(false);

            painter.setOpaque(true);
            painter.setHorizontalAlignment(SwingConstants.CENTER);
            painter.setVerticalAlignment(SwingConstants.CENTER);
            painter.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, gridColor));

            // set quadrant painters
            setViewportView(bottomRightQuadrant);
            setColumnHeaderView(topRightQuadrant);
            setRowHeaderView(bottomLeftQuadrant);
            setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, topLeftQuadrant);

            setViewportBorder(BorderFactory.createEmptyBorder());
        }

        private void repaintSheet(Rectangle rect) {
            topLeftQuadrant.repaintSheet(rect);
            topRightQuadrant.repaintSheet(rect);
            bottomLeftQuadrant.repaintSheet(rect);
            bottomRightQuadrant.repaintSheet(rect);
        }

        /**
         * Scroll cell into view.
         *
         * @param cell the cell to scroll to
         */
        public void ensureCellIsVisibile(Cell cell) {
            final Rectangle cellRect = sheetPainter.getCellRect(cell);
            boolean aboveSplit = getSplitY() >= cellRect.getBottom();
            boolean toLeftOfSplit = getSplitX() >= cellRect.getRight();

            cellRect.translate(toLeftOfSplit ? 0 : -sheetPainter.getSplitX(), aboveSplit ? 0 : -sheetPainter.getSplitY());

            if (aboveSplit && toLeftOfSplit) {
                // nop: cell is always visible!
            } else if (aboveSplit) {
                // only scroll x
                java.awt.Rectangle r = new java.awt.Rectangle(
                        xS2D(cellRect.getX()),
                        yS2D(cellRect.getY()) - getY(),
                        wS2D(cellRect.getW()),
                        1
                );
                bottomRightQuadrant.scrollRectToVisible(r);
            } else if (toLeftOfSplit) {
                // only scroll y
                java.awt.Rectangle r = new java.awt.Rectangle(
                        xS2D(cellRect.getX()) - getX(),
                        yS2D(cellRect.getY()),
                        1,
                        hS2D(cellRect.getH())
                );
                bottomRightQuadrant.scrollRectToVisible(r);
            } else {
                bottomRightQuadrant.scrollRectToVisible(rectS2D(cellRect));
            }
        }

        public int getLabelWidth() {
            return labelWidth;
        }

        public int getLabelHeight() {
            return labelHeight;
        }

        @Override
        public void validate() {
            if (sheet != null) {
                // create a string with the maximum number of digits needed to
                // represent the highest row number (use a string only consisting
                // of zeroes instead of the last row number because a proportional
                // font might be used)
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= sheetPainter.getNumberOfRows(); i *= 10) {
                    sb.append('0');
                }
                painter.setText(new String(sb));
                final Dimension labelSize = painter.getPreferredSize();
                labelWidth = labelSize.width;
                labelHeight = labelSize.height;

                topLeftQuadrant.validate();
                topRightQuadrant.validate();
                bottomLeftQuadrant.validate();
                bottomRightQuadrant.validate();
            }

            super.validate();
        }

        private Rectangle getCellRectInViewCoordinates(Cell cell) {
            boolean isTop = cell.getRowNumber() < sheet.getSplitRow();
            boolean isLeft = cell.getColumnNumber() < sheet.getSplitColumn();

            final QuadrantPainter quadrant;
            if (isTop) {
                quadrant = isLeft ? topLeftQuadrant : topRightQuadrant;
            } else {
                quadrant = isLeft ? bottomLeftQuadrant : bottomRightQuadrant;
            }

            boolean insideViewPort = !(isLeft && isTop);

            final Container parent = quadrant.getParent();
            Point pos = insideViewPort ? ((JViewport) parent).getViewPosition() : new Point();

            int i = cell.getRowNumber();
            int j = cell.getColumnNumber();
            double x = sheetPainter.getColumnPos(j);
            double w = sheetPainter.getColumnPos(j + cell.getHorizontalSpan()) - x + 1;
            double y = sheetPainter.getRowPos(i);
            double h = sheetPainter.getRowPos(i + cell.getVerticalSpan()) - y + 1;
            x -= quadrant.getXMinInViewCoordinates();
            x += parent.getX();
            x -= pos.x;
            y -= quadrant.getYMinInViewCoordinates();
            y += parent.getY();
            y -= pos.y;

            return new Rectangle(x, y, w, h);
        }

        private void setScrollable(boolean b) {
            getHorizontalScrollBar().setEnabled(b);
            getVerticalScrollBar().setEnabled(b);
            getViewport().getView().setEnabled(b);
        }

        private abstract class QuadrantPainter extends JPanel implements Scrollable {

            QuadrantPainter() {
                super(null, false);

                init();
            }

            private void init() {
                setOpaque(true);
                setDoubleBuffered(false);

                // listen to mouse events
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        Point p = e.getPoint();
                        translateMousePosition(p);
                        onMousePressed(p.x, p.y);
                    }
                });
            }

            abstract int getFirstColumn();

            abstract int getLastColumn();

            abstract int getFirstRow();

            abstract int getLastRow();

            abstract int getXMinInViewCoordinates();

            abstract int getYMinInViewCoordinates();

            void repaintSheet(Rectangle rect) {
                java.awt.Rectangle rect2 = rectS2D(rect);
                rect2.translate(-getXMinInViewCoordinates(), -getYMinInViewCoordinates());
                repaint(rect2);
            }

            @Override
            public boolean isOptimizedDrawingEnabled() {
                return true;
            }

            void translateMousePosition(Point p) {
                p.translate(getXMinInViewCoordinates(), getYMinInViewCoordinates());
            }

            private boolean hasColumnHeaders() {
                return getLastRow() < sheet.getSplitRow();
            }

            private boolean hasRowHeaders() {
                return getLastColumn() < sheet.getSplitColumn();
            }

            private boolean hasHLine() {
                return getLastRow() >= 0 && getLastRow() < sheet.getLastRowNum();
            }

            private boolean hasVLine() {
                return getLastColumn() >= 0 && getLastColumn() < sheet.getLastColNum();
            }

            @Override
            public void validate() {
                if (sheet != null) {
                    // the width is the width for the labels showing row names ...
                    double width = hasRowHeaders() ? labelWidth : 1;

                    // ... plus the width of the columns displayed ...
                    width += sheetPainter.getColumnPos(getLastColumn() + 1) - sheetPainter.getColumnPos(getFirstColumn());

                    // ... plus 1 pixel for drawing a line at the split position.
                    if (hasVLine()) {
                        width += 1;
                    }

                    // the height is the height for the labels showing column names ...
                    double height = hasColumnHeaders() ? labelHeight : 1;

                    // ... plus the height of the rows displayed ...
                    height += sheetPainter.getRowPos(getLastRow() + 1) - sheetPainter.getRowPos(getFirstRow());

                    // ... plus 1 pixel for drawing a line below the lines above the split.
                    if (hasHLine()) {
                        height += 1;
                    }

                    final Dimension size = new Dimension((int) Math.round(width), (int) Math.round(height));
                    setSize(size);
                    setPreferredSize(size);
                }

                super.validate();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                sheetPainter.drawSheet(new SwingGraphicsContext(g));
            }

            protected void drawRowLabels(Graphics g) {
                if (!hasRowHeaders()) {
                    return;
                }

                java.awt.Rectangle clipBounds = g.getClipBounds();
                int startRow = Math.max(getFirstRow(), sheetPainter.getRowNumberFromY(clipBounds.y));
                int endRow = Math.min(1 + sheetPainter.getRowNumberFromY(clipBounds.y + clipBounds.height), getLastRow() + 1);
                for (int i = startRow; i < endRow; i++) {
                    int y = yS2D(sheetPainter.getRowPos(i));
                    int h = hS2D(sheetPainter.getRowPos(i + 1) - y);
                    String text = getRowName(i);

                    painter.setBounds(0, 0, labelWidth, h);
                    painter.setText(text);
                    painter.paint(g.create(-labelWidth, y, labelWidth, h));
                }
            }

            protected void drawColumnLabels(Graphics g) {
                if (!hasColumnHeaders()) {
                    return;
                }

                java.awt.Rectangle clipBounds = g.getClipBounds();
                int startCol = Math.max(getFirstColumn(), sheetPainter.getColumnNumberFromX(clipBounds.x));
                int endCol = Math.min(1 + sheetPainter.getColumnNumberFromX(clipBounds.x + clipBounds.width), getLastColumn() + 1);
                for (int j = startCol; j < endCol; j++) {
                    int x = xS2D(sheetPainter.getColumnPos(j));
                    int w = wS2D(sheetPainter.getColumnPos(j + 1) - x);
                    String text = getColumnName(j);

                    painter.setBounds(0, 0, w, labelHeight);
                    painter.setText(text);
                    painter.paint(g.create(x, -labelHeight, w, labelHeight));
                }
            }

            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }

            @Override
            public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
                if (orientation == SwingConstants.VERTICAL) {
                    // scroll vertical
                    if (direction < 0) {
                        //scroll up
                        final int y = visibleRect.y;
                        double yPrevious = 0;
                        for (int i = sheet.getSplitRow(); i <= sheetPainter.getNumberOfRows(); i++) {
                            final double pos = sheetPainter.getRowPos(i);
                            if (pos >= y) {
                                return (int) Math.round(y - yPrevious);
                            }
                            yPrevious = pos;
                        }
                        // should never be reached
                        return 0;
                    } else {
                        // scroll down
                        final int y = visibleRect.y + visibleRect.height;
                        for (int i = sheet.getSplitRow(); i <= sheetPainter.getNumberOfRows(); i++) {
                            final double pos = sheetPainter.getRowPos(i);
                            if (pos > y) {
                                return (int) Math.round(pos - y);
                            }
                        }
                        // should never be reached
                        return 0;
                    }
                } else // scroll horizontal
                {
                    if (direction < 0) {
                        //scroll left
                        final int x = visibleRect.x;
                        double xPrevious = 0;
                        for (int j = sheet.getSplitColumn(); j <= sheetPainter.getNumberOfColumns(); j++) {
                            final double pos = sheetPainter.getColumnPos(j);
                            if (pos >= x) {
                                return (int) Math.round(x - xPrevious);
                            }
                            xPrevious = pos;
                        }
                        // should never be reached
                        return 0;
                    } else {
                        // scroll right
                        final int x = visibleRect.x + visibleRect.width;
                        for (int j = sheet.getSplitColumn(); j <= sheetPainter.getNumberOfColumns(); j++) {
                            final double pos = sheetPainter.getColumnPos(j);
                            if (pos > x) {
                                return (int) Math.round(pos - x);
                            }
                        }
                        // should never be reached
                        return 0;
                    }
                }
            }

            @Override
            public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
                return 3 * getScrollableUnitIncrement(visibleRect, orientation, direction);
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }

            @Override
            public boolean getScrollableTracksViewportHeight() {
                return false;
            }
        }

        private class TopRightQuadrant extends QuadrantPainter {

            @Override
            int getXMinInViewCoordinates() {
                return xS2D(sheetPainter.getColumnPos(getFirstColumn()));
            }

            @Override
            int getYMinInViewCoordinates() {
                return -getLabelHeight();
            }

            @Override
            int getFirstColumn() {
                return sheet.getSplitColumn();
            }

            @Override
            int getLastColumn() {
                return sheet.getLastColNum();
            }

            @Override
            int getFirstRow() {
                return 0;
            }

            @Override
            int getLastRow() {
                return sheet.getSplitRow() - 1;
            }
        }

        private class BottomRightQuadrant extends QuadrantPainter {

            @Override
            int getXMinInViewCoordinates() {
                return xS2D(sheetPainter.getColumnPos(getFirstColumn()));
            }

            @Override
            int getYMinInViewCoordinates() {
                return yS2D(sheetPainter.getRowPos(getFirstRow()));
            }

            @Override
            int getFirstColumn() {
                return sheet.getSplitColumn();
            }

            @Override
            int getLastColumn() {
                return sheet.getLastColNum();
            }

            @Override
            int getFirstRow() {
                return sheet.getSplitRow();
            }

            @Override
            int getLastRow() {
                return sheet.getLastRowNum();
            }
        }

        private class BottomLeftQuadrant extends QuadrantPainter {

            @Override
            int getXMinInViewCoordinates() {
                return -getLabelWidth();
            }

            @Override
            int getYMinInViewCoordinates() {
                return yS2D(sheetPainter.getRowPos(getFirstRow()));
            }

            @Override
            int getFirstColumn() {
                return 0;
            }

            @Override
            int getLastColumn() {
                return sheet.getSplitColumn() - 1;
            }

            @Override
            int getFirstRow() {
                return sheet.getSplitRow();
            }

            @Override
            int getLastRow() {
                return sheet.getLastRowNum();
            }
        }

        private class TopLeftQuadrant extends QuadrantPainter {

            @Override
            int getXMinInViewCoordinates() {
                return -getLabelWidth();
            }

            @Override
            int getYMinInViewCoordinates() {
                return -getLabelHeight();
            }

            @Override
            int getFirstColumn() {
                return 0;
            }

            @Override
            int getLastColumn() {
                return sheet.getSplitColumn() - 1;
            }

            @Override
            int getFirstRow() {
                return 0;
            }

            @Override
            int getLastRow() {
                return sheet.getSplitRow() - 1;
            }
        }
    }
}
