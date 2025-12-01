# Meja Changelog

## Version 9.0.0 (in progress)

- snapshot builds are published to maven snapshots
- migrate from OSS-RH to Central Publishing Portal
- use JReleaser dor deploying/publishing
- renamed the `meja` module to `meja-core`
- use SonarQube static analysis, jacoco aggregate reporting
- update Apache POI to 5.5.1
- HtmlWorkbookWriter
    - declared methods not intended to be called from non-library code as private
    - added export methods for single sheets
    - changed CSS style name ID generation from MD5 to UUID, shortened CSS style names
- small fixes and improvements

## Version 8.1.2

- added SonarCloud analysis and code coverage to the GitHub workflow
- fixed issues reported by sonar

## Version 8.1.1

- dependency updates

## Version 8.1.0

- dependency updates
- fixed HTML export not escaping font names containing spaces (fixed in dependency)

## Version 8.0.4

- dependency updates
- refactor column and row size calculations; consolidate behaviour of autoSizeColumn() and autosizeColumns()

## Version 8.0.3

- add save functionality to the FxExcelViewer
- update dependencies

## Version 8.0.2

- improve column width calculations in headless mode for the known Excel default fonts Arial and Calibri
- enable a previously disabled test for correctly parsing dates (only runs whe the build uses a german language locale)
  as the test is locale-dependent)

## Version 8.0.1

- fix localized date formatting for Excel cells
- updated Gradle wrapper so that meja can be built on Java 24
- update dependencies
- code cleanup

## Version 8.0.0

- IMPORTANT: will require Java 21. Stay on 7.x if you are still on Java 17. 7.x will receive bugfixes until the next
  Java LTS version is released (Java 25).
- BREAKING: when passing `java.util.Date` as argument to `cell.set(Object)`, the cell will be set to 
  the `Date` object's `toString()` value; previously, the `Date`object was inspected and the
  value would be converted to either a `LocalTime` or a `LocalDateTime`instance before setting
  the cell value
- code cleanup after raising the Java version

## Version 7.2.2

- bugfix release; fixes a module dependency and a possible Java 17 incompatibility
- gradle build: forbiddenapis plugin added; fixed up some build file issues

## Version 7.2.1

- Apache POI 5.4.1; update other dependencies

## Version 7.2.0

- update utility to 17.0.0
- GitHub actions: fix CI build, including Qodana scan
- add/improve Javadoc
- use a fixed factor to convert between internal units and points to get the same column widths independent of platform

## Version 7.1.0

- update utility to 16.2.0 (breaking change in Font class)

## Version 7.0.2

- many Javadoc additions and corrections
- the internal abstract base classes have been refactored to reduce code duplication
- dependency updates

## Version 7.0.1

- fix SXSSF not writing column widths correctly after updating POI

## Version 7

- update Apache POI to 5.4.0
- sheet.getCurrentCell() returns cell directly, not wrapped in an Optional
- fix getRotation() for XSSF files for values outside of the range [0, 90]
- UI components for Swing and JavaFX:
- support rotated text
- support text layout 'distributed'
- allow navigating beyond spreadsheet row/column range

### Known Issues

- FXSheetView: mouse panning ~~and changing zoom~~ does not update scrollbars

## Version 6.3.0

- update utility
- fix rendering issues
- JavaFX WorkbookView fixes

## Version 6.2.1

- update utility; fixes a bug where the text color in POI sheets was set to black

## Version 6.2.0

- update utility to 15.x
- added sheet as argument to SwingSheetView and FxSheetView constructors
- small fixes

## Version 6.1.0

- BREAKING: null arguments to methods wil throw IllegalArgumentException instead of NullPointerException
- dependency updates and small fixes
- javadoc

## Version 6.0.0

- BREAKING: some Event class method names changed to be more consistent; some methods that could return null have been 
  changed to return Optional instead
- JSpecify annotations are used and all modules are @NullMarked
- use log4j-bom
- Javadoc additions
- dependency updates
- fix issues when formatting dates
- several fixes for the JavaFX components that display Workbooks and sheets

## Version 5.0.1

- use Java 17 toolchain for all modules but the JavaFX support module that uses Java 21

## Version 5.0

- new JavaFX WorkbookView
- rewritten large parts of the Swing sheet rendering code
- Methods that previously might return null have been changed to return Optional instead
- update utility to next major version
- use Rectangle2f from utility; changes coordinates to float
- code cleanup
- NOTE: there are some glitches with border drawing in Swing that will be fixed in a coming release

## Version 4.1.5

- fixes for workbook-relative hyperlinks in the Excel implementation

## Version 4.1.5

- allow workbook-relative hyperlinks

## Version 4.1.4

- override transitive Apache POI dependency on commons-compress to non-vulnerable version 1.26.1
- update utility

## Version 4.1.3

- dependency updates

## Version 4.1.2

- add missing package-info, annotations
- update gradle, cabe, dependencies

## Version 4.1.1

- dependency updates

## Version 4.1.0

- internal logging was migrated from SLF4J to Log4j-api
- removed unused interface IRowBuilder
- vulnerability scanning using GitHub CodeQL
- add Cell.setError(), Cell.isError()
- small fixes and enhancements
- update dependencies

## Version 4.0.3

- update utility and log4j to latest versions for better JPMS support

## Version 4.0.2

- fix locale dependent formatting of dates and numbers in both Excel and generic implementations when the same cell is
  queried multiple times with different locales
- update Apache POI to 5.2.4
- update JavaFX to 21
- code cleanup

## Version 4.0.1

- Excel: make sure cells are date or date/time formatted when setting value to a LocalDate or LocalDateTime

## Version 4.0.0

