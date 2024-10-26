import org.jspecify.annotations.NullMarked;

/**
 * This module provides JavaFX support for the Meja library.
 */
@NullMarked
module com.dua3.meja.fx {
    exports com.dua3.meja.ui.fx;
    opens com.dua3.meja.ui.fx;

    requires javafx.controls;
    requires javafx.graphics;

    requires transitive com.dua3.meja;
    requires transitive com.dua3.meja.ui;
    requires com.dua3.utility;
    requires com.dua3.utility.fx;
    requires org.apache.logging.log4j;
    requires java.desktop;
    requires org.jspecify;
}
