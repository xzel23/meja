/**
 * The com.dua3.meja.ui module provides interface definitions for the com.dua3.meja library's UI classes.
 */
module com.dua3.meja.ui {
    exports com.dua3.meja.ui;

    requires transitive com.dua3.meja;

    requires com.dua3.utility;

    requires static com.dua3.cabe.annotations;
    requires org.apache.logging.log4j;
}
