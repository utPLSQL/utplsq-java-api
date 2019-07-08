package org.utplsql.api.v2;

import oracle.jdbc.OracleConnection;
import org.utplsql.api.exception.UtplsqlException;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Created by Pavel Kaplya on 05.05.2019.
 */
class UtplsqlDataSourceImpl implements UtplsqlDataSource {

    private final DataSource delegate;

    UtplsqlDataSourceImpl(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isWrapperFor(OracleConnection.class)) {
                throw new UtplsqlException("Wrong driver is used. Oracle JDBC driver is needed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.delegate = dataSource;
    }

    @Override
    public OracleConnection getConnection() throws SQLException {
        return delegate.getConnection().unwrap(OracleConnection.class);
    }

    @Override
    public OracleConnection getConnection(String username, String password) throws SQLException {
        return delegate.getConnection(username, password).unwrap(OracleConnection.class);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }
}
