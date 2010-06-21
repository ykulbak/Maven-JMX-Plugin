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
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;

public class LocalMBeanServerChooser implements MBeanServerChooser {

    private String hasDomain;

    private String hasMBean;

    @Override
    public void validate() throws MojoExecutionException {
        if (hasMBean != null) {
            try {
                new ObjectName(hasMBean);
            } catch (MalformedObjectNameException e) {
                throw new MojoExecutionException("could not create an object name from " + hasMBean, e);
            }
        }
    }

    @Override
    public boolean choose(MBeanServerConnection connection) throws Exception {
        if (hasDomain != null && !containsDomain(connection, hasDomain)) {
            return false;
        }

        if (hasMBean != null) {
            ObjectName name = new ObjectName(hasMBean);
            if (!connection.isRegistered(name)) {
                return false;
            }
        }
        return true;
    }

    private boolean containsDomain(MBeanServerConnection connection, String hasDomain) throws IOException {
        for (String domain : connection.getDomains()) {
            if (domain.equals(hasDomain)) {
                return true;
            }
        }
        return false;
    }

    public String getDomain() {
        return hasDomain;
    }

    public String getMBean() {
        return hasMBean;
    }
}
