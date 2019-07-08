package org.utplsql.api.reporter;

import oracle.sql.Datum;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import org.utplsql.api.compatibility.CompatibilityProxy;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * This class manages the instantiation of reporters.
 * One can register a supplier method for a specific name which will then be callable via createReporter(name)
 * <p>
 * Use the static createEmpty or createDefault methods to get a new instance.
 * We don't allow direct instantiation because we want
 * <ul>
 * <li>Register default ReporterFactoryMethods for Core-Reporters</li>
 * <li>Be able to add more than one ReporterFactory implementation due to backwards-compatibility in future</li>
 * </ul>
 *
 * @author pesse
 */
public final class ReporterFactory implements ORADataFactory {

    private final Map<String, ReporterFactoryMethodInfo> reportFactoryMethodMap = new HashMap<>();

    ReporterFactory() {
    }

    /**
     * Returns a new instance of an empty ReporterFactory with no registered ReporterFactoryMethods
     * Normally, you should be using createDefault-method instead.
     *
     * @return a new ReporterFactory instance
     */
    public static ReporterFactory createEmpty() {
        return new ReporterFactory();
    }

    /**
     * Returns a new instance of a ReporterFactory with the default ReporterFactoryMethods registered.
     * This can depend on the version of utPLSQL, therefore you have to provide a CompatibilityProxy
     *
     * @param proxy Compatibility proxy
     * @return a new ReporterFactory instance with all default ReporterFactoryMethods registered
     */
    public static ReporterFactory createDefault(CompatibilityProxy proxy) {
        ReporterFactory reporterFactory = new ReporterFactory();
        DefaultReporterFactoryMethodRegistrator.registerDefaultReporters(reporterFactory, proxy);
        return reporterFactory;
    }

    /**
     * Registers a creation method for a specified reporter name. Overrides eventually existing creation method
     *
     * @param reporterName  the reporter's name to register
     * @param factoryMethod the method which will return the reporter
     * @param description   the description of the reporter
     * @return Object with information about the registered reporter
     */
    public synchronized ReporterFactoryMethodInfo registerReporterFactoryMethod(String reporterName, BiFunction<String, Object[], ? extends Reporter> factoryMethod, String description) {
        return reportFactoryMethodMap.put(reporterName.toUpperCase(), new ReporterFactoryMethodInfo(factoryMethod, description));
    }

    /**
     * Unregisters a specified reporter name.
     *
     * @param reporterName the reporter's name to unregister
     * @return information about the reporter which was previously registered or null
     */
    public synchronized ReporterFactoryMethodInfo unregisterReporterFactoryMethod(String reporterName) {
        return reportFactoryMethodMap.remove(reporterName.toUpperCase());
    }

    /**
     * Checks whether a given reporter has a registered FactoryMethod or not
     *
     * @param reporterName the reporter's name
     * @return true or false
     */
    public synchronized boolean hasRegisteredFactoryMethodFor(String reporterName) {
        return reportFactoryMethodMap.containsKey(reporterName.toUpperCase());
    }

    /**
     * Returns a new reporter of the given name.
     * If no specific ReporterFactoryMethod is registered, returns a default {Reporter}
     *
     * @param reporterName the reporter's name to create a new instance of
     * @param attributes   attributes from STRUCT
     * @return A reporter
     */
    public Reporter createReporter(String reporterName, @Nullable Object[] attributes) {

        reporterName = reporterName.toUpperCase();
        BiFunction<String, Object[], ? extends Reporter> supplier = DefaultReporter::new;

        if (reportFactoryMethodMap.containsKey(reporterName)) {

            ReporterFactoryMethodInfo ri = reportFactoryMethodMap.get(reporterName);
            if (ri == null) {
                throw new RuntimeException("ReporterFactoryMethodInfo for " + reporterName + " was null");
            }

            supplier = ri.factoryMethod;
        }

        if (supplier == null) {
            throw new RuntimeException("No factory method for " + reporterName);
        }

        return supplier.apply(reporterName, attributes);
    }

    /**
     * Returns a new reporter of the given name (or should do so).
     * If no specific ReporterFactoryMethod is registered, returns a default {Reporter}
     */
    public Reporter createReporter(String reporterName) {
        return createReporter(reporterName, null);
    }

    /**
     * Returns a set of all registered reporter's names
     *
     * @return Set of reporter names
     */
    public Map<String, String> getRegisteredReporterInfo() {
        Map<String, String> descMap = new HashMap<>(reportFactoryMethodMap.size());

        for (Map.Entry<String, ReporterFactoryMethodInfo> entry : reportFactoryMethodMap.entrySet()) {
            descMap.put(entry.getKey(), entry.getValue().description);
        }
        return descMap;
    }

    @Override
    @Nullable
    public ORAData create(Datum d, int sqlType) throws SQLException {
        if (d == null) return null;
        if (d instanceof Struct) {
            String sqlName = ((Struct) d).getSQLTypeName();
            return createReporter(sqlName, ((Struct) d).getAttributes());
        }

        return null;
    }

    public static class ReporterFactoryMethodInfo {
        public final BiFunction<String, Object[], ? extends Reporter> factoryMethod;
        public final String description;

        public ReporterFactoryMethodInfo(BiFunction<String, Object[], ? extends Reporter> factoryMethod, String description) {
            this.factoryMethod = factoryMethod;
            this.description = description;
        }
    }
}
