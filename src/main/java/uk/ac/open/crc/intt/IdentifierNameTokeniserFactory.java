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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Review
/**
 * Provides an API for configuring and creating {@code IdentifierNameTokeniser}
 * objects.
 *
 * <p>
 * The default configuration consists of the default dictionary, a set of separator
 * characters ('$' and '_').
 * </p>
 *
 * <p>
 * The default dictionary consists of:</p>
 * <ul>
 * <li>A main dictionary composed of some 300,000 words from the SCOWL word lists
 * (up to size 80) for English with American and Canadian spelling variants
 * (<a href="http://wordlist.sourceforge.net/scowl-readme">http://wordlist.sourceforge.net/scowl-readme</a>),
 * with an additional list of 160 computing terms and Java
 * neologisms. The wordlists used are part of the mdsc library which is a 
 * dependency of intt and can be found at 
 * <a href="https://github.com/sjbutler/mdsc">https://github.com/sjbutler/mdsc</a>.</li>
 * <li>A list of 189 (common) computing abbreviations and acronyms.</li>
 * <li>A list of 74 abbreviations and acronyms containing digits.</li>
 * <li>A list of 88 English language prefixes.</li>
 * <li>A list of 352 English language suffixes.</li>
 * </ul>
 *
 * <p>
 * Each component of the dictionary may be replaced independently. For example,
 * the main dictionary could be replaced with a word list created using the
 * word lists provided with Ispell.
 * </p>
 *
 * <p>
 * <strong>Alternative separators</strong><br>
 * Sets of separator characters can be specified to replace the default set
 * of '$' and '_'. e.g. for Lisp the set could be specified as '-' and for
 * R as '.'.
 * </p>
 *
 * <p>
 * <strong>Alternative dictionaries</strong><br>
 * INTT uses five separate dictionaries, each of which can be replaced using
 * the factory class. The main dictionary, which is usually large can be 
 * extracted from files either in persistent storage or within a jar file. 
 * The file should consist of one entry per line.
 * The other four dictionaries are for abbreviations, abbreviations
 * containing digits, prefixes and suffixes. These dictionaries are specified as
 * lists of strings arrays because they consist of relatively few terms,
 * e.g. 189 in the default technical abbreviation dictionary. The class 
 * {@linkplain uk.ac.open.crc.intt.text.WordListReader} provides a simple
 * API for reading dictionary files that include comments.
 * </p>
 *
 * <p>
 * When replacing dictionaries each component's function should be carefully 
 * considered. The main dictionary and the two abbreviations dictionaries are
 * used to support the tokenisation of single case identifier names through
 * the identification of known words or abbreviations. The prefix and suffix
 * dictionaries are used in conjunction with the main dictionary to look for
 * simple neologisms formed by concatenation that do not require morphological
 * changes, e.g. "zoomable" where "zoom" is an entry in the main dictionary
 * and "able" is found in the suffix dictionary. Entries in the suffix and
 * prefix dictionaries should be bound morphemes.
 * </p>
 *
 * <p>
 * The main dictionary can be replaced with a word list for any
 * natural language where the character set can be represented entirely in
 * UTF-16. The prefix and suffix dictionaries should be replaced accordingly.
 * The tokeniser should then work with languages other than English, with
 * the constraint that morphological rules are not followed when testing for
 * neologisms, only simple concatenation is used.
 * </p>
 *
 * <p>
 * The project vocabulary is an empty dictionary that can be configured to have tokens
 * added during tokenisation ({@linkplain #setProjectVocabularyThreshold(int)}). The
 * project vocabulary is used in conjunction with the main and abbreviation
 * dictionaries in the tokenisation of single case identifier names.
 * The project vocabulary can also be populated with an initial vocabulary 
 * prior to tokenisation that
 * can be used as a domain- or project-specific dictionary. NB the project 
 * vocabulary is switched off by default and can be enabled using the 
 * method {@linkplain #setProjectVocabularyThreshold(int)} to zero or a 
 * positive value.
 * </p>
 *
 */
public final class IdentifierNameTokeniserFactory {

    private DictionarySet dictionarySet;

    private String separatorCharacters = "$_";

    private int projectVocabularyThreshold = -1;

    private static final Logger LOGGER
            = LoggerFactory.getLogger( IdentifierNameTokeniserFactory.class );

