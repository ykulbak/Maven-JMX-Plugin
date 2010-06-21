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
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class Invoke implements Action {

    private String objectName;

    private String operation;

    private Parameter[] parameters;

    @Override
    public void validate() throws MojoExecutionException {
        if (StringUtils.isEmpty(objectName)) {
            throw new MojoExecutionException("objectName can't be empty");
        }

        if (StringUtils.isEmpty(operation)) {
            throw new MojoExecutionException("operation can't be empty");
        }

    }

    @Override
    public Object execute(MBeanServerConnection connection) throws Exception {
        ObjectName name = new ObjectName(objectName);
        int len = parameters == null ? 0 : parameters.length;
        String[] signature = new String[len];
        Object[] arguments = new Object[len];

        ConvertUtilsBean converter = new ConvertUtilsBean();

        for (int i = 0; i < len; i++) {
            signature[i] = parameters[i].getType();
            arguments[i] = converter.convert(parameters[i].getValue(), ClassUtils.getClass(parameters[i].getType()));
            if (signature[i].equals("java.lang.String") && arguments[i] == null) {
                arguments[i] = "";
            }
        }
        return connection.invoke(name, operation, arguments, signature);
    }


    public String getObjectName() {
        return objectName;
    }

    public String getOperation() {
        return operation;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public static class Parameter {
        private String value;

        private String type;

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }
    }

    @Override
    public String toString() {
        return "Invoke{" +
                "objectName='" + objectName + '\'' +
                ", operation='" + operation + '\'' +
                '}';
    }
}
