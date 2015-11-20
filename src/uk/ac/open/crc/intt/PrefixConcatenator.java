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
 * Experimental class for combining tokens that the name creator has separated
 * with a typographical word boundary. For example, {@code subMenu} is split to 
 * {@code sub},{@code menu} and this class recombines it to {@code submenu}. 
 * 
 * <p>
 * Functionality is currently limited to the prefixes 'pre' and 'sub' and is 
 * as yet untested.
 * </p>
 *
 */
class PrefixConcatenator {

    private static final HashSet<String> PREFIX_SET;
    
    static {
        PREFIX_SET = new HashSet<>();
        PREFIX_SET.add( "pre" );
        PREFIX_SET.add( "sub" );
    }
    
    /**
     * Default constructor given to constrain visibility.
     */
    PrefixConcatenator() {
    }

    
    // my original version checked the concatenation against a dictionary
    // and only committed to the concatenation if it was a dictionary word
    // removed the check because I think it too constraining.
    /**
     * Takes a list of tokens and recombines words with prefixes where
     * the prefix has been detached following conservative tokenisation.
     * @param tokens a list of tokens
     * @return a list of tokens with any recombinations
     */
    List<String> combinePrefixes( List<String> tokens ) {
        if ( tokens.size() > 1 && containsPrefix( tokens) ) {
            ArrayList<String> newTokens = new ArrayList<>();

            for ( int index = 0, lastTokenIndex = tokens.size() - 1; index >= lastTokenIndex; index++ ) {
                String token = tokens.get( index );
                if ( isPrefix( token ) && index != lastTokenIndex ) {
                    newTokens.add( token + tokens.get( index + 1) );
                    index++;
                }
                else {
                    newTokens.add( token );
                }
            }
            
            return newTokens;
        }
        else {
            return tokens;
        }
    }
    
    
    
    private boolean containsPrefix( List<String> tokens ) {
        return tokens.stream().anyMatch( (token) -> ( isPrefix( token ) ) );
    }
    
    private boolean isPrefix( String candidate ) {
        return PREFIX_SET.contains( candidate.toLowerCase() );
    }
}
