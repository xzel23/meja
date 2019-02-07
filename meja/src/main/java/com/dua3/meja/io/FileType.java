/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
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

import java.util.List;
import java.util.Collections;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public abstract class FileType {

    private static final List<FileType> types;

    public static List<FileType> filetypes() {
        return Collections.unmodifiableList(types);
    }

    public static void add(FileType type) {
        types.add(type);
    }

    private final String name;
    private final String description;
    private final String[] extensions;
  
    public FileType(String name, String description, String... extensions) {
        this.name = name;
        this.description = description;
        this.extensions = Arrays.copyOf(extensions, extensions.length);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    public List<String> getExtensions() {
        return List.of(extensions);
    }

    public boolean matches(String filename) {
        String ext1 = IOUtil.getExtension(filename);
        for (String ext2: extensions) {
            if (ext2.equals(ext1)) {
                return true;
            }
        }
        return false;
    }

    public abstract WorkbookReader newReader();
    public abstract WorkbookWriter newWriter();

}
