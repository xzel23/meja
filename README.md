Méja is a library for handling tabular data such as Excel-Sheets, CSV-data etc.

## Design goals
- ease of use
- abstraction from the underlying file type
- provide ui elements for rendering tables

## Requirements
- JDK 7 (I plan to use this library in office where we cannot yet upgrade to Java 8)
- Apache Ant
 
Required libraries (currently that's just Apache POI) should be automatically downloaded by Ant (provided I got the Ivy part right).

## Documentation
More information is available on the project`s [github pages](http://xzel23.github.io/meja/) where you will also find the [API docs](http://xzel23.github.io/meja/javadoc/index.html).

## License
Méja is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## What already works
- Reading and writing Excel files in both .xls and .xlsx format. 
- Reading CSV files.
- Rendering a workbook or single sheets (currently .xls seems to give better results).
- Formula Evaluation in Excel files.
- Basic editing support.

## Questions?
*So why not just use Apache POI?* While it's a great library (and it is used for processing excel files by Méja), there is no support for other file types, and while newer versions introduced interfaces that make using the same code for old and new file types (.xls and .xlsx) much easier, this goal has not yet been fully reached.

*Why a custom component instead of a just a table model to be used together with JTable?* Because of JTable limitations. In fact, I started with JTable and abandoned that path when I tried to render merged cells correctly.

*But my table still doesn't look the same as in Excel!* Just grab the code, I am looking forward to your contribution.

*And what about the name?* This library is about tables, and it's about working with tables in Java. So I called it after the javanese word for the furniture. (Normally, it is written without accent, but found it to look more interesting like this...)
