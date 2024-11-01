// Copyright 2019 Axel Howind
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import org.jspecify.annotations.NullMarked;

/**
 * The com.dua3.meja.poi module provides MS Excel compatible implementations of Workbooks etc. based on Apache POI.
 */
@NullMarked
module com.dua3.meja.poi {
    exports com.dua3.meja.model.poi;
    opens com.dua3.meja.model.poi;

    exports com.dua3.meja.model.poi.io;
    opens com.dua3.meja.model.poi.io;

    provides com.dua3.meja.model.WorkbookFactory with com.dua3.meja.model.poi.PoiWorkbookFactory;
    provides com.dua3.utility.io.FileType with com.dua3.meja.model.poi.io.FileTypeExcel;

    requires transitive com.dua3.meja;

    requires org.apache.poi.ooxml;
    requires com.dua3.utility;

    requires org.jspecify;
    requires org.apache.logging.log4j;
}
