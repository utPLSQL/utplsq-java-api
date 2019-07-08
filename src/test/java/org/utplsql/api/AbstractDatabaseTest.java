package org.utplsql.api;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDatabaseTest implements Closeable {

    private static String sUrl;
    private static String sUser;
    private static String sPass;

    static {
        sUrl = EnvironmentVariableUtil.getEnvValue("DB_URL", "localhost:1521:XE");
        sUser = EnvironmentVariableUtil.getEnvValue("DB_USER", "app");
        sPass = EnvironmentVariableUtil.getEnvValue("DB_PASS", "app");
    }

    private Connection conn;
    private List<Connection> connectionList = new ArrayList<>();
    private HikariDataSource dataSource;

    public AbstractDatabaseTest() {
        this.dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:oracle:thin:@" + sUrl);
        dataSource.setUsername(sUser);
        dataSource.setPassword(sPass);
        dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public static String getUser() {
        return sUser;
    }

    @BeforeEach
    public void setupConnection() throws SQLException {
        conn = newConnection();
    }


    protected Connection getConnection() {
        return conn;
    }

    protected synchronized Connection newConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@" + sUrl, sUser, sPass);
        connectionList.add(conn);
        return conn;
    }

    @AfterEach
    public void teardownConnection() {
        for (Connection conn : connectionList) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
