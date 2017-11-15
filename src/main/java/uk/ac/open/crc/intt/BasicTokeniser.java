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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that manages the splitting of identifier names to tokens of
 * a single case.
 *
 */
class BasicTokeniser {

    private final ArrayList<String> words;

    // internal flag used to indicate whether a digit or two have been found
    @Deprecated
    private boolean needsOracle;

    // the following stores data in the form (start, finish, start, ...)
    private final ArrayList<Integer> boundaries;

    private final AggregatedDictionary aggregatedDictionary;

    private final DigitAbbreviationDictionary digitAbbreviationDictionary;
    private final AbbreviationDictionary abbreviationDicitonary;

    private final NumericTokeniser numericTokeniser;

    private final HashSet<Integer> separatorCharactersSet;

    private static final Logger LOGGER = LoggerFactory.getLogger( BasicTokeniser.class );

    /**
     * Creates an instance using a set of dictionaries and a set of 
     * separator characters.
     * @param dictionarySet a set of dictionaries
     * @param separatorCharacters a set of separator characters
     */
    BasicTokeniser ( DictionarySet dictionarySet, String separatorCharacters ) {
        this.words = new ArrayList<>();
        this.needsOracle = false;
        this.boundaries = new ArrayList<>();
        this.aggregatedDictionary = dictionarySet.getAggregatedDictionary();
        this.numericTokeniser = new NumericTokeniser( dictionarySet );
        this.digitAbbreviationDictionary = dictionarySet.getDigitAbbreviationDictionary();
        this.abbreviationDicitonary = dictionarySet.getAbbreviationDictionary();

        this.separatorCharactersSet = new HashSet<>();
        for ( Integer index = 0; index < separatorCharacters.length(); index++ ) {
            separatorCharactersSet.add( separatorCharacters.codePointAt( index ) );
        }
    }

    // retain case is currently redundant as case is retained.
    // left in in case behaviour is modified later
    // indicated as deprecated for the moment
    @Deprecated
    List<String> naiveTokensationRetainCase ( String identifierName ) {
        List<String> tokens = tokeniseOnSeparators( identifierName );

        List<String> naiveTokens = tokeniseOnLowercaseToUppercase( tokens );

        return naiveTokens;
    }

    /**
     * Undertakes conservative tokenisation of a string.
     * @param identifierName a name to tokenise
     * @return a list of naive tokens
     */
    List<String> naiveTokensation ( String identifierName ) {
        List<String> tokens = tokeniseOnSeparators( identifierName );

        List<String> naiveTokens = tokeniseOnLowercaseToUppercase( tokens );

        return naiveTokens;
    }

    /**
     * Tokenises a name aggresively according to the library configuration.
     * @param identifierName a name to tokenise
     * @return a list of tokens
     */
    List<String> tokenise ( String identifierName ) {
        List<String> splits = tokeniseOnSeparators( identifierName );

        splits = tokeniseOnLowercaseToUppercase( splits );

        ArrayList<String> tokens = new ArrayList<>();
        for ( String split : splits ) {
            if ( split.matches( "^.*[0-9]+.*$" ) ) {
                tokens.addAll( tokeniseOnUppercaseToLowercase( this.numericTokeniser.tokenise( split, identifierName.endsWith( split ) ) ) );
            }
            else {
                tokens.addAll( tokeniseOnUppercaseToLowercase( split ) );
            }
        }

        return tokens;
    }

