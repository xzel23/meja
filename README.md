# Meja spreadsheet library
[![Apache License](https://img.shields.io/badge/license-Apache-blue)](LICENSE)
[![Language](https://img.shields.io/badge/language-Java-blue.svg?style=flat-square)](https://github.com/topics/java)
[![build](https://github.com/xzel23/meja/actions/workflows/CI.yml/badge.svg)](https://github.com/xzel23/meja/actions/workflows/CI.yml)

Meja is a library for handling tabular data such as Excel-Sheets, CSV-data etc.

## Name

As it's rather common to give everything Java related a name of indo/malay origin, I chose 'meja' which is the
indonesian word for table (as in furniture).

## Using Meja in your projects

* The Minimal Java version is **Java 17**.
* **Java 21/JavaFX 21** is needed to use the **JavaFX related modules**.

Meja is available in the Maven central repository.

You need at least the following modules:

* `meja`: core functionality, CSV import and export, HTML export
* `meja-generic`: generic implementation (memory efficient)

Add any combination of the following as needed:

* `meja-poi`: Apache POI based implementation (supports Excel XLSX and XLS file format)
* `meja-db`: for reading data directly from JDBC result sets
* `meja-swing`: for displaying sheets and workbooks in Swing applications
* `meja-fx`: for displaying sheets and workbooks in JavaFX applications

**Maven**

```
    <dependency>
        <groupId>com.dua3.meja</groupId>
        <artifactId>[meja_module]</artifactId>
        <version>[meja_version]</version>
    </dependency>
```

**Gradle (Groovy DSL)**

```
    implementation 'com.dua3.meja:[meja_module]:[meja_version]'
```

**Gradle (Kotlin DSL)**

```
    implementation("com.dua3.meja:[meja_module]:[meja_version]")
```

## Building

Java 21 is required for building Meja.

Clone the repository and run `./gradlew build` to build the library, `./gradlew publishToMavenLocal` to publish to
your local Maven repository.

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

## Handling of `null` parameters and return values

Meja uses [Cabe](https://github.com/xzel23/cabe) to ensure correct handling of `null` values.

This means that:

- Only parameters marked as @Nullable accept `null` values. When `null` is passed for a parameter not marked as
`@Nullable`, an `IllegalArgumentException` is thrown. The offending parameter is given in the exception message.

- Only methods where the return value is marked as `@Nullable` will ever return `null`.
