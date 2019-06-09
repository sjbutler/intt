/*
 Copyright (C) 2010-2015 The Open University
 Copyright (C) 2019 Simon Butler

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package uk.ac.open.crc.intt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Provides a base class for prefix dictionaries.
 *
 */
class PrefixDictionary implements AffixDictionary {

    private HashSet<String> prefixSet;

    private List<String> prefixes;

    private Integer longest = 0;

    protected String name = "Prefix Dictionary";

    private AggregatedDictionary dictionary;

    /**
     * Creates a dictionary using the given list of prefixes.
     * @param prefixes a list of prefixes
     * @param aggregatedDictionary a dictionary to refer to when testing
     * neologisms
     */
    PrefixDictionary ( 
            List<String> prefixes, 
            AggregatedDictionary aggregatedDictionary ) {
        this.prefixes = prefixes;
        prefixSet = new HashSet<>( this.prefixes );

        for ( String prefix : this.prefixes ) {
            if ( prefix.length() > this.longest ) {
                this.longest = prefix.length();
            }
        }

        this.dictionary = aggregatedDictionary;
    }

    /**
     * Creates a named dictionary using the given list of prefixes.
     * @param prefixes a list of prefixes
     * @param aggregatedDictionary a dictionary to refer to when testing
     * neologisms
     * @param name a name for the dictionary
     */
    PrefixDictionary ( 
            List<String> prefixes, 
            AggregatedDictionary aggregatedDictionary, 
            String name ) {
        this( prefixes, aggregatedDictionary );
        this.name = name;
    }

    /**
     * Determines if the given string is a neologism.
     * @param token a string to test
     * @return {@code true} if when a known prefix is removed from the string
     * the remainder is a known word
     */
    @Override
    public Boolean isNeologism ( String token ) {
        boolean isWord = false;

        for ( String prefix : this.prefixes ) {
            if ( token.startsWith( prefix ) ) {
                String test = token.substring( prefix.length() );
                isWord = this.dictionary.isWord( test );
                if ( isWord ) {
                    break;
                }
            }
        }
        
        return isWord;
    }

    /**
     * Indicates if the given string is found in the dictionary.
     * @param token a string to test
     * @return {@code true} if the string is found in the dictionary
     */
    @Override
    public boolean isWord ( String token ) {
        return this.prefixSet.contains( token.toLowerCase() );
    }

    @Override
    public List<String> tags( String word ) {
        List<String> tags = new ArrayList<>();
        
        if ( isWord( word ) ) {
            tags.add( "prefix" );
        }
        
        return tags;
    }
    
    /** 
     * Recovers the length of the longest prefix in the dictionary.
     * @return the length of the longest entry
     */
    @Override
    public Integer getLongest () {
        return this.longest;
    }

    /**
     * Provides a description of the object.
     * @return a string containing the name of the dictionary and the number of 
     * entries
     */
    @Override
    public String toString () {
        StringBuilder output = new StringBuilder( "name=" );
        output.append( this.name );
        output.append( ", entries=" );
        output.append( this.prefixSet.size() );
        output.append( ";" );

        return output.toString();
    }

}
