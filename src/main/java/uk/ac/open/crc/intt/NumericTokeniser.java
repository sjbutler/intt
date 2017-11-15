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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a semi-greedy mechanism for splitting identifier names
 * that contain abbreviations containing digits.
 *
 */
class NumericTokeniser {

    private final AggregatedDictionary aggregatedDictionary;
    private final DigitAbbreviationDictionary numericAbbreviationDictionary;

    private static final Logger LOGGER = LoggerFactory.getLogger( NumericTokeniser.class );

    /**
     * Creates a tokeniser with an associated set of dictionaries.
     * @param dictionarySet a set of dictionaries
     */
    NumericTokeniser ( DictionarySet dictionarySet ) {
        this.aggregatedDictionary = dictionarySet.getAggregatedDictionary();
        this.numericAbbreviationDictionary = 
                dictionarySet.getDigitAbbreviationDictionary();
    }

    /**
     * Takes a single token as an argument. The token is left over following
     * tokenisation on separators, LCUC and UCLC boundaries. i.e the token
     * is something containing one or more digit, and one or more runs of digits.
     *
     * @param nameFragment part of a name containing digits
     * @param isLastToken  indicates if the token terminates the identifier name.
     * @return a list of tokens
     */
    // TODO - read and revise. Refactor into smaller methods.
    ArrayList<String> tokenise ( String nameFragment, boolean isLastToken ) {
        ArrayList<String> tokens = new ArrayList<>();
        // establish whether we are dealing with a known numeric abbreviation
        // or abbreviations
        List<String> knownSubstrings = 
                this.numericAbbreviationDictionary.findKnownSubstrings( nameFragment );
        if ( ! knownSubstrings.isEmpty() ) {
            // look again at the boundaries and revise them with the known
            // abbreviations as guides.
            // NB this heuristic fails where a know numeric abbreviation is
            // a substring of something else. e.g. isXpp3Dom splits to {is, Xpp3, Dom}
            // this poses a problem if both Xpp3 and 3d are known abbreviations as
            // the following code is not sufficiently thorough as it does not consider
            // the case of overlapping strings or differentiate between the
            // possible tokenisations.
            // to be revised in next version
            if ( knownSubstrings.size() == 1 ) {
//                System.out.println(knownSubstrings[0] + "--");
                // relatively simple algorithm
                // first check that we don't have the abbreviation as an isolated string
                if ( knownSubstrings.get( 0 ).equals( nameFragment.toLowerCase() ) ) {
                    tokens.add( nameFragment );
                }
                else {
                    // So we have something to split
                    int index = nameFragment.toLowerCase().indexOf( knownSubstrings.get( 0 ).toLowerCase() );
//                    System.out.println("-- " + index + " --");
                    // so the abbreviation is in there somewhere
                    // (1) fetch leading fragment 
                    // (2) add in abbreviation
                    // (3) fetch trailing fragment
                    String leadingFragment = nameFragment.substring( 0, index );
                    if ( leadingFragment.length() > 0 ) {
                        tokens.add( leadingFragment );
                    }
                    tokens.add( nameFragment.substring( index, index + knownSubstrings.get( 0 ).length() ) );
                    String trailingFragment = nameFragment.substring( index + knownSubstrings.get( 0 ).length() );
                    if ( trailingFragment.length() > 0 ) {
                        tokens.add( trailingFragment );
                    }
                }

            }
            else {
                // two or more identified digit abbreviations
                // general case is more complex
                // adapt the above Leading|Abbreviation|Trailing
                // to F*(AF*)+ where F is fragment and A is abbreviation
                // so the cardinality of F = A + 1 at most (fencepost problem)
                // !!! the fatal assumption is that the abbreviations are
                // discrete. As noted above, they may overlap !!!
                // SO some thought needs to be given to making this section
                // more sophisticated. Maybe the solution is recursive
                // e.g. split on each in order, then see what fragments remain.
                // !!! must revise to handle ambiguity !!!
                String fragment = null;
                int startIndex = 0;
                int endIndex = 0;
                for ( int i = 0; i < knownSubstrings.size(); i++ ) {
                    endIndex = nameFragment.toLowerCase().indexOf( knownSubstrings.get( i ) );
                    // DEBUG - begin
                    // System.out.println(startIndex + "-->" + endIndex);
                    // DEBUG - end
                    fragment = nameFragment.substring( startIndex, endIndex ); // !!! TODO rare calls to this line cause String to through ArrayOutOfBounds. Review code
                    if ( fragment.length() > 0 ) {
                        tokens.add( fragment );
                    }
                    fragment = nameFragment.substring( endIndex, endIndex + knownSubstrings.get( i ).length() );
                    tokens.add( fragment ); // no guard as this should never be zero length
                    // set the start for the beginning of thge next iteration
                    startIndex = endIndex + knownSubstrings.get( i ).length();
                }

                // now handle the trailing fragment, if any
                fragment = nameFragment.substring( startIndex, nameFragment.length() );
                if ( fragment.length() > 0 ) {
                    tokens.add( fragment );
                }
            }

        }
        else {
            // We are dealing with an unknown mixture of digits and
            // words

            if ( nameFragment.matches( "^[a-zA-Z]+[0-9]+$" ) == true ) {
                // we have a single run of characters, followed by a
                // one or more digits. This fragment can only have been
                // at the RH end of a camel case identifer name and thus we
                // can assume that the digits are trailing and we can assume
                // (largely) that they are some form of index.
                // HOWEVER, the token may be a token from the middle of
                // an underscore separated name and
                // SO
                if ( isLastToken == true ) {
                    tokens.addAll( tokeniseOnDigits( nameFragment ) );
                }
                else {
                    tokens.add( nameFragment );
                }
            }
            else if ( nameFragment.matches( "^[0-9]+[a-zA-Z]+$" ) ) {
                // The provision for leading digits is necessary in practise
                // as there are some examples out there.
                // Are they meaningful??
                // What to do with them

                tokens.addAll( tokeniseOnDigits( nameFragment ) );

            }
            else if ( nameFragment.matches( "^.+[0-9]+.+$" ) == true ) {
                // is the test necessary?

                // We have embedded digit(s) that we don't recognise
                // this includes things like Right5Out where the digit
                // either leads or trails the abbreviation
                // these can be split by using conventional boundaries, and
                // the dictionary.
                // e.g. a possible split is {Right5, Out}
                // we may also have leading and trailing digits as well
                // as the embedded.
                // 
                // The digit(s) will be embedded in one of the candidate words
                ArrayList<String> candidatesA = new ArrayList<>();

                // Eliminate those with embedded digits
                // clearly intended by the author
                // these are of the form A1A and A1a
                // i.e. simple tokens with embedded digits (the former may only
                // occur as the final token or as the entire name
                if ( nameFragment.matches( "^[A-Z][0-9][A-Za-z]$" ) ) {
                    tokens.add( nameFragment );
                }
                else { 
                    // there may be a UC/LC boundary
                    // we could do a crude split as camel case and would
                    // be correct most of the time, but ...
                    //
                    Integer ucLcBoundary = getUcLcBoundary( nameFragment );
                    if ( ucLcBoundary > 0 ) {
                        tokens.addAll( tokeniseOnUcLcBoundary( nameFragment, ucLcBoundary, isLastToken ) );
                    }
                    else {
                        // split the string on digits
                        // which gives at least one pair of words
                        // separated by a digit or string of digits
                        tokens.addAll( splitMixedString( nameFragment ) );
                    }
                }

                tokens.addAll( candidatesA );

                // text-speak - now check that there isn't a cleaner split
                // available with text speak
                // (U|P|R)+(2|4)(U|P|R)+ - where U is unrecognised, P project and R recognised words
                // U may be P at this point so the compromise is to test R+(2|4)R+
                // each of the recognised strings may consist of a number of
                // substrings, so the above algorithm can be used to split these.
                // need to compare this with the above result to see which is the
                // 'better' split
                //
                if ( nameFragment.matches( "^[a-zA-Z]+(2|4)[a-zA-Z]+$" ) ) {
                    List<String> textSpeakWords = textSpeakSplit( nameFragment );
                    // now establish if this is a better split, than the embedded
                    // digits gives
                    int conventionalScore = 
                            this.aggregatedDictionary.percentageKnown( tokens );
                    int textSpeakScore = 
                            this.aggregatedDictionary.percentageKnown( textSpeakWords );

                    if ( textSpeakScore >= conventionalScore ) {
                        // favour the text-speak solution
                        tokens.clear(); // this is to allow the previous sections to be ignored if there is text speak
                        // results to be ignored for the moment 
                        //   -- needs more detailed tests
                        tokens.addAll( textSpeakWords );
                    }
                }
                // NB the text-speak code only deals with the first text-speak
                // digit in a name - separated digits are not catered for. 
                // Do they occur in practice?
            }
            else {
                // if we cannot split, admit defeat and return the
                // identifier name fragment as the sole member of the list.
                tokens.add( nameFragment );
            }
        }

        return tokens;
    }

    
    private List<String> tokeniseOnUcLcBoundary( 
            String fragment, 
            int boundary, 
            boolean isLastToken ) {
        // look at the camel case split
        Boolean camelCase = 
                this.aggregatedDictionary.isWord( fragment.substring( 0, boundary ) ) 
                || this.aggregatedDictionary.isWord( fragment.substring( boundary ) );
        Boolean alternateCase = 
                this.aggregatedDictionary.isWord( fragment.substring( 0, boundary + 1 ) ) 
                || this.aggregatedDictionary.isWord( fragment.substring( boundary + 1 ) );
        List<String> fragments = new ArrayList<>();
        if ( camelCase == true ) {
            // split camel case and check which fragment has the digit
            fragments.add( fragment.substring( 0, boundary ) );
            fragments.add( fragment.substring( boundary ) );
        }
        else {
            // review -- why assume that if camel case is incorrect 
            // then the alternate case split is better? 
            // Is it really the case? Or a gamble?
            fragments.add( fragment.substring( 0, boundary + 1 ) );
            fragments.add( fragment.substring( boundary + 1 ) );
        }

        List<String> tokens = new ArrayList<>();
        if ( fragments.get( 0 ).matches( "^.*[0-9]+.*$" ) ) {
            tokens.addAll( tokenise( fragments.get( 0 ), false ) );
            tokens.add( fragments.get( 1 ) );
        }
        else {
            tokens.add( fragments.get( 0 ) );
            tokens.addAll( tokenise( fragments.get( 1 ), isLastToken ) );
        }
        
        return tokens;
    }
    
