/*
 Copyright (C) 2010-2015 The Open University
 Copyright (C) 2020 Simon Butler

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that manages the splitting of identifier names to tokens of
 * a single case.
 *
 */
class BasicTokeniser {

    private final ArrayList<String> words;

    private final AggregatedDictionary aggregatedDictionary;

    private final DigitAbbreviationDictionary digitAbbreviationDictionary;

    private final AbbreviationDictionary abbreviationDicitonary;

    private final NumericTokeniser numericTokeniser;

    private final HashSet<String> separatorCharactersSet;

    private static final Pattern lcUcPattern;
    private static final Pattern ucLcPattern;
    private static final Set<String> escapableCharacters;
    
    static {
	lcUcPattern = Pattern.compile("\\p{Ll}\\p{Lu}");
	ucLcPattern = Pattern.compile("\\p{Lu}\\p{Ll}");
        escapableCharacters = new HashSet<>();
        Collections.addAll(escapableCharacters, "<", "(", "[", "{", "\\", "^", "-", "=", "$", "!", "|", "]", "}", ")", "?", "*", "+", ".", ">");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger( BasicTokeniser.class );
    
    private final Pattern separatorPattern;
    
    /**
     * Creates an instance using a set of dictionaries and a set of 
     * separator characters.
     * @param dictionarySet a set of dictionaries
     * @param separatorCharacters a set of separator characters
     */
    BasicTokeniser ( DictionarySet dictionarySet, String separatorCharacters ) {
        this.words = new ArrayList<>();
        this.aggregatedDictionary = dictionarySet.getAggregatedDictionary();
        this.numericTokeniser = new NumericTokeniser( dictionarySet );
        this.digitAbbreviationDictionary = dictionarySet.getDigitAbbreviationDictionary();
        this.abbreviationDicitonary = dictionarySet.getAbbreviationDictionary();

        this.separatorCharactersSet = new HashSet<>();
        List<String> chars = new ArrayList<>();
        if ( separatorCharacters.isEmpty() ) {
            separatorCharacters = "_$";  // default to Java
        }
        for ( Integer index = 0; index < separatorCharacters.length(); index++ ) {
            String character = separatorCharacters.substring(index, index+1);
            if ( escapableCharacters.contains(character) ) {
                separatorCharactersSet.add(String.format("\\%s", character));
            }
            else {
                separatorCharactersSet.add( character );
            }
        }
        this.separatorPattern = Pattern.compile(separatorCharactersSet.stream()
						.collect(Collectors.joining("", "[", "]+")));
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
     * Tokenises a name aggressively according to the library configuration.
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


    private List<String> tokeniseOnSeparators( String name ) {
        List<String> splits = this.separatorPattern.splitAsStream(name)
	    .filter(s -> s.length() > 0)
	    .collect(Collectors.toList());

        return splits;
    }

    /**
     * Provides a naive camel case splitter to work on character only strings.
     *
     * @param identifierName a character only string
     * @return an Array list of components of the input string that result
     * from splitting on LCUC boundaries.
     */
    private ArrayList<String> tokeniseOnLowercaseToUppercase ( String identifierName ) {
	ArrayList<String> splits = new ArrayList<>();
        Matcher matcher = lcUcPattern.matcher(identifierName);
	int start = 0;
	while ( matcher.find() ) {
	    splits.add(identifierName.substring(start, matcher.end() - 1));
	    start = matcher.end() - 1;
	}
	splits.add(identifierName.substring(start, identifierName.length()));

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
    List<String> tokeniseOnUppercaseToLowercase ( ArrayList<String> tokens ) {
        List<String> splits = new ArrayList<>();

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

        Matcher matcher = ucLcPattern.matcher( token );

	return  matcher.find() ? matcher.start() : -1;
    }
}
