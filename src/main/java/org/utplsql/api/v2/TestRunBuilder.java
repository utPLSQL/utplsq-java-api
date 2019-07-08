package org.utplsql.api.v2;

import org.utplsql.api.FileMapperOptions;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

/**
 * Created by Pavel Kaplya on 11.02.2019.
 */
public interface TestRunBuilder {

    TestRunBuilder paths(List<String> paths);

    TestRunBuilder colorConsole(boolean colorConsole);

    TestRunBuilder addCoverageScheme(String coverageScheme);

    TestRunBuilder includeObjects(List<String> obj);

    TestRunBuilder excludeObjects(List<String> obj);

    TestRunBuilder sourceMappingOptions(FileMapperOptions mapperOptions);

    TestRunBuilder testMappingOptions(FileMapperOptions mapperOptions);

    TestRunBuilder failOnErrors(boolean failOnErrors);

    TestRunBuilder skipCompatibilityCheck(boolean skipCompatibilityCheck);

    TestRunBuilder clientCharacterSet(Charset clientCharacterSet);

    TestRunBuilder randomTestOrder(boolean randomTestOrder);

    TestRunBuilder randomTestOrderSeed(@Nullable Integer randomTestOrderSeed);

    TestRunBuilder tags(Set<String> tags);

    TestRun build();
}
