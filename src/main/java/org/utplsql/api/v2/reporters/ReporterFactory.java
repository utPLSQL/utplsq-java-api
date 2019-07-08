package org.utplsql.api.v2.reporters;

import java.io.OutputStream;
import java.util.List;

/**
 * Created by Pavel Kaplya on 02.03.2019.
 */
public interface ReporterFactory {
    DocumentationReporter documentationReporter();
    DocumentationReporter documentationReporter(OutputStream outputStream);

    CoverageHTMLReporter coverageHtmlReporter();
    CoverageSonarReporter coverageSonarReporter();
    CoverageCoberturaReporter coverageCuberturaReporter();
    CoverallsReporter coverallsReporter();

    SonarTestReporter sonarTestReporter();
    TeamcityReporter teamcityReporter();
    XUnitReporter xUnitReporter();

    CustomReporter customReporter(String reporterTypeName);

    List<Reporter> getAvailableReporters();
}
