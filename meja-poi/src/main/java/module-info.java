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

module meja.poi {
    exports com.dua3.meja.model.poi;
    opens com.dua3.meja.model.poi;

    exports com.dua3.meja.model.poi.io;
    opens com.dua3.meja.model.poi.io;

    provides com.dua3.meja.model.WorkbookFactory with com.dua3.meja.model.poi.PoiWorkbookFactory;
    provides com.dua3.utility.io.FileType with com.dua3.meja.model.poi.io.FileTypeExcel;

    requires transitive meja;

    requires org.apache.poi.ooxml;
    requires org.apache.poi.ooxml.schemas;
    requires java.logging;
    requires dua3_utility;
}
