package org.utplsql.api.v2.reporters;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Pavel Kaplya on 01.05.2019.
 * Reporter producing its result as plain text
 */
public interface TextualReporter {

    /**
     * Print the lines as soon as they are produced and call the callback passing the new line.
     *
     * @param onLineFetched the callback to be called
     */
    void onReportLine(Consumer<String> onLineFetched);

    /**
     * Get all lines from output buffer and return it as a list of strings.
     *
     * @return the lines
     */
    List<String> getFullReport();

}
