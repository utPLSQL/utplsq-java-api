package org.utplsql.api.v2.reporters;

import org.utplsql.api.Version;
import org.utplsql.api.exception.InvalidVersionException;
import org.utplsql.api.exception.UtplsqlException;
import org.utplsql.api.v2.UtplsqlSession;

import java.io.OutputStream;
import java.util.List;

/**
 * Created by Pavel Kaplya on 16.03.2019.
 */
public class ReporterFactoryImpl implements ReporterFactory {
    private final UtplsqlSession session;

    public ReporterFactoryImpl(UtplsqlSession session) {
        this.session = session;
    }

    private TextOutputFetcher outputFetcher() {
        try {
            final Version installedVersion = session.getInstalledVersionInfo();
            if (installedVersion.isGreaterOrEqualThan(Version.V3_1_0)) {
                return new DefaultOutputFetcher();
            } else {
                return new Pre310OutputFetcherImpl();
            }
        } catch (InvalidVersionException e) {
            throw new UtplsqlException(e);
        }
    }

    @Override
    public DocumentationReporter documentationReporter() {
        return new DocumentationReporterImpl(session.getDataSource(), outputFetcher());
    }

    @Override
    public DocumentationReporter documentationReporter(OutputStream outputStream) {
        return null;
    }

    @Override
    public CoverageHTMLReporter coverageHtmlReporter() {
        return null;
    }

    @Override
    public CoverageSonarReporter coverageSonarReporter() {
        return null;
    }

    @Override
    public CoverageCoberturaReporter coverageCuberturaReporter() {
        return null;
    }

    @Override
    public CoverallsReporter coverallsReporter() {
        return null;
    }

    @Override
    public SonarTestReporter sonarTestReporter() {
        return null;
    }

    @Override
    public TeamcityReporter teamcityReporter() {
        return null;
    }

    @Override
    public XUnitReporter xUnitReporter() {
        return null;
    }

    @Override
    public CustomReporter customReporter(String reporterTypeName) {
        return null;
    }

    @Override
    public List<Reporter> getAvailableReporters() {
        return null;
    }
}
