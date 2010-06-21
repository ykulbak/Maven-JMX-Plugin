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


import org.apache.maven.plugin.MojoExecutionException;

import javax.management.MBeanServerConnection;
import java.io.IOException;

/**
 * Goal which invokes a JMX operation.
 *
 * @goal action
 */
public class ActionMojo extends AbstractJmxMojo {

    /**
     * Selects on which JMX instance we should operate
     *
     * @parameter
     * @required
     */
    private LocalMBeanServerChooser localMBeanServer;


    /**
     * @parameter
     * @required
     */
    private Action[] actions;

    @Override
    public void execute() throws MojoExecutionException {
        sleep(2000);
        getLog().info("JMX Actions mojo: start");
        localMBeanServer.validate();
        try {
            super.interactWithAllLocalMBeanServers(new MBeanServerCallback() {
                @Override
                public void doWithMBeanServer(MBeanServerConnection mbeanServerConnection) throws Exception {
                    if (localMBeanServer.choose(mbeanServerConnection)) {
                        for (Action action : actions) {
                            action.validate();
                            Object result = action.execute(mbeanServerConnection);
                            getLog().info("Successfully executed " + action + ", result=" + result);
                        }
                    }
                }
            });
        } catch (IOException e) {
            throw new MojoExecutionException("Failed while executing action ", e);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep( millis);
        } catch (InterruptedException ignored) {
        }
    }


    public LocalMBeanServerChooser getLocalMBeanServer() {
        return localMBeanServer;
    }

    public Action[] getActions() {
        return actions;
    }
}
