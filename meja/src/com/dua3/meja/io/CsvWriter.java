/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.io;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 *
 * @author axel@dua3.com
 */
public class CsvWriter implements AutoCloseable, Flushable {

  private PrintWriter out;
  private String lineDelimiter = "%n";
  private char separator = ';';
  private char delimiter = '"';
  private int fieldsInRow = 0;

  public CsvWriter(Writer writer) {
    this(Csv.DEFAULT_SEPARATOR, Csv.DEFAULT_DELIMITER, writer);
  }

  public CsvWriter(OutputStream out) {
    this(Csv.DEFAULT_SEPARATOR, Csv.DEFAULT_DELIMITER, out);
  }

  public CsvWriter(PrintWriter out) {
    this(Csv.DEFAULT_SEPARATOR, Csv.DEFAULT_DELIMITER, out);
  }

  public CsvWriter(char separator, char delimiter, Writer writer) {
    this(separator, delimiter, new PrintWriter(writer));
  }

  public CsvWriter(char separator, char delimiter, OutputStream out) {
    this(separator, delimiter, new PrintWriter(out));
  }

  public CsvWriter(char separator, char delimiter, PrintWriter out) {
    this.separator = separator;
    this.delimiter = delimiter;
    this.out = out;
  }

  public void nextRow() {
    out.printf(lineDelimiter);
    fieldsInRow = 0;
  }

  public void addField(String text) {
    if (fieldsInRow > 0) {
      out.print(separator);
    }
    out.print(quote(text));
    fieldsInRow++;
  }

  private String quote(String text) {
    return delimiter+text.replaceAll("\"", "\"\"")+delimiter;
  }

  @Override
  public void close() throws IOException {
    if (fieldsInRow>0) {
      nextRow();
    }
    out.close();
  }

  @Override
  public void flush() {
    out.flush();
  }

}