    // the contract is that the argument must match
    // the regular expression "^[a-zA-Z]+(2|4)[a-zA-Z]+$"
    private List<String> textSpeakSplit( String fragment ) {
        int textSpeakDigitIndex = fragment.indexOf( "2" );
        if ( textSpeakDigitIndex == -1 ) {
            textSpeakDigitIndex = fragment.indexOf( "4" );
        }

        List<String> textSpeakWords = new ArrayList<>();
        textSpeakWords.add( fragment.substring( 0, textSpeakDigitIndex ) );
        textSpeakWords.add( fragment.substring( textSpeakDigitIndex, textSpeakDigitIndex + 1 ) );
        textSpeakWords.add( fragment.substring( textSpeakDigitIndex + 1 ) );

        return textSpeakWords;
    }
    
    
    /**
     * Utility method for splitting identifier name fragments containing
     * digits. The input will be a string lacking camel case or separator
     * characters that may contain one or more runs of one or more digits.
     *
     * @param fragment a name fragment
     * @return a list of tokens
     */
    private ArrayList<String> tokeniseOnDigits ( String fragment ) {
        ArrayList<String> tokens = new ArrayList<>();
        ArrayList<Integer> boundaries = new ArrayList<>();

        // now process the listy looking for boundaries
        for ( Integer index = 0; index < fragment.length(); index++ ) {
            // mark the beginning of the string
            if ( index == 0 ) {
                boundaries.add( index );
            }

            if ( Character.isDigit( fragment.codePointAt( index ) ) ) {
                // we mark two boundaries - the beginning
                // and end of a run of one or more digits

                // we are the first of a run of digits,
                // i.e. there is a preceding character
                if ( index > 0 && !Character.isDigit( fragment.codePointAt( index - 1 ) ) ) {
                    boundaries.add( index - 1 );
                    boundaries.add( index );
                }

                // we are the last of a run of digits
                // i.e. there is a following character
                if ( index < ( fragment.length() - 1 )
                        && !Character.isDigit( fragment.codePointAt( index + 1 ) ) ) {
                    boundaries.add( index );
                    boundaries.add( index + 1 );
                }
            }

            // NB: this MUST follow the digit check
            // now check whether this is the terminal character.
            // and record it to give us the final boundary
            if ( index == fragment.length() - 1 ) {
                boundaries.add( index );
            }
        }

        if ( boundaries.size() % 2 == 1 ) {
            LOGGER.warn( "Odd number of boundaries found for: \"{}\"", fragment );
        }

        for ( int i = 0; i < boundaries.size(); i += 2 ) {
            tokens.add( fragment.substring( boundaries.get( i ), boundaries.get( i + 1 ) + 1 ) );
        }

        return tokens;
    }

