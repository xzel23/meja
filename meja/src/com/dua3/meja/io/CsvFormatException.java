/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
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

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class CsvFormatException extends FileFormatException {
    private static final long serialVersionUID = 1L;

    private final String source;
    private final int line;

    public CsvFormatException(String message, String source, int line) {
        super(message);
        this.source=source;
        this.line = line;
    }

    @Override
    public String getMessage() {
        if (source!=null && !source.isEmpty()) {
            return "["+source+":"+line+"] "+super.getMessage();
        } else {
            return "["+line+"] "+super.getMessage();
        }
    }

}
