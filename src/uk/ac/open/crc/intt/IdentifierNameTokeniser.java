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

/**
 * Provides a simple API
 * for tokenising identifier names.
 *
 * <p>
 * Instances of this class are configured and created by using
 * {@linkplain IdentifierNameTokeniserFactory}.
 * </p>
 *
 */
public final class IdentifierNameTokeniser {

    private final BasicTokeniser basicTokeniser;
    private final SingleCaseTokeniser singleCaseTokeniser;

    private final DictionarySet dictionarySet;

    private final AggregatedDictionary aggregatedDictionary;
    private final ProjectVocabulary projectVocabulary;

    private final int projectVocabularyThreshold;

    private final boolean recursiveSplit;
    private final boolean expandModals;
    
    private final PrefixConcatenator prefixConcatenator;
    
    /**
     * Creates an identifier tokeniser.
     * @param dictionarySet a set of dictionaries
     * @param separatorCharacters a {@code String} that will be interpreted as 
     * contain a set of separator characters
     * @param vocabularyThreshold a minimum length of a dictionary entity
     * @param recursiveSplit indicates if single character strings should be split
     * @param expandModals indicates if negated modal verbs should be split
     */
    IdentifierNameTokeniser(
            DictionarySet dictionarySet, 
            String separatorCharacters, 
            int vocabularyThreshold,
            boolean recursiveSplit,
            boolean expandModals) {
        this.dictionarySet = dictionarySet;
        this.basicTokeniser = 
                new BasicTokeniser(this.dictionarySet, separatorCharacters);
        this.singleCaseTokeniser = 
                new SingleCaseTokeniser(this.dictionarySet);
        this.aggregatedDictionary = 
                this.dictionarySet.getAggregatedDictionary();
        this.projectVocabulary = this.dictionarySet.getProjectVocabulary();
        this.projectVocabularyThreshold = vocabularyThreshold;
        this.recursiveSplit = recursiveSplit;
        this.expandModals = expandModals;
        
        this.prefixConcatenator = new PrefixConcatenator();
    }


    private synchronized List<String> greedySplit(String name) {
        return this.singleCaseTokeniser.split(name);
    }

    /**
     * Tokenises an identifier name, returning a {@code List} of tokens.
     *
     * @param identifierName an identifier name
     * 
     * @return an array of tokens
     */
    public synchronized List<String> tokenise(String identifierName) {

        List<String> words = this.basicTokeniser.tokenise(identifierName);


        ArrayList<String> tokens = new ArrayList<>();
        // expand the modals if set
        if ( this.expandModals ) {
            ArrayList<String> modalTokens = new ArrayList<>();
            ModalExpansion expander = ModalExpansion.getInstance();
            boolean modalExpanded = false;
            
            for (String word : words) {
                if ( expander.isExpandable( word ) ) {
                    modalTokens.addAll( expander.getExpansionFor( word ) );
                    modalExpanded = true;
                }
                else {
                    modalTokens.add( word );
                }
            }
            
            if ( modalExpanded ) {
                // now copy it all back to words, if altered
                words = modalTokens;
            }
        }
        
        
        if ( this.recursiveSplit ) {
            // applies greedysplit to every unrecognised token.
            for (String word : words) {
                if (word.length() > 1 
                        && ! this.aggregatedDictionary.isWord( word )
                        && ! word.matches("^.*[0-9]+.*$")) {
                    tokens.addAll( greedySplit( word ) );
                } 
                else {
                    tokens.add(word);
                }
            }
        }
        else {
            // the original behaviour from v0.2.0 (i.e. roughly the ECOOP paper behaviour)
            // this only splits a single case identifer name
            if (words.size() == 1
                    && words.get( 0 ).length() > 1
                    && ! this.aggregatedDictionary.isWord( words.get( 0 ) )
                    && ! words.get( 0 ).matches("^.*[0-9]+.*$")) {
                // now use the greedy
                tokens.addAll( greedySplit( words.get( 0 ) ) );
            }
            else {
                tokens.addAll( words );
            }
        } 
        
            
        tokens.stream()
                .filter( (token) -> ( token.length() > this.projectVocabularyThreshold ) )
                .forEach( (token) -> {
            this.projectVocabulary.add( token );
        } );
        
        return tokens;
    }

    /**
     * Undertakes a naive or conservative tokenisation using separator 
     * characters and LCUC boundaries.
     * @param identifierName a name
     * @return a list of tokens resulting from conservative tokenisation 
     */
    public synchronized List<String> naiveTokensation( String identifierName ) {
        return this.basicTokeniser.naiveTokensation( identifierName );
    }
    
    // input is a tokenised identifier name.
    /**
     * Processes a tokenised name joining tokens that have been inadvertently 
     * split by the developer. For example, subMenu is tokenised to sub and menu, 
     * and this method will revise the tokenisation to submenu.
     * @param tokens a list of tokens from a tokenised name
     * @return a revised list of tokens where prefixes have been concatenated
     * with their orphaned words
     */
    public synchronized List<String> prefixConcatenation( List<String> tokens ) {
        return this.prefixConcatenator.combinePrefixes( tokens );
    }
    
    /**
     * Expands any contractions of negated modal verbs. For example, cant 
     * becomes can not.
     * @param tokens the tokens of a tokenised name
     * @return a revision of the input list with any negated modal verbs expanded
     */
    public synchronized List<String> modalExpansion( List<String> tokens ) {
        ModalExpansion expander = ModalExpansion.getInstance();
        if ( ! expander.containsExpandable( tokens ) ) {
            return tokens;
        }
        
        ArrayList<String> modalTokens = new ArrayList<>();
        tokens.stream().forEach( (token) -> {
            if ( expander.isExpandable( token ) ) {
                modalTokens.addAll( expander.getExpansionFor( token ) );
            }
            else {
                modalTokens.add( token );
            }
        } );
        return modalTokens;
    }
    
}