    private boolean recursiveSplit = false;

    private boolean expandModals = false;

    /**
     * Creates an instance of {@code IdentifierNameTokeniserFactory}.
     *
     * The default state is:
     * <ul>
     * <li>Dictionaries - the default set</li>
     * <li>Separators - '$' and '_'</li>
     * <li>Project vocabulary - empty, and disabled.</li>
     * </ul>
     *
     * @throws IllegalStateException if the default main dictionary cannot be
     * created. This is most likely the result of a problem with the jar file.
     */
    public IdentifierNameTokeniserFactory () {
        try {
            this.dictionarySet = new DictionarySet();
        }
        catch ( IOException ioEx ) {
            ioEx.printStackTrace( System.err );
            LOGGER.error( "Unable to instantiate the default main dictionary." );
            throw new IllegalStateException( 
                    "Unable to instantiate the default main dictionary.", ioEx );
        }
    }

    /**
     * Specifies the set of separator characters to used to separate words in
     * identifier names. The default value is "$_".
     *
     * @param separatorCharacters a {@code String} of characters used to separate
     *                            words in identifier names. The empty string
     *                            is an acceptable argument and means that no
     *                            separator characters are used.
     *
     * @throws IllegalArgumentException when the argument is null.
     *
     */
    public void setSeparatorCharacters ( String separatorCharacters ) {
        if ( separatorCharacters == null ) {
            throw new IllegalArgumentException( 
                    "Null reference passed to setSeparatorCharacters()" );
        }

        this.separatorCharacters = separatorCharacters;
    }

    /**
     * Sets the minimum length of component word to be added to the project
     * vocabulary. Component words with the same or more characters than the
     * threshold are added to the project vocabulary following the tokenisation
     * of an identifier name.
     *
     * <p>
     * The project vocabulary is used to support the
     * tokenisation of single case identifier names by giving
     * preference to terms used in the same project to resolve ambiguous
     * tokenisations. The project vocabulary is disabled by default.
     * </p>
     *
     * <p>
     * The threshold prevents short tokens being recorded in the project vocabulary.
     * Large numbers of short tokens can result in oversplitting of single
     * case identifier names.
     * </p>
     *
     * @param minimumWordLength the minimum length of a word to be added to
     *                          the project vocabulary. 0 allows all words to
     *                          be added. A negative value turns off the
     *                          project vocabulary.
     *
     */
    public void setProjectVocabularyThreshold ( int minimumWordLength ) {
        this.projectVocabularyThreshold = minimumWordLength;
    }

    /**
     * Sets the source of a replacement for the main dictionary. The file or
     * resource referenced should consist of one word per line.
     *
     * @param source an instance of {@code BufferedReader} that points to a
     *               file either within a jar file or on a storage medium.
     * @throws IOException from {@link java.io.BufferedReader}
     * @throws FileNotFoundException from {@link java.io.BufferedReader}
     * @throws IllegalArgumentException if a {@code null} argument is
     * passed to the method.
     */
    public void setMainDictionarySource ( BufferedReader source )
            throws IOException, FileNotFoundException {
        if ( source == null ) {
            throw new IllegalArgumentException( "Null source for main dictionary" );
        }

        this.dictionarySet.setMainDictionary( new MainDictionary( source ) );
    }

    /**
     * Sets the source of a replacement for the main dictionary. The file or
     * resource referenced should consist of one word per line.
     *
     * @param source an instance of {@code BufferedReader} that points to a
     *               file either within a jar file or on a storage medium.
     * @param name   a {@code String} used to identify the dictionary.
     *               Output by the {@link #toString()} method.
     * @throws IOException from {@link java.io.BufferedReader}
     * @throws FileNotFoundException from {@link java.io.BufferedReader}
     * @throws IllegalArgumentException if a {@code null} argument is
     * passed to the method.
     */
    public void setMainDictionarySource ( BufferedReader source, String name )
            throws IOException, FileNotFoundException {
        if ( source == null ) {
            throw new IllegalArgumentException( 
                    "Null source for main dictionary" );
        }

        this.dictionarySet.setMainDictionary( new MainDictionary( source, name ) );
    }

