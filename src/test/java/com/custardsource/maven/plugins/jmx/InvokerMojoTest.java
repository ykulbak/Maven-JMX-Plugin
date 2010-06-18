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

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

public class InvokerMojoTest extends AbstractMojoTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testFullConfiguration() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/unit/invokerDummyPom.xml");

        ActionMojo mojo = (ActionMojo) lookupMojo("action", testPom);
        assertNotNull(mojo);

        LocalMBeanServerChooser localMBeanServer = mojo.getLocalMBeanServer();
        assertNotNull(localMBeanServer);
        assertEquals(localMBeanServer.getDomain(), "a-domain");
        assertEquals(localMBeanServer.getMBean(), "an-mbean");

        Action[] actions = mojo.getActions();
        assertNotNull(actions);
        assertEquals(mojo.getActions().length, 2);

        assertTrue(actions[0] instanceof Invoke);
        Invoke invoke = (Invoke) actions[0];
        assertEquals(invoke.getObjectName(), "anObjectName");
        assertEquals(invoke.getOperation(), "anOperation");
        assertEquals(invoke.getParameters().length, 1);
        Invoke.Parameter parameter = invoke.getParameters()[0];
        assertEquals(parameter.getType(), "a-type");
        assertEquals(parameter.getValue(), "a-value");

        assertTrue(actions[1] instanceof SetAttribute);
        SetAttribute setAttribute = (SetAttribute)actions[1];
        assertEquals(setAttribute.getObjectName(),"anObjectName");
        assertEquals(setAttribute.getAttributeName(), "moshe");
        assertEquals(setAttribute.getType(), "a-type");
        assertEquals(setAttribute.getValue(), "a-value");
    }

}