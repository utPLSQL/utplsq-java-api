package org.utplsql.api.testRunner;

import org.utplsql.api.TestRunnerOptions;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides the call to run tests for the most actual Framework version.
 * Includes fail on error
 *
 * @author pesse
 */
class Pre317TestRunnerStatement extends AbstractTestRunnerStatement {

    public Pre317TestRunnerStatement(TestRunnerOptions options, Connection connection) throws SQLException {
        super(options, connection);
    }

    @Override
    protected String getSql() {
        // Workaround because Oracle JDBC doesn't support passing boolean to stored procedures.
        String colorConsoleStr = Boolean.toString(options.isColorConsole());
        String failOnErrors = Boolean.toString(options.isFailOnErrors());

        return
                "BEGIN " +
                        "ut_runner.run(" +
                        "a_paths                => ?, " +
                        "a_reporters            => ?, " +
                        "a_color_console        => " + colorConsoleStr + ", " +
                        "a_coverage_schemes     => ?, " +
                        "a_source_file_mappings => ?, " +
                        "a_test_file_mappings   => ?, " +
                        "a_include_objects      => ?, " +
                        "a_exclude_objects      => ?, " +
                        "a_fail_on_errors       => " + failOnErrors + ", " +
                        "a_client_character_set => ?); " +
                        "END;";
    }

    @Override
    protected int createStatement() throws SQLException {
        int curParamIdx = super.createStatement();

        callableStatement.setString(++curParamIdx, options.getClientCharacterSet().toString());

        return curParamIdx;
    }
}
