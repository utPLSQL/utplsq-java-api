package org.utplsql.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.db.DatabaseInformation;
import org.utplsql.api.db.DefaultDatabaseInformation;
import org.utplsql.api.exception.OracleCreateStatmenetStuckException;
import org.utplsql.api.exception.SomeTestsFailedException;
import org.utplsql.api.exception.UtPLSQLNotInstalledException;
import org.utplsql.api.reporter.DocumentationReporter;
import org.utplsql.api.reporter.Reporter;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.api.testRunner.TestRunnerStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Vinicius Avellar on 12/04/2017.
 *
 * @author Vinicius Avellar
 * @author pesse
 */
public class TestRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    private final TestRunnerOptionsImpl options = new TestRunnerOptionsImpl();
    private final List<String> reporterNames = new ArrayList<>();
    private CompatibilityProxy compatibilityProxy;
    private ReporterFactory reporterFactory;

    public TestRunner addPath(String path) {
        options.getPathList().add(path);
        return this;
    }

    public TestRunner addPathList(List<String> paths) {
        options.getPathList().addAll(paths);
        return this;
    }

    public TestRunner addReporter(Reporter reporter) {
        options.getReporterList().add(reporter);
        return this;
    }

    public TestRunner addReporter(String reporterName) {
        if (reporterFactory != null) {
            options.getReporterList().add(reporterFactory.createReporter(reporterName));
        } else {
            reporterNames.add(reporterName);
        }
        return this;
    }

    public TestRunner colorConsole(boolean colorConsole) {
        options.colorConsole = colorConsole;
        return this;
    }

    public TestRunner addReporterList(List<Reporter> reporterList) {
        options.getReporterList().addAll(reporterList);
        return this;
    }

    public TestRunner addCoverageScheme(String coverageScheme) {
        options.getCoverageSchemes().add(coverageScheme);
        return this;
    }

    public TestRunner includeObject(String obj) {
        options.getIncludeObjects().add(obj);
        return this;
    }

    public TestRunner excludeObject(String obj) {
        options.getExcludeObjects().add(obj);
        return this;
    }

    public TestRunner includeObjects(List<String> obj) {
        options.getIncludeObjects().addAll(obj);
        return this;
    }

    public TestRunner excludeObjects(List<String> obj) {
        options.getExcludeObjects().addAll(obj);
        return this;
    }

    public TestRunner sourceMappingOptions(FileMapperOptions mapperOptions) {
        options.sourceMappingOptions = mapperOptions;
        return this;
    }

    public TestRunner testMappingOptions(FileMapperOptions mapperOptions) {
        options.testMappingOptions = mapperOptions;
        return this;
    }

    public TestRunner failOnErrors(boolean failOnErrors) {
        options.failOnErrors = failOnErrors;
        return this;
    }

    public TestRunner skipCompatibilityCheck(boolean skipCompatibilityCheck) {
        options.skipCompatibilityCheck = skipCompatibilityCheck;
        return this;
    }

    public TestRunner setReporterFactory(ReporterFactory reporterFactory) {
        this.reporterFactory = reporterFactory;
        return this;
    }

    public TestRunner randomTestOrder(boolean randomTestOrder ) {
        this.options.randomTestOrder = randomTestOrder;
        return this;
    }

    public TestRunner randomTestOrderSeed( Integer seed ) {
        this.options.randomTestOrderSeed = seed;
        if ( seed != null ) this.options.randomTestOrder = true;
        return this;
    }

    public TestRunner addTag( String tag ) {
        this.options.tags.add(tag);
        return this;
    }

    public TestRunner addTags(Collection<String> tags) {
        this.options.tags.addAll(tags);
        return this;
    }

    public TestRunnerOptions getOptions() { return options; }

    private void delayedAddReporters() {
        if (reporterFactory != null) {
            reporterNames.forEach(this::addReporter);
        } else {
            throw new IllegalStateException("ReporterFactory must be set to add delayed Reporters!");
        }
    }

    private void handleException(Throwable e) throws SQLException {
        // Just pass exceptions already categorized
        if ( e instanceof UtPLSQLNotInstalledException ) throw (UtPLSQLNotInstalledException)e;
        else if ( e instanceof SomeTestsFailedException ) throw (SomeTestsFailedException)e;
        else if ( e instanceof OracleCreateStatmenetStuckException ) throw (OracleCreateStatmenetStuckException)e;
        // Categorize exceptions
        else if (e instanceof SQLException) {
            SQLException sqlException = (SQLException) e;
            if (sqlException.getErrorCode() == SomeTestsFailedException.ERROR_CODE) {
                throw new SomeTestsFailedException(sqlException.getMessage(), e);
            } else if (((SQLException) e).getErrorCode() == UtPLSQLNotInstalledException.ERROR_CODE) {
                throw new UtPLSQLNotInstalledException(sqlException);
            } else {
                throw sqlException;
            }
        } else {
            throw new SQLException("Unknown exception, wrapping: " + e.getMessage(), e);
        }
    }

    public void run(Connection conn) throws SQLException {

        logger.info("TestRunner initialized");

        DatabaseInformation databaseInformation = new DefaultDatabaseInformation();

        if ( options.isSkipCompatibilityCheck() ) {
            compatibilityProxy = new CompatibilityProxy(conn, Version.LATEST, databaseInformation);
        } else {
            compatibilityProxy = new CompatibilityProxy(conn, databaseInformation);
        }
        logger.info("Running on utPLSQL {}", compatibilityProxy.getVersionDescription());

        if (reporterFactory == null) {
            reporterFactory = ReporterFactory.createDefault(compatibilityProxy);
        }

        delayedAddReporters();

        // First of all check version compatibility
        compatibilityProxy.failOnNotCompatible();

        logger.info("Initializing reporters");
        for (Reporter r : options.getReporterList()) {
            validateReporter(conn, r);
        }

        if (options.getPathList().isEmpty()) {
            options.getPathList().add(databaseInformation.getCurrentSchema(conn));
        }

        if (options.getReporterList().isEmpty()) {
            logger.info("No reporter given so choosing ut_documentation_reporter");
            options.getReporterList().add(new DocumentationReporter().init(conn));
        }

        TestRunnerStatement testRunnerStatement = null;
        try {
            testRunnerStatement = initStatementWithTimeout(conn);
            logger.info("Running tests");
            testRunnerStatement.execute();
            logger.info("Running tests finished.");
            testRunnerStatement.close();
        } catch (OracleCreateStatmenetStuckException e) {
            // Don't close statement in this case for it will be stuck, too
            throw e;
        } catch (SQLException e) {
            if (testRunnerStatement != null) testRunnerStatement.close();
            handleException(e);
        }
    }

    private TestRunnerStatement initStatementWithTimeout( Connection conn ) throws OracleCreateStatmenetStuckException, SQLException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<TestRunnerStatement> callable = () -> compatibilityProxy.getTestRunnerStatement(options, conn);
        Future<TestRunnerStatement> future = executor.submit(callable);

        // We want to leave the statement open in case of stuck scenario
        TestRunnerStatement testRunnerStatement = null;
        try {
            testRunnerStatement = future.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.error("Detected Oracle driver stuck during Statement initialization");
            executor.shutdownNow();
            throw new OracleCreateStatmenetStuckException(e);
        } catch (InterruptedException e) {
            handleException(e);
        } catch (ExecutionException e) {
            handleException(e.getCause());
        }

        return testRunnerStatement;
    }

    /**
     * Check if the reporter was initialized, if not call reporter.init.
     *
     * @param conn     the database connection
     * @param reporter the reporter
     * @throws SQLException any sql exception
     */
    private void validateReporter(Connection conn, Reporter reporter) throws SQLException {
        if (!reporter.isInit() || reporter.getId() == null || reporter.getId().isEmpty()) {
            reporter.init(conn, compatibilityProxy, reporterFactory);
        }
    }

    /**
     * Returns the databaseVersion the TestRunner was run against
     *
     * @return Version of the database the TestRunner was run against
     */
    public Version getUsedDatabaseVersion() {
        if (compatibilityProxy != null) {
            return compatibilityProxy.getUtPlsqlVersion();
        } else {
            return null;
        }
    }

}
