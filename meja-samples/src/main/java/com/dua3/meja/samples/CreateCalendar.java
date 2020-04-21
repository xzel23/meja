package com.dua3.meja.samples;

import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class CreateCalendar {
    
    public static void main(String[] args) throws IOException {
        try {
            Path file = Paths.get("calendar.xlsx");
            //LangUtil.check(!Files.exists(file), "outputfile exists: " + file);

            writeCalendar(file);
        } catch (Throwable t) {
            System.out.println("Exception: "+t.getMessage());
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

        Workbook wb = GenericWorkbookFactory.instance().create();

        Sheet sheet = wb.createSheet(String.format("%d calendar", year));

        int monthCols = 8;
        int monthRows = 7;
        for (Month month: Month.values()) {
            int r = month.ordinal()/2;
            int c = month.ordinal()%2;
            
            int i = r*(monthCols+2);
            int j = c*(monthRows+2);
            
            // write month name
            sheet.getCell(i++,j).set(month.getDisplayName(TextStyle.FULL, locale)).merge(monthCols,1);
            
            // write weekday names
            Row row = sheet.getRow(i++);
            for (int d = 0; d < 7; d++) {
                // only use the first two chars of the day name
                String dayName=DayOfWeek.values()[(d+firstDayOfWeek.ordinal()) % daysPerWeek]
                        .getDisplayName(TextStyle.FULL, locale)
                        .substring(0,2);
                row.getCell(j+d+1).set(dayName);
            }
            
            LocalDate firstOfMonth = LocalDate.of(year, month.getValue(), 1);
            int offset = (firstOfMonth.getDayOfWeek().getValue()-firstDayOfWeek.getValue()) % daysPerWeek;
            for (LocalDate d=firstOfMonth; d.getMonth()==month; d=d.plusDays(1)) {
                // write date
                int idx = offset+d.getDayOfMonth()-1;
                int ii = idx/daysPerWeek;
                int jj = idx%daysPerWeek;
                sheet.getCell(i+ii,j+jj+1).set(d.getDayOfMonth());
                
                // write week number
                if (d.getDayOfMonth()==1 || d.getDayOfWeek()==firstDayOfWeek) {
                    sheet.getCell(i+ii, j).set(d.get(weekFields.weekOfWeekBasedYear()));
                }
            }
            
        }

        wb.write(file);
    }
}
