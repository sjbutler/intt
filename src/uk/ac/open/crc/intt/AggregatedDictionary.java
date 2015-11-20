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

import java.util.List;

/**
 * An aggregation of individual dictionaries that behaves as a single
 * dictionary. Provides a convenient means to identify known terms.
 *
 */
class AggregatedDictionary implements Dictionary {

    private Dictionary abbreviations;
    private Dictionary digitAbbreviations;
    private Dictionary words;

    /**
     * Creates an aggregate of the supplied dictionaries.
     *
     * @param abbreviationDictionary      an abbreviation dictionary
     * @param digitAbbreviationDictionary a digit abbreviation dictionary
     * @param mainDictionary              a dictionary of words
     */
    AggregatedDictionary ( AbbreviationDictionary abbreviationDictionary,
            DigitAbbreviationDictionary digitAbbreviationDictionary,
            MainDictionary mainDictionary ) {
        this.abbreviations = abbreviationDictionary;
        this.digitAbbreviations = digitAbbreviationDictionary;
        this.words = mainDictionary;
    }

    /**
     * Changes the set of dictionaries used to the supplied set.
     *
     * @param dictionarySet a set of dictionaries
     */
    void changeDictionaries ( DictionarySet dictionarySet ) {
        this.abbreviations = dictionarySet.getAbbreviationDictionary();
        this.digitAbbreviations = dictionarySet.getDigitAbbreviationDictionary();
        this.words = dictionarySet.getMainDictionary();
    }

    /**
     * Determines if a word is found in any of the dictionaries.
     *
     * @param token a {@code String} to be tested.
     *
     * @return {@code true} if the word is found in any of the dictionaries.
     */
    @Override
    public synchronized boolean isWord ( String token ) {
        return ( this.words.isWord( token )
                || this.abbreviations.isWord( token )
                || this.digitAbbreviations.isWord( token ) );
    }

    // Needs renaming!? There must be a more appropriate name
    /**
     * Checks if all the strings in a list can be found in the dictionaries.
     *
     *
     * @param tokens a list of tokens found in a name
     *
     * @return {@code true} if all the tokens are recognised.
     */
    public synchronized boolean isAllKnownWords ( List<String> tokens ) {
        return tokens.stream().allMatch( (token) -> { return isWord( token ); } );
    }

    /**
     * Returns an integer percentage of the component words
     * contained in the dictionary.
     *
     * @param tokens a {@code List} of tokens
     * @return the percentage as an integer of tokens found in the dictionary
     */
    public synchronized int percentageKnown ( List<String> tokens ) {
        int knownCount = 0;

        for ( String token : tokens ) {
            if ( this.isWord( token ) == true ) {
                knownCount++;
            }
        }

        return (int) ( 100 * knownCount / tokens.size() );
    }

}
