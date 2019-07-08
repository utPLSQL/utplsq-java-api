package org.utplsql.api.v2.reporters;

import oracle.jdbc.OracleConnection;
import org.utplsql.api.exception.ReporterConsumedException;
import org.utplsql.api.reporter.CoreReporters;
import org.utplsql.api.reporter.Reporter;
import org.utplsql.api.v2.UtplsqlDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Pavel Kaplya on 15.03.2019.
 */
public class DocumentationReporterImpl extends AbstractReporter implements DocumentationReporter {

    private final static String REPORTER_NAME = CoreReporters.UT_DOCUMENTATION_REPORTER.name();
    private final org.utplsql.api.reporter.DocumentationReporter reporterObject;
    private final TextOutputFetcher fetcher;
    private volatile boolean consumed = false;

    DocumentationReporterImpl(UtplsqlDataSource dataSource, TextOutputFetcher fetcher) {
        super(REPORTER_NAME, dataSource);
        this.reporterObject = new org.utplsql.api.reporter.DocumentationReporter();
        this.fetcher = fetcher;
    }

    @Override
    public Reporter getReporterObject() {
        return reporterObject;
    }

    @Override
    public void onReportLine(Consumer<String> onLineFetched) {
        synchronized (this) {
            if (!consumed) {
                consumed = true;
            } else {
                throw new ReporterConsumedException();
            }
        }
        try {
            reporterObject.waitInit();
        } catch (InterruptedException e) {
            return;
        }
        try (OracleConnection conn = dataSource.getConnection()) {
            fetcher.fetchLine(conn, reporterObject, onLineFetched);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getFullReport() {
        List<String> output = new ArrayList<>();
        onReportLine(output::add);
        return output;
    }
}
