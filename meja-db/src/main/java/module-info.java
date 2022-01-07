module meja.db {
    exports com.dua3.meja.db;

    requires transitive meja;

    requires java.logging;
    requires java.sql;
    requires dua3_utility;
    
    requires static com.dua3.cabe.annotations;
}
