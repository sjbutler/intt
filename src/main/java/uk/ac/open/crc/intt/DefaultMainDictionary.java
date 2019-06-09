/*
 Copyright (C) 2010-2015 The Open University
 Copyright (C) 2017-2019 Simon Butler

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import uk.ac.open.crc.intt.text.WordList;

import uk.ac.open.crc.mdsc.wordlists.Wordlist;
import uk.ac.open.crc.mdsc.wordlists.Wordlists;

/**
 * Provides a main dictionary for the default configuration.
 *
 */
class DefaultMainDictionary extends MainDictionary {

    /**
     * The only instance of this class.
     *
     */
    private static DefaultMainDictionary instance = null;
    
    private static HashSet<String> WORD_LISTS;
    
    private static List<WordList> wordLists;

    /**
     * Returns the instance of this class.
     *
     * @return the instance of this class
     */
    synchronized static DefaultMainDictionary getInstance()  
            throws IOException, FileNotFoundException {
        if ( wordLists == null ) {
            wordLists = new ArrayList<>();
            wordLists.add( new WordList(Wordlists.SCOWL_EN_CA.tag(), new Wordlist(Wordlists.SCOWL_EN_CA,true).list()));
            wordLists.add( new WordList(Wordlists.SCOWL_EN_GB.tag(), new Wordlist( Wordlists.SCOWL_EN_GB,true).list())); //"/scowl/en_GB" );
	    wordLists.add( new WordList( Wordlists.SCOWL_EN_US.tag(), new Wordlist(Wordlists.SCOWL_EN_US,true).list())); //"/scowl/en_US" );
	    wordLists.add( new WordList( Wordlists.SCOWL_HACKER.tag(), new Wordlist(Wordlists.SCOWL_HACKER,true).list()));
	    wordLists.add( new WordList( Wordlists.SCOWL_PROPER_NOUNS.tag(), new Wordlist(Wordlists.SCOWL_PROPER_NOUNS,true).list()));
	    wordLists.add( new WordList( Wordlists.SCOWL_RUDE.tag(), new Wordlist(Wordlists.SCOWL_RUDE,true).list())); //"/scowl/rude" );
	    wordLists.add( new WordList( Wordlists.TECHNICAL.tag(), new Wordlist(Wordlists.TECHNICAL,true).list())); //"/technical" );
        }

        if (instance == null) {
            instance = new DefaultMainDictionary();
        }

        return instance;
    }

    // -------------- instance methods and fields ----------

    /**
     * Private constructor for the Singleton.
     */
    private DefaultMainDictionary() throws FileNotFoundException, IOException {
        super( wordLists );
    }
    
}
