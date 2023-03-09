module com.dua3.meja.db {
    exports com.dua3.meja.db;

    requires transitive com.dua3.meja;

    requires java.sql;
    requires com.dua3.utility;

    requires static com.dua3.cabe.annotations;
    requires org.slf4j;
}
