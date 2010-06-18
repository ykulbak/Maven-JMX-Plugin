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
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which invokes a JMX operation.
 *
 * @goal getAttribute
 */
public class GetAttributeMojo extends AbstractJmxMojo {

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
    private GetAttribute[] attributes;

    /**
     * @parameter
     * @required
     */
    private String targetPropertyName;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
