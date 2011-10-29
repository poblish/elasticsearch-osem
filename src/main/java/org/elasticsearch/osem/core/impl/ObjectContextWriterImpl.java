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

import com.google.common.collect.Lists;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import java.util.Map.Entry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.osem.annotations.AttributeSource;
import org.elasticsearch.osem.annotations.IndexableAttribute;
import org.elasticsearch.osem.annotations.SerializableAttribute;
import org.elasticsearch.osem.core.ObjectContextSerializationException;
import org.elasticsearch.osem.core.ObjectContextWriter;
import org.elasticsearch.osem.property.PropertySignature;
import org.elasticsearch.osem.property.PropertySignatureSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author alois.cochard
 *
 */
public class ObjectContextWriterImpl implements ObjectContextWriter {

    private final static Logger	s_Logger = LoggerFactory.getLogger( ObjectContextWriterImpl.class );

    private AttributeSource attributes;

    private PropertySignatureSource signatures;

    public ObjectContextWriterImpl(AttributeSource attributes, PropertySignatureSource signatures) {
        this.attributes = attributes;
        this.signatures = signatures;
    }

    @Override
    public XContentBuilder write(Object object) throws ObjectContextSerializationException {
        Exception exception = null;
        try {
            XContentBuilder builder = JsonXContent.contentBuilder();
            builder.startObject();

	    System.out.println("********* " + object);

            writeObject( builder, object, Lists.newArrayList(object));
            builder.endObject();
            return builder;
        } catch (IllegalArgumentException e) {
            exception = e;
        } catch (IOException e) {
            exception = e;
        } catch (IllegalAccessException e) {
            exception = e;
        } catch (InvocationTargetException e) {
            exception = e;
        }
        throw new ObjectContextSerializationException(object.getClass(), exception);
    }

    @SuppressWarnings("unchecked")
    private void writeObject( XContentBuilder builder, Object object, final Collection<Object> ioChain)
				throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException
    {
        for ( Entry<PropertyDescriptor,SerializableAttribute> entry : attributes.getSerializableProperties( object.getClass() ).entrySet())
	{
		// TODO [alois.cochard] Handle serialization attribute
		// TODO [alois.cochard] SerializableAttribute serializable = entry.getValue();

		writeProperty( entry.getKey(), builder, object, ioChain);
        }

        // Add _class field
        builder.field(ObjectContextImpl.CLASS_FIELD_NAME, object.getClass().getCanonicalName());
    }

	@SuppressWarnings("unchecked")
	private void writeProperty( final PropertyDescriptor inProperty, XContentBuilder builder, Object object, final Collection<Object> ioChain)
					throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException
	{
	    IndexableAttribute indexable = attributes.getIndexableProperties(object.getClass()).get(inProperty);
	    String name = indexable != null && indexable.getIndexName() != null ? indexable.getIndexName() : inProperty.getName();

	    // (AGR) starts

	    Object	theMethodResult;

	    try
	    {
		  theMethodResult = inProperty.getReadMethod().invoke(object);
	    }
	    catch (RuntimeException e)
	    {
		    return;
	    }

	    final PropertySignature	theSig = signatures.get(inProperty);

	    if (theSig.getTypeClass().isPrimitive())
	    {
		   if ( theMethodResult instanceof Long)
		   {
			  builder.field( name, ((Long) theMethodResult).longValue());
		   }
		   else if ( theMethodResult instanceof Integer)
		   {
			  builder.field( name, ((Integer) theMethodResult).intValue());
		   }
		   else if ( theMethodResult instanceof Boolean)
		   {
			  builder.field( name, ((Boolean) theMethodResult).booleanValue());
		   }
		   else if ( theMethodResult instanceof Double)
		   {
			  builder.field( name, ((Double) theMethodResult).doubleValue());
		   }
		   else if ( theMethodResult instanceof Float)
		   {
			  builder.field( name, ((Float) theMethodResult).floatValue());
		   }
		   else if ( theMethodResult instanceof Byte)
		   {
			  builder.field( name, ((Byte) theMethodResult).byteValue());
		   }
		   else if ( theMethodResult instanceof Short)
		   {
			  builder.field( name, ((Short) theMethodResult).shortValue());
		   }
		   else	// Well, what?
		   {
			  builder.field( name, String.valueOf(theMethodResult));
		   }
	    }
	    else if ( theSig.getTypeClass().isAssignableFrom( Map.class ))
	    {
		    builder.field( name, (Map) theMethodResult);
	    }
	    else if ( theSig.getTypeClass().isAssignableFrom( Locale.class ))
	    {
		    final Locale	theLocale = (Locale) theMethodResult;

		    builder.field(name);
		    builder.startObject();

		    builder.field( "lang", theLocale.getLanguage());

		    if ( theLocale.getCountry().length() > 0) {
			builder.field( "country", theLocale.getCountry());
		    }

		    if ( theLocale.getVariant().length() > 0) {
			    builder.field( "variant", theLocale.getVariant());
		    }

		    builder.endObject();
	    }
	    else
	    {
		    if (name.equals("_id") && theMethodResult == null) {
			// Filtering "_id" field with null value, for automatic id generation
		        return;
		    }

		    writePropertyValue( builder, theSig, name, theMethodResult, ioChain);
	    }
    }

