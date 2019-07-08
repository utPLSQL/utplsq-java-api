package org.utplsql.api.v2.reporters;

/**
 * Created by Pavel Kaplya on 02.03.2019.
 */
public interface Reporter {

    String getName();

    org.utplsql.api.reporter.Reporter getReporterObject();
}