    // ----------- helpers -------------
    // now for the code that does the real work
    @Deprecated
    private void findBoundaries ( String identifierName ) {
        Integer currentChar;
        Boolean inPrefix = true;

        // now process the array looking for boundaries
        for ( Integer index = 0; index < identifierName.length(); index++ ) {
            currentChar = identifierName.codePointAt( index );

            if ( isSeparator( currentChar ) ) {
                // we have a character that
                //  (a) can be tossed
                //  (b) may have a boundary both before and after
                if ( inPrefix == false ) {
                    // when not in the prefix, we are one beyond the end
                    // of the previous component (mostly). So need to ensure
                    // that we're not in the middle of a series
                    // of separators
                    if ( index > 0 && !isSeparator( identifierName.codePointAt( index - 1 ) ) ) {
                        // (U|L|D)+S boundary
                        this.boundaries.add( index - 1 );
                        // NB: this also records the final char of an identifer
                        // with trailing underscores
                    }
                }

            }
            else if ( inPrefix == true ) {
                // we have the first character of the initial token of the
                // identifier
                inPrefix = false;
                this.boundaries.add( index );
                // now we need to add an additional check to account for
                // single character identifiers, whether they have prefixes
                // or not e.g. z and _z and $z
                if ( index == ( identifierName.length() - 1 ) ) {
                    boundaries.add( index );
                }
            }
            else {
                // now to worry about other boundaries
                // S+ -> (U|L|D)
                // D+ -> S // not dealt with explicitly, but may have value to signal a conventional break
                // L+ -> U+
                // UL and U+UL
                // optimised for most likely characters to reduce number
                // of tests at run time
                if ( Character.isLowerCase( currentChar ) ) {
                    if ( Character.isUpperCase( identifierName.codePointAt( index - 1 ) )
                            && index > 2
                            && Character.isUpperCase( identifierName.codePointAt( index - 2 ) ) ) {
                        // U+UL
                        this.boundaries.add( index - 2 );  // end on final char of U+
                        this.boundaries.add( index - 1 );  // begin on U
                    }
                    else if ( isSeparator( identifierName.codePointAt( index - 1 ) ) ) {
                        // SL boundary
                        this.boundaries.add( index );
                    }
                }
                else if ( Character.isUpperCase( currentChar ) ) {
                    if ( Character.isLowerCase( identifierName.codePointAt( index - 1 ) ) ) {
                        // LU
                        this.boundaries.add( index - 1 ); // end on L
                        this.boundaries.add( index ); // begin on U
                    }
                    else if ( isSeparator( identifierName.codePointAt( index - 1 ) ) ) {
                        // SU boundary
                        this.boundaries.add( index );
                    }
                }
                else if ( Character.isDigit( currentChar ) ) {
                    // D - so we need to refer to the oracle
                    // but mark an SD border if that is the case
                    if ( isSeparator( identifierName.codePointAt( index - 1 ) ) ) {
                        this.boundaries.add( index );
                    }

                }
                else {
                    // here we have other unicode characters in
                    // classes that we don't know how to handle
                    // e.g. other currency symbols, and connecting
                    // punctuation.
                    // So report this?
                    LOGGER.warn(
                            "Unexpected unicode character in: \"{}\"",
                            identifierName );
                }

                // now check whether this is the terminal character.
                // and record it to give us the final boundary
                if ( index == identifierName.length() - 1 ) {
                    this.boundaries.add( index );
                }
            }

        }

    }

    /**
     * Determines if the code point passed in is one of the string of separator
     * characters set in the constructor.
     *
     * @param codePoint a unicode code point.
     * @return true if the code point is part of the specified set of separator characters.
     *
     */
    private Boolean isSeparator ( Integer codePoint ) {
        return this.separatorCharactersSet.contains( codePoint );
    }

    private ArrayList<String> tokeniseOnSeparators ( String identifierName ) {
        ArrayList<String> splits = new ArrayList<>();

        ArrayList<Integer> candidateBoundaries = new ArrayList<>();

        Integer currentChar;

        // now process the string looking for boundaries
        for ( Integer index = 0; index < identifierName.length(); index++ ) {
            currentChar = identifierName.codePointAt( index );

            if ( isSeparator( currentChar ) ) {
                // we have a character that
                //  (a) can be tossed; and
                //  (b) may have a boundary both before and after
                // so, boundary before
                if ( index > 0
                        && !isSeparator( identifierName.codePointAt( index - 1 ) ) ) {
                    // (U|L|D)+S boundary
                    candidateBoundaries.add( index - 1 );
                    // NB: this also records the final char of an identifer
                    // with trailing underscores
                }

                // and boundary after
                // now to mark the separator to any character transition
                // using lookahead
                if ( index < ( identifierName.length() - 1 )
                        && !isSeparator( identifierName.codePointAt( index + 1 ) ) ) {
                    candidateBoundaries.add( index + 1 );
                }
            }
            else {
                if ( index == 0 ) {
                    candidateBoundaries.add( index );
                }

                // now check whether this is the terminal character.
                // and record it to give us the final boundary
                if ( index == identifierName.length() - 1 ) {
                    candidateBoundaries.add( index );
                }
            }
        }

        if ( candidateBoundaries.size() % 2 == 1 ) {
            LOGGER.warn(
                    "Odd number of boundaries found for: \"{}\"",
                    identifierName );
        }

        for ( int i = 0; i < candidateBoundaries.size(); i += 2 ) {
            splits.add(
                    identifierName.substring( candidateBoundaries.get( i ),
                            candidateBoundaries.get( i + 1 ) + 1 ) );
        }

        return splits;
    }