    /**
     * Locates a UCLC boundary (there is only one in the string) and reports
     * the position of the UC character. There is also a sanity check that
     * a UCLC boundary is not a simple camel case boundary i.e. we
     * have U+UL at all positions other than 0
     *
     * @param token a String that contains at most one UCLC boundary
     *
     * @return The integer position of the upper case character of a UCLC case
     * change. Or -1 if no UCLC boundary is found.
     */
    private Integer getUcLcBoundary ( String token ) {
        Integer boundary = -1;

        // we'll do this mechanically - there are, I think neater solutions
        // e.g. there is a early out if the first character is not UC
        for ( Integer index = 0; index < ( token.length() - 1 ); index++ ) {
            if ( Character.isUpperCase( token.codePointAt( index ) )
                    && Character.isLowerCase( token.codePointAt( index + 1 ) ) ) {
                // make sure that we're not reporting a camel case
                // boundary by accident
                if ( index == 0 || Character.isUpperCase( token.codePointAt( index - 1 ) ) ) {
                    boundary = index;
                    break;
                }
            }
        }

        return boundary;
    }

    /**
     * Returns a list of tokens for a string of the form LDLDLDL(D).
     *
     * <p>
     * The intention of this is laudable. However it may well break spectacularly
     * as the right binding branch has never been tested properly</p>
     *
     * @param fragment a String of the form LDL(D|DL)?
     * @return a list of tokens
     */
    private ArrayList<String> splitMixedString ( String fragment ) {
        ArrayList<String> tokens = new ArrayList<>();

        ArrayList<String> fragments = tokeniseOnDigits( fragment );

        if ( fragments.size() == 2 ) {
            // this should be entirely unnecessary
            tokens.add( fragments.get( 0 ) + fragments.get( 1 ) );
        }
        else {
            for ( Integer i = 0, base; i < fragments.size(); ) {
                // is there an RH candidate?
                if ( i + 2 < fragments.size() ) {
                    // check the RH candidate
                    if ( this.aggregatedDictionary.isWord( fragments.get( i + 2 ) ) ) {
                        tokens.add( fragments.get( i ) + fragments.get( i + 1 ) );
                        i += 2;
                    }
                    else {
                        if ( this.aggregatedDictionary.isWord( fragments.get( i ) ) ) {
                            // concatenate the digits with the RH frag
                            tokens.add( fragments.get( i ) );
                            tokens.add( fragments.get( i + 1 ) + fragments.get( i + 2 ) );
                            base = i + 3; // i.e. the fragment following
                            i += 4; // skip to the next non-digit frag
                            // if there are any more fragments the digits must
                            // now bind to the right
                            while ( base < fragments.size() ) {
                                if ( i < fragments.size() ) {
                                    tokens.add( fragments.get( base ) + fragments.get( i ) );
                                }
                                else {
                                    // the base points to the final token
                                    tokens.add( fragments.get( base ) );
                                }
                                base += 2;
                                i += 2;
                            }
                        }
                        else {
                            // both are unknown so attach the digits to the left
                            tokens.add( fragments.get( i ) + fragments.get( i + 1 ) );
                            i += 2;
                        }
                    }
                }
                else {
                    // we have an orphaned token or two
                    if ( i < fragments.size() - 1 ) {
                        tokens.add( fragments.get( i ) + fragments.get( i + 1 ) );
                    }
                    else {
                        tokens.add( fragments.get( i ) );
                    }

                    i += 2;
                }
            }
        }

        return tokens;
    }
}
