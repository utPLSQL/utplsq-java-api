package org.utplsql.api.reporter;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import oracle.sql.Datum;
import oracle.sql.ORAData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.outputBuffer.OutputBuffer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

/**
 * This is a basic Reporter implementation, using ORAData interface
 *
 * @author pesse
 */
public abstract class Reporter implements ORAData {

    private static final Logger logger = LoggerFactory.getLogger(Reporter.class);
    protected OutputBuffer outputBuffer;
    private String selfType;
    private String id;
    private Object[] attributes;
    private volatile boolean init = false;
    private final CountDownLatch initFlag = new CountDownLatch(1);

    public Reporter(String typeName, Object[] attributes) {
        setTypeName(typeName);
        setAttributes(attributes);
    }

    public Reporter init(Connection con, CompatibilityProxy compatibilityProxy, ReporterFactory reporterFactory) throws SQLException {

        if (compatibilityProxy == null) {
            compatibilityProxy = new CompatibilityProxy(con);
        }
        if (reporterFactory == null) {
            reporterFactory = new ReporterFactory();
        }

        OracleConnection oraConn = con.unwrap(OracleConnection.class);

        initDbReporter(oraConn, reporterFactory);

        init = true;

        initOutputBuffer(oraConn, compatibilityProxy);

        initFlag.countDown();

        return this;
    }

    public void waitInit() throws InterruptedException {
        initFlag.await();
    }

    public Reporter init(Connection con) throws SQLException {
        return init(con, null, null);
    }

    protected abstract void initOutputBuffer(OracleConnection oraConn, CompatibilityProxy compatibilityProxy) throws SQLException;

    /**
     * Initializes the Reporter from database
     * This is necessary because we set up DefaultOutputBuffer (and maybe other stuff) we don't want to know and care about
     * in the java API. Let's just do the instantiation of the Reporter in the database and map it into this object.
     *
     * @param oraConn
     * @throws SQLException
     */
    private void initDbReporter(OracleConnection oraConn, ReporterFactory reporterFactory) throws SQLException {
        OracleCallableStatement callableStatement = (OracleCallableStatement) oraConn.prepareCall("{? = call " + selfType + "()}");
        callableStatement.registerOutParameter(1, OracleTypes.STRUCT, "UT_REPORTER_BASE");
        callableStatement.execute();

        Reporter obj = (Reporter) callableStatement.getORAData(1, reporterFactory);

        setAttributes(obj.getAttributes());

        logger.debug("Database-reporter initialized, Type: {}, ID: {}", selfType, id);
    }

    protected Object[] getAttributes() {
        return attributes;
    }

    protected void setAttributes(Object[] attributes) {
        if (attributes != null) {
            this.id = DatatypeConverter.printHexBinary((byte[]) attributes[1]);
        }
        this.attributes = attributes;
    }

    public boolean isInit() {
        return init;
    }

    public String getTypeName() {
        return this.selfType;
    }

    protected void setTypeName(String typeName) {
        this.selfType = typeName.replaceAll("[^0-9a-zA-Z_\\.]", "");
    }

    public String getId() {
        return this.id;
    }

    @Override
    public Datum toDatum(Connection c) throws SQLException {
        return (Datum) c.createStruct(getTypeName(), getAttributes());
    }

    public OutputBuffer getOutputBuffer() {
        return outputBuffer;
    }

    private static class DatatypeConverter {
        private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

        static String printHexBinary(byte[] data) {
            StringBuilder r = new StringBuilder(data.length * 2);
            for (byte b : data) {
                r.append(hexCode[(b >> 4) & 0xF]);
                r.append(hexCode[(b & 0xF)]);
            }
            return r.toString();
        }
    }
}
