# Meja spreadsheet library

Meja is a library for handling tabular data such as Excel-Sheets, CSV-data etc.

## Building

Clone the repository and run `./gradlew`. This will also install meja into your local maven repository.

## License

Meja is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Modules

Meja consistsof different modules, each providing different functionality.

### Module: meja (`com.dua3.meja`)

This is the base module providing functionality shared by different models.

### Module: meja.generic (`com.dua3.meja.generic`)

A generic Workbook implementation. Fast and memory efficient. Use this implementation when creating workbooks in memory. Also defines the CSV FileType.

### meja.poi (`com.dua3.meja.poi`)

An implementation backed by the Apache POI implementation of the Microsoft Office Excel file format. Defines FileTypes for xls and xlsx files. Use this implementation to read and modify Excel files.

### meja.swing (`com.dua3.meja.swing`)

Defines Swing controls for displaying Sheets and Workbooks.

### meja.samples (`com.dua3.meja.samples`)

Several small samples to demonstrate how to use this library.

### meja.fx (`com.dua3.meja.fx`)

Utilites for JavaFX.

## Changes

### Version 2.0

__BETA1__:

- Meja requires Java 11 to compile and run.
- Provides Jigsaw modules. However Apache POI is not yet fully modularised, so keep in mind when using jlink with meja.poi.
- WorkbookFactory implementations can be loaded by `ServiceProvider.load()`. Loading is done automatically when using FileType.forPath(...).factory().
- To run samples, run `./gradlew run` in the project directory

__BETA2__:

- Removed the "locale dependent" setting from CSV.
- Removed `FileType.getDescription()`. Use `FileType.getName()` instead.
- Added `FileTypeExcel` that combines the old and new Excel formats (only for reading).
- new subproject `meja.poi.module` that wraps the Apache POI Excel implementation so that it cn be used in modular jlinked projects. Just include `meja.poi.module` instead of `meja.poi`.

__BETA3__:

- WorkbookWriter is now an interface (was an abstract class before)
- FileType implements Comparable
- WorkbookWriter: accepts callback for progress updates

__BETA4__:

- CsvWorkbookWriter: if workbook contains more than one sheet, prepend each sheet with a single line containing only `!<sheet name>!`
- the deprecated `Cell.getDate()` method returning `java.util.Date` has been replaced by a method of the same name returning `java.time.LocalDate`
- The cell type DATE has been split into DATE and DATE_TIME. This should make many things easier. The reason for not making this distinction in earlier versions is that Excel (which at the time was the only implementation) doesn't distinguish between the two.
- bugfixes

__BETA5__:

- Changes to build files
- Fix ClassCastException when writing CSV data containing dates.
- added `Sheet.createRow()` and `Row.createCell()`

__BETA6__:

- Fix Javadoc encoding issue
- Fix some POI deprecation warnings
- Database utility class to copy data from ResultSet into Sheet
- small code cleanups
- Fix Off-by-One error in Row.createCell()

__BETA7__:

- update utility
- remove version file

