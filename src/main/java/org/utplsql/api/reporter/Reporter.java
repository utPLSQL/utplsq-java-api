package org.utplsql.api.reporter;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import oracle.sql.Datum;
import oracle.sql.ORAData;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

import java.sql.*;

/** This is a basic Reporter implementation, using ORAData interface
 *
 * @author pesse
 */
public class Reporter implements ORAData {

    private String selfType;
    private String id;
    private Object[] attributes;
    private boolean hasOutput = false;
    private boolean init = false;

    public Reporter( String typeName, Object[] attributes ) {
        setTypeName(typeName);
        setAttributes( attributes );
    }

    private void setTypeName( String typeName ) {
        this.selfType = typeName.replaceAll("[^0-9a-zA-Z_]", "");
    }

    public void setParameters( Object[] parameters ) {
        // Empty method
    }

    public Reporter init( Connection con ) throws SQLException {

        OracleConnection oraConn = con.unwrap(OracleConnection.class);

        initDbReporter( oraConn );
        initHasOutput( oraConn );

        init = true;

        return this;
    }

    /** Initializes the Reporter from database
     * This is necessary because we set up OutputBuffer (and maybe other stuff) we don't want to know and care about
     * in the java API. Let's just do the instantiation of the Reporter in the database and map it into this object.
     *
     * @param oraConn
     * @throws SQLException
     */
    private void initDbReporter( OracleConnection oraConn ) throws SQLException {
        OracleCallableStatement callableStatement = (OracleCallableStatement) oraConn.prepareCall("{? = call " + selfType + "()}");
        callableStatement.registerOutParameter(1, OracleTypes.STRUCT, "UT_REPORTER_BASE");
        callableStatement.execute();

        Reporter obj = (Reporter) callableStatement.getORAData(1, ReporterFactory.getInstance());

        setAttributes(obj.getAttributes());
    }

    /** Checks whether the Reporter has an output or not
     *
     * @param oraConn
     * @throws SQLException
     */
    private void initHasOutput( OracleConnection oraConn ) throws SQLException {
        OracleCallableStatement cstmt = (OracleCallableStatement)oraConn.prepareCall("{? = call ?.has_output()}");

        cstmt.registerOutParameter(1, OracleTypes.INTEGER);
        cstmt.setORAData(2, this);
        cstmt.execute();

        Integer i = cstmt.getInt(1);
        if ( i != null && i == 1 ) {
            hasOutput = true;
        }
        else {
            hasOutput = false;
        }
    }

    protected void setAttributes(Object[] attributes ) {
        if (attributes != null) {
            this.id = String.valueOf(attributes[1]);
        }
        this.attributes = attributes;
    }

    protected Object[] getAttributes() {
        return attributes;
    }

    public boolean hasOutput() {
        return hasOutput;
    }

    public boolean isInit() {
        return init;
    }

    public String getTypeName() {
        return this.selfType;
    }

    public String getId() {
        return this.id;
    }


    public Datum toDatum(Connection c) throws SQLException
    {
        StructDescriptor sd =
                StructDescriptor.createDescriptor(getTypeName(), c);
        return new STRUCT(sd, c, getAttributes());
    }

}
