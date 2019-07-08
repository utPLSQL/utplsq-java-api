package org.utplsql.api.v2.reporters;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Created by Pavel Kaplya on 05.05.2019.
 */
class Pre310OutputFetcherImpl implements TextOutputFetcher {

    private final int fetchSize = 100;

    @Override
    public void fetchLine(OracleConnection conn, org.utplsql.api.reporter.Reporter reporter, Consumer<String> onLineFetched) throws SQLException {
        try {
            reporter.waitInit();
        } catch (InterruptedException e) {
            return;
        }
        try (CallableStatement cstmt = getLinesCursorStatement(conn, reporter)) {
            cstmt.execute();
            cstmt.setFetchSize(fetchSize);

            try (ResultSet resultSet = (ResultSet) cstmt.getObject(1)) {
                while (resultSet.next()) {
                    onLineFetched.accept(resultSet.getString("text"));
                }
            }
        }
    }


    private CallableStatement getLinesCursorStatement(OracleConnection conn, org.utplsql.api.reporter.Reporter reporter) throws SQLException {
        CallableStatement cstmt = conn.prepareCall("BEGIN ? := ut_output_buffer.get_lines_cursor(?); END;");
        cstmt.registerOutParameter(1, OracleTypes.CURSOR);
        cstmt.setString(2, reporter.getId());
        return cstmt;
    }
}
