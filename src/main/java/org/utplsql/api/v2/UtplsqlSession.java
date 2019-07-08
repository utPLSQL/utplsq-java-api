package org.utplsql.api.v2;

import oracle.jdbc.OracleConnection;
import org.utplsql.api.Version;
import org.utplsql.api.exception.InvalidVersionException;
import org.utplsql.api.reporter.inspect.ReporterInfo;
import org.utplsql.api.v2.reporters.ReporterFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Pavel Kaplya on 08.03.2019.
 */
public interface UtplsqlSession {

    UtplsqlDataSource getDataSource();

    TestRunBuilder createTestRun();

    ReporterFactory reporterFactory();

    List<ReporterInfo> getAvailableReporters() throws SQLException, InvalidVersionException;

    Version getInstalledVersionInfo();

    OracleConnection getConnection() throws SQLException;
}
