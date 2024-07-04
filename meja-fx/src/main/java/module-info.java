/**
 * This module provides JavaFX support for the Meja library.
 */
module com.dua3.meja.fx {
    exports com.dua3.meja.ui.fx;
    opens com.dua3.meja.ui.fx;

    requires javafx.controls;
    requires javafx.graphics;

    requires transitive com.dua3.meja;
    requires transitive com.dua3.meja.ui;
    requires com.dua3.utility;
    requires com.dua3.utility.fx;
    requires com.dua3.cabe.annotations;
    requires org.apache.logging.log4j;
    requires java.desktop;
}
