package org.utplsql.api.v2.reporters;

import oracle.jdbc.OracleConnection;

import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Created by Pavel Kaplya on 05.05.2019.
 */
interface TextOutputFetcher {
    void fetchLine(OracleConnection connection, org.utplsql.api.reporter.Reporter reporter, Consumer<String> onLineFetched) throws SQLException;
}
