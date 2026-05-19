package com.techchallenge.config;

import org.hibernate.dialect.H2Dialect;

/**
 * Custom H2 dialect for tests.
 * Disables INSERT...RETURNING syntax to avoid H2 compatibility issues.
 * Hibernate 6.x with H2 >= 2.2.220 tries to use INSERT RETURNING, but
 * H2 2.2.x can fail with syntax errors in certain configurations.
 * This dialect forces the use of standard JDBC getGeneratedKeys() instead.
 */
public class H2TestDialect extends H2Dialect {

    @Override
    public boolean supportsInsertReturning() {
        return false;
    }

    @Override
    public boolean supportsInsertReturningGeneratedKeys() {
        return false;
    }
}
