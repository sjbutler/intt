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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import uk.ac.open.crc.intt.text.WordList;
import nu.floss.wordlists.Wordlist;
import nu.floss.wordlists.Wordlists;

/**
 * A class providing a default German language dictionary
 * 
 */
class GermanMainDictionary extends MainDictionary {


    /**
     * The only instance of this class.
     *
     */
    private static GermanMainDictionary instance = null;
    
    private static List<WordList> wordLists;

    /**
     * Returns the instance of this class.
     *
     * @return the instance of this class
     */
    synchronized static GermanMainDictionary getInstance()  
            throws IOException, FileNotFoundException {
        if ( wordLists == null ) {
            wordLists = new ArrayList<>();
            wordLists.add( new WordList(Wordlists.GERMAN_DE_50K.tag(), new Wordlist(Wordlists.GERMAN_DE_50K, true).list()));
	    wordLists.add( new WordList( Wordlists.SCOWL_HACKER.tag(), new Wordlist(Wordlists.SCOWL_HACKER,true).list()));
	    wordLists.add( new WordList( Wordlists.SCOWL_RUDE.tag(), new Wordlist(Wordlists.SCOWL_RUDE,true).list())); 
	    wordLists.add( new WordList( Wordlists.TECHNICAL.tag(), new Wordlist(Wordlists.TECHNICAL,true).list())); 
        }
        if (instance == null) {
            instance = new GermanMainDictionary();
        }

        return instance;
    }

    // -------------- instance methods and fields ----------

    /**
     * Private constructor for the Singleton.
     */
    private GermanMainDictionary() throws FileNotFoundException, IOException {
        super( wordLists );
    }
}
