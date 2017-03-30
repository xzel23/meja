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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

import com.dua3.meja.util.Option;

/**
 *
 * @author axel@dua3.com
 */
public class CsvWriter extends Csv implements AutoCloseable, Flushable {
  public static CsvWriter create(File file, Map<Option<?>, Object> options) throws FileNotFoundException {
    return create(new FileOutputStream(file), options);
  }

  public static CsvWriter create(OutputStream out, Map<Option<?>, Object> options) {
      Charset charset = (Charset) getOptionValue(OPTION_CHARSET, options);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, charset));
      return create(writer, options);
  }

  public static CsvWriter create(BufferedWriter writer, Map<Option<?>, Object> options) {
    CsvWriter csvWriter = new CsvWriter(writer, options);
    return csvWriter;
  }

  private static final String allowedChars = "!§$%&/()=?`°^'.,:;-_#'+~*<>|@ \t";

  private final BufferedWriter out;
  private final String lineDelimiter;
  private final String separator;
  private final String delimiter;
  private int fieldsInRow = 0;

  public CsvWriter(BufferedWriter out, Map<Option<?>, Object> options) {
    this.separator = String.valueOf(getOptionValue(OPTION_SEPARATOR, options));
    this.delimiter = String.valueOf(getOptionValue(OPTION_DELIMITER, options));
    this.lineDelimiter = String.format("%n");
    this.out = out;
  }

  public void nextRow() throws IOException {
    out.write(lineDelimiter);
    fieldsInRow = 0;
  }

  public void addField(String text) throws IOException {
    if (fieldsInRow > 0) {
      out.write(separator);
    }
    out.write(quoteIfNeeded(text));
    fieldsInRow++;
  }

  private String quoteIfNeeded(String text) {
    return isQuoteNeeded(text) ? quote(text) : text;
  }


  private boolean isQuoteNeeded(String text) {
    // quote if separator or delimiter are present
    if (text.indexOf(separator)>=0 || text.indexOf(delimiter)>=0) {
      return true;
    }

    // also quote if unusual characters are present
    for(char c:text.toCharArray()) {
        if (!Character.isLetterOrDigit(c) && allowedChars.indexOf(c)==-1) {
            return true;
        }
    }
    return false;
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
  public void flush() throws IOException {
    out.flush();
  }

}
