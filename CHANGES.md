# Meja Changelog

## Version 2.0.13.1

 - update utility (to be consistent with other libs)

## Version 2.0.13

 - output relative links in HtmlWorkbookWriter if possible
 
## Version 2.0.12

 - update utility to 6.2.3
 - add some tests for HtmlWorkbookWriter (in the POI package since an excel-workbook is used as input)

## Version 2.0.11

 - fix display of merged cells in HTML output
 - support hyperlinks in HTML output
 - update utility to 6.2.2
 - code cleanup
 
## Version 2.0.10.1

 - fix NPE in GenericSheet.setColumnWidth()

## Version 2.0.10

 - initial support for output in HTML format

## Version 2.0.9

 - sync with utility

## Version 2.0.8

 - update utility dependency
 - fix hyperlinks - links are now passed by URI as problems with absolute paths on windows could not be resolved with URLs
 
## Version 2.0.7

 - changes to Hyperlinking (still broken, fixed in 2.0.8)

## Version 2.0.6

 - Workbook.resolve() to resolve workbook relative paths
 - fix Hyperlink to file (again)

## Version 2.0.5

 - fix Hyperlink to file

## Version 2.0.4

 - fix writing GenericWorkbook to file with xls extension wrongly using xlsx format
 - add Hyperlink support
 - Workbook.write(Path)
 - add CreateCalendar sample
 
## Version 2.0.3

 - remove Java 9 API usage
 - update JavaFX to 14
 - make dependency on JavaFX non-transitive

## Version 2.0.1, 2.0.2

 - remove Java 9+ API usage

## Version 2.0

 - restore Java 8 compatibility
 - enable compilation on JDK 14
 - update utility to 5.3.4
 - update Apache POI to 4.1.2
 - update JavaFX to 13.0.2
 - update spotbugs to 4.0.1
 - update gradle plugins

__BETA_19__:

 - update gradle to 6.0 to make compilation on JDK 13 possible
 - update utility dependency
 - update openjfx to 13.0.1

__BETA_18__:

 - update POI to 4.1.1
 - remove (now) unnecessary conversions to java.util.Date
 - add extension "xlsm" to xlsx filetype
 
__BETA_17__:

 - further code cleanup
 - update utility dependency
 - remove usage of deprecated API (deprecated XSSFColor constructor)
 - performance improvements
 - small bug fixes
  
__BETA_16__:

 - code cleanup
 - buildable with JDK 13
 - performance improvements
 - bug fixes

__BETA_15__:

 - update dependencies, rename module descriptors: `meja` -> `meja`
 - code cleanup

__BETA_14__:

 - update dependencies, i.e. OpenJFX 13 (still compatible with Java 11)

__BETA_13__:

 - When exceptions are thrown while accessing cells, the cell ref is prefixed to the exception message
 - update dependencies

__BETA_12__:

 - use URI instead of Path everywhere (because Path makes problems when referring to files located inside jars)
 
__BETA11__:

 - __BREAKING__: Added parameter `Object... values` to `Sheet.createRow()`. This requires recompilation, but no source changes.
 - `MejaHelper.printTable()`: `FIRST_LINE_IS_HEADER` to indicate that the first line is table header.
 - (BETA11a): fix IllegalArgumentException when no PrintOptions are supplied.
  
__BETA10__:

 - `MejaHelper.printTable`: output sheet as text in tabular form
 - removed MejaConfig. This had the sole purpose of toggling XOR-drawing in the sheet painter and I never really used it. If XOR-drawing does not work for whatever reason, create an issue and set the system property `MEJA_USE_XOR_DRAWING` to `false`.

__BETA9__:

 - support reading numerical values from formula cells
 
__BETA8__:

- `MejaHelper.openWorkbook()` determines the correct type by looking at the file extension.

__BETA7__:

- update utility
- remove version file

__BETA6__:

- Fix Javadoc encoding issue
- Fix some POI deprecation warnings
- Database utility class to copy data from ResultSet into Sheet
- small code cleanups
- Fix Off-by-One error in Row.createCell()

__BETA5__:

- Changes to build files
- Fix ClassCastException when writing CSV data containing dates.
- added `Sheet.createRow()` and `Row.createCell()`

__BETA4__:

- CsvWorkbookWriter: if workbook contains more than one sheet, prepend each sheet with a single line containing only `!<sheet name>!`
- the deprecated `Cell.getDate()` method returning `java.util.Date` has been replaced by a method of the same name returning `java.time.LocalDate`
- The cell type DATE has been split into DATE and DATE_TIME. This should make many things easier. The reason for not making this distinction in earlier versions is that Excel (which at the time was the only implementation) doesn't distinguish between the two.
- bugfixes

__BETA3__:

- WorkbookWriter is now an interface (was an abstract class before)
- FileType implements Comparable
- WorkbookWriter: accepts callback for progress updates

__BETA2__:

- Removed the "locale dependent" setting from CSV.
- Removed `FileType.getDescription()`. Use `FileType.getName()` instead.
- Added `FileTypeExcel` that combines the old and new Excel formats (only for reading).
- new subproject `meja.poi.module` that wraps the Apache POI Excel implementation so that it cn be used in modular jlinked projects. Just include `meja.poi.module` instead of `meja.poi`.

__BETA1__:

- Meja requires Java 11 to compile and run.
- Provides Jigsaw modules. However Apache POI is not yet fully modularised, so keep in mind when using jlink with meja.poi.
- WorkbookFactory implementations can be loaded by `ServiceProvider.load()`. Loading is done automatically when using FileType.forPath(...).factory().
- To run samples, run `./gradlew run` in the project directory

