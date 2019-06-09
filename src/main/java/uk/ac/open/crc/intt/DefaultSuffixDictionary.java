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
import uk.ac.open.crc.intt.text.WordListReader;

/**
 * The default suffix dictionary.
 *
 */
class DefaultSuffixDictionary extends SuffixDictionary {
    private static DefaultSuffixDictionary instance = null;

    /**
     * Retrieves the instance of this class.
     * @param aggregatedDictionary a dictionary to use when testing for neologisms.
     * @return the instance of this class
     */
    static DefaultSuffixDictionary getinstance(
            AggregatedDictionary aggregatedDictionary) {
        if (instance == null) {
            WordListReader reader = new WordListReader( "samurai-suffixes.txt" );
            instance = new DefaultSuffixDictionary( 
                    reader.asLowerCaseList(), 
                    aggregatedDictionary );
        }

        return instance;
    }

    // ------------ instance methods and fields -------------

    
    private DefaultSuffixDictionary( 
            List<String> suffixes, 
            AggregatedDictionary aggregatedDictionary) {
        super( suffixes, aggregatedDictionary, "suffix" );
    }
    
}