    /**
     * Sets the source of a replacement prefix dictionary.
     *
     * @param prefixes an {@code List} of {@code String} objects where each member
     *                 is a separate prefix.
     *
     * @throws IllegalArgumentException if a {@code null} argument or an
     * empty array is passed to the method.
     */
    public void setPrefixDictionarySource ( List<String> prefixes ) {
        if ( prefixes == null || prefixes.isEmpty() ) {
            throw new IllegalArgumentException( 
                    "Null or zero length prefix source list" );
        }

        this.dictionarySet.setPrefixDictionary( 
                new PrefixDictionary( 
                        prefixes, 
                        this.dictionarySet.getAggregatedDictionary() ) );
    }

    /**
     * Sets the source of a replacement prefix dictionary.
     *
     * @param prefixes an array of {@code String} objects where each member
     *                 is a separate prefix.
     * @param name     a {@code String} used to identify the dictionary.
     *                 Output by the {@code toString()} method.
     *
     * @throws IllegalArgumentException if a {@code null} argument or an
     * empty list is passed to the method.
     */
    public void setPrefixDictionarySource ( List<String> prefixes, String name ) {
        if ( prefixes == null || prefixes.isEmpty() ) {
            throw new IllegalArgumentException( 
                    "Null or zero length prefix source array" );
        }

        this.dictionarySet.setPrefixDictionary( 
                new PrefixDictionary( 
                        prefixes, 
                        this.dictionarySet.getAggregatedDictionary(), 
                        name ) );
    }

    /**
     * Sets the source of a replacement suffix dictionary.
     *
     * @param suffixes an array of {@code String} objects where each member
     *                 is a separate suffix.
     *
     * @throws IllegalArgumentException if a {@code null} argument or an
     * empty list is passed to the method.
     */
    public void setSuffixDictionarySource ( List<String> suffixes ) {
        if ( suffixes == null || suffixes.isEmpty() ) {
            throw new IllegalArgumentException( 
                    "Null or zero length suffix source list" );
        }

        this.dictionarySet.setSuffixDictionary( 
                new SuffixDictionary( 
                        suffixes, 
                        this.dictionarySet.getAggregatedDictionary() ) );
    }

    /**
     * Sets the source of a replacement suffix dictionary.
     *
     * @param suffixes an array of {@code String} objects where each member
     *                 is a separate suffix.
     * @param name     a {@code String} used to identify the dictionary.
     *                 Output by the {@code toString()} method. 
     *
     * @throws IllegalArgumentException if a {@code null} argument or an
     * empty array is passed to the method.
     */
    public void setSuffixDictionarySource ( List<String> suffixes, String name ) {
        if ( suffixes == null || suffixes.isEmpty() ) {
            throw new IllegalArgumentException( 
                    "Null or zero length suffix source array" );
        }

        this.dictionarySet.setSuffixDictionary( 
                new SuffixDictionary( 
                        suffixes, 
                        this.dictionarySet.getAggregatedDictionary(), 
                        name ) );
    }

    /**
     * Sets the source of a replacement digit abbreviations dictionary.
     * Abbreviations containing digits are used to support the tokenisation
     * of identifier names with digits.
     *
     * @param digitAbbreviations an array of {@code String} objects where each member
     *                           is a separate digit containing abbreviation.
     *
     * @throws IllegalArgumentException if a {@code null} argument, an
     * empty list is passed to the method, or
     * any entry does not contain at least one digit.
     */
    public void setDigitAbbreviationsSource ( List<String> digitAbbreviations ) {
        if ( digitAbbreviations == null || digitAbbreviations.isEmpty() ) {
            throw new IllegalArgumentException( 
                    "Null or zero length digit abbreviation source array" );
        }

        for ( String entry : digitAbbreviations ) {
            if ( !entry.matches( "^.*[0-9]+.*$" ) ) {
                throw new IllegalArgumentException( 
                        "Digit not found in entry \"" + entry + "\"" );
            }
        }

        this.dictionarySet.setDigitAbbreviationDictionary( 
                new DigitAbbreviationDictionary( digitAbbreviations ) );
    }

