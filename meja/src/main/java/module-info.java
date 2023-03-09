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

module com.dua3.meja {
    exports com.dua3.meja.util;
    exports com.dua3.meja.model;
    exports com.dua3.meja.io;
    opens com.dua3.meja.util;
    opens com.dua3.meja.model;
    opens com.dua3.meja.io;

    requires java.desktop;
    requires com.dua3.utility;

    requires static com.dua3.cabe.annotations;
    requires org.slf4j;

    uses com.dua3.meja.model.WorkbookFactory;

    provides com.dua3.utility.io.FileType with com.dua3.meja.io.FileTypeHtml;
}
