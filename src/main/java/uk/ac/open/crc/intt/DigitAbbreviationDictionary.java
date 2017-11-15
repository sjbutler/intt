/*
 Copyright (C) 2010-2015 The Open University

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * A dictionary of digit containing abbreviations.
 *
 */
class DigitAbbreviationDictionary implements Dictionary {

    private final HashSet<String> abbreviationSet;
    private final List<String> abbreviations;
    private String name = "Digit Abbreviation Dictionary";

    /**
     * Creates a dictionary using the supplied list of abbreviations.
     *
     * @param abbreviations a list of abbreviations
     */
    DigitAbbreviationDictionary ( List<String> abbreviations ) {
        this.abbreviations = abbreviations;
        this.abbreviationSet = new HashSet<>();
        abbreviations.stream().forEach( (abbreviation) -> {
            this.abbreviationSet.add( abbreviation.toLowerCase() );
        } );
    }

    /**
     * Creates a dictionary using the supplied list of abbreviations.
     *
     * @param abbreviations a list of abbreviations
     * @param name          a name for the dictionary
     */
    DigitAbbreviationDictionary ( List<String> abbreviations, String name ) {
        this( abbreviations );
        this.name = name;
    }

    /**
     * Indicates if the name (fragment) is a recognised abbreviation.
     *
     * @param token an identifier name, or name fragment
     *
     * @return {@code true} if the fragment is a recognised abbreviation
     *
     */
    @Override
    public synchronized boolean isWord ( String token ) {
        return this.abbreviationSet.contains( token.toLowerCase() );
    }

    // REVIEW AND REVISE
    // contains the assumption that more than one digit abbreviation 
    // cannot start from the same character.
    // Review whether it is the caller's responsibility to identify
    // overlaps -- I think it is.
    /**
     * Takes an identifier name and returns an list of known numeric
     * abbreviations found in the identifier in the order in which they 
     * are found. Complexity is O(n), but n is, currently, small.
     *
     * @param identifierNameFragment a {@code String} containing an
     *                               identifier name fragment
     *
     * @return a list of recognised numeric abbreviations found to be a
     * substring of identifier name.
     */
    synchronized List<String> findKnownSubstrings ( String identifierNameFragment ) {
        HashMap<Integer, String> foundWords = new HashMap<>();

        String normalisedFragment = identifierNameFragment.toLowerCase();

        // NB must work on the list, not the <code>HashSet</code>
        // too small to worry about selecting a more efficient data structure
        this.abbreviations.stream().forEach( (abbreviation) -> {
            int index = normalisedFragment.indexOf( abbreviation );
            if ( index != -1 ) {
                foundWords.put( index, abbreviation );
            }
        } );

        // need to sort into the order they appear in the identifier name
        // as some callers expect that ordering
        // only necessary where we have two or more abbreviations
        ArrayList<String> sortedWords = new ArrayList<>();
        if ( foundWords.size() > 1 ) {
            List<Integer> indexes = new ArrayList<>( foundWords.keySet() );
            Collections.sort( indexes );
            // now add the words to sortedWords in the correct order
            // this should process the list of words
            indexes.stream().forEach( (index) -> { 
                sortedWords.add( foundWords.get( index ) );
            } );
        }
        else {
            // review this -- there may be a nicer way of handling 1 or 0
            foundWords.keySet().stream().forEach( (key) -> {
                sortedWords.add( foundWords.get( key ) );
            } );
        }

        return sortedWords;
    }

    /**
     * Provides a descriptive string containing the name of the
     * dictionary and the number of entries.
     *
     * @return a descriptive string
     */
    @Override
    public String toString () {
        StringBuilder output = new StringBuilder( "name=" );
        output.append( name );
        output.append( ", entries=" );
        output.append( this.abbreviationSet.size() );
        output.append( ";" );

        return output.toString();
    }

}
