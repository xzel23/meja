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
import java.util.Optional;
import java.util.Collections;
import java.util.LinkedList;
import java.nio.file.Path;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.util.Option;
import com.dua3.utility.io.IOUtil;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public abstract class FileType {

    private static final List<FileType> types = new LinkedList<>();

    public static List<FileType> filetypes() {
        return Collections.unmodifiableList(types);
    }

    public static void add(FileType type) {
        types.add(type);
    }

    public static Optional<FileType> forPath(Path p) {
        String ext = IOUtil.getExtension(p);
        for (FileType t: types) {
            if (t.extensions.contains(ext)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    private final String name;
    private final String description;
    private final List<String> extensions; // unmodifiable!
    private final OpenMode mode;
  
    public FileType(String name, String description, OpenMode mode, String... extensions) {
        this.name = name;
        this.description = description;
        this.extensions = List.of(extensions);
        this.mode = mode;
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
        return extensions;
    }

    public boolean isSupported(OpenMode mode) {
        return (this.mode.n&mode.n) == mode.n;
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

    public abstract WorkbookFactory<? extends Workbook> factory();
    public abstract WorkbookWriter getWriter();

	public static List<FileType> getFileTypes(OpenMode mode) {
        List<FileType> list = new LinkedList<>(types);
        list.removeIf(t -> (t.mode.n&mode.n)!=mode.n);
		return list;
	}

    public List<Option<?>> getSettings() {
        return Collections.emptyList();
    }
}
