module com.dua3.meja.db {
    exports com.dua3.meja.db;

    requires transitive com.dua3.meja;

    requires java.sql;

    requires static com.dua3.cabe.annotations;
    requires org.apache.logging.log4j;
}
