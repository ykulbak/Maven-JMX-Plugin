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

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.lang.ClassUtils;
import org.apache.maven.plugin.MojoExecutionException;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class SetAttribute implements Action {

    private String objectName;

    private String attributeName;

    private String value;

    private String type;

    @Override
    public void validate() throws MojoExecutionException {
    }

    @Override
    public Object execute(MBeanServerConnection connection) throws Exception {
        ObjectName name = new ObjectName(objectName);
        ConvertUtilsBean converter = new ConvertUtilsBean();

        Object attributeValue = converter.convert(value, ClassUtils.getClass(type));
        connection.setAttribute(name, new Attribute(attributeName, attributeValue));
        return connection.getAttribute(name, attributeName);
    }

    public String getObjectName() {
        return objectName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
