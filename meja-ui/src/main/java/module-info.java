/**
 * The com.dua3.meja.swing module provides Swing-based user interfaces for the com.dua3.meja library.
 * <p>
 * The module exports the following packages:
 * - com.dua3.meja.ui: Contains common UI interfaces and classes
 * - com.dua3.meja.ui.swing: Contains UI implementations using Swing
 */
module com.dua3.meja.ui {
    exports com.dua3.meja.ui;

    requires transitive com.dua3.meja;

    requires com.dua3.utility;

    requires static com.dua3.cabe.annotations;
    requires org.apache.logging.log4j;
    requires java.desktop;
}
