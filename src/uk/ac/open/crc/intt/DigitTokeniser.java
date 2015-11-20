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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tokenises fragments of identifier name containing digits.
 *
 * <p>
 * NB: this class is a barely started work in progress, and should not be used. 
 * The intention is to replace the 
 * functionality of NumericTokeniser with much cleaner and more readable code.
 * </p>
 *
 */
class DigitTokeniser {
    private final AggregatedDictionary aggregatedDictionary;
    private final DigitAbbreviationDictionary numericAbbreviationDictionary;
    
    private static final Logger LOGGER = LoggerFactory.getLogger( DigitTokeniser.class );

    
    DigitTokeniser( DictionarySet dictionarySet ) {
        this.aggregatedDictionary = dictionarySet.getAggregatedDictionary();
        this.numericAbbreviationDictionary = dictionarySet.getDigitAbbreviationDictionary();
        
    }
    
    
    ArrayList<String> tokenise(String identifierNameFragment, boolean isLastToken) {
        throw new UnsupportedOperationException( "Functionality not yet implemented" );
    }
    
    
    /**
     * Utility method for splitting identifier name fragments containing
     * digits. The input will be a string lacking camel case or separator
     * characters that may contain one or more runs of one or more digits.
     * 
     * @param fragment a fragment of a name
     * @return a list of tokens
     */
    private ArrayList<String> tokeniseOnDigits( String fragment ) {
        throw new UnsupportedOperationException( "Functionality not yet implemented" );
    }


    // is the assumption correct
    /**
     * Locates a UCLC boundary (there is only one in the string) and reports
     * the position of the UC character. There is also a sanity check that
     * a UCLC boundary is not a simple camel case boundary i.e. we
     * have U+UL at all positions other than 0
     *
     * @param token a String that contains at most one UCLC boundary
     *
     * @return  The integer position of the upper case character of a UCLC case
     *          change. Or -1 if no UCLC boundary is found.
     */
    private Integer getUcLcBoundary(String token) {
        Integer boundary = -1;

        // we'll do this mechanically - there are, I think neater solutions
        // e.g. there is a early out if the first character is not UC
        for (Integer index = 0; index < (token.length() - 1); index++) {
            if (Character.isUpperCase(token.codePointAt(index))
                    && Character.isLowerCase(token.codePointAt(index + 1))) {
                // make sure that we're not reporting a camel case
                // boundary by accident
                if (index ==  0 || Character.isUpperCase(token.codePointAt(index - 1))) {
                    boundary = index;
                    break;
                }
            }
        }

        return boundary;
    }

    // work in progress.
    /**
     * Returns a list of tokens for a string of the form LDLDLDL(D).
     *
     * <p>The intention of this is laudable. However it may well break 
     * spectacularly as the right binding branch has not been tested properly 
     * -- and appears to be unreachable.</p>
     *
     * @param fragment a String of the form LDL(D|DL)?
     * @return a list of tokens
     */
    private ArrayList<String> splitMixedString( String fragment ) {
        throw new UnsupportedOperationException( "Functionality not yet implemented" );
    }

}
