import org.jspecify.annotations.NullMarked;

/**
 * The com.dua3.meja.swing module provides Swing-based user interfaces for the com.dua3.meja library.
 * <p>
 * The module exports the following packages:
 * - com.dua3.meja.ui: Contains common UI interfaces and classes
 * - com.dua3.meja.ui.swing: Contains UI implementations using Swing
 */
@NullMarked
module com.dua3.meja.swing {
    exports com.dua3.meja.ui.swing;

    requires transitive com.dua3.meja;
    requires transitive com.dua3.meja.ui;

    requires java.desktop;
    requires com.dua3.utility;
    requires com.dua3.utility.swing;

    requires org.jspecify;
    requires org.apache.logging.log4j;
}
