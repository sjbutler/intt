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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.crc.intt.text.WordList;
import uk.ac.open.crc.intt.text.WordListReader;

/**
 * Provides the main dictionary of words for intt.
 *
 */
class MainDictionary implements Dictionary {
    private static final Logger LOGGER = 
            LoggerFactory.getLogger( MainDictionary.class );
    private String name = "Main Dictionary";

    private final int INITIAL_CAPACITY = 120_000;

    @Deprecated
    private HashSet<String> dictionary;
    
    private List<WordList> wordLists;

    /**
     * Creates a new instance of the main dictionary populated from the
     * resource pointed to by the {@code BufferedReader}.
     *
     * The terms added to the dictionary should be in a text file format with
     * one term per line. Behaviour with alternative input formats is
     * undocumented.
     *
     * @param in An instance of BufferedReader
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    MainDictionary ( BufferedReader in ) 
            throws IOException, FileNotFoundException {
        Set<String> set = new HashSet<>( INITIAL_CAPACITY );
        String line;

        while ( ( line = in.readLine() ) != null ) {
            set.add( line.trim().toLowerCase() );
        }

        in.close();
        
        this.wordLists.add( new WordList("user-defined", set ) );
        
        if ( this.dictionary.isEmpty() ) {
            LOGGER.warn( "Main dictionary is empty" );
        }
    }

    /**
     * Creates a new instance of the main dictionary populated from the
     * resource pointed to by the {@see BufferedReader} instance.
     *
     * The terms added to the dictionary should be in a text file format with
     * one term per line. Behaviour with alternative input formats is
     * undocumented.
     *
     * @param in   An instance of BufferedReader
     * @param name A {@code String} used to identify the dictionary. The name
     *             is returned by the {@code #toString()} method only.
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    MainDictionary ( BufferedReader in, String name ) 
            throws IOException, FileNotFoundException {
        this( in );
        this.name = name;
    }

    /**
     * Creates an instance of the main dictionary using a file contained
     * within the jar file. This constructor is is principally for internal
     * use and has not been tested for external calls.
     *
     * @param pathToFile a path to a file in a jar file
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    MainDictionary ( String pathToFile ) {
        WordListReader reader = new WordListReader( pathToFile );
        List<String> wordList = reader.asLowerCaseList();
        
        this.dictionary = new HashSet<>( wordList );
        
        if ( this.dictionary.isEmpty() ) {
            LOGGER.warn( "Main dictionary is empty" );
        }
    }

    /**
     * Creates an instance of the main dictionary using a file contained
     * within the jar file. This constructor is is principally for internal
     * use and has not been tested for external calls.
     *
     * @param pathToFile a path to a file in a jar file
     * @param name       a {@code String} used to identify the dictionary. 
     *                   The name is returned by the toString method only.
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    MainDictionary ( String pathToFile, String name )
            throws IOException, FileNotFoundException {
        this( pathToFile );
        this.name = name;
    }

    /**
     * Creates a single dictionary from multiple word lists.
     * @param paths a list of file paths
     */
//    MainDictionary( List<String> paths ) {
//        this.dictionary = new HashSet<>();
//        
//        paths.stream().forEach( ( pathToFile ) -> {
//            WordListReader reader = new WordListReader( pathToFile );
//            this.dictionary.addAll( reader.asLowerCaseListNoSingleLetters() );
//        } );
//        
//        if ( this.dictionary.isEmpty() ) {
//            LOGGER.warn( "Main dictionary is empty" );
//        }
//    }
    
    /**
     * Creates a single dictionary from multiple word lists.
     * @param wordLists a list of word lists
     */
    MainDictionary( List<WordList> wordLists ) {
        this.wordLists = wordLists;
    }
    
    /**
     * Creates a single named dictionary from multiple word lists.
     * @param paths a list of file paths
     * @param name a name for the dictionary
     */
//    MainDictionary( List<String> paths, String name ) {
//        this( paths );
//        this.name = name;
//    }
    
    MainDictionary( HashSet<String> dictionary, String name) {
	this(dictionary);
	this.name = name;
    }
    
    @Deprecated
    MainDictionary( HashSet<String> dictionary ) {
	this.dictionary = dictionary;
    }
    
    /**
     * Tests if the argument is recognised as a word
     *
     * @param token a String
     *
     * @return {@code true} if the argument is a dictionary word
     */
    @Override
    public synchronized boolean isWord ( String token ) {
        for ( WordList w : this.wordLists ) {
            if ( w.isWord(token) ) {
                return true;
            }
        }
        return false;
        //return dictionary.contains( token.toLowerCase() );
    }

    @Override
    public List<String> tags( String token ) {
        List<String> tags = new ArrayList<>();
        for ( WordList w : this.wordLists ) {
            if (w.isWord( token )) {
                tags.add( w.tag() );
            }
        }
        return tags;
    }
    
    /** Counts the number of words stored in the word lists.
     * 
     * @return total number of words in the dictionary
     */
    protected int dictionarySize() {
        int count = 0;
        
        for ( WordList w : this.wordLists ) {
            count += w.size();
        }
        
        return count;
    } 
    
    
    /**
     * Provides a descriptive string indicating the name
     * of the dictionary and the number of entries.
     *
     * @return a descriptive string
     */
    @Override
    public String toString () {
        StringBuilder output = new StringBuilder( "name=" );
        output.append( this.name );
        output.append( ", entries=" );
        output.append( this.dictionary.size() );
        output.append( ";" );

        return output.toString();
    }
}
