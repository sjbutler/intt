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
 * A dictionary of suffixes.
 *
 */
class SuffixDictionary implements AffixDictionary {

    private Integer longest = 0;

    private HashSet<String> suffixSet;
    private List<String> suffixes;

    protected String name = "Suffix Dictionary";
    
    private AggregatedDictionary dictionary;

    /**
     * Creates a suffix dictionary.
     *
     * @param suffixes             a list of suffixes
     * @param aggregatedDictionary the aggregated dictionary to which the
     *                             suffixes belong
     */
    SuffixDictionary ( List<String> suffixes, AggregatedDictionary aggregatedDictionary ) {
        this.dictionary = aggregatedDictionary;
        this.suffixes = suffixes;
        this.suffixSet = new HashSet<>( this.suffixes );
        for ( String suffix : suffixes ) {
            if ( suffix.length() > longest ) {
                longest = suffix.length();
            }
        }
    }

    /**
     * Creates a named suffix dictionary.
     *
     * @param suffixes             a list of suffixes
     * @param aggregatedDictionary the aggregated dictionary to which the
     *                             suffixes belong
     * @param name                 a name for the suffix dictionary
     */
    SuffixDictionary ( 
            List<String> suffixes, 
            AggregatedDictionary aggregatedDictionary, 
            String name ) {
        this( suffixes, aggregatedDictionary );
        this.name = name;
    }

    /**
     * Determines if the string is found in the list of suffixes.
     *
     * @param token a string
     * @return {@code true} if the string is found in the list of suffixes
     */
    @Override
    public boolean isWord ( String token ) {
        return suffixSet.contains( token.toLowerCase() );
    }

    
    @Override
    public List<String> tags( String word ) {
        List<String> tags = new ArrayList<>();
        if ( isWord( word ) ) {
            tags.add( "suffix" );
        }
        return tags;
    }
    
    
    /**
     * Determines whether the token is a known word with a recognised
     * suffix appended. In essence this class is a mechanism for
     * identifying neologisms that follow particular patterns.
     *
     * @param token a string to test
     * @return {@code true} iff the string consists of a known word and a suffix
     */
    @Override
    public Boolean isNeologism ( String token ) {
        boolean isNeologism = false;

        for ( String suffix : this.suffixes ) {
            if ( token.endsWith( suffix ) ) {
                String test = token.substring( 0, token.lastIndexOf( suffix ) );
                isNeologism = dictionary.isWord( test );
                if ( isNeologism ) {
                    break;
                }
            }
        }
        
        return isNeologism;
    }

    /**
     * Recovers the length of the longest suffix in the dictionary.
     *
     * @return the length of the longest suffix
     */
    @Override
    public Integer getLongest () {
        return longest;
    }

    /**
     * Provides a description of the dictionary consisting of its name
     * and the number of suffixes stored in the dictionary.
     *
     * @return a descriptive string
     */
    @Override
    public String toString () {
        StringBuilder output = new StringBuilder( "name=" );
        output.append( name );
        output.append( ", entries=" );
        output.append( this.suffixSet.size() );
        output.append( ";" );

        return output.toString();
    }

}
