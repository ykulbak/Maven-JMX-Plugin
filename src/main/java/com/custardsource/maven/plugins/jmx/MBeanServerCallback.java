package com.custardsource.maven.plugins.jmx;

import javax.management.MBeanServerConnection;

/**
 * Callback for Interacting with an MBeanServer.
 */
public interface MBeanServerCallback {

    void doWithMBeanServer(MBeanServerConnection mbeanServerConnection) throws Exception;
}
