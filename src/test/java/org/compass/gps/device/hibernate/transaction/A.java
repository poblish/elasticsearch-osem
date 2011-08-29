/*
 * Copyright 2004-2009 the original author or authors.
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

package org.compass.gps.device.hibernate.transaction;

import org.elasticsearch.osem.annotations.Searchable;

/**
 * @author kimchy
 */
@Searchable
public class A {

    Long id;

    String value1;

    String value2;

    // (AGR) 25 August 2011
    public Long getId() {
	    return id;
    }

    // (AGR) 25 August 2011
    public void setId( Long x) {
	    id = x;
    }
}