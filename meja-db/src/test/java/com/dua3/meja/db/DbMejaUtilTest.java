package com.dua3.meja.db;

import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DbMejaUtilTest {

    @Test
    void testFillWithHeaders() throws SQLException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("TestSheet");

        String[] headers = {"ID", "NAME", "SCORE"};
        List<Object[]> rows = List.of(
                new Object[]{1, "Alice", 95.5},
                new Object[]{2, "Bob", 88.0}
        );

        ResultSet rs = createMockResultSet(headers, rows);

        int count = DbMejaUtil.fill(sheet, rs, true);
        assertEquals(2, count);
        assertEquals(3, sheet.getRowCount());

        // Check header row
        assertEquals("ID", sheet.getCell(0, 0).getText().toString());
        assertEquals("NAME", sheet.getCell(0, 1).getText().toString());
        assertEquals("SCORE", sheet.getCell(0, 2).getText().toString());

        // Check data rows
        assertEquals(1, sheet.getCell(1, 0).getNumber().intValue());
        assertEquals("Alice", sheet.getCell(1, 1).getText().toString());
        assertEquals(95.5, sheet.getCell(1, 2).getNumber().doubleValue());

        assertEquals(2, sheet.getCell(2, 0).getNumber().intValue());
        assertEquals("Bob", sheet.getCell(2, 1).getText().toString());
        assertEquals(88.0, sheet.getCell(2, 2).getNumber().doubleValue());
    }

    @Test
    void testFillWithoutHeaders() throws SQLException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("NoHeaders");

        String[] headers = {"COL1", "COL2"};
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Val1", 10});

        ResultSet rs = createMockResultSet(headers, rows);

        int count = DbMejaUtil.fill(sheet, rs, false);
        assertEquals(1, count);
        assertEquals(1, sheet.getRowCount());

        assertEquals("Val1", sheet.getCell(0, 0).getText().toString());
        assertEquals(10, sheet.getCell(0, 1).getNumber().intValue());
    }

    @Test
    void testFillEmptyResultSet() throws SQLException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Empty");

        String[] headers = {"A", "B"};
        List<Object[]> rows = List.of();

        ResultSet rs = createMockResultSet(headers, rows);

        int count = DbMejaUtil.fill(sheet, rs, true);
        assertEquals(0, count);
        assertEquals(1, sheet.getRowCount()); // Header only
        assertEquals("A", sheet.getCell(0, 0).getText().toString());
        assertEquals("B", sheet.getCell(0, 1).getText().toString());
    }

    @Test
    void testFillWithClob() throws SQLException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("ClobSheet");

        Clob normalClob = createMockClob("Hello Clob World", false);
        Clob errorClob = createMockClob("Error", true);

        String[] headers = {"TEXT_CLOB", "ERR_CLOB"};
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{normalClob, errorClob});

        ResultSet rs = createMockResultSet(headers, rows);

        int count = DbMejaUtil.fill(sheet, rs, false);
        assertEquals(1, count);
        assertEquals(1, sheet.getRowCount());

        assertEquals("Hello Clob World", sheet.getCell(0, 0).getText().toString());
        assertEquals("###", sheet.getCell(0, 1).getText().toString());
    }

    @Test
    void testNullArguments() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("NullSheet");
        ResultSet rs = createMockResultSet(new String[]{"A"}, List.of());

        assertThrows(Throwable.class, () -> DbMejaUtil.fill(null, rs, true));
        assertThrows(Throwable.class, () -> DbMejaUtil.fill(sheet, null, true));
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<DbMejaUtil> constructor = DbMejaUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        DbMejaUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }

    private static ResultSet createMockResultSet(String[] headers, List<Object[]> rows) {
        ResultSetMetaData meta = (ResultSetMetaData) Proxy.newProxyInstance(
                ResultSetMetaData.class.getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class},
                (proxy, method, args) -> {
                    if ("getColumnCount".equals(method.getName())) {
                        return headers.length;
                    }
                    if ("getColumnLabel".equals(method.getName())) {
                        int index = (int) args[0];
                        return headers[index - 1];
                    }
                    return null;
                }
        );

        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                new java.lang.reflect.InvocationHandler() {
                    private int currentRow = -1;

                    @Override
                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                        if ("getMetaData".equals(method.getName())) {
                            return meta;
                        }
                        if ("next".equals(method.getName())) {
                            currentRow++;
                            return currentRow < rows.size();
                        }
                        if ("getObject".equals(method.getName())) {
                            int colIndex = (int) args[0];
                            return rows.get(currentRow)[colIndex - 1];
                        }
                        return null;
                    }
                }
        );
    }

    private static Clob createMockClob(String text, boolean throwOnSubString) {
        return (Clob) Proxy.newProxyInstance(
                Clob.class.getClassLoader(),
                new Class<?>[]{Clob.class},
                (proxy, method, args) -> {
                    if ("length".equals(method.getName())) {
                        if (throwOnSubString) {
                            throw new SQLException("Simulated Clob length error");
                        }
                        return (long) text.length();
                    }
                    if ("getSubString".equals(method.getName())) {
                        if (throwOnSubString) {
                            throw new SQLException("Simulated Clob getSubString error");
                        }
                        long pos = (long) args[0];
                        int length = (int) args[1];
                        return text.substring((int) pos - 1, (int) (pos - 1 + length));
                    }
                    return null;
                }
        );
    }
}
