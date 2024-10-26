import org.jspecify.annotations.NullMarked;

/**
 * This module provides database functionality for the Meja library.
 */
@NullMarked
module com.dua3.meja.db {
    exports com.dua3.meja.db;

    requires transitive com.dua3.meja;

    requires java.sql;

    requires static org.jspecify;
    requires org.apache.logging.log4j;
}
