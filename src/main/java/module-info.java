module com.dua3.meja {
    requires java.desktop;
    requires java.logging;

    requires poi;
    requires com.dua3.utility;
    
    exports com.dua3.meja.io;
    exports com.dua3.meja.model;
    exports com.dua3.meja.model.generic;
    exports com.dua3.meja.model.poi;
    exports com.dua3.meja.util;
    exports com.dua3.meja.ui;
}