	private void writePropertyValue( final XContentBuilder ioBuilder, final PropertySignature signature,
					final String inName, final Object value, final Collection<Object> ioChain)
						throws IllegalArgumentException, IOException, IllegalAccessException, InvocationTargetException
	{
		if ( value == null) {
			if ( inName != null) {
				ioBuilder.field(inName);
			}

			ioBuilder.nullValue();
			return;
		}

	if ( signature.getType() == null)	// (AGR)
	{
		if ( inName != null) {
			ioBuilder.field(inName);
		}

	/*	if ( value instanceof Locale)
		{
			ioBuilder.startObject();
			writeObject( ioBuilder, "hello:", ioChain);
			ioBuilder.endObject();
		}
		else */ if ( value instanceof org.hibernate.collection.internal.PersistentSet)
		{
			Object[] a = value.getClass().isArray() ? (Object[]) value : ((Collection) value).toArray();
			ioBuilder.startArray();
			for (Object o : a)
			{
				handleNestedObject( ioBuilder, inName, o, ioChain);
			}
			ioBuilder.endArray();
		}
		else
		{
			ioBuilder.value(value);
		}

		return;
	}

	switch (signature.getType())
	{
		case Array:
		case Collection:
			if ( inName != null) {
				ioBuilder.field(inName);
			}

			Object[] a = value.getClass().isArray() ? (Object[]) value : ((Collection) value).toArray();
			ioBuilder.startArray();
			for (Object o : a) {
				writePropertyValue( ioBuilder, signature.getComposite(), null, o, ioChain);
			}
			ioBuilder.endArray();
			break;
		case Object:
			handleNestedObject( ioBuilder, inName, value, ioChain);
			break;
		default:
			// TODO [alois.cochard] add access to serializable attribute

			if ( inName != null) {
				ioBuilder.field(inName);
			}

			ioBuilder.value(( value != null) ? signature.getType().getAdapter().write( null, value) : value);
	}
    }

	private void handleNestedObject( final XContentBuilder ioBuilder, final String inName, final Object inValue,
					final Collection<Object> ioChain) throws IllegalArgumentException, IOException, IllegalAccessException, InvocationTargetException
	{
		if (ioChain.contains(inValue))
		{
//			System.out.println("**** CACHE HIT FOR " + inValue);
			return;
		}

		ioChain.add(inValue);

//		System.out.println("---- Chain now [" + ioChain.size() + "]: " + ioChain);

		if ( inName != null) {
			ioBuilder.field(inName);
		}

		ioBuilder.startObject();
		writeObject( ioBuilder, inValue, ioChain);
		ioBuilder.endObject();
	}
}