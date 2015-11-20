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

import java.util.HashSet;
import java.util.List;

/**
 * A dictionary of abbreviations. 
 *
 */
class AbbreviationDictionary implements Dictionary {

    private HashSet<String> abbreviationSet;

    String name = "Abbreviation Dictionary";

    /**
     * Builds a new abbreviation dictionary from the list of abbreviations
     * passed in. The dictionary entries are normalised to lower case. 
     * @param abbreviations a list of abbreviations 
     */
    public AbbreviationDictionary( List<String> abbreviations ) {
        this.abbreviationSet = new HashSet<>( abbreviations.size() );
        abbreviations.stream().forEach( ( abbreviation ) -> {
            this.abbreviationSet.add( abbreviation.toLowerCase() );
        });
    }

    /**
     * Creates an abbreviation dictionary from the list of abbreviations passed
     * in and names the dictionary. The dictionary entries are normalised to 
     * lower case.
     * @param abbreviations a list of abbreviations
     * @param name a name for the dictionary
     */
    public AbbreviationDictionary( List<String> abbreviations, String name ) {
        this( abbreviations );
        this.name = name;
    }

   /**
     * Indicates if the name (fragment) is a recognised abbreviation.
     *
     * @param s An identifier name, or name fragment.
     *
     * @return {@code true} if the fragment is a recognised abbreviation
     *
     */
    @Override
    public synchronized boolean isWord( String s ) {
        return abbreviationSet.contains( s.toLowerCase() );
    }

    /**
     * A simple string representation of the object's state.
     * @return a descriptive string containing the name of the dictionary and 
     * the number of entries
     */
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("name=");
        output.append(name);
        output.append(", entries=");
        output.append(this.abbreviationSet.size());
        output.append(";");

        return output.toString();
    }


}