    /**
     * Sets the source of a replacement digit abbreviations dictionary.
     * Abbreviations containing digits are used to support the tokenisation
     * of identifier names with digits.
     *
     * @param digitAbbreviations an array of {@code String} objects where each member
     *                           is a separate digit containing abbreviation.
     * @param name               a {@code String} used to identify the dictionary.
     *                           Output by the {@code toString()} method.
     *
     * @throws IllegalArgumentException if a {@code null} argument, an
     * empty list is passed to the method, or
     * any entry does not contain at least one digit.
     */
    public void setDigitAbbreviationsSource ( 
            List<String> digitAbbreviations, 
            String name ) {
        if ( digitAbbreviations == null || digitAbbreviations.isEmpty() ) {
            throw new IllegalArgumentException( 
                    "Null or zero length digit abbreviation source array" );
        }

        for ( String entry : digitAbbreviations ) {
            if ( !entry.matches( "^.*[0-9]+.*$" ) ) {
                throw new IllegalArgumentException( 
                        "Digit not found in entry \"" + entry + "\"" );
            }
        }

        this.dictionarySet.setDigitAbbreviationDictionary( 
                new DigitAbbreviationDictionary( digitAbbreviations, name ) );
    }

    /**
     * Sets the source of a replacement abbreviation dictionary.
     *
     * @param abbreviations a {@code List} of {@code String} objects where each member
     *                      is a separate abbreviation.
     *
     * @throws IllegalArgumentException if a {@code null} argument or an
     * empty list is passed to the method.
     */
    public void setAbbreviationsSource ( List<String> abbreviations ) {
        if ( abbreviations == null || abbreviations.isEmpty() ) {
            throw new IllegalArgumentException(
                    "Null or empty abbreviation list" );
        }

        this.dictionarySet.setAbbreviationDictionary( 
                new AbbreviationDictionary( abbreviations ) );
    }

    /**
     * Sets the source of a replacement abbreviation dictionary.
     *
     * @param abbreviations a {@code List} of {@code String} objects where each member
     *                      is a separate abbreviation.
     * @param name          a {@code String} used to identify the dictionary.
     *                      Output by the {@code toString()} method.
     *
     * @throws IllegalArgumentException if a {@code null} argument or an
     * empty list is passed to the method.
     */
    public void setAbbreviationsSource ( List<String> abbreviations, String name ) {
        if ( abbreviations == null || abbreviations.isEmpty() ) {
            throw new IllegalArgumentException(
                    "Null or empty abbreviation list" );
        }

        this.dictionarySet.setAbbreviationDictionary( new AbbreviationDictionary( abbreviations, name ) );
    }

    /**
     * Sets the source of a replacement project vocabulary.
     *
     * <p>
     * The replacement vocabulary can be used to introduce a domain or project
     * specific vocabulary to support the tokenisation of single case
     * identifier names.
     * </p>
     *
     * @param vocabulary an array of {@code String} objects where each member
     *                   is a separate entry.
     *
     * @throws IllegalArgumentException if a {@code null} argument or an
     * empty list is passed to the method.
     */
    public void setProjectVocabularySource ( List<String> vocabulary ) {
        if ( vocabulary == null || !vocabulary.isEmpty() ) {
            throw new IllegalArgumentException( "Null or empty vocabulary source list" );
        }

        this.dictionarySet.setProjectVocabulary( new ProjectVocabulary( vocabulary ) );
    }

    /**
     * Configures the tokeniser to apply the algorithms used to split single-case
     * identifier names to component words that are
     * not found in the dictionary. For example, {@code GUIScrollbarbutton} is
     * split to {@code gui,scrollbarbutton} by default. This behaviour,
     * if enabled, would try to tokenise {@code scrollbarbutton}.
     * <p>
     * This behaviour is experimental and is turned off by default. However, 
     * despite the potential to be noisy, the behaviour should be switched on
     * for analysis.
     * </p>
     */
    public void setRecursiveSplitOn () {
        this.recursiveSplit = true;
    }

    /**
     * Configures the tokeniser apply the split component words only on explicit
     * word boundaries, and only to attempt to apply more aggressive techniques 
     * to single token names that are not found in the dictionary. For 
     * example, with this switched off {@code GUIScrollbarbutton} is
     * split to {@code gui,scrollbarbutton}, but attempts are made to tokenise 
     * names that are single case compounds such as {@code thenewestone}. 
     * This is the default behaviour.
     */
    public void setRecursiveSplitOff () {
        this.recursiveSplit = false;
    }

