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

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.SearchOptions;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.Cache;
import com.dua3.meja.util.MejaHelper;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
public class SwingSheetView extends JPanel implements com.dua3.meja.ui.SheetView {

    private static final long serialVersionUID = 1L;

    static final int MAX_WIDTH = 800;

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

    private final CellRenderer renderer;
    private final CellEditor editor;
    private final SheetPane sheetPane;
    private SearchDialog searchDialog = null;

    Cache<Float, java.awt.Stroke> strokeCache = new Cache<Float, java.awt.Stroke>() {
        @Override
        protected java.awt.Stroke create(Float width) {
            return new BasicStroke(width);
        }
    };

    /**
     * The scale used to calculate screen sizes dependent of display resolution.
     */
    private float scaleDpi = 1;

    /**
     * Array with column positions (x-axis) in pixels.
     */
    private float columnPos[];

    /**
     * Array with column positions (y-axis) in pixels.
     */
    private float rowPos[];

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
     * Horizontal padding.
     */
    private final int paddingX = 2;

    /**
     * Vertical padding.
     */
    private final int paddingY = 1;

    /**
     * Color used to draw the selection rectangle.
     */
    private Color selectionColor = Color.BLACK;

    /**
     * Width of the selection rectangle borders.
     */
    private final int selectionStrokeWidth = 4;

    /**
     * Stroke used to draw the selection rectangle.
     */
    private Stroke selectionStroke = getStroke((float) selectionStrokeWidth);

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

