package org.utplsql.api.testRunner;

import org.junit.jupiter.api.Test;
import org.utplsql.api.AbstractDatabaseTest;
import org.utplsql.api.TestRunnerOptionsImpl;
import org.utplsql.api.Version;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestRunnerStatementProviderIT extends AbstractDatabaseTest {

    AbstractTestRunnerStatement getTestRunnerStatementForVersion( Version version ) throws SQLException {
        return (AbstractTestRunnerStatement)TestRunnerStatementProvider.getCompatibleTestRunnerStatement(version, new TestRunnerOptionsImpl(), getConnection());
    }

    @Test
    void testGettingPre303Version() throws SQLException {
        AbstractTestRunnerStatement stmt = getTestRunnerStatementForVersion(Version.V3_0_2);
        assertEquals(Pre303TestRunnerStatement.class, stmt.getClass());
        assertThat(stmt.getSql(), not(containsString("a_fail_on_errors")));
        assertThat(stmt.getSql(), not(containsString("a_client_character_set")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order_seed")));
        assertThat(stmt.getSql(), not(containsString("a_tags")));
    }


    @Test
    void testGettingPre312Version_from_303() throws SQLException {
        AbstractTestRunnerStatement stmt = getTestRunnerStatementForVersion(Version.V3_0_3);
        assertEquals(Pre312TestRunnerStatement.class, stmt.getClass());
        assertThat(stmt.getSql(), containsString("a_fail_on_errors"));
        assertThat(stmt.getSql(), not(containsString("a_client_character_set")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order_seed")));
        assertThat(stmt.getSql(), not(containsString("a_tags")));
    }

    @Test
    void testGettingPre312Version_from_311() throws SQLException {
        AbstractTestRunnerStatement stmt = getTestRunnerStatementForVersion(Version.V3_1_1);
        assertThat(stmt, instanceOf(Pre312TestRunnerStatement.class));
        assertThat(stmt.getSql(), containsString("a_fail_on_errors"));
        assertThat(stmt.getSql(), not(containsString("a_client_character_set")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order_seed")));
        assertThat(stmt.getSql(), not(containsString("a_tags")));
    }

    @Test
    void testGettingPre317Version_from_312() throws SQLException {
        AbstractTestRunnerStatement stmt = getTestRunnerStatementForVersion(Version.V3_1_2);
        assertThat(stmt, instanceOf(Pre317TestRunnerStatement.class));
        assertThat(stmt.getSql(), containsString("a_fail_on_errors"));
        assertThat(stmt.getSql(), containsString("a_client_character_set"));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order_seed")));
        assertThat(stmt.getSql(), not(containsString("a_tags")));
    }

    @Test
    void testGettingPre317Version_from_316() throws SQLException {
        AbstractTestRunnerStatement stmt = getTestRunnerStatementForVersion(Version.V3_1_6);
        assertThat(stmt, instanceOf(Pre317TestRunnerStatement.class));
        assertThat(stmt.getSql(), containsString("a_fail_on_errors"));
        assertThat(stmt.getSql(), containsString("a_client_character_set"));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order_seed")));
        assertThat(stmt.getSql(), not(containsString("a_tags")));
    }

    @Test
    void testGettingActualVersion_from_latest() throws SQLException {
        AbstractTestRunnerStatement stmt = getTestRunnerStatementForVersion(Version.LATEST);
        assertThat(stmt, instanceOf(ActualTestRunnerStatement.class));
        assertThat(stmt.getSql(), containsString("a_fail_on_errors"));
        assertThat(stmt.getSql(), containsString("a_client_character_set"));
        assertThat(stmt.getSql(), containsString("a_random_test_order"));
        assertThat(stmt.getSql(), containsString("a_random_test_order_seed"));
        assertThat(stmt.getSql(), containsString("a_tags"));
    }
}