    /**
     * Provides a naive camel case splitter to work on character only string.
     *
     * @param identifierName a character only string
     * @return an Array list of components of the input string that result
     * from splitting on LCUC boundaries.
     */
    private ArrayList<String> tokeniseOnLowercaseToUppercase ( String identifierName ) {
        ArrayList<String> splits = new ArrayList<>();
        // the following stores data in pairs (start, finish, start, ...)
        ArrayList<Integer> candidateBoundaries = new ArrayList<>();

        // now process the array looking for boundaries
        for ( Integer index = 0; index < identifierName.length(); index++ ) {
            if ( index == 0 ) {
                // the first character is always a boundary
                candidateBoundaries.add( index );
            }
            else if ( Character.isUpperCase( identifierName.codePointAt( index ) )
                    && Character.isLowerCase( identifierName.codePointAt( index - 1 ) ) ) {
                candidateBoundaries.add( index - 1 );
                candidateBoundaries.add( index );
            }

            // now check whether this is the terminal character.
            // and record it to give us the final boundary
            if ( index == identifierName.length() - 1 ) {
                candidateBoundaries.add( index );
            }
        }

        if ( candidateBoundaries.size() % 2 == 1 ) {
            LOGGER.warn(
                    "Odd number of boundaries found for: \"{}\"",
                    identifierName );
        }

        for ( int i = 0; i < candidateBoundaries.size(); i += 2 ) {
            splits.add( identifierName.substring( candidateBoundaries.get( i ), candidateBoundaries.get( i + 1 ) + 1 ) );
//        // DEBUG
//            System.out.println(boundaries.get(i) + " -> " + boundaries.get(i+1));
//        // DEBUG
        }

        return splits;
    }

    /**
     * Wraps the single argument version of the method.
     *
     * @param tokens
     * @return
     */
    private List<String> tokeniseOnLowercaseToUppercase ( List<String> tokens ) {
        ArrayList<String> splits = new ArrayList<>();

        tokens.stream().forEach( (token) -> {
            splits.addAll( tokeniseOnLowercaseToUppercase( token ) );
        } );

        return splits;
    }

    private ArrayList<String> tokeniseOnUppercaseToLowercase ( String fragment ) {
        ArrayList<String> splits = new ArrayList<>();

        // the input fragment has been split at separators and lc/uc boundaries
        // so will be single case, contain a digit, or a UC/LC boundary
        // we examine UC/LC only
        // consider performing a simple sanity check to ensure that
        // we are not dealing with an unconventionally capitalised
        // abbreviation such as OSGi or a known digit abbreviation such as IPv6.
        if ( this.digitAbbreviationDictionary.isWord( fragment )
                || this.abbreviationDicitonary.isWord( fragment ) ) {
            splits.add( fragment );
        }
        else {

            Integer boundary = getUcLcBoundary( fragment );
            if ( boundary == -1 || boundary == 0 ) {
                splits.add( fragment );
            }
            // checking boundary == 0 is overly fussy, and probably unnecessary
            else {
                // now we are dealing with genuine U+UL+ form
                // !!unless there are digits in there!!
                // so we need to choose between the camel case split
                // and the alternating case split where there are no digits
                List<String> camelSplits = new ArrayList<>();
                camelSplits.add( fragment.substring( 0, boundary ) );
                camelSplits.add( fragment.substring( boundary ) );
                List<String> alternateSplits = new ArrayList<>();
                alternateSplits.add( fragment.substring( 0, boundary + 1 ) );
                alternateSplits.add( fragment.substring( boundary + 1 ) );

                if ( this.aggregatedDictionary.percentageKnown( camelSplits )
                        >= this.aggregatedDictionary.percentageKnown( alternateSplits ) ) {
                    splits.addAll( camelSplits );
                }
                else {
                    splits.addAll( alternateSplits );
                }
            }
        }

        return splits;
    }

    /**
     * Wraps the single argument version of the method.
     *
     * @param tokens a {@code List} of tokens
     *
     * @return a {@code List} of tokens where any tokens previously
     * containing UCLC boundaries have been split
     */
    ArrayList<String> tokeniseOnUppercaseToLowercase ( ArrayList<String> tokens ) {
        ArrayList<String> splits = new ArrayList<>();

        tokens.stream().forEach( (token) -> {
            splits.addAll( tokeniseOnUppercaseToLowercase( token ) );
        } );

        return splits;
    }

    /**
     * Locates a UCLC boundary (there is only one in the string) and reports
     * the position of the UC character.
     *
     * @param token a String that contains at most one UCLC boundary
     *
     * @return The integer position of the upper case character of a UCLC case
     * change. Or -1 if no UCLC boundary is found.
     */
    private Integer getUcLcBoundary ( String token ) {
        Integer boundary = -1;

        // we'll do this mechanically - there are, I think, neater solutions
        // e.g. there is a early out if the first character is not UC
        for ( Integer index = 0; index < ( token.length() - 1 ); index++ ) {
            if ( Character.isUpperCase( token.codePointAt( index ) )
                    && Character.isLowerCase( token.codePointAt( index + 1 ) ) ) {
                boundary = index;
                break;
            }
        }

        return boundary;
    }

}
