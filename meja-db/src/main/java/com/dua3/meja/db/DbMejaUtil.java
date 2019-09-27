package com.dua3.meja.db;

import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbMejaUtil {

    /** Logger */
    protected static final Logger LOG = Logger.getLogger(DbMejaUtil.class.getSimpleName());
    private static final String ERROR_TEXT = "###";

    /**
     * Fill Sheet with data from {@link ResultSet}.
     *
     * The result set data will be appended at the bottom of the sheet.
     * @param sheet
     *  the sheet to fill
     * @param rs
     *  the ResultSet
     * @param addTableHeader
     *  whether to generate a header row
     * @return
     *  the number of rows read
     * @throws SQLException
     *  if an error occurs while reading from the ResultSet.
     */
    public static int fill(Sheet sheet, ResultSet rs, boolean addTableHeader) throws SQLException {
        LOG.fine("populating Sheet with ResultSet data");

        // read result metadata
        LOG.finer("reading result meta data ...");
        ResultSetMetaData meta = rs.getMetaData();
        int nColumns = meta.getColumnCount();

        // create table header
        if (addTableHeader) {
            LOG.finer("creating table header ...");
            Row header = sheet.createRow();
            for (int i = 1; i <= nColumns; i++) {
                String label = meta.getColumnLabel(i);
                header.createCell().set(label);
            }
        }

        // read result
        LOG.finer("reading result data ...");
        int k = 0;
        while (rs.next()) {
            Row row = sheet.createRow();
            for (int i = 1; i <= nColumns; i++) {
                row.createCell().set(getObject(rs, i));
            }
            k++;
        }
        final int n = k;
        LOG.finer(() -> "read "+n+" rows of data");

        return n;
    }

    private static Object getObject(ResultSet rs, int i) throws SQLException {
        Object obj = rs.getObject(i);

        if (obj instanceof Clob) {
            obj = toString((Clob) obj);
        }

        return obj;
    }

    private static String toString(Clob clob) {
        try {
            return clob.getSubString(1, (int) Math.min(Integer.MAX_VALUE, clob.length()));
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "could no convert Clob to String", e);
            return ERROR_TEXT;
        }
    }


    private DbMejaUtil() {
        // utility class
    }
}
