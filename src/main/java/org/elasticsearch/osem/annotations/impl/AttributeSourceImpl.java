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
package org.elasticsearch.osem.annotations.impl;

import org.hamcrest.Matcher;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.osem.annotations.AttributeSource;
import org.elasticsearch.osem.annotations.Exclude;
import org.elasticsearch.osem.annotations.Index;
import org.elasticsearch.osem.annotations.Indexable;
import org.elasticsearch.osem.annotations.IndexableAttribute;
import org.elasticsearch.osem.annotations.Searchable;
import org.elasticsearch.osem.annotations.SearchableAttribute;
import org.elasticsearch.osem.annotations.Serializable;
import org.elasticsearch.osem.annotations.SerializableAttribute;
import org.elasticsearch.osem.common.springframework.core.annotation.AnnotationUtils;
import org.elasticsearch.osem.core.ObjectContextException;

import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map.Entry;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author alois.cochard
 */

public class AttributeSourceImpl implements AttributeSource {

    private static final ESLogger logger = Loggers.getLogger(AttributeSourceImpl.class);

    // FIXME [alois.cochard] set all inner map to unmodifiable, and all returned map/collection to unmodifiable in all projct !

    private ConcurrentMap<Class<?>, Map<String,Collection<PropertyDescriptor>>> classProperties = new ConcurrentHashMap<Class<?>, Map<String,Collection<PropertyDescriptor>>>();

    private ConcurrentMap<PropertyDescriptor, SerializableAttribute> serializableAttributes = new ConcurrentHashMap<PropertyDescriptor, SerializableAttribute>();

    private ConcurrentMap<PropertyDescriptor, IndexableAttribute> indexableAttributes = new ConcurrentHashMap<PropertyDescriptor, IndexableAttribute>();

    private ConcurrentMap<PropertyDescriptor, Boolean> excludedProperties = new ConcurrentHashMap<PropertyDescriptor, Boolean>();

    //    private ConcurrentMap<Class<?>, Boolean> searchableClass = new ConcurrentHashMap<Class<?>, Boolean>();
    private ConcurrentMap<Class<?>, SearchableAttribute> searchables = new ConcurrentHashMap<Class<?>, SearchableAttribute>();

    private ConcurrentMap<Class<?>, Map<PropertyDescriptor, IndexableAttribute>> indexables = new ConcurrentHashMap<Class<?>, Map<PropertyDescriptor, IndexableAttribute>>();

    private ConcurrentMap<Class<?>, Map<PropertyDescriptor, SerializableAttribute>> serializables = new ConcurrentHashMap<Class<?>, Map<PropertyDescriptor, SerializableAttribute>>();

