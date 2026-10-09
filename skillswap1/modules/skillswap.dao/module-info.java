module skillswap.dao {
    requires transitive skillswap.model;
    requires transitive skillswap.exception;
    requires skillswap.util;
    requires java.sql;

    exports com.skillswap.dao;
    exports com.skillswap.dao.inmemory;
    exports com.skillswap.dao.jdbc;
}
