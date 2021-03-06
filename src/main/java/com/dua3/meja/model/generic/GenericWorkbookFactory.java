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
package com.dua3.meja.model.generic;

import java.io.IOException;
import java.nio.file.Path;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.io.WorkbookReader;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.util.Options;
import com.dua3.utility.lang.LangUtil;

/**
 * A Factory for creating instances of {@link GenericWorkbook}.
 */
public class GenericWorkbookFactory extends WorkbookFactory<GenericWorkbook> {

    private static final GenericWorkbookFactory INSTANCE = new GenericWorkbookFactory();

    public static GenericWorkbookFactory instance() {
        return INSTANCE;
    }

    private GenericWorkbookFactory() {
    }

    @Override
    public GenericWorkbook create() {
        return new GenericWorkbook(null);
    }

    @Override
    public GenericWorkbook createStreaming() {
        return create();
    }

    @Override
    public GenericWorkbook open(Path path, Options importSettings) throws IOException {
        FileType type = FileType.forPath(path).orElse(FileType.CSV);

        LangUtil.check(type.isSupported(OpenMode.READ), "Reading is not supported for files of type '%s'.",
                type.getDescription());

        WorkbookReader reader = type.getReader();

        reader.setOptions(importSettings);

        return reader.read(GenericWorkbookFactory.instance(), path);
    }

}
