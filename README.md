# Meja spreadsheet library [![CI](https://github.com/xzel23/meja/actions/workflows/gradle.yml/badge.svg)](https://github.com/xzel23/meja/actions/workflows/gradle.yml)

Meja is a library for handling tabular data such as Excel-Sheets, CSV-data etc.

## Name

As it's rather common to give everything Java related a name of indo/malay origin, I chose 'meja' which is the
indonesian word for table (as in furniture).

## Building

The required Java version is 17.

Clone the repository and run `./gradlew`. This will also install meja into your local maven repository.

## License

Meja is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Modules

Meja consists of different modules, each providing different functionality.

### Module: com.dua3.meja

This is the base module providing functionality shared by different models.

### Module: com.dua3.meja.generic

A generic Workbook implementation. Fast and memory efficient. Use this implementation when creating workbooks in memory.
Also defines the CSV FileType.

### com.dua3.meja.poi

An implementation backed by the Apache POI implementation of the Microsoft Office Excel file format. Defines FileTypes
for xls and xlsx files. Use this implementation to read and modify Excel files.

### com.dua3.meja.swing

Defines Swing controls for displaying Sheets and Workbooks.

### com.dua3.meja.samples

Several small samples to demonstrate how to use this library.

### com.dua3.meja.fx

Utilities for JavaFX.

## Logging

Meja uses Log4J2-API for logging facade, the same as the Apache POI library used for reading and writing Excel files.
