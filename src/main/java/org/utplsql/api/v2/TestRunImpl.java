package org.utplsql.api.v2;

import oracle.jdbc.OracleConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utplsql.api.FileMapperOptions;
import org.utplsql.api.TestRunner;
import org.utplsql.api.TestRunnerOptions;
import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.db.DefaultDatabaseInformation;
import org.utplsql.api.exception.SomeTestsFailedException;
import org.utplsql.api.exception.UtPLSQLNotInstalledException;
import org.utplsql.api.exception.UtplsqlConfigurationException;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.api.testRunner.TestRunnerStatement;
import org.utplsql.api.v2.reporters.Reporter;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Created by Pavel Kaplya on 08.03.2019.
 */
class TestRunImpl implements TestRun {

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    private final List<String> paths;
    private final TestRunOptions options;
    private final UtplsqlSession utplsqlSession;
    private final List<Reporter> reporters = new ArrayList<>();

    TestRunImpl(UtplsqlSession utplsqlSession, List<String> paths, TestRunOptions options) {
        this.utplsqlSession = utplsqlSession;
        this.paths = paths;
        this.options = options;
    }

    @Override
    public TestRun addReporter(Reporter reporter) {
        Objects.requireNonNull(reporter);
        reporters.add(reporter);
        return this;
    }

    @Override
    public CompletableFuture<TestRunResults> executeAsync() throws UtplsqlConfigurationException {
        if (reporters.isEmpty()) {
            throw new UtplsqlConfigurationException("Reporter list is empty");
        }

        return CompletableFuture.supplyAsync(this::execute);

    }

    @Override
    public TestRunResults execute() {
        try (OracleConnection conn = utplsqlSession.getConnection()) {

            DefaultDatabaseInformation defaultDatabaseInformation = new DefaultDatabaseInformation();
            CompatibilityProxy compatibilityProxy = new CompatibilityProxy(conn, options.isSkipCompatibilityCheck(), defaultDatabaseInformation);
            logger.info("Running on utPLSQL {}", compatibilityProxy.getDatabaseVersion());

            // First of all check version compatibility
            compatibilityProxy.failOnNotCompatible();

            logger.info("TestRun initialized");

            if (reporters.isEmpty()) {
                reporters.add(utplsqlSession.reporterFactory().documentationReporter());
            }

            ReporterFactory reporterORADataFactory = ReporterFactory.createDefault(compatibilityProxy);
            for (Reporter reporter : reporters) {
                reporter.getReporterObject().init(conn, compatibilityProxy, reporterORADataFactory);
            }

            String currentSchema = defaultDatabaseInformation.getCurrentSchema(conn);
            try (TestRunnerStatement testRunnerStatement = compatibilityProxy.getTestRunnerStatement(getOptions(currentSchema), conn)) {
                logger.info("Running tests");
                testRunnerStatement.execute();
                logger.info("Running tests finished.");
            } catch (SQLException e) {
                if (e.getErrorCode() == SomeTestsFailedException.ERROR_CODE) {
                    throw new SomeTestsFailedException(e.getMessage(), e);
                } else if (e.getErrorCode() == UtPLSQLNotInstalledException.ERROR_CODE) {
                    throw new UtPLSQLNotInstalledException(e);
                } else {
                    throw e;
                }
            }

            return new TestRunResultsImpl();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private TestRunnerOptions getOptions(String currentSchema) {
        return new TestRunnerOptions() {
            @Override
            public List<String> getPathList() {
                return !paths.isEmpty() ? paths : Collections.singletonList(currentSchema);
            }

            @Override
            public List<org.utplsql.api.reporter.Reporter> getReporterList() {
                return reporters.stream().map(Reporter::getReporterObject).collect(Collectors.toList());
            }

            @Override
            public List<String> getCoverageSchemes() {
                return options.getCoverageSchemes();
            }

            @Override
            public List<String> getIncludeObjects() {
                return options.getIncludeObjects();
            }

            @Override
            public List<String> getExcludeObjects() {
                return options.getExcludeObjects();
            }

            @Override
            public boolean isColorConsole() {
                return options.isColorConsole();
            }

            @Override
            public FileMapperOptions getSourceMappingOptions() {
                return options.getSourceMappingOptions();
            }

            @Override
            public FileMapperOptions getTestMappingOptions() {
                return options.getTestMappingOptions();
            }

            @Override
            public boolean isFailOnErrors() {
                return options.isFailOnErrors();
            }

            @Override
            public boolean isSkipCompatibilityCheck() {
                return options.isSkipCompatibilityCheck();
            }

            @Override
            public Charset getClientCharacterSet() {
                return options.getClientCharacterSet();
            }

            @Override
            public boolean isRandomTestOrder() {
                return options.isRandomTestOrder();
            }

            @Nullable
            @Override
            public Integer getRandomTestOrderSeed() {
                return options.getRandomTestOrderSeed();
            }

            @Override
            public Set<String> getTags() { return options.getTags(); }

            @Override
            public String getTagsAsString() { return options.getTagsAsString(); }

        };
    }

}
