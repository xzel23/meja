package com.dua3.meja.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.RefOption;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.io.FileSystemView;

public class WorkbookTestHelper {

	private static final Logger LOGGER = Logger.getLogger(WorkbookTestHelper.class.getName());

	public static Workbook loadWorkbook() throws IOException {
		Class<? extends WorkbookTestHelper> clazz = WorkbookTestHelper.class;
		String fileName = clazz.getSimpleName() + ".xlsx";

		Workbook workbook;

		// the FileSystemView is needed in case the test is run from a jar file
		try (FileSystemView fsv = FileSystemView.forClass(clazz)) {
			Path wbPath = fsv.resolve(fileName);
			workbook = MejaHelper.openWorkbook(wbPath);
		} catch (IOException e) {
			// WORKAROUND - If anyone knows a less hackish solution for this, please send a
			// pull request!

			// When tests are run from within gradle, resources are placed in another
			// location (outside of classpath).
			// In that case, we try to guess the correct location of the resource files to
			// be able to perform the tests.
			String pathStr = clazz.getResource(".").getPath();

			// When started from within Bash on windows, a slash is prepended to the path
			// returned by getResource.
			// We have to remove it again.
			if (pathStr.matches("^/[a-zA-Z]:/.*")) {
				pathStr = pathStr.replaceFirst("^/", "");
			}

			// Change the path so that it points to the probable resource dir.
			String s = pathStr.replaceAll("/build/classes/java/test/", "/build/resources/test/");

			LOGGER.log(Level.WARNING, "Resource not found! Trying to load from ''{0}''.", pathStr);

			// Then try to load the workbook from there.
			Path path = Paths.get(s);
			try (FileSystemView fsv = FileSystemView.create(path)) {
				Path wbPath = fsv.resolve(fileName);
				workbook = MejaHelper.openWorkbook(wbPath);
			}
		}

		return workbook;
	}

	/**
	 * Test formatting applied when calling Cell.getAsText().
	 * <p>
	 * The workboook 'FormatTest.xlsx' is read from the classpath. Each sheet
	 * contains for columns used for testing:
	 * <ul>
	 * <li>description of what is being tested in the current row
	 * <li>value with an applied format to be tested
	 * <li>the expected result as a {@code String}
	 * <li>an optional remark - if it contains the text {@literal #IGNORE#}, the row
	 * is skipped
	 * </ul>
	 * </p>
	 * 
	 * @param workbook the workbook under test
	 */
	public static void testFormat_getAsText(Workbook workbook) {
		testFormatHelper(workbook, (cell, locale) -> cell.getAsText(locale).toString());
	}

	/**
	 * Test formatting applied when calling Cell.toString().
	 * <p>
	 * The workboook 'FormatTest.xlsx' is read from the classpath. Each sheet
	 * contains for columns used for testing:
	 * <ul>
	 * <li>description of what is being tested in the current row
	 * <li>value with an applied format to be tested
	 * <li>the expected result as a {@code String}
	 * <li>an optional remark - if it contains the text {@literal #IGNORE#}, the row
	 * is skipped
	 * </ul>
	 * </p>
	 * 
	 * @param workbook the workbook under test
	 */
	static public void testFormat_toString(Workbook workbook) {
		testFormatHelper(workbook, (cell, locale) -> cell.toString(locale));
	}