    private static Collection<PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) {
        Collection<PropertyDescriptor> descriptors = new HashSet<PropertyDescriptor>();
        try {
            BeanInfo bean;
            bean = Introspector.getBeanInfo(clazz);
            descriptors.addAll(Arrays.asList(bean.getPropertyDescriptors()));
        } catch (IntrospectionException e) {
            throw new ObjectContextException("Unable to introspect class '" + clazz.getName() + "'", e);
        }
        for (Class<?> i : clazz.getInterfaces()) {
            descriptors.addAll(getPropertyDescriptors(i));
        }
        if (clazz.getSuperclass() != null) {
            descriptors.addAll(getPropertyDescriptors(clazz.getSuperclass()));
        }
	return descriptors;
    }

    @Override
    public Collection<PropertyDescriptor> getProperties(Class<?> clazz) {
	final Collection<PropertyDescriptor>	theColl = new ArrayList<PropertyDescriptor>();

	for ( Collection<PropertyDescriptor> eachColl : getClassProperties(clazz).values())
	{
		theColl.addAll(eachColl);
	}

        return theColl;
    }

    @Override
    public Collection<PropertyDescriptor> getProperty(Class<?> clazz, String indexName) {
	final Collection<PropertyDescriptor> theColl = getClassProperties(clazz).get(indexName);
	if ( theColl != null) {
		return theColl;
	}
	return Collections.emptyList();
    }

    @Override
    public SearchableAttribute getSearchableAttribute(Class<?> clazz) {
        SearchableAttribute searchableAttr = null;
        if (!searchables.containsKey(clazz)) {
            Searchable searchable = AnnotationUtils.findAnnotation(clazz, Searchable.class);
            if (searchable != null) {
                searchableAttr = AttributeBuilder.build(searchable);
            }

	    if ( searchableAttr != null)	{	// (AGR) Allowing null will cause pointless NPE
		SearchableAttribute s = searchables.putIfAbsent(clazz, searchableAttr);
		searchableAttr = s != null ? s : searchableAttr;
	    }
        } else {
            searchableAttr = searchables.get(clazz);
        }
        return searchableAttr;
    }

    @Override
    public Map<PropertyDescriptor, IndexableAttribute> getIndexableProperties(Class<?> clazz) {
        Map<PropertyDescriptor, IndexableAttribute> indexableProperties = indexables.get(clazz);

	if (indexableProperties == null) {
            indexableProperties = new HashMap<PropertyDescriptor, IndexableAttribute>();
            boolean searchable = isSearchable(clazz);

            for (PropertyDescriptor property : getProperties(clazz)) {
                // Filtering excluded properties
                if (!isExcluded(property)) {
                    IndexableAttribute indexable = getIndexableAttribute( clazz, property);

		    if (indexable == null) {
                        if (searchable) {
                            // Searchable class properties are implicitly Indexable
                            indexable = new IndexableAttributeImpl();
                        } else {
                            if (getSerializableAttribute(property) != null) {
                                // Serializable properties are implicitly Stored but not Indexed
                                // TODO [alois.cochard] is it correct ? or Serializable properties must be implicitly indexed too ?
                                indexable = new IndexableAttributeImpl().setStored(true).setIndex(Index.NA);
                            }
                        }
                        if (indexable != null) {
                            IndexableAttribute i = indexableAttributes.putIfAbsent(property, indexable);
                            indexable = i != null ? i : indexable;
                        }
                    }
                    if (indexable != null) {
                        indexableProperties.put(property, indexable);
                    }
                }
		else
		{
			// System.out.println( "@@ EXCLUDED " + property.getReadMethod());
		}
            }
            // Caching
            Map<PropertyDescriptor, IndexableAttribute> i = indexables.putIfAbsent(clazz, indexableProperties);
            indexableProperties = i != null ? i : indexableProperties;
        }
	return indexableProperties;
    }

    @Override
    public Map<PropertyDescriptor, SerializableAttribute> getSerializableProperties(Class<?> clazz) {
        Map<PropertyDescriptor, SerializableAttribute> serializableProperties = serializables.get(clazz);
        if (serializableProperties == null) {
            serializableProperties = new HashMap<PropertyDescriptor, SerializableAttribute>();
            boolean searchable = isSearchable(clazz);
            for (PropertyDescriptor property : getProperties(clazz)) {
                // Filtering excluded properties
                if (!isExcluded(property)) {
                    SerializableAttribute serializable = getSerializableAttribute(property);
                    if (serializable == null) {
                        if (searchable) {
                            // Searchable class properties are implicitly Serializable
                            serializable = new SerializableAttributeImpl();
                        } else {
                            if (getIndexableAttribute( clazz, property) != null) {
                                // Indexable properties are implicitly Serializable
                                serializable = new SerializableAttributeImpl();
                            }
                        }
                        if (serializable != null) {
                            // Caching
                            SerializableAttribute s = serializableAttributes.putIfAbsent(property, serializable);
                            serializable = s != null ? s : serializable;
                        }
                    }
                    if (serializable == null) {
                        // If property isn't Serializable nor Indexable and class isn't Searchable, add property to excluded list
                        excludedProperties.putIfAbsent(property, true);
                    } else {
                        serializableProperties.put(property, serializable);
                    }
                }
            }
            // Caching
            Map<PropertyDescriptor, SerializableAttribute> s = serializables.putIfAbsent(clazz, serializableProperties);
            serializableProperties = s != null ? s : serializableProperties;
        }
        return serializableProperties;
    }

    private <A extends Annotation> A getAnnotation( final Class<?> inClass, PropertyDescriptor property, Class<A> annotationType) {
        // Look for annotation on setter only
        Method setter = property.getWriteMethod();
        if (setter != null) {
            final A	theAnnotation = AnnotationUtils.findAnnotation( setter, annotationType);
	    if ( theAnnotation != null) {
		    return theAnnotation;
	    }
        }

	if ( inClass != null) {
		Matcher<String>	theMatcher = isIn( getPossibleFieldNamesForProperty( property.getName() ) );

		for ( Field eachField : inClass.getDeclaredFields()) {
			if ( theMatcher.matches( eachField.getName() )) {
				final A	theAnnotation = eachField.getAnnotation(annotationType);
				if ( theAnnotation != null) {
					return theAnnotation;
				}
			}
		}
	}

	return null;
    }

    private static List<String> getPossibleFieldNamesForProperty( final String inPropName)
    {
	return Arrays.asList( inPropName,
				"_" + inPropName,
				"m_" + Character.toUpperCase( inPropName.charAt(0) ) + inPropName.substring(1));
    }

    private Map<String,Collection<PropertyDescriptor>> getClassProperties(Class<?> clazz) {
        Map<String,Collection<PropertyDescriptor>> properties = classProperties.get(clazz);
        if (properties == null) {
            properties = new HashMap<String,Collection<PropertyDescriptor>>();

	    final Collection<PropertyDescriptor>	thePropDescriptors = getPropertyDescriptors(clazz);

            for (PropertyDescriptor property : thePropDescriptors) {
                String name;
                IndexableAttribute attribute = getIndexableAttribute( clazz, property);
                if (attribute != null && attribute.getIndexName() != null) {
                    name = attribute.getIndexName();
                } else {
		    name = null;
		}
                name = name == null ? property.getName() : name;

		///////////////////////////////////////////////////////////////////////////

		if ( property.getReadMethod() == null)
		{
			continue;
		}

		///////////////////////////////////////////////////////////////////////////

		final Collection<PropertyDescriptor>	x = properties.get(name);

		if ( x == null)
		{
			properties.put( name, Sets.newHashSet(property));
		}
		else
		{
			x.add(property);
		}
            }

	    ///////////////////////////////////////////////////////////////////////////////

            Map<String,Collection<PropertyDescriptor>> p = classProperties.putIfAbsent(clazz, properties);
            properties = p != null ? p : properties;
        }
        return properties;
    }
 
    private IndexableAttribute getIndexableAttribute(final Class<?> inClass, PropertyDescriptor property) {
        IndexableAttribute indexableAttr = indexableAttributes.get(property);
        if (indexableAttr == null) {
            Indexable indexable = getAnnotation( inClass, property, Indexable.class);
            if (indexable != null) {
                indexableAttr = AttributeBuilder.build(indexable);
                IndexableAttribute i = indexableAttributes.putIfAbsent(property, AttributeBuilder.build(indexable));
                indexableAttr = i != null ? i : indexableAttr;
            }
        }
        return indexableAttr;
    }

    private SerializableAttribute getSerializableAttribute(PropertyDescriptor property) {
        SerializableAttribute serializableAttr = serializableAttributes.get(property);
        if (serializableAttr == null) {
            Serializable serializable = getAnnotation( null, property, Serializable.class);
            if (serializable != null) {
                serializableAttr = AttributeBuilder.build(serializable);
                SerializableAttribute s = serializableAttributes.putIfAbsent(property, serializableAttr);
                serializableAttr = s != null ? s : serializableAttr;
            }
        }
        return serializableAttr;
    }

    private boolean isExcluded(PropertyDescriptor property) {
        Boolean excluded = excludedProperties.get(property);
        if (excluded == null) {
            if (property.getWriteMethod() == null) {
                // TODO [alois.cochard] warn about ignored properties
                excluded = true;
            } else {
                excluded = getAnnotation( null, property, Exclude.class) != null;
                if (excluded) {
//		    System.out.println( "@@--> Marked as excluded: " + property.getReadMethod());
                    // Warn if other annotation present
                    List<Class<?>> annotationTypes = new ArrayList<Class<?>>();
                    if (getAnnotation( null, property, Serializable.class) != null) {
                        annotationTypes.add(Serializable.class);
                    }
                    if (getAnnotation( null, property, Indexable.class) != null) {
                        annotationTypes.add(Indexable.class);
                    }
                    for (Class<?> annotationType : annotationTypes) {
                        logger.warn("The property '{}' of class '{}' have both @Exclude and @{}, @{} will be ignored", property.getName(),
                                property.getWriteMethod().getDeclaringClass().getName(), annotationType.getSimpleName(),
                                annotationType.getSimpleName());
                    }
                }
            }
            Boolean e = excludedProperties.putIfAbsent(property, excluded);
            excluded = e != null ? e : excluded;
        }
        return excluded;
    }

    private boolean isSearchable(Class<?> clazz) {
        return getSearchableAttribute(clazz) != null;
    }

    private static String propertyDescriptorString( final PropertyDescriptor inPD)
    {
	    return "[R: " + inPD.getReadMethod() + ", W: " + inPD.getWriteMethod() + "]";
    }

	@Override
	public Iterable<Entry<Class<?>,SearchableAttribute>> getSearchableAttributes()
	{
		return searchables.entrySet();
	}
}