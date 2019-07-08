package org.utplsql.api.v2;

import javax.sql.DataSource;

/**
 * Created by Pavel Kaplya on 08.03.2019.
 */
public final class SessionFactory {
    public static UtplsqlSession create(DataSource dataSource) {
        UtplsqlSessionImpl utplsqlSession = new UtplsqlSessionImpl(dataSource);

        utplsqlSession.doCompatibilityCheckWithDatabase();

        return utplsqlSession;
    }
    private SessionFactory() {
        throw new UnsupportedOperationException();
    }
}
