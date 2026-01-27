package com.dua3.meja.samples;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.utility.data.Color;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * The {@code CreateCalendar} class is a sample application tha generates
 * a calendar for the current year in a spreadsheet format. It organizes
 * the calendar data into months and weeks, formatting the layout
 * accordingly. The class also supports styling specific days, such as holidays,
 * with distinct formatting features.
 *
 * <p>The calendar generated includes the following details:</p>
 * <ul>
 *   <li>Month names and their corresponding weekly layout.</li>
 *   <li>Days of the week formatted based on the system's locale.</li>
 *   <li>Week numbers displayed per the locale-specific week definitions.</li>
 *   <li>Special styling for significant dates such as holidays (e.g., Christmas).</li>
 * </ul>
 */
public final class CreateCalendar {

    private CreateCalendar() {
    }

    /**
     * The main entry point of the application. This method creates a calendar
     * HTML file at the specified path and handles any exceptions that may occur during
     * the process. The calendar is generated for the current year.
     *
     * @param args command-line arguments (not used in this implementation)
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String[] args) {
        try {
            Path file = Paths.get("calendar.html");

            writeCalendar(file);
        } catch (Exception t) {
            System.out.println("Exception: " + t.getMessage());
            t.printStackTrace(System.err);
        }
    }

    private static void writeCalendar(Path file) throws IOException {
        // the locale to create a calendar for
        Locale locale = Locale.getDefault();
        // week fields for locale
        WeekFields weekFields = WeekFields.of(locale);
        // get the current year
        int year = LocalDate.now().getYear();
        // get the first day of week (generally monday or sunday)
        DayOfWeek firstDayOfWeek = weekFields.getFirstDayOfWeek();
        // Days per week (should be 7)
        int daysPerWeek = DayOfWeek.values().length;

        try (Workbook wb = GenericWorkbookFactory.instance().create()) {

            Sheet sheet = wb.createSheet(String.format("%d calendar", year));

            int monthCols = 8;
            int monthRows = 7;
            for (Month month : Month.values()) {
                int r = month.ordinal() / 2;
                int c = month.ordinal() % 2;

                int i = r * (monthCols + 2);
                int j = c * (monthRows + 2);

                // write month name
                sheet.getCell(i++, j).set(month.getDisplayName(TextStyle.FULL, locale)).merge(monthCols, 1);

                // write weekday names
                Row row = sheet.getRow(i++);
                for (int d = 0; d < 7; d++) {
                    // only use the first two chars of the day name
                    String dayName = DayOfWeek.values()[(d + firstDayOfWeek.ordinal()) % daysPerWeek]
                            .getDisplayName(TextStyle.FULL, locale)
                            .substring(0, 2);
                    row.getCell(j + d + 1).set(dayName);
                }

                LocalDate firstOfMonth = LocalDate.of(year, month.getValue(), 1);
                int offset = (firstOfMonth.getDayOfWeek().getValue() - firstDayOfWeek.getValue()) % daysPerWeek;
                for (LocalDate d = firstOfMonth; d.getMonth() == month; d = d.plusDays(1)) {
                    // write date
                    int idx = offset + d.getDayOfMonth() - 1;
                    int ii = idx / daysPerWeek;
                    int jj = idx % daysPerWeek;
                    Cell cell = sheet.getCell(i + ii, j + jj + 1).set(d.getDayOfMonth());
                    setStyle(cell, d);

                    // write week number
                    if (d.getDayOfMonth() == 1 || d.getDayOfWeek() == firstDayOfWeek) {
                        sheet.getCell(i + ii, j).set(d.get(weekFields.weekOfWeekBasedYear()));
                    }
                }
            }

            wb.write(file);
        }
    }

    private static void setStyle(Cell cell, LocalDate d) {
        if (d.getMonth() == Month.DECEMBER && d.getDayOfMonth() == 25) {
            cell.setHyperlink(URI.create("https://en.wikipedia.org/wiki/Christmas"));
            CellStyle csHoliday = cell.getWorkbook().getCellStyle("holiday");
            csHoliday.setFillFgColor(Color.INDIANRED.brighter());
            csHoliday.setFillPattern(FillPattern.SOLID);
            cell.setCellStyle(csHoliday);
        }
    }
}