    /**
     * Construct a new SheetView for the given sheet.
     *
     * @param sheet the sheet to display
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public SwingSheetView(Sheet sheet) {
        super(new GridLayout(1, 1));

        renderer = new DefaultCellRenderer();
        editor = new DefaultCellEditor(this);
        sheetPane = new SheetPane();
        searchDialog = new SearchDialog();

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
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_HOME, InputEvent.CTRL_DOWN_MASK ), Actions.MOVE_HOME);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_END, InputEvent.CTRL_DOWN_MASK ), Actions.MOVE_END);
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
                onMousePressed(e.getX() + getSplitX(), e.getY() + getSplitY());
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

        setSheet(sheet);
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

        scrollToCurrentCell();
    }

    /**
     * Move the selection rectangle to an adjacent cell.
     *
     * @param d direction
     */
    private void movePage(Direction d) {
        Cell cell = getCurrentCell().getLogicalCell();
        Rectangle cellRect = getCellRect(cell);
        int x = cellRect.x;
        int y = cellRect.y;
        switch (d) {
            case NORTH:
                y = Math.max(0, y-getVisibleRect().height);
                break;
            case SOUTH:
                y = Math.min(getSheetHeight()-1, y+getVisibleRect().height);
                break;
        }

        int row = getRowNumberFromY(y);
        int col = getColumnNumberFromX(x);
        setCurrentCell(row, col);
        scrollToCurrentCell();
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
     * Get display coordinates of selection rectangle.
     *
     * @return selection rectangle in display coordinates
     */
    private Rectangle getSelectionRect() {
        Rectangle cellRect = getCellRect(getCurrentCell().getLogicalCell());
        int extra = (selectionStrokeWidth + 1) / 2;
        cellRect.x -= extra;
        cellRect.y -= extra;
        cellRect.width += 2 * extra;
        cellRect.height += 2 * extra;
        return cellRect;
    }

    /**
     * Scroll the currently selected cell into view.
     */
    @Override
    public void scrollToCurrentCell() {
        sheetPane.ensureCellIsVisibile(getCurrentCell().getLogicalCell());
    }

    /**
     * Calculate the rectangle the cell occupies on screen.
     *
     * @param cell the cell whose area is requested
     * @return rectangle the rectangle the cell takes up in screen coordinates
     */
    public Rectangle getCellRect(Cell cell) {
        final int i = cell.getRowNumber();
        final int j = cell.getColumnNumber();

        final int x = getColumnPos(j);
        final int w = getColumnPos(j + cell.getHorizontalSpan()) - x + 1;
        final int y = getRowPos(i);
        final int h = getRowPos(i + cell.getVerticalSpan()) - y + 1;

        return new Rectangle(x, y, w, h);
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
        Cell oldCell = getCurrentCell().getLogicalCell();
        Rectangle oldRect = getSelectionRect();

        int newRowNum = Math.max(sheet.getFirstRowNum(), Math.min(sheet.getLastRowNum(), rowNum));
        int newColNum = Math.max(sheet.getFirstColNum(), Math.min(sheet.getLastColNum(), colNum));
        sheet.setCurrentCell(newRowNum, newColNum);
        scrollToCurrentCell();

        Cell newCell = getCurrentCell().getLogicalCell();
        if (newCell.getRowNumber() != oldCell.getRowNumber()
                || newCell.getColumnNumber() != oldCell.getColumnNumber()) {
            // get new selection for repainting
            Rectangle newRect = getSelectionRect();

            sheetPane.repaintSheet(oldRect);
            sheetPane.repaintSheet(newRect);

            return true;
        } else {
            return false;
        }
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
        editorComp.setBounds(cellRect);
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
        return sheet == null ? 1.0f : sheet.getZoom() * scaleDpi;
    }

    /**
     * @param j the column number
     * @return the columnPos
     */
    public int getColumnPos(int j) {
        return Math.round(columnPos[j] * getScale());
    }

    /**
     * @param i the row number
     * @return the rowPos
     */
    public int getRowPos(int i) {
        return Math.round(rowPos[i] * getScale());
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
        this.sheet = sheet;
        updateContent();
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
        int row = getRowNumberFromY(y);
        int col = getColumnNumberFromX(x);
        boolean currentCellChanged = setCurrentCell(row, col);
        requestFocusInWindow();

        if (!currentCellChanged) {
            // if it already was the current cell, start cell editing
            if (isEditable()) {
                startEditing();
                editing = true;
            }
        } else {
            // otherwise stop cell editing
            if (editing) {
                stopEditing(true);
                editing = false;
            }
            // scroll the selected cell into view
            scrollToCurrentCell();
        }
    }

    /**
     * Set the grid color.
     *
     * @param gridColor the color for th grid
     */
    @Override
    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    /**
     * Get the grid color.
     *
     * @return color of grid
     */
    @Override
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * Get y-coordinate of split.
     *
     * @return y coordinate of split
     */
    int getSplitY() {
        return sheet == null ? 0 : getRowPos(sheet.getSplitRow());
    }

    /**
     * Get x-coordinate of split.
     *
     * @return x coordinate of split
     */
    int getSplitX() {
        return sheet == null ? 0 : getColumnPos(sheet.getSplitColumn());
    }

    /**
     * Get the row number that the given y-coordinate belongs to.
     *
     * @param y y-coordinate
     *
     * @return
     * <ul>
     * <li> -1, if the first row is displayed below the given coordinate
     * <li> number of rows, if the lower edge of the last row is displayed above
     * the given coordinate
     * <li> the number of the row that belongs to the given coordinate
     * </ul>
     */
    public int getRowNumberFromY(int y) {
        int i = 0;
        while (i < rowPos.length && getRowPos(i) <= y) {
            i++;
        }
        return i - 1;
    }

    /**
     * Get the column number that the given x-coordinate belongs to.
     *
     * @param x x-coordinate
     *
     * @return
     * <ul>
     * <li> -1, if the first column is displayed to the right of the given
     * coordinate
     * <li> number of columns, if the right edge of the last column is displayed
     * to the left of the given coordinate
     * <li> the number of the column that belongs to the given coordinate
     * </ul>
     */
    public int getColumnNumberFromX(int x) {
        int j = 0;
        while (j < columnPos.length && getColumnPos(j) <= x) {
            j++;
        }
        return j - 1;
    }

    public Dimension getSheetSize() {
        return new Dimension(getSheetWidth() + 1, getSheetHeight() + 1);
    }

    /**
     * Get number of columns for the currently loaded sheet.
     *
     * @return number of columns
     */
    private int getNumberOfColumns() {
        return columnPos.length - 1;
    }

    /**
     * Get number of rows for the currently loaded sheet.
     *
     * @return number of rows
     */
    private int getNumberOfRows() {
        return rowPos.length - 1;
    }

    private java.awt.Stroke getStroke(Float width) {
        return strokeCache.get(width);
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

    /**
     * Return the current cell.
     *
     * @return current cell
     */
    @Override
    public Cell getCurrentCell() {
        return sheet.getCurrentCell();
    }

    @Override
    public void updateContent() {
        // scale according to screen resolution
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        scaleDpi = dpi / 72f;

        // determine sheet dimensions
        if (sheet == null) {
            sheetWidthInPoints = 0;
            sheetHeightInPoints = 0;
            rowPos = new float[]{0};
            columnPos = new float[]{0};
            return;
        }

        Lock readLock = sheet.readLock();
        readLock.lock();
        try {
            sheetHeightInPoints = 0;
            rowPos = new float[2 + sheet.getLastRowNum()];
            rowPos[0] = 0;
            for (int i = 1; i < rowPos.length; i++) {
                sheetHeightInPoints += sheet.getRowHeight(i - 1);
                rowPos[i] = sheetHeightInPoints;
            }

            sheetWidthInPoints = 0;
            columnPos = new float[2 + sheet.getLastColNum()];
            columnPos[0] = 0;
            for (int j = 1; j < columnPos.length; j++) {
                sheetWidthInPoints += sheet.getColumnWidth(j - 1);
                columnPos[j] = sheetWidthInPoints;
            }
        } finally {
            readLock.unlock();
        }

        revalidate();
        repaint();
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

    protected static enum CellDrawMode {

        /**
         *
         */
        DRAW_CELL_BACKGROUND,
        /**
         *
         */
        DRAW_CELL_BORDER,
        /**
         *
         */
        DRAW_CELL_FOREGROUND
    }

    private class SearchDialog extends JDialog {
        private static final long serialVersionUID = 1L;

        private final JTextField jtfText;
        private final JCheckBox jcbIgnoreCase;
        private final JCheckBox jcbMatchCompleteText;

        SearchDialog() {
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
            jtfText = new JTextField(40);
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
            jcbIgnoreCase = new JCheckBox("ignore case", true);
            add(jcbIgnoreCase, c);

            c.gridx = 3;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            jcbMatchCompleteText = new JCheckBox("match complete text", false);
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

        private final JLabel painter;
        private int labelHeight = 0;
        private int labelWidth = 0;
        private final TopLeftQuadrant topLeftQuadrant = new TopLeftQuadrant();
        private final TopRightQuadrant topRightQuadrant = new TopRightQuadrant();
        private final BottomLeftQuadrant bottomLeftQuadrant = new BottomLeftQuadrant();
        private final BottomRightQuadrant bottomRightQuadrant = new BottomRightQuadrant();

        SheetPane() {
            setDoubleBuffered(false);

            painter = new JLabel();
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
            final Rectangle cellRect = getCellRect(cell);
            boolean aboveSplit = getSplitY() >= cellRect.getMaxY() - 1;
            boolean toLeftOfSplit = getSplitX() >= cellRect.getMaxX() - 1;

            cellRect.translate(toLeftOfSplit ? 0 : -getSplitX(), aboveSplit ? 0 : -getSplitY());

            if (aboveSplit && toLeftOfSplit) {
                // nop: cell is always visible!
            } else if (aboveSplit) {
                // only scroll x
                cellRect.y = -getY();
                cellRect.height = 1;
                bottomRightQuadrant.scrollRectToVisible(cellRect);
            } else if (toLeftOfSplit) {
                // only scroll y
                cellRect.x = -getX();
                cellRect.width = 1;
                bottomRightQuadrant.scrollRectToVisible(cellRect);
            } else {
                bottomRightQuadrant.scrollRectToVisible(cellRect);
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
                for (int i = 1; i <= getNumberOfRows(); i *= 10) {
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
            int x = getColumnPos(j);
            int w = getColumnPos(j + cell.getHorizontalSpan()) - x + 1;
            int y = getRowPos(i);
            int h = getRowPos(i + cell.getVerticalSpan()) - y + 1;
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
                Rectangle rect2 = new Rectangle(rect);
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
                    int width = hasRowHeaders() ? labelWidth : 1;

                    // ... plus the width of the columns displayed ...
                    width += getColumnPos(getLastColumn() + 1) - getColumnPos(getFirstColumn());

                    // ... plus 1 pixel for drawing a line at the split position.
                    if (hasVLine()) {
                        width += 1;
                    }

                    // the height is the height for the labels showing column names ...
                    int height = hasColumnHeaders() ? labelHeight : 1;

                    // ... plus the height of the rows displayed ...
                    height += getRowPos(getLastRow() + 1) - getRowPos(getFirstRow());

                    // ... plus 1 pixel for drawing a line below the lines above the split.
                    if (hasHLine()) {
                        height += 1;
                    }

                    final Dimension size = new Dimension(width, height);
                    setSize(size);
                    setPreferredSize(size);
                }

                super.validate();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (sheet == null) {
                    return;
                }

                Lock readLock = sheet.readLock();
                readLock.lock();
                try {
                    Graphics2D g2d = (Graphics2D) g;

                    final int x = getXMinInViewCoordinates();
                    final int y = getYMinInViewCoordinates();
                    final int width = getWidth();
                    final int height = getHeight();

                    // set origin
                    g2d.translate(-x, -y);

                    // draw labels
                    drawColumnLabels(g2d);
                    drawRowLabels(g2d);
                    drawSheet(g2d);

                    // draw lines
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(getStroke(1f));
                    if (hasHLine()) {
                        g2d.drawLine(x, height + y - 1, width + x - 1, height + y - 1);
                    }
                    if (hasVLine()) {
                        g2d.drawLine(width + x - 1, y, width + x - 1, height + y - 1);
                    }
                } finally {
                    readLock.unlock();
                }
            }

            protected void drawRowLabels(Graphics g) {
                if (!hasRowHeaders()) {
                    return;
                }

                Rectangle clipBounds = g.getClipBounds();
                int startRow = Math.max(getFirstRow(), getRowNumberFromY(clipBounds.y));
                int endRow = Math.min(1 + getRowNumberFromY(clipBounds.y + clipBounds.height), getLastRow() + 1);
                for (int i = startRow; i < endRow; i++) {
                    int y = getRowPos(i);
                    int h = getRowPos(i + 1) - y + 1;
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

                Rectangle clipBounds = g.getClipBounds();
                int startCol = Math.max(getFirstColumn(), getColumnNumberFromX(clipBounds.x));
                int endCol = Math.min(1 + getColumnNumberFromX(clipBounds.x + clipBounds.width), getLastColumn() + 1);
                for (int j = startCol; j < endCol; j++) {
                    int x = getColumnPos(j);
                    int w = getColumnPos(j + 1) - x + 1;
                    String text = getColumnName(j);

                    painter.setBounds(0, 0, w, labelHeight);
                    painter.setText(text);
                    painter.paint(g.create(x, -labelHeight, w, labelHeight));
                }
            }

            private void drawSheet(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;

                Lock readLock = sheet.readLock();
                readLock.lock();
                try {
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                    g2d.setBackground(sheet.getWorkbook().getDefaultCellStyle().getFillBgColor());
                    g2d.clearRect(getColumnPos(0), getRowPos(0), getSheetWidth(), getSheetHeight());

                    drawCells(g2d, CellDrawMode.DRAW_CELL_BACKGROUND);
                    drawCells(g2d, CellDrawMode.DRAW_CELL_BORDER);
                    drawCells(g2d, CellDrawMode.DRAW_CELL_FOREGROUND);
                    drawSelection(g2d);
                } finally {
                    readLock.unlock();
                }
            }

            /**
             * Draw cells.
             *
             * Since borders can be draw over by the background of adjacent
             * cells and text can overlap, drawing is done in three steps:
             * <ul>
             * <li> draw background for <em>all</em> cells
             * <li> draw borders for <em>all</em> cells
             * <li> draw foreground <em>all</em> cells
             * </ul>
             * This is controlled by {@code cellDrawMode}.
             *
             * @param g the graphics object to use
             * @param cellDrawMode the draw mode to use
             */
            void drawCells(Graphics2D g, CellDrawMode cellDrawMode) {
                // no sheet, no drawing
                if (sheet == null) {
                    return;
                }

                int maxWidthScaled = (int) (MAX_WIDTH * getScale());

                Rectangle clipBounds = g.getClipBounds();

                // determine visible rows and columns
                int startRow = Math.max(0, getRowNumberFromY(clipBounds.y));
                int endRow = Math.min(getNumberOfRows(), 1 + getRowNumberFromY(clipBounds.y + clipBounds.height));
                int startColumn = Math.max(0, getColumnNumberFromX(clipBounds.x));
                int endColumn = Math.min(getNumberOfColumns(), 1 + getColumnNumberFromX(clipBounds.x + clipBounds.width));

                // Collect cells to be drawn
                for (int i = startRow; i < endRow; i++) {
                    Row row = sheet.getRow(i);

                    if (row == null) {
                        continue;
                    }

                    // if first/last displayed cell of row is empty, start drawing at
                    // the first non-empty cell to the left/right to make sure
                    // overflowing text is visible.
                    int first = startColumn;
                    while (first > 0 && getColumnPos(first) + maxWidthScaled > clipBounds.x && row.getCell(first).isEmpty()) {
                        first--;
                    }

                    int end = endColumn;
                    while (end < getNumberOfColumns() && getColumnPos(end) - maxWidthScaled < clipBounds.x + clipBounds.width && (end <= 0 || row.getCell(end - 1).isEmpty())) {
                        end++;
                    }

                    for (int j = first; j < end; j++) {
                        Cell cell = row.getCell(j);
                        Cell logicalCell = cell.getLogicalCell();

                        final boolean visible;
                        if (cell == logicalCell) {
                            // if cell is not merged or the topleft cell of the
                            // merged region, then it is visible
                            visible = true;
                        } else {
                            // otherwise calculate row and column numbers of the
                            // first visible cell of the merged region
                            int iCell = Math.max(startRow, logicalCell.getRowNumber());
                            int jCell = Math.max(first, logicalCell.getColumnNumber());
                            visible = i == iCell && j == jCell;
                            // skip the other cells of this row that belong to the same merged region
                            j = logicalCell.getColumnNumber() + logicalCell.getHorizontalSpan() - 1;
                            // filter out cells that cannot overflow into the visible region
                            if (j < startColumn && isWrapping(cell.getCellStyle())) {
                                continue;
                            }
                        }

                        // draw cell
                        if (visible) {
                            switch (cellDrawMode) {
                                case DRAW_CELL_BACKGROUND:
                                    drawCellBackground(g, logicalCell);
                                    break;
                                case DRAW_CELL_BORDER:
                                    drawCellBorder(g, logicalCell);
                                    break;
                                case DRAW_CELL_FOREGROUND:
                                    drawCellForeground(g, logicalCell);
                                    break;
                            }
                        }

                    }
                }
            }

            /**
             * Draw cell background.
             *
             * @param g the graphics context to use
             * @param cell cell to draw
             */
            private void drawCellBackground(Graphics2D g, Cell cell) {
                Rectangle cr = getCellRect(cell);

                // draw grid lines
                g.setColor(gridColor);
                g.drawRect(cr.x, cr.y, cr.width - 1, cr.height - 1);

                CellStyle style = cell.getCellStyle();
                FillPattern pattern = style.getFillPattern();

                if (pattern == FillPattern.NONE) {
                    return;
                }

                if (pattern != FillPattern.SOLID) {
                    Color fillBgColor = style.getFillBgColor();
                    if (fillBgColor != null) {
                        g.setColor(fillBgColor);
                        g.fillRect(cr.x, cr.y, cr.width, cr.height);
                    }
                }

                if (pattern != FillPattern.NONE) {
                    Color fillFgColor = style.getFillFgColor();
                    if (fillFgColor != null) {
                        g.setColor(fillFgColor);
                        g.fillRect(cr.x, cr.y, cr.width, cr.height);
                    }
                }
            }

            /**
             * Draw cell border.
             *
             * @param g the graphics context to use
             * @param cell cell to draw
             */
            private void drawCellBorder(Graphics2D g, Cell cell) {
                CellStyle styleTopLeft = cell.getCellStyle();

                Cell cellBottomRight = sheet.getRow(cell.getRowNumber() + cell.getVerticalSpan() - 1).getCell(cell.getColumnNumber() + cell.getHorizontalSpan() - 1);
                CellStyle styleBottomRight = cellBottomRight.getCellStyle();

                Rectangle cr = getCellRect(cell);

                // draw border
                for (Direction d : Direction.values()) {
                    boolean isTopLeft = d == Direction.NORTH || d == Direction.WEST;
                    CellStyle style = isTopLeft ? styleTopLeft : styleBottomRight;

                    BorderStyle b = style.getBorderStyle(d);
                    if (b.getWidth() == 0) {
                        continue;
                    }

                    Color color = b.getColor();
                    if (color == null) {
                        color = Color.BLACK;
                    }

                    g.setColor(color);
                    g.setStroke(getStroke(b.getWidth() * getScale()));
                    switch (d) {
                        case NORTH:
                            g.drawLine(cr.x, cr.y, cr.x + cr.width - 1, cr.y);
                            break;
                        case EAST:
                            g.drawLine(cr.x + cr.width - 1, cr.y, cr.x + cr.width - 1, cr.y + cr.height - 1);
                            break;
                        case SOUTH:
                            g.drawLine(cr.x, cr.y + cr.height - 1, cr.x + cr.width - 1, cr.y + cr.height - 1);
                            break;
                        case WEST:
                            g.drawLine(cr.x, cr.y, cr.x, cr.y + cr.height - 1);
                            break;
                    }
                }
            }

            /**
             * Draw cell foreground.
             *
             * @param g the graphics context to use
             * @param cell cell to draw
             */
            private void drawCellForeground(Graphics2D g, Cell cell) {
                if (cell.isEmpty()) {
                    return;
                }

                // the cell rectangle, used for positioning the text
                Rectangle cellRect = getCellRect(cell);
                cellRect.x += paddingX;
                cellRect.width -= 2 * paddingX;
                cellRect.y += paddingY;
                cellRect.height -= 2 * paddingY;

                // the clipping rectangle
                final Rectangle clipRect;
                final CellStyle style = cell.getCellStyle();
                if (isWrapping(style)) {
                    clipRect = cellRect;
                } else {
                    Row row = cell.getRow();
                    int clipXMin = cellRect.x;
                    for (int j = cell.getColumnNumber() - 1; j > 0; j--) {
                        if (!row.getCell(j).isEmpty()) {
                            break;
                        }
                        clipXMin = getColumnPos(j) + paddingX;
                    }
                    int clipXMax = cellRect.x + cellRect.width;
                    for (int j = cell.getColumnNumber() + 1; j < getNumberOfColumns(); j++) {
                        if (!row.getCell(j).isEmpty()) {
                            break;
                        }
                        clipXMax = getColumnPos(j + 1) - paddingX;
                    }
                    clipRect = new Rectangle(clipXMin, cellRect.y, clipXMax - clipXMin, cellRect.height);
                }

                renderer.render(g, cell, cellRect, clipRect, getScale());
            }

            /**
             * Draw frame around current selection.
             *
             * @param g2d graphics object used for drawing
             */
            private void drawSelection(Graphics2D g2d) {
                // no sheet, no drawing
                if (sheet == null) {
                    return;
                }

                Cell logicalCell = getCurrentCell().getLogicalCell();
                Rectangle rect = getCellRect(logicalCell);

                g2d.setColor(selectionColor);
                g2d.setStroke(selectionStroke);
                g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
            }

            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }

            @Override
            public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                if (orientation == SwingConstants.VERTICAL) {
                    // scroll vertical
                    if (direction < 0) {
                        //scroll up
                        final int y = visibleRect.y;
                        int yPrevious = 0;
                        for (int i = sheet.getSplitRow(); i < rowPos.length; i++) {
                            if (getRowPos(i) >= y) {
                                return y - yPrevious;
                            }
                            yPrevious = getRowPos(i);
                        }
                        // should never be reached
                        return 0;
                    } else {
                        // scroll down
                        final int y = visibleRect.y + visibleRect.height;
                        for (int i = sheet.getSplitRow(); i < rowPos.length; i++) {
                            if (getRowPos(i) > y) {
                                return getRowPos(i) - y;
                            }
                        }
                        // should never be reached
                        return 0;
                    }
                } else {
                    // scroll horizontal
                    if (direction < 0) {
                        //scroll left
                        final int x = visibleRect.x;
                        int xPrevious = 0;
                        for (int j = sheet.getSplitColumn(); j < columnPos.length; j++) {
                            if (getColumnPos(j) >= x) {
                                return x - xPrevious;
                            }
                            xPrevious = getColumnPos(j);
                        }
                        // should never be reached
                        return 0;
                    } else {
                        // scroll right
                        final int x = visibleRect.x + visibleRect.width;
                        for (int j = sheet.getSplitColumn(); j < columnPos.length; j++) {
                            if (getColumnPos(j) > x) {
                                return getColumnPos(j) - x;
                            }
                        }
                        // should never be reached
                        return 0;
                    }
                }
            }

            @Override
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
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
                return getColumnPos(getFirstColumn());
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
                return getColumnPos(getFirstColumn());
            }

            @Override
            int getYMinInViewCoordinates() {
                return getRowPos(getFirstRow());
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
                return getRowPos(getFirstRow());
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
