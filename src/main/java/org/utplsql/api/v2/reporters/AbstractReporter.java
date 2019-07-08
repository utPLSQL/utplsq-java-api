package org.utplsql.api.v2.reporters;

import org.utplsql.api.v2.UtplsqlDataSource;

/**
 * Created by Pavel Kaplya on 16.03.2019.
 */
public abstract class AbstractReporter implements Reporter {

    private final String name;
    protected final UtplsqlDataSource dataSource;

    protected AbstractReporter(String name, UtplsqlDataSource dataSource) {
        this.name = name;
        this.dataSource = dataSource;
    }

    @Override
    public String getName() {
        return name;
    }
}
