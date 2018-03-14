# Meja spreadsheet library

[ ![Download](https://api.bintray.com/packages/dua3/public/com.dua3.meja/images/download.svg?version=1.0-beta13) ](https://bintray.com/dua3/public/com.dua3.meja/1.0-beta13/link)

Meja is a library for handling tabular data such as Excel-Sheets, CSV-data etc.

## Design goals
- ease of use
- abstraction from the underlying file type
- provide ui elements for rendering tables

## Requirements
- JDK 8

## Installation

You can either clone and compile Meja yourself or add dependencies to your project's build file. The Maven repository is loacated at [https://dl.bintray.com/dua3/public](https://dl.bintray.com/dua3/public).

### Gradle

Add the repostiory:

    repositories {
        ...
        maven { url  "https://dl.bintray.com/dua3/public" }
    }

Add dependencies:

    dependencies {
        ...
        compile 'com.dua3.meja:meja:1.0-beta13'
        compile 'com.dua3.meja:meja-swing:1.0-beta13'
    }

### Maven

Add the repository [https://dl.bintray.com/dua3/public](https://dl.bintray.com/dua3/public), then add the dependencies:

    <dependency>
        <groupId>com.dua3.meja</groupId>
        <artifactId>meja</artifactId>
        <version>1.0-beta13</version>
        <type>pom</type>
    </dependency>
    <dependency>
        <groupId>com.dua3.meja</groupId>
        <artifactId>meja-swing</artifactId>
        <version>1.0-beta13</version>
        <type>pom</type>
    </dependency>

### Building from source
Clone the repository and run `gradlew`. This will also install meja into your local maven repository.

## License
Meja is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## What already works
- Reading and writing CSV and Excel files (.xls and .xlsx) format.
- Reading workbook in Excel format and writing as CSV and vice versa.
- Rendering a workbook or single sheets (Swing component, supported features: merged cells, freeze panes, fonts and colors)
- Formula Evaluation in Excel files.

## GUI components
- A GUI component to display spreadsheets in Swing applications is included.
- I removed the work-in-progress code for a JavaFX component. I plan to instead provide a way to easily set up a [ControlsFX SpreadSheetView](http://fxexperience.com/controlsfx/features/#spreadsheetview) for workbooks/spreadsheets.  
