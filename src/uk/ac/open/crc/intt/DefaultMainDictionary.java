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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Returns the instance of this class.
     *
     * @return the instance of this class
     */
    synchronized static DefaultMainDictionary getInstance()  
            throws IOException, FileNotFoundException {
        if (instance == null) {
            instance = new DefaultMainDictionary();
        }

        return instance;
    }

    // experimenting with mdsc wordlists
    private static final List<String> WORD_LISTS;
    
    static {
        WORD_LISTS = new ArrayList<>();
        WORD_LISTS.add( "/uk/ac/open/crc/mdsc/wordlists/scowl/en_CA" );
        WORD_LISTS.add( "/uk/ac/open/crc/mdsc/wordlists/scowl/en_GB" );
        WORD_LISTS.add( "/uk/ac/open/crc/mdsc/wordlists/scowl/en_US" );
        WORD_LISTS.add( "/uk/ac/open/crc/mdsc/wordlists/scowl/hacker" );
        WORD_LISTS.add( "/uk/ac/open/crc/mdsc/wordlists/scowl/proper-nouns" );
        WORD_LISTS.add( "/uk/ac/open/crc/mdsc/wordlists/scowl/rude" );
        WORD_LISTS.add( "/uk/ac/open/crc/mdsc/wordlists/technical" );
    }
    
    // -------------- instance methods and fields ----------

    /**
     * Private constructor for the Singleton.
     */
    private DefaultMainDictionary() throws FileNotFoundException, IOException {
        super( WORD_LISTS, "Default");
//        super( "wordlist.txt", "Default");
    }
    
}
