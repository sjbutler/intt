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

import java.util.List;
import uk.ac.open.crc.intt.text.WordListReader;

/**
 * The default prefix dictionary.
 *
 */
class DefaultPrefixDictionary extends PrefixDictionary {
    private static DefaultPrefixDictionary instance = null;

    /**
     * Retrieve the instance of this class.
     * @param aggregatedDictionary an instance of {@code AggregatedDictionary}
     * to use
     * @return the instance of this class
     */
    static DefaultPrefixDictionary getInstance( AggregatedDictionary aggregatedDictionary ) {
        if (instance == null) {
            WordListReader reader = new WordListReader( "samurai-prefixes.txt" );
            instance = new DefaultPrefixDictionary( 
                    reader.asLowerCaseList(), 
                    aggregatedDictionary );
        }

        return instance;
    }



    private DefaultPrefixDictionary( 
            List<String> prefixes, 
            AggregatedDictionary aggregatedDictionary ) {
        super( prefixes, aggregatedDictionary, "prefix" );
    }

}
