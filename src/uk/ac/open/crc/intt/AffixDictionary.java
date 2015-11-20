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

/**
 * An interface for an affix dictionary.
 *
 */
interface AffixDictionary extends Dictionary{

    /**
     * Determines if the 'word' tested is a simple derived neologism, i{.}e{.} 
     * a neologism created by adding a prefix and/or suffix to a dictionary word.
     * The intention is to identify where developers may have created a new 
     * word by an obvious mechanism and to avoid splitting it.
     * @param word a 'word' to test
     * @return {@code true} if the 'word' consists of a dictionary word combined 
     * with a known prefix or suffix or both
     */
    public Boolean isNeologism(String word);
    
    /**
     * Recovers the number of characters in the longest affix in the 
     * dictionary.
     * @return the length in characters of the longest dictionary entry 
     */
    public Integer getLongest();
}
