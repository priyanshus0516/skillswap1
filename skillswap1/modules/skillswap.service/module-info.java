module skillswap.service {
    requires transitive skillswap.model;
    requires transitive skillswap.exception;
    requires skillswap.dao;
    requires skillswap.util;

    exports com.skillswap.service;
}