	/**
	 * Test formatting.
	 * <p>
	 * The workboook 'FormatTest.xlsx' is read from the classpath. Each sheet
	 * contains for columns used for testing:
	 * <ul>
	 * <li>A Flag to indicate ignored test cases
	 * <li>description of what is being tested in the current row
	 * <li>the locale to use
	 * <li>value with an applied format to be tested
	 * <li>the expected result as a {@code String}
	 * <li>an optional remark
	 * </ul>
	 * </p>
	 * 
	 * @param extract a lambda expression maps (Cell, Locale) -> (formatted cell
	 *                content)
	 */
	public static void testFormatHelper(Workbook workbook, BiFunction<Cell, Locale, String> extract) {
		workbook.sheets().peek(s -> LOGGER.log(Level.INFO, "Processing sheet ''{0}''", s.getSheetName())).forEach(s -> {
			s.rows().skip(1).forEach(r -> {
				boolean ignored = r.getCell(0).toString().equalsIgnoreCase("x");
				if (ignored) {
					LOGGER.log(Level.FINE, "line {0} ignored", r.getRowNumber() + 1);
				} else {
					String description = r.getCell(1).toString();

					Cell languageCell = r.getCell(2);
					String languageTag = languageCell.toString();
					Locale locale = Locale.forLanguageTag(languageTag);
					if (!languageTag.equals(locale.toLanguageTag())) {
						LOGGER.log(Level.SEVERE, "Language tag does not match for cell {0}.",
								languageCell.getCellRef(RefOption.WITH_SHEET));
						throw new IllegalStateException(
								"Check language tag in cell " + languageCell.getCellRef(RefOption.WITH_SHEET));
					}

					Cell actualCell = r.getCell(3);
					Cell expectedCell = r.getCell(4);
					Cell alternativeCell = r.getCell(4);

					String styleName = actualCell.getCellStyle().getName();

					String actual = extract.apply(actualCell, locale);
					String expected = expectedCell.toString();
					String alternative = alternativeCell.toString();

					// accept both forms
					if (!alternative.isEmpty() && !actual.equals(expected)) {
						assertEquals(expected, alternative,
								String.format("in line %d [style=%s]: %s - alternative '%s', actual '%s'",
										r.getRowNumber() + 1, styleName, description, expected, alternative));
						LOGGER.log(Level.INFO, "Cell {0} matches alternative result.",
								alternativeCell.getCellRef(RefOption.WITH_SHEET));
					} else {
						assertEquals(expected, actual,
								String.format("in line %d [style=%s]: %s - expected '%s', actual '%s'",
										r.getRowNumber() + 1, styleName, description, expected, actual));
					}
				}
			});
		});
	}

	private static String[][] mergeData = { { "1:1", "2:1", null, "2:3", null }, { "2:4", null, "1:1", null, null },
			{ null, null, "1:2", null, null }, { null, null, null, "2:2", null }, { null, null, "1:1", null, null } };

	public static void testMergeUnmerge(Workbook workbook) {
		Sheet sheet = workbook.createSheet("merge test");

		// merge cells
		for (int i = 0; i < mergeData.length; i++) {
			Row row = sheet.getRow(i);
			String[] mergeDataRow = mergeData[i];
			for (int j = 0; j < mergeDataRow.length; j++) {
				Cell cell = row.getCell(j);
				String content = mergeDataRow[j];
				if (content == null) {
					// null => cell should already be merged
					assertTrue(cell.isMerged());
					assertTrue(cell.getLogicalCell() != cell);
				} else {
					// merge the cell
					assertFalse(cell.isMerged());

					if (content.equals("1:1")) {
						// do not merge solitary cell
						continue;
					}

					String[] parts = content.split(":");
					if (parts.length != 2) {
						throw new IllegalStateException();
					}

					int spanX = Integer.parseInt(parts[0]);
					int spanY = Integer.parseInt(parts[1]);

					cell.merge(spanX, spanY);
				}
			}
		}

		// check that cells are merged
		for (int i = 0; i < mergeData.length; i++) {
			Row row = sheet.getRow(i);
			String[] mergeDataRow = mergeData[i];
			for (int j = 0; j < mergeDataRow.length; j++) {
				Cell cell = row.getCell(j);
				String content = mergeDataRow[j];

				if (content == null) {
					assertTrue(cell.isMerged());
				} else if (content.equals("1:1")) {
					assertTrue(!cell.isMerged());
				} else {
					String[] parts = content.split(":");
					if (parts.length != 2) {
						throw new IllegalStateException();
					}

					int spanX = Integer.parseInt(parts[0]);
					int spanY = Integer.parseInt(parts[1]);

					assertEquals(spanX, cell.getHorizontalSpan());
					assertEquals(spanY, cell.getVerticalSpan());
				}
			}
		}

		// unmerge cells again
		for (int i = 0; i < mergeData.length; i++) {
			Row row = sheet.getRow(i);
			String[] mergeDataRow = mergeData[i];
			for (int j = 0; j < mergeDataRow.length; j++) {
				Cell cell = row.getCell(j);
				String content = mergeDataRow[j];
				if (content != null && !content.equals("1:1")) {
					cell.unMerge();
				}
			}
		}

		// check that all cells are unmerged
		for (int i = 0; i < mergeData.length; i++) {
			Row row = sheet.getRow(i);
			String[] mergeDataRow = mergeData[i];
			for (int j = 0; j < mergeDataRow.length; j++) {
				Cell cell = row.getCell(j);
				assertFalse(cell.isMerged());
			}
		}
	}
}
