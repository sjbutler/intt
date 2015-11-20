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

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A convenience class for that contains a set of dictionaries
 * to pass to various constructors.
 *
 */
class DictionarySet {
    private AbbreviationDictionary abbreviationDictionary;
    private final AggregatedDictionary aggregatedDictionary;
    private DigitAbbreviationDictionary digitAbbreviationDictionary;
    private MainDictionary mainDictionary;
    private PrefixDictionary prefixDictionary;
    private ProjectVocabulary projectVocabulary;
    private SuffixDictionary suffixDictionary;


    /**
     * Creates a new dictionary set populated with the default dictionaries.
     * @throws IOException if a problem is encountered reading the dictionary
     * @throws FileNotFoundException thrown if the input word list file is not found
     */
    DictionarySet() throws IOException, FileNotFoundException {
        this.abbreviationDictionary = DefaultAbbreviationDictionary.getInstance();
        this.digitAbbreviationDictionary = DefaultDigitAbbreviationDictionary.getInstance();
        this.mainDictionary = DefaultMainDictionary.getInstance();
        this.aggregatedDictionary = new AggregatedDictionary(
                this.abbreviationDictionary,
                this.digitAbbreviationDictionary,
                this.mainDictionary);
        this.prefixDictionary = 
                DefaultPrefixDictionary.getInstance(this.aggregatedDictionary);
        this.suffixDictionary = 
                DefaultSuffixDictionary.getinstance(this.aggregatedDictionary);
        this.projectVocabulary = new ProjectVocabulary();
    }

    /**
     * Registers a different abbreviation dictionary.
     * 
     * @param abbreviationDictionary an alternative abbreviation dictionary to use
     */
    void setAbbreviationDictionary(AbbreviationDictionary abbreviationDictionary) {
        this.abbreviationDictionary = abbreviationDictionary;
        // now propagate the change
        this.aggregatedDictionary.changeDictionaries(this);
    }

    /**
     * Registers a different dictionary of digit abbreviations.
     * @param digitAbbreviationDictionary an alternative dictionary
     */
    void setDigitAbbreviationDictionary(DigitAbbreviationDictionary digitAbbreviationDictionary) {
        this.digitAbbreviationDictionary = digitAbbreviationDictionary;
        // now propagate the change
        this.aggregatedDictionary.changeDictionaries(this);
    }

    /**
     * Registers a different main dictionary.
     * @param mainDictionary an alternative main dictionary
     */
    void setMainDictionary(MainDictionary mainDictionary) {
        this.mainDictionary = mainDictionary;
        // now propagate the change
        this.aggregatedDictionary.changeDictionaries(this);
    }

    /**
     * Registers a different prefix dictionary.
     * @param prefixDictionary an alternative prefix dictionary
     */
    void setPrefixDictionary(PrefixDictionary prefixDictionary) {
        this.prefixDictionary = prefixDictionary;
    }

    /**
     * Registers a different project vocabulary.
     * @param projectVocabulary an alternative project vocabulary
     */
    void setProjectVocabulary(ProjectVocabulary projectVocabulary) {
        this.projectVocabulary = projectVocabulary;
    }

    /**
     * Registers a different suffix dictionary.
     * @param prefixDictionary an alternative suffix dictionary
     */
    void setSuffixDictionary(SuffixDictionary suffixDictionary) {
        this.suffixDictionary = suffixDictionary;
    }


    /**
     * Retrieves the abbreviation dictionary.
     * @return the currently registered abbreviation dictionary
     */
    AbbreviationDictionary getAbbreviationDictionary() {
        return this.abbreviationDictionary;
    }

    /**
     * Retrieves the aggregated dictionary.
     * @return the aggregated dictionary
     */
    AggregatedDictionary getAggregatedDictionary() {
        return this.aggregatedDictionary;
    }

    /**
     * Retrieves the digit abbreviation dictionary.
     * @return the digit abbreviation dictionary.
     */
    DigitAbbreviationDictionary getDigitAbbreviationDictionary() {
        return this.digitAbbreviationDictionary;
    }

    /**
     * Retrieves the main dictionary.
     * @return the main dictionary
     */
    MainDictionary getMainDictionary() {
        return this.mainDictionary;
    }

    /**
     * Retrieves the prefix dictionary.
     * @return the prefix dictionary
     */
    PrefixDictionary getPrefixDictionary() {
        return this.prefixDictionary;
    }

    /**
     * Retrieves the project vocabulary dictionary.
     * @return the project vocabulary
     */
    ProjectVocabulary getProjectVocabulary() {
        return this.projectVocabulary;
    }

    /**
     * Retrieves the suffix dictionary.
     * @return the suffix dictionary
     */
    SuffixDictionary getSuffixDictionary() {
        return this.suffixDictionary;
    }

    /**
     * Indicates if any of the components are missing.
     * 
     * @return {@code true} if any component has a {@code null} reference.
     */
    Boolean hasNullComponent() {
        return (this.abbreviationDictionary == null
                || this.aggregatedDictionary == null
                || this.digitAbbreviationDictionary == null
                || this.mainDictionary == null
                || this.prefixDictionary == null
                || this.projectVocabulary == null
                || this.suffixDictionary == null);
    }
}
