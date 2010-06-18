/*
 * Copyright 2010 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.custardsource.maven.plugins.jmx;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.apache.commons.lang.ArrayUtils;
import org.apache.maven.plugin.AbstractMojo;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides common JMX infrastructure to subclasses.
 */
public abstract class AbstractJmxMojo extends AbstractMojo {

    private static final String AGENT_RELATIVE_PATH = File.separator + "lib" + File.separator + "management-agent.jar";
    private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

    /**
     * Lists all vms that are <b>probably</b> an instance of babylon.
     *
     * @return a list of vm instances.
     */
    public List<JMXServiceURL> listBabylonServiceUrls() throws IOException {
        List<JMXServiceURL> allURLs = listAllAccessibleUrls();
        List<JMXServiceURL> babylonServiceUrls = new ArrayList<JMXServiceURL>();
        for (JMXServiceURL url : allURLs) {
            MBeanServerConnection connection = connect(url);
            if (ArrayUtils.lastIndexOf(connection.getDomains(), "Aconex") >= 0) {
                babylonServiceUrls.add(url);
            }
        }
        return babylonServiceUrls;
    }

    private List<JMXServiceURL> listAllAccessibleUrls() throws IOException {
        List<JMXServiceURL> allURLs = new ArrayList<JMXServiceURL>();

        for (VirtualMachineDescriptor desc : VirtualMachine.list()) {
            VirtualMachine vm = attach(desc);
            if (vm != null) {
                String connectorAddress = getConnectorAddress(desc, vm);
                if (connectorAddress != null) {
                    allURLs.add(new JMXServiceURL(connectorAddress));
                }
            }
        }
        return allURLs;
    }

    private VirtualMachine attach(VirtualMachineDescriptor descriptor) throws IOException {
        try {
            return VirtualMachine.attach(descriptor.id());
        } catch (AttachNotSupportedException e) {
            getLog().debug("Can't attach to " + descriptor.displayName(), e);
        } catch (IOException e) {
            getLog().debug("Can't attach to " + descriptor.displayName(), e);
        }
        return null;
    }

    private String getConnectorAddress(VirtualMachineDescriptor descriptor, VirtualMachine vm) {
        String connectorAddress = null;
        try {
            connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            if (connectorAddress == null) {
                String agent = vm.getSystemProperties().getProperty("java.home") + AGENT_RELATIVE_PATH;
                try {
                    vm.loadAgent(agent);
                } catch (AgentLoadException e) {
                    getLog().debug("Can't load agent to " + descriptor.displayName(), e);
                } catch (AgentInitializationException e) {
                    getLog().debug("Can't initialize agent in " + descriptor.displayName(), e);
                }

                // agent is started, get the connector address
                return vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            }
        } catch (IOException e) {
            getLog().debug("IO exception while getting connector address for  " + vm.id(), e);
        }
        return null;
    }

    private MBeanServerConnection connect(JMXServiceURL jmxServiceURL) throws IOException {
        JMXConnector connector = JMXConnectorFactory.connect(jmxServiceURL);
        connector.connect();
        return connector.getMBeanServerConnection();
    }

    /*public void close(MBeanServerConnection connection) throws IOException {
        ListIterator<ConnectorConnectionPair> iterator = connectorConnectionsList.listIterator();
        while (iterator.hasNext()) {
            ConnectorConnectionPair pair = iterator.next();
            if (pair.getConnection() == connection) {
                pair.getConnector().close();
                iterator.remove();
                return;
            }
        }
        throw new IllegalArgumentException("Unable to find connector for " + connection);
    }*/

    public Object invoke(MBeanServerConnection connection, ObjectName name, String zeroArgsOpName) throws IOException, MBeanException {
        return invoke(connection, name, zeroArgsOpName, ArrayUtils.EMPTY_OBJECT_ARRAY, ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public Object invoke(MBeanServerConnection connection, ObjectName name, String opName, Object[] arguments,
                         String[] signature) throws IOException, MBeanException {
        try {
            return connection.invoke(name, opName, arguments, signature);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException("Wrapping InstanceNotFoundException with a runtime exception", e);
        } catch (ReflectionException e) {
            throw new RuntimeException("Wrapping ReflectionException with a runtime exception", e);
        }
    }


    private static class ConnectorConnectionPair {

        private JMXConnector connector;
        private MBeanServerConnection connection;

        private ConnectorConnectionPair(JMXConnector connector, MBeanServerConnection connection) {
            this.connector = connector;
            this.connection = connection;
        }

        public JMXConnector getConnector() {
            return connector;
        }

        public MBeanServerConnection getConnection() {
            return connection;
        }
    }

}
