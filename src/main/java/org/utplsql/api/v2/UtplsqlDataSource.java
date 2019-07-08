package org.utplsql.api.v2;

import oracle.jdbc.OracleConnection;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by Pavel Kaplya on 05.05.2019.
 */
public interface UtplsqlDataSource extends DataSource {
    @Override
    OracleConnection getConnection() throws SQLException;

    @Override
    OracleConnection getConnection(String username, String password) throws SQLException;
}
