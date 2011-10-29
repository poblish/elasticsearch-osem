/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.osem.core.impl;

import org.elasticsearch.osem.annotations.AttributeSource;
import org.elasticsearch.osem.annotations.impl.AttributeSourceImpl;
import org.elasticsearch.osem.core.AbstractObjectContext;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.osem.property.PropertySignatureSource;
import org.elasticsearch.osem.property.impl.PropertySignatureSourceImpl;

/**
 * 
 * @author alois.cochard
 *
 */
public class ObjectContextImpl extends AbstractObjectContext implements ObjectContext {

    public static final String CLASS_FIELD_NAME = "_class";

    private AttributeSource attributes = new AttributeSourceImpl();

    private PropertySignatureSource signatures = new PropertySignatureSourceImpl();

    public ObjectContextImpl() {
        mapper = new ObjectContextMapperImpl(attributes, signatures);
        writer = new ObjectContextWriterImpl(attributes, signatures);
        reader = new ObjectContextReaderImpl(attributes, signatures);
    }
}