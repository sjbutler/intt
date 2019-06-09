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
import java.util.HashSet;
import java.util.List;

/**
 * A store of project specific vocabulary. This is for tokens that 
 * are found in a project that are not recognised in dictionaries
 * yet using conservative tokenisation are clearly used in names. 
 *
 */
class ProjectVocabulary implements Dictionary {

    private HashSet<String> vocabularySet;

    private final int MINIMUM_CAPACITY = 10000;

    /**
     * Creates an empty dictionary for project vocabulary.
     */
    ProjectVocabulary () {
        this.vocabularySet = new HashSet<>( MINIMUM_CAPACITY );
    }

    /**
     * Creates a dictionary and populates it with the given vocabulary.
     * @param vocabulary a {@code List} of 'words' to add to a project vocabulary
     */
    ProjectVocabulary ( List<String> vocabulary ) {
        this();
        vocabulary.stream().forEach( ( word ) -> { 
            this.vocabularySet.add( word.toLowerCase() ); 
        } );
    }

    /**
     * Add a word to the vocabulary.
     * @param word a term to add tot he dictionary
     */
    final synchronized void add ( String word ) {
        // HashSet.add() is constant time
        this.vocabularySet.add( word.toLowerCase() );
    }

    /**
     * Checks if a string is found in the project vocabulary.
     * @param token a string to test
     * @return {@code true} iff the test string is found in the vocabulary
     */
    @Override
    public synchronized boolean isWord ( String token ) {
        return this.vocabularySet.contains( token.toLowerCase() );
    }

    @Override
    public List<String> tags( String word ) {
        List<String> tags = new ArrayList<>();
        if (isWord( word )) {
            tags.add( "project" );
        }
        return tags;
    }
    
    
    /**
     * Reports the percentage of tokens in the supplied list that are found in 
     * the vocabulary.
     * @param tokens a {@code List} of tokens
     * @return the percentage of member of the supplied list that are found in 
     * the project vocabulary
     */
    synchronized int percentageKnown ( List<String> tokens ) {
        int knownCount = 0;

        for ( String word : tokens ) {
            if ( this.isWord( word ) == true ) {
                knownCount++;
            }
        }

        return (int) ( 100 * knownCount / tokens.size() );
    }

    /**
     * Reports the percentage of tokens in the supplied list that are found in 
     * the vocabulary, plus the number of tokens found. 
     * @param tokens a {@code List} of tokens
     * @return the percentage of member of the supplied list that are found in 
     * the project vocabulary
     */
    synchronized int weightedPercentageKnown ( List<String> tokens ) {
        int knownCount = 0;

        for ( String token : tokens ) {
            if ( this.isWord( token ) == true ) {
                knownCount++;
            }
        }

        return knownCount + (int) ( 100 * knownCount / tokens.size() );
    }

    /**
     * Reports the percentage of tokens in the supplied list that are found in 
     * the vocabulary and have three or more cahracters, plus the number of 
     * tokens found. 
     * @param tokens a {@code List} of tokens
     * @return the percentage of member of the supplied list that are found in 
     * the project vocabulary plus the number of tokens found
     */
    synchronized int percentageKnownA ( List<String> tokens ) {
        int knownCount = 0;

        for ( String token : tokens ) {
            if ( token.length() > 2 && this.isWord( token ) == true ) {
                knownCount++;
            }
        }

        return (int) ( 100 * knownCount / tokens.size() );
    }

    /**
     * Reports the percentage of tokens in the supplied list that are found in 
     * the vocabulary and have three or more characters, plus the number of 
     * tokens found. 
     * @param tokens a {@code List} of tokens
     * @return the percentage of member of the supplied list that are found in 
     * the project vocabulary plus the number of tokens found 
     */
    synchronized int weightedPercentageKnownA ( List<String> tokens ) {
        int knownCount = 0;

        for ( String token : tokens ) {
            if ( token.length() > 2 && this.isWord( token ) == true ) {
                knownCount++;
            }
        }

        return knownCount + (int) ( 100 * knownCount / tokens.size() );
    }

}
