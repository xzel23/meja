module com.dua3.meja {
    exports com.dua3.meja.ui.swing;

    requires com.dua3.meja.io;
    requires com.dua3.meja.model;
    requires com.dua3.meja.util;
    requires com.dua3.meja.ui;

    requires org.apache.logging.log4j;
    requires com.dua3.utility;
}