    /**
     * Configures the tokeniser to expand negations of modal verbs. For example
     * "cant" is expanded to "can not", and "isnt" to "is not". The advantage of 
     * expanding negated modal verbs is that they are recognised more readily 
     * by part of speech taggers than the contractions.
     *
     */
    public void seModalExpansionOn () {
        this.expandModals = true;
    }

    /**
     * Turns off modal expansion. This is the default behaviour.
     */
    public void seModalExpansionOff () {
        this.expandModals = false;
    }

    /**
     * Creates a new instance of {@code IdentifierNameTokeniser} using the 
     * current state of the IdentifierNameTokeniserFactory.
     *
     * @return an instance of IdentifierNameTokeniser.
     */
    public IdentifierNameTokeniser create () {

        // check that we can instantiate
        if ( this.dictionarySet.hasNullComponent() == true ) {
            throw new IllegalStateException( 
                    "At least one oracle has a null reference" );
        }

        return new IdentifierNameTokeniser(
                this.dictionarySet,
                this.separatorCharacters,
                this.projectVocabularyThreshold,
                this.recursiveSplit,
                this.expandModals );
    }

    /**
     * Returns a {@code String} representation of the state of the
     * {@code IdentifierNameTokeniserFactory}. The string is formatted
     * using the syntax "component-name: field-name=value (, field-name=value)*;"
     * and returns values confirming the status of each major setting.
     *
     * <p>
     * <code>
     * Library:name=intt, version=0.8.0<br>
     * Main-dictionary:name=Default, entries=117089;<br>
     * Abbreviation-dictionary:name=Default, entries=103;<br>
     * Digit-Abbreviation-dictionary:name=Default, entries=50;<br>
     * Prefix-dictionary:name=Default, entries=15;<br>
     * Suffix-dictionary:name=Default, entries=57;<br>
     * Project-vocabulary:threshold=-1;<br>
     * Separator-character-set:cardinality=2, members={$, _};<br>
     * Recursive-split:false;<br>
     * Expand-modals: false;<br>
     * Opcode-Dictionary:name=Empty, entries=0;<br>
     * </code>
     * </p>
     *
     * @return a formatted {@code String} containing the current state
     * of the {@code IdentifierNameTokeniserFactory}
     */
    @Override
    public String toString () {
        StringBuilder output = new StringBuilder();

        String newLine = System.lineSeparator();
        output.append( "Library:name=" );
        output.append( Version.getName() );
        output.append( " ,version=" );
        output.append( Version.getVersion() );
        output.append( newLine );

        output.append( "Main-dictionary:" );
        output.append( this.dictionarySet.getMainDictionary().toString() );
        output.append( newLine );

        output.append( "Abbreviation-dictionary:" );
        output.append( this.dictionarySet.getAbbreviationDictionary().toString() );
        output.append( newLine );

        output.append( "Digit-Abbreviation-dictionary:" );
        output.append( this.dictionarySet.getDigitAbbreviationDictionary().toString() );
        output.append( newLine );

        output.append( "Prefix-dictionary:" );
        output.append( this.dictionarySet.getPrefixDictionary().toString() );
        output.append( newLine );

        output.append( "Suffix-dictionary:" );
        output.append( this.dictionarySet.getSuffixDictionary().toString() );
        output.append( newLine );

        output.append( "Project-vocabulary:" );
        output.append( "threshold=" );
        output.append( this.projectVocabularyThreshold );
        output.append( ";" );
        output.append( newLine );

        output.append( "Separator-character-set:" );
        output.append( "cardinality=" );
        output.append( this.separatorCharacters.length() );
        output.append( ", members={" );
        for ( Integer i = 0; i < this.separatorCharacters.length(); i++ ) {
            output.append( this.separatorCharacters.charAt( i ) );
            if ( i < this.separatorCharacters.length() - 1 ) {
                output.append( ", " );
            }
        }
        output.append( "};" );
        output.append( newLine );

        output.append( "Recursive-split:" );
        output.append( this.recursiveSplit );
        output.append( ";" );
        output.append( newLine );

        output.append( "Expand-modals:" );
        output.append( this.expandModals );
        output.append( ";" );
        output.append( newLine );

        return output.toString();
    }
}
