# Meja spreadsheet library

Meja is a library for handling tabular data such as Excel-Sheets, CSV-data etc.

## Building

Clone the repository and run `./gradlew`. This will also install meja into your local maven repository.

## License

Meja is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Changes

### Version 2.0

- Requires Java 11.
- Provides Jigsaw modules. However Apache POI is not yet fully modularised.
- WorkbookFactory implementations can be loaded by `ServiceProvider.load()`.
- To run samples, run `./gradlew run` in the project directory
