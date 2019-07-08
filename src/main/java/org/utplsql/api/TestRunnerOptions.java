package org.utplsql.api;

import org.utplsql.api.reporter.Reporter;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

/**
 * Created by Pavel Kaplya on 16.03.2019.
 */
public interface TestRunnerOptions {
    List<String> getPathList();

    List<Reporter> getReporterList();

    List<String> getCoverageSchemes();

    List<String> getIncludeObjects();

    List<String> getExcludeObjects();

    boolean isColorConsole();

    FileMapperOptions getSourceMappingOptions();

    FileMapperOptions getTestMappingOptions();

    boolean isFailOnErrors();

    boolean isSkipCompatibilityCheck();

    Charset getClientCharacterSet();
    boolean isRandomTestOrder();

    @Nullable
    Integer getRandomTestOrderSeed();

    Set<String> getTags();

    String getTagsAsString();
}
