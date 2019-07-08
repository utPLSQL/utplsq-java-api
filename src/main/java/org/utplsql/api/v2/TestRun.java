package org.utplsql.api.v2;

import org.utplsql.api.exception.UtplsqlConfigurationException;
import org.utplsql.api.v2.reporters.Reporter;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Pavel Kaplya on 02.03.2019.
 */
public interface TestRun {
    TestRun addReporter(Reporter reporterFactory);

    TestRunResults execute() throws UtplsqlConfigurationException;

    CompletableFuture<TestRunResults> executeAsync() throws UtplsqlConfigurationException;
}
