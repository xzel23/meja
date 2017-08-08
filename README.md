Meja is a library for handling tabular data such as Excel-Sheets, CSV-data etc.

## Design goals
- ease of use
- abstraction from the underlying file type
- provide ui elements for rendering tables

## Requirements
- JDK 8
 
Other required libraries (Apache POI) will be automatically downloaded by gradle.

## Installation

You can either clone and compile Meja yourself or use precompiled packages.

- Read how to use prebuilt packages in your build on [Jitpack.io/#xzel23/meja](https://jitpack.io/#xzel23/meja).

- If you want to **build Meja from source**: clone the repository and run `gradlew build`.

## Documentation
More information is available on the project`s [github pages](http://xzel23.github.io/meja/) where you will also find the [API docs](http://xzel23.github.io/meja/doc/index.html).

## License
Meja is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## What already works
- Reading and writing CSV and Excel files (.xls and .xlsx) format.
- Reading workbook in Excel format and writing as CSV and vice versa.
- Rendering a workbook or single sheets (Swing component, supported features: merged cells, freeze panes, fonts and colors)
- Formula Evaluation in Excel files.
- Basic editing support.

## Questions?
*So why not just use Apache POI?* While it's a great library (and it is used for processing excel files by Meja), there is no support for other file types, and while newer versions introduced interfaces that make using the same code for old and new file types (.xls and .xlsx) much easier, this goal has not yet been fully reached.

*Why a custom Swing component instead of a just a table model to be used together with JTable?* Because of JTable limitations. In fact, I started with JTable and abandoned that path when I tried to render merged cells correctly.

*But my table still doesn't look the same as in Excel!* Just grab the code, I am looking forward to your contribution.

*And what about the name?* This library is about tables, and it's about working with tables in Java. So I called it after the javanese word for the furniture.
