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
import org.apache.maven.plugin.MojoExecutionException;

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

/**
 * Provides common JMX infrastructure to subclasses.
 */
public abstract class AbstractJmxMojo extends AbstractMojo {

    private static final String AGENT_RELATIVE_PATH = File.separator + "lib" + File.separator + "management-agent.jar";
    private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

    public final void interactWithAllLocalMBeanServers(MBeanServerCallback callback) throws IOException, MojoExecutionException {
        for (VirtualMachineDescriptor desc : VirtualMachine.list()) {
            VirtualMachine vm = attach(desc);
            if (vm != null) {
                JMXServiceURL connectorAddress = getConnectorAddress(desc, vm);
                if (connectorAddress != null) {
                    interact(connectorAddress, callback);
                }
            }
        }
    }

    private void interact(JMXServiceURL connectorAddress, MBeanServerCallback callback) throws IOException, MojoExecutionException {
        JMXConnector connector = JMXConnectorFactory.connect(connectorAddress);
        connector.connect();
        try {
            callback.doWithMBeanServer(connector.getMBeanServerConnection());
        } catch (IOException e) {
            throw (IOException) e.fillInStackTrace();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed while interacting with MBean Server callback", e);
        } finally {
            connector.close();
        }
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

    private JMXServiceURL getConnectorAddress(VirtualMachineDescriptor descriptor, VirtualMachine vm) {
        try {
            String connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
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
                return new JMXServiceURL(vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS));
            }
        } catch (IOException e) {
            getLog().debug("IO exception while getting connector address for  " + vm.id(), e);
        }
        return null;
    }

}
