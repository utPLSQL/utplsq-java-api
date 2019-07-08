package org.utplsql.api;

import org.junit.jupiter.api.Test;
import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.exception.InvalidVersionException;
import org.utplsql.api.reporter.CoreReporters;
import org.utplsql.api.reporter.DefaultReporter;
import org.utplsql.api.reporter.DocumentationReporter;
import org.utplsql.api.reporter.Reporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration-test for OutputBuffers
 *
 * @author viniciusam
 * @author pesse
 */
class OutputBufferIT extends AbstractDatabaseTest {

    private Reporter createReporter() throws SQLException {
        Reporter reporter = new DocumentationReporter().init(newConnection());
        System.out.println("Reporter ID: " + reporter.getId());
        return reporter;
    }

    @Test
    void printAvailableLines() throws SQLException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            final Reporter reporter = createReporter();

            Future<Object> task1 = executorService.submit(() -> {
                try {
                    new TestRunner()
                            .addPath(getUser())
                            .addReporter(reporter)
                            .run(getConnection());

                    return Boolean.TRUE;
                } catch (SQLException e) {
                    return e;
                }
            });

            Future<Object> task2 = executorService.submit(() -> {
                FileOutputStream fileOutStream = null;
                File outFile = new File("output.txt");
                try {
                    fileOutStream = new FileOutputStream(outFile);

                    List<PrintStream> printStreams = new ArrayList<>();
                    printStreams.add(System.out);
                    printStreams.add(new PrintStream(fileOutStream));

                    reporter.getOutputBuffer()
                            .setFetchSize(1)
                            .printAvailable(newConnection(), printStreams);

                    return Boolean.TRUE;
                } catch (SQLException e) {
                    return e;
                } finally {
                    if (fileOutStream != null) {
                        fileOutStream.close();
                        outFile.delete();
                    }
                }
            });

            executorService.shutdown();
            executorService.awaitTermination(60, TimeUnit.MINUTES);

            Object res1 = task1.get();
            Object res2 = task2.get();

            if (res1 instanceof Exception) {
                fail((Exception) res1);
            }

            if (res2 instanceof Exception) {
                fail((Exception) res2);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    void fetchAllLines() throws SQLException {
        final Reporter reporter = createReporter();
        new TestRunner()
                .addPath(getUser())
                .addReporter(reporter)
                .run(getConnection());

        List<String> outputLines = reporter.getOutputBuffer().fetchAll(getConnection());

        assertThat(outputLines, not(emptyIterable()));
    }

    @Test
    void getOutputFromSonarReporter() throws SQLException {
        Reporter reporter = new DefaultReporter(CoreReporters.UT_COVERAGE_HTML_REPORTER.name(), null).init(newConnection());

        new TestRunner()
                .addPath(getUser())
                .addReporter(reporter)
                .run(getConnection());

        List<String> outputLines = reporter.getOutputBuffer().fetchAll(getConnection());

        assertThat(outputLines, not(emptyIterable()));
    }

    @Test
    void sonarReporterHasEncodingSet() throws SQLException, InvalidVersionException {
        CompatibilityProxy proxy = new CompatibilityProxy(newConnection());

        if (proxy.getUtPlsqlVersion().isGreaterOrEqualThan(Version.V3_1_2)) {
            Reporter reporter = new DefaultReporter(CoreReporters.UT_SONAR_TEST_REPORTER.name(), null).init(getConnection());

            TestRunner tr = new TestRunner()
                    .addPath(getUser())
                    .addReporter(reporter);

            tr.run(getConnection());

            List<String> outputLines = reporter.getOutputBuffer().fetchAll(getConnection());

            String defaultCharset = Charset.defaultCharset().toString();
            String actualCharset = outputLines.get(0).replaceAll("(.*)encoding=\"([^\"]+)\"(.*)", "$2");

            assertEquals(defaultCharset.toLowerCase(), actualCharset.toLowerCase());
        }

    }
}
