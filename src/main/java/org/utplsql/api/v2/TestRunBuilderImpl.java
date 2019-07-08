package org.utplsql.api.v2;

import org.utplsql.api.FileMapperOptions;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.*;

import static java.util.Collections.emptyList;

/**
 * Created by Pavel Kaplya on 11.02.2019.
 */
class TestRunBuilderImpl implements TestRunBuilder {
    private List<String> pathList = new ArrayList<>();
    private boolean colorConsole = false;
    private List<String> coverageSchemes;
    private List<String> includeObjects;
    private List<String> excludeObjects;
    private FileMapperOptions sourceMappingOptions;
    private FileMapperOptions testMappingOptions;
    private boolean failOnErrors = false;
    private boolean skipCompatibilityCheck = false;
    private Charset clientCharacterSet = Charset.defaultCharset();
    private boolean randomTestOrder = false;
    @Nullable
    private Integer randomTestOrderSeed;
    private Set<String> tags;

    private final UtplsqlSession utplsqlSession;

    TestRunBuilderImpl(UtplsqlSession utplsqlSession) {
        this.utplsqlSession = utplsqlSession;
    }

    @Override
    public TestRunBuilderImpl paths(List<String> paths) {
        this.pathList.addAll(paths != null ? paths : emptyList());
        return this;
    }

    @Override
    public TestRunBuilderImpl colorConsole(boolean colorConsole) {
        this.colorConsole = colorConsole;
        return this;
    }

    @Override
    public TestRunBuilderImpl addCoverageScheme(String coverageScheme) {
        this.coverageSchemes.add(coverageScheme);
        return this;
    }

    @Override
    public TestRunBuilderImpl includeObjects(List<String> obj) {
        this.includeObjects.addAll(obj != null ? obj : emptyList());
        return this;
    }

    @Override
    public TestRunBuilderImpl excludeObjects(List<String> obj) {
        this.excludeObjects.addAll(obj != null ? obj : emptyList());
        return this;
    }

    @Override
    public TestRunBuilderImpl sourceMappingOptions(FileMapperOptions mapperOptions) {
        this.sourceMappingOptions = mapperOptions;
        return this;
    }

    @Override
    public TestRunBuilderImpl testMappingOptions(FileMapperOptions mapperOptions) {
        this.testMappingOptions = mapperOptions;
        return this;
    }

    @Override
    public TestRunBuilderImpl failOnErrors(boolean failOnErrors) {
        this.failOnErrors = failOnErrors;
        return this;
    }

    @Override
    public TestRunBuilderImpl skipCompatibilityCheck(boolean skipCompatibilityCheck) {
        this.skipCompatibilityCheck = skipCompatibilityCheck;
        return this;
    }

    @Override
    public TestRunBuilderImpl clientCharacterSet(Charset clientCharacterSet) {
        Objects.requireNonNull(clientCharacterSet);
        this.clientCharacterSet = clientCharacterSet;
        return this;
    }

    @Override
    public TestRunBuilderImpl randomTestOrder(boolean randomTestOrder) {
        this.randomTestOrder = randomTestOrder;
        return this;
    }

    @Override
    public TestRunBuilderImpl randomTestOrderSeed(@Nullable Integer randomTestOrderSeed) {
        this.randomTestOrderSeed = randomTestOrderSeed;
        return this;
    }

    @Override
    public TestRunBuilder tags( Set<String> tags ) {
        this.tags = tags;
        return this;
    }

    @Override
    public TestRun build() {
        TestRunOptions options = new TestRunOptions(
                colorConsole,
                coverageSchemes != null ? new ArrayList<>(coverageSchemes) : emptyList(),
                includeObjects != null ? new ArrayList<>(includeObjects) : emptyList(),
                excludeObjects != null ? new ArrayList<>(excludeObjects) : emptyList(),
                sourceMappingOptions,
                testMappingOptions,
                failOnErrors, skipCompatibilityCheck,
                clientCharacterSet,
                randomTestOrder,
                randomTestOrderSeed,
                tags);
        return new TestRunImpl(
                utplsqlSession,
                pathList != null ? Collections.unmodifiableList(pathList) : emptyList(),
                options
        );
    }

}
