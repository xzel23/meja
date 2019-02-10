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

- Requires Java 11.
- Provides Jigsaw modules. However Apache POI is not yet fully modularised.
- WorkbookFactory implementations can be loaded by `ServiceProvider.load()`.
- To run samples, run `./gradlew run` in the project directory

