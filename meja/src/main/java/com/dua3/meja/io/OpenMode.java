/*
 * Copyright 2015 a5xysq1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.meja.io;

/**
 * Mode for opening files.
 */
public enum OpenMode {
    /**
     * None of reading and writing.
     */
    NONE(0),
    /**
     * Open file for reading.
     */
    READ(1),
    /**
     * Open file for writing.
     */
    WRITE(2),
    /**
     * Open file for reading and/or writing.
     */
    READ_AND_WRITE(3);

    int n;

    private OpenMode(int n) {
        this.n = n;
    }
}
