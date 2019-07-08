package org.utplsql.api.testRunner;

import oracle.jdbc.OracleConnection;
import org.utplsql.api.CustomTypes;
import org.utplsql.api.FileMapper;
import org.utplsql.api.FileMapping;
import org.utplsql.api.TestRunnerOptions;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Abstract class which creates a callable statement for running tests
 * The SQL to be used has to be implemented for there are differences between the Framework-versions
 *
 * @author pesse
 */
abstract class AbstractTestRunnerStatement implements TestRunnerStatement {

    protected final TestRunnerOptions options;
    protected final CallableStatement callableStatement;
    private final OracleConnection conn;

    protected AbstractTestRunnerStatement(TestRunnerOptions options, Connection conn) throws SQLException {
        this.options = options;
        this.conn = conn.unwrap(OracleConnection.class);

        callableStatement = conn.prepareCall(getSql());

        createStatement();
    }

    protected abstract String getSql();

    protected int createStatement() throws SQLException {

        int paramIdx = 0;

        callableStatement.setArray(
                ++paramIdx, conn.createOracleArray(CustomTypes.UT_VARCHAR2_LIST, options.getPathList().toArray()));

        callableStatement.setArray(
                ++paramIdx, conn.createOracleArray(CustomTypes.UT_REPORTERS, options.getReporterList().toArray()));

        if (options.getCoverageSchemes().isEmpty()) {
            callableStatement.setNull(++paramIdx, Types.ARRAY, CustomTypes.UT_VARCHAR2_LIST);
        } else {
            callableStatement.setArray(
                    ++paramIdx, conn.createOracleArray(CustomTypes.UT_VARCHAR2_LIST, options.getCoverageSchemes().toArray()));
        }

        if (options.getSourceMappingOptions() == null) {
            callableStatement.setNull(++paramIdx, Types.ARRAY, CustomTypes.UT_FILE_MAPPINGS);
        } else {
            List<FileMapping> sourceMappings = FileMapper.buildFileMappingList(conn, options.getSourceMappingOptions());

            callableStatement.setArray(
                    ++paramIdx, conn.createOracleArray(CustomTypes.UT_FILE_MAPPINGS, sourceMappings.toArray()));
        }

        if (options.getTestMappingOptions() == null) {
            callableStatement.setNull(++paramIdx, Types.ARRAY, CustomTypes.UT_FILE_MAPPINGS);
        } else {
            List<FileMapping> sourceMappings = FileMapper.buildFileMappingList(conn, options.getTestMappingOptions());

            callableStatement.setArray(
                    ++paramIdx, conn.createOracleArray(CustomTypes.UT_FILE_MAPPINGS, sourceMappings.toArray()));
        }

        if (options.getIncludeObjects().isEmpty()) {
            callableStatement.setNull(++paramIdx, Types.ARRAY, CustomTypes.UT_VARCHAR2_LIST);
        } else {
            callableStatement.setArray(
                    ++paramIdx, conn.createOracleArray(CustomTypes.UT_VARCHAR2_LIST, options.getIncludeObjects().toArray()));
        }

        if (options.getExcludeObjects().isEmpty()) {
            callableStatement.setNull(++paramIdx, Types.ARRAY, CustomTypes.UT_VARCHAR2_LIST);
        } else {
            callableStatement.setArray(
                    ++paramIdx, conn.createOracleArray(CustomTypes.UT_VARCHAR2_LIST, options.getExcludeObjects().toArray()));
        }

        return paramIdx;
    }

    public void execute() throws SQLException {
        callableStatement.execute();
    }

    @Override
    public void close() throws SQLException {
        if (callableStatement != null) {
            callableStatement.close();
        }
    }
}
