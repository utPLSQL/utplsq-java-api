package org.utplsql.api.testRunner;

import org.utplsql.api.TestRunnerOptions;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * TestRunner-Statement for Framework version before 3.0.3
 * Does not know about failOnErrors option
 *
 * @author pesse
 */
class Pre303TestRunnerStatement extends AbstractTestRunnerStatement {

    public Pre303TestRunnerStatement(TestRunnerOptions options, Connection conn) throws SQLException {
        super(options, conn);
    }

    @Override
    protected String getSql() {
        // Workaround because Oracle JDBC doesn't support passing boolean to stored procedures.
        String colorConsoleStr = Boolean.toString(options.isColorConsole());

        return "BEGIN " +
                "ut_runner.run(" +
                "a_paths            => ?, " +
                "a_reporters        => ?, " +
                "a_color_console    => " + colorConsoleStr + ", " +
                "a_coverage_schemes => ?, " +
                "a_source_file_mappings => ?, " +
                "a_test_file_mappings   => ?, " +
                "a_include_objects  => ?, " +
                "a_exclude_objects  => ?); " +
                "END;";
    }
}
