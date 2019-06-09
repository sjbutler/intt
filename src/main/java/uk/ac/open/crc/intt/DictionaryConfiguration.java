/*
 * Copyright (C) 2019 Simon Butler
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
package uk.ac.open.crc.intt;

/**
 * A set of {@code DictionarySet} configurations for intt.
 * 
 */
public enum DictionaryConfiguration {
    DEFAULT ( "default" ),
    GERMAN ( "german" ),
    COMBINED ( "combined" );
    
    private String id;
    DictionaryConfiguration( String id ) {
        this.id = id;
    }
    
    public String identity() {
        return this.id;
    }
    
}