- BREAKING: Sheet.getRowIfExists() and Row.getCellIfExists() return Optional
- BREAKING: move MejaHelper.find() methods to Sheet/Row classes
- FIX: TableModel created by MejaSwingHelper.createTableModel() adds empty cells to sheet
- FIX: Sheet.clear() not firing change event
- FIX: TableModel not updating last row when changes are made to sheet
- NEW: Sheet.austoSizeRow(), takes into account rotated text
- NEW: Sheet.getCellIfExists()
- NEW: introduce SearchSettings record
- NEW: TableModel created by MejaSwingHelper.createTableModel() allows editing; changes write through to the workbook
- Javadoc updates and additions
- added more unit tests
- update dependencies
- common implementation for autoSizeColumn()
- allow null values in Sheet.createRow()
- fixes in SheetTableModel
- refactorings

## Version 3.2.1

- parametrized unit tests
- update utility to fix version

## Version 3.2.0

- run GitHub CI on both Windows and Linux
- fix HTML export ignoring locale
- improved unit tests
- update dependencies

## Version 3.1.6

- update dependencies
- enable Github CI
- fix unit tests on windows
- fix unit tests depending on system locale
- **breaking:** remove Workbook.resolve()

## Version 3.1.4

- update dependencies
- code cleanup
- cleanup JavaScript of exported files
- add gradle tasks for running samples
- fix log4j warning when running tests

## Version 3.1.3

- update dependencies
- improve test coverage
- respect options when reading CSV

## Version 3.1.2

- update dependencies
- use version catalogue for project dependencies

## Version 3.1.1

- update utility
- update Apache POI

## Version 3.1.0

- update utility
- all logging is done through SLF4J

## Version 3.0.1

- small fixes improvements

## Version 3.0.0

- **Java 17 required**
- module names changed to `com.dua3.meja`
- Apache POI updated to 5.2.0
- some method names have changed because a few classes have been replaced by records (i.e. `getxxx()` -> `xxx()`)
- API is "not-null-per default", i.e. all parameters of object type must not be null unless otherwise stated in the
  documentation. This is checked at runtime when assertions are enabled.
- All overrides of MejaHelper.find() return Optional

## version 2.4

- JDK 11+
- POI 5.2.0
- update utility to 9.0.1

## Version 2.2.x

- update utility to 7.0.x bugfix releases
- fix exception when copying cell styles (2.2.6)

## Version 2.2

- update utility to 7; this is a major update of that library
- bugfixes; refactorings, and improvements

## Version 2.1

- POI: fix copying sheet with autofilter
- fix rotated text in HTML output
- bugfixes in POI implementation
- fix CellStyle.getRotation() returning values >90 degrees in XSSF implementation
- fix Poi implementation: Row.getLastCellNum() returned -2 for empty rows()
- Row.getFirstCellNum() returns 0 for empty rows (was unspecified)
- minor fixes
- HTML export: improved CSS for rotated cells
- Cell.merge() returns `this`
- several minor fixes
- change Cell.setCellStyle() return type from void to Cell to allow chaining
- HTML-export: display links in color defined by cell style
- disabled the POI-module build for the time being - I will either enable it for final or switch to POI 5 which will
  hopefully be Jigsaw compatible by itself so that the POI-module will not be needed anymore
- update utility
- update spotbugs
- code cleanup
- text rotation in CellStyle (not yet supported in viewer)
- HTML output: support cell styles

## Version 2.0.14

- Workbook.findSheetByName() and Workbook.getOrCreateSheet()
- update utility
- add some javadoc to the Workbook object caching methods

## Version 2.0.13.2

- update utility (to be consistent with other libs)

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
- fix hyperlinks - pass links by URI as problems with absolute paths on Windows could not be resolved with URLs

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

- __BREAKING__: Added parameter `Object... values` to `Sheet.createRow()`. This requires recompilation, but no source
  changes.
- `MejaHelper.printTable()`: `FIRST_LINE_IS_HEADER` to indicate that the first line is table header.
- (BETA11a): fix IllegalArgumentException when no PrintOptions are supplied.

__BETA10__:

- `MejaHelper.printTable`: output sheet as text in tabular form
- removed MejaConfig. This had the sole purpose of toggling XOR-drawing in the sheet painter and I never really used it.
  If XOR-drawing does not work for whatever reason, create an issue and set the system property `MEJA_USE_XOR_DRAWING`
  to `false`.

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

- CsvWorkbookWriter: if workbook contains more than one sheet, prepend each sheet with a single line containing
  only `!<sheet name>!`
- the deprecated `Cell.getDate()` method returning `java.util.Date` has been replaced by a method of the same name
  returning `java.time.LocalDate`
- The cell type DATE has been split into DATE and DATE_TIME. This should make many things easier. The reason for not
  making this distinction in earlier versions is that Excel (which at the time was the only implementation) doesn't
  distinguish between the two.
- bugfixes

__BETA3__:

- WorkbookWriter is now an interface (was an abstract class before)
- FileType implements Comparable
- WorkbookWriter: accepts callback for progress updates

__BETA2__:

- Removed the "locale dependent" setting from CSV.
- Removed `FileType.getDescription()`. Use `FileType.getName()` instead.
- Added `FileTypeExcel` that combines the old and new Excel formats (only for reading).
- new subproject `meja.poi.module` that wraps the Apache POI Excel implementation so that it cn be used in modular
  jlinked projects. Just include `meja.poi.module` instead of `meja.poi`.

__BETA1__:

- Meja requires Java 11 to compile and run.
- Provides Jigsaw modules. However, Apache POI is not yet fully modularized, so keep in mind when using jlink with
  meja.poi.
- WorkbookFactory implementations can be loaded by `ServiceProvider.load()`. Loading is done automatically when using
  FileType.forPath(...).factory().
- To run samples, run `./gradlew run` in the project directory

