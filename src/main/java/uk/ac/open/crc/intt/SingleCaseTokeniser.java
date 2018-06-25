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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This provides the last resort, a greedy algorithm for splitting
 * identifier names that have not been split by other means. Because it is
 * greedy, this should not be called unless absolutely necessary. No result
 * guaranteed.
 * <p>
 * Criteria for choosing between possible solutions:<br />
 * (1) Where solutions are equal (i.e. the same set of splits) select one<br />
 * (2) Where solutions differ:<br />
 * (a) the greater proportion of words found in either
 * AggregatedDictionary or the Vocabulary<br />
 * (b) where (a) is tied, pick the first and log it. If it is a problem
 * then we shall just have to deal with it later.
 * </p>
 * <p>
 * There are considerable opportunities for refactoring the methods
 * in this class.
 * </p>
 * 
 */
class SingleCaseTokeniser {

    private final AggregatedDictionary dictionary;
    private final ProjectVocabulary projectVocabulary;
    private final SuffixDictionary suffixDictionary;
    private final PrefixDictionary prefixDictionary;

    private static final Logger LOGGER
            = LoggerFactory.getLogger( SingleCaseTokeniser.class );

    /**
     * Creates a tokeniser.
     *
     * @param dictionarySet a set of dictionaries
     */
    SingleCaseTokeniser ( DictionarySet dictionarySet ) {
        this.dictionary = dictionarySet.getAggregatedDictionary();
        this.projectVocabulary = dictionarySet.getProjectVocabulary();
        this.prefixDictionary = dictionarySet.getPrefixDictionary();
        this.suffixDictionary = dictionarySet.getSuffixDictionary();
    }

    // refactor -- this method is way too unwieldy
    /**
     * Splits a name into a list of tokens.
     *
     * @param identifier a name
     * @return a list of tokens
     */
    synchronized List<String> split ( String identifier ) {
        // we've been passed a word so first screen it
        // to determine if it is a neologism compose of a known word
        // with recognised prefixes and/or suffixes attached
        if ( isNeologism( identifier ) ) {
            // return it intact
            List<String> neologism = new ArrayList<>();
            neologism.add( identifier );
            return neologism;
        }

        // OK - so we try splitting forwards
        ArrayList<ArrayDeque<String>> candidates
                = splitForwards( "", identifier );

        // Then backwards
        ArrayList<ArrayDeque<String>> bCandidates
                = splitBackwards( "", identifier );

        // tidy up each list looking for obvious incidents of oversplitting
        cleanup( candidates );
        cleanup( bCandidates );

        // pick the best from each set  of candidates
        // NB - processing the general case simplifies the code
        List<String> bestForward = this.findTheBest( candidates );
        List<String> bestBackward = this.findTheBest( bCandidates );

        List<String> output;
        // and choose

        // check early for conditions required to run the algorithm in 
        // more detail
        // this could be a good place to move the start points and resplit
        // if zero is the best score we have
        // we want single string, score of zero and identifier longer than 5 characters
        // allow plenty of shortcuts to bail out early
        if ( bestForward.equals( bestBackward )
                && bestForward.size() == 1
                && this.weightedCombinedScore( bestForward ) == 0
                && identifier.length() > 4 ) {
            // a zero score is inevitable if both ends start with
            // an unknown word
            LOGGER.debug(
                    "no simple split found from ends for: {}. Applying most greedy algorithm.",
                    identifier );
            // SO
            // start by trimming the leading and trailing characters
            // limit to trimming is length/2 - 1
            // this will find any recognised component word, even if it is
            // the only one
            // also need to set a minimum length of say 4|5 characters
            // in the identifier before running the revised algorithm

            ArrayList<ArrayDeque<String>> forwardSlidingCandidates
                    = new ArrayList<>();
            ArrayList<ArrayDeque<String>> backwardSlidingCandidates
                    = new ArrayList<>();
            for ( int i = 0; i < ( identifier.length() / 2 ) - 1; i++ ) {
                String prefix = identifier.substring( 0, i + 1 );
                String suffix = identifier.substring( identifier.length() - i - 1, identifier.length() );
                ArrayList<ArrayDeque<String>> fSlidingCandidates
                        = splitForwards( prefix, identifier );
                // now add the prefix to the head of each deque
                fSlidingCandidates.stream().map( (candidate) -> {
                    candidate.addFirst( prefix );
                    return candidate;
                } ).forEach( (candidate) -> {
                    forwardSlidingCandidates.add( candidate );
                } );

                ArrayList<ArrayDeque<String>> bSlidingCandidates
                        = splitBackwards( suffix, identifier );
                // now add the suffix to the tail of each deque
                for ( ArrayDeque<String> candidate : bSlidingCandidates ) {
                    candidate.addLast( suffix );
                    backwardSlidingCandidates.add( candidate );
                }
            }

            cleanup( forwardSlidingCandidates );
            cleanup( backwardSlidingCandidates );

            // Now we can replace the best values
            bestForward = this.findTheBest( forwardSlidingCandidates );
            bestBackward = this.findTheBest( backwardSlidingCandidates );
            // now test whether these are any improvement over
            // not splitting.
            if ( this.weightedCombinedScore( bestForward ) == 0 ) {
                bestForward = new ArrayList<>();
                bestForward.add( identifier );
            }

            if ( this.weightedCombinedScore( bestBackward ) == 0 ) {
                bestBackward = new ArrayList<>();
                bestBackward.add( identifier );
            }
        }
        // check these two are not identical
        if ( bestForward.equals( bestBackward ) ) {
            // easy choice - both are the same
            output = bestForward;
        }
        else {
            // differentiate between the two
            int forwardScore = this.getBestScore( bestForward );
            int backwardScore = this.getBestScore( bestBackward );

            if ( forwardScore > backwardScore ) {
                output = bestForward;
            }
            else if ( forwardScore == backwardScore ) {
                // now we try the weighted score - giving a little more weight
                // to candidates containing more vocabulary words
                // As getBestScore() splits on the combination of
                // the dictionary and the vocabulary, then the existing
                // observation shows backward is more likely to be the cleaner split
                output = bestBackward;
                // tell the user what has happened
                // so they can appreciate whether there is a genuine 
                // lack of choice or the algorithm needs a tweak
                LOGGER.debug( "Cannot differentiate between {} and {}", 
                        candidateToString( bestForward ), 
                        candidateToString( bestBackward ) );
            }
            else {
                // backwards is best
                output = bestBackward;
            }
        }

        return output;
    }

    /**
     * Recursive algorithm that works through a string forwards looking for
     * matches. Where a match with a dictionary word is found then that is
     * used as a prefix and the remainder of the string processed.
     *
     * @param prefix a known word or abbreviation
     * @param name   the string to be split
     * @return a list of candidate splittings. In the worst case this will be
     * the original name passed in.
     */
    private ArrayList<ArrayDeque<String>> splitForwards ( String prefix, String name ) {
        HashSet<String> candidates = new HashSet<>();
        ArrayList<ArrayDeque<String>> candidateSets
                = new ArrayList<>();
        String testWord = "";

        // see String.substring(a, b) to understand the
        // bounds of the for loop
        int start = prefix.length();
        // now extract every recognised component
        for ( int end = start + 1; end <= name.length(); end++ ) {
            testWord = name.substring( start, end );
            // may need not to use the length test - especially when catching unknowns
            if ( testWord.length() > 2
                    && ( dictionary.isWord( testWord ) || projectVocabulary.isWord( testWord ) ) ) {
                candidates.add( testWord ); 
            }
        }
        // at the end of the name
        // either we've found nothing, or not matched the last fragment
        // so save whatever we've got
        if ( candidates.isEmpty() == true ) {
            ArrayDeque<String> remainder = new ArrayDeque<>();
            // prevents the loop falling through and a
            // meaningless "" being added to the list of component words
            if ( testWord.length() > 0 ) {
                remainder.add( testWord );
            }
            candidateSets.add( remainder );
        }
        else {
            // now recurse
            ArrayList<ArrayDeque<String>> subCandidateSet;
            for ( String candidate : candidates ) {
                subCandidateSet = splitForwards( prefix + candidate, name );
                // add the candidate to the front of each returned deque
                for ( ArrayDeque<String> subCandidate : subCandidateSet ) {
                    subCandidate.addFirst( candidate );
                    candidateSets.add( subCandidate );
                }
            }
        }

        return candidateSets;
    }

    private ArrayList<ArrayDeque<String>> splitBackwards ( String suffix, String name ) {
        HashSet<String> candidates = new HashSet<>();
        ArrayList<ArrayDeque<String>> candidateSets
                = new ArrayList<>();
        String testWord = "";

        // see String.substring(a, b) to understand the
        // bounds of the for loop
        int start = name.length() - suffix.length();

        // now extract every recognised component
        for ( int end = start - 1; end >= 0; end-- ) {
            testWord = name.substring( end, start );
            // may need not to use the length test - especially when catching unknowns
            if ( testWord.length() > 2
                    && ( dictionary.isWord( testWord ) || projectVocabulary.isWord( testWord ) ) ) {
                candidates.add( testWord );
            }
        }
        // at the end of the name
        // either we've found nothing, or not matched the last fragment
        // so save whatever we've got
        if ( candidates.isEmpty() == true ) {
            ArrayDeque<String> remainder = new ArrayDeque<>();
            // the stop condition is more difficult to interpret for backwards
            // so we may end up with an extra recursive call
            if ( testWord.length() > 0 ) {
                remainder.add( testWord );
            }
            candidateSets.add( remainder );
        }
        else {
            // now recurse
            ArrayList<ArrayDeque<String>> subCandidateSet;
            for ( String candidate : candidates ) {
                subCandidateSet = splitBackwards( candidate + suffix, name );
                // add the candidate to the front of each returned deque
                for ( ArrayDeque<String> subCandidate : subCandidateSet ) {
                    subCandidate.addLast( candidate );
                    candidateSets.add( subCandidate );
                }
            }
        }

        return candidateSets;
    }

    
    private List<String> findTheBest ( ArrayList<ArrayDeque<String>> candidates ) {
        ArrayDeque<String> best = null;
        ArrayList<ArrayDeque<String>> bestSplits = new ArrayList<>();
        int bestScore = 0;

        // select the most parsimonious splits first
        int topParsimonyScore = 0;
        ArrayDeque<String> mostParsimonious = null;
        ArrayList<ArrayDeque<String>> parsimoniousSplits = new ArrayList<>();

        for ( ArrayDeque<String> candidate : candidates ) {
            int parsimony = parsimonyScore( candidate.toArray( new String[0] ) );

            if ( parsimony > topParsimonyScore ) {
                topParsimonyScore = parsimony;
                mostParsimonious = candidate;
                // now, as we are doing better, we need to clear the
                // lower scoring candidates from the list
                parsimoniousSplits.clear();
                // now add the new high scorer
                parsimoniousSplits.add( candidate );
            }
            else if ( parsimony == topParsimonyScore ) {
                // then add the candidate to the list of best splits
                parsimoniousSplits.add( candidate );
                // for a zero score - should it be the only one we get
                if ( mostParsimonious == null ) {
                    mostParsimonious = candidate;
                }
            }
        }

        // then process the candidates looking for preferable splits
        for ( ArrayDeque<String> candidate : parsimoniousSplits ) {
//        for (ArrayDeque<String> candidate : candidates) {
            int score = getBestScore( new ArrayList<>( candidate ) );

            // DEBUG
//            System.out.println(score);
            // DEBUG
            if ( score > bestScore ) {
                bestScore = score;
                best = candidate;
                // now, as we are doing better, we need to clear the
                // lower scoring candidates from the list
                bestSplits.clear();
                // now add the new high scorer
                bestSplits.add( candidate );
            }
            else if ( score == bestScore ) {
                // then add the candidate to the list of best splits
                bestSplits.add( candidate );
                // for a zero score - should it be the only one we get
                if ( best == null ) {
                    best = candidate;
                }
            }
        }

        if ( bestSplits.size() > 1 ) {
            // now make a choice between the the competing candidates
            // probably redundant
            ArrayList<ArrayDeque<String>> competingSplits = new ArrayList<>();
            // let's be parsimonious!!
            ArrayDeque<String> shortest = null;
            int length = 200;  // ridiculous number to get started
            for ( ArrayDeque<String> candidate : bestSplits ) {
                if ( candidate.size() < length ) {
                    shortest = candidate;
                    competingSplits.clear();
                    competingSplits.add( candidate );
                }
                else if ( candidate.size() == length ) {
                    competingSplits.add( candidate );
                    if ( shortest == null ) {
                        shortest = candidate;
                    }
                }
            }

            best = shortest;

            if ( competingSplits.size() > 1 ) {
                // now we should be able to split on dictionary scores if necessary
                LOGGER.debug(
                        "Unable to pick best split from {0}",
                        candidatesToString( competingSplits ) );
            }
        }

        return new ArrayList<>( best );
    }

    // much of this may well be redundant
    // The weightedCombinedScore alone may be sufficient
    // to disambiguate between potential solutions
    // MUST reason about this to determine if suspicions are correct.
    private int getBestScore ( List<String> words ) {
        int dictionaryScore = this.dictionary.percentageKnown( words );
        int vocabularyScore = this.projectVocabulary.weightedPercentageKnownA( words );
        int combinedScore = weightedCombinedScore( words );
        return combinedScore;
        //return Math.max(combinedScore, Math.max(dictionaryScore, vocabularyScore));
    }

    // performs a percentage known type test combining the dictionary and
    // the vocabulary.
    private int combinedScore ( String[] words ) {
        int knownCount = 0;
        for ( int i = 0; i < words.length; i++ ) {
            if ( this.dictionary.isWord( words[i] )
                    || this.projectVocabulary.isWord( words[i] ) ) {
                knownCount++;
            }
        }

        return (int) ( knownCount * 100 / words.length );
    }

    // Review
    private int weightedCombinedScore ( List<String> tokens ) {
        int knownCount = 0;
        int knownVocabularyCount = 0;
        boolean isDictionaryWord;
        boolean isVocabularyWord;

        for ( String token : tokens ) {
            isDictionaryWord = this.dictionary.isWord( token );
            isVocabularyWord = this.projectVocabulary.isWord( token );
            if ( isDictionaryWord || isVocabularyWord  ) {
                knownCount++;
                if ( isVocabularyWord ) {
                    knownVocabularyCount++;
                }
            }
        }

        // add the count of vocabulary words found as a weight
        return knownVocabularyCount + (int) ( knownCount * 100 / tokens.size() );
    }

    private String candidatesToString ( ArrayList<ArrayDeque<String>> candidates ) {
        StringBuilder output = new StringBuilder();

        for ( ArrayDeque<String> candidate : candidates ) {
            output.append( candidate.stream().collect( Collectors.joining( ",", "{", "}" ) ) );
        }

        return output.toString();
    }
    
    private String candidateToString ( List<String> candidate ) {
        return candidate.stream().collect( Collectors.joining( ", ", "{", "}" ) );
    }

    private int parsimonyScore ( String[] words ) {
        int totalLength = 0;
        int knownLength = 0;

        for ( String word : words ) {
            totalLength += word.length();
            if ( this.dictionary.isWord( word ) ) {
                knownLength += word.length();
            }
        }

        return (int) ( 1000 * ( knownLength / totalLength ) / words.length );
    }

    // only tests for contrived terms using real words
    // does *not* claim a neologism simply because
    // something unknown has a recognised prefix or suffix
    private boolean isNeologism ( String token ) {

        Integer longestPrefix = this.prefixDictionary.getLongest();
        Integer longestSuffix = this.suffixDictionary.getLongest();

        Boolean hasPrefix = false;
        Boolean hasSuffix = false;

        ArrayList<String> prefixes = new ArrayList<>();
        ArrayList<String> suffixes = new ArrayList<>();

        // now hunt for a prefix
        // upper bound in terms of the token length is an interesting issue
        String candidatePrefix;
        for ( int i = 0; i < token.length() - 1 && i < longestPrefix; i++ ) {
            candidatePrefix = token.substring( 0, i + 1 );
            if ( this.prefixDictionary.isWord( candidatePrefix ) ) {
                prefixes.add( candidatePrefix );
            }
        }

        // and now look for a suffix
        String candidateSuffix;
        for ( int i = token.length() - 1; i > 0 && ( token.length() - i < longestSuffix ); i-- ) {
            candidateSuffix = token.substring( i );
            if ( this.suffixDictionary.isWord( candidateSuffix ) ) {
                suffixes.add( candidateSuffix );
            }
        }

        // if we have found neither we don't have a simple neologism
        if ( prefixes.isEmpty() && suffixes.isEmpty() ) {
            return false;
        }

        // now check whether what we have found prefixes, or suffixes a known word
        if ( !prefixes.isEmpty() ) {
            for ( String prefix : prefixes ) {
                // create the candidate term
                String candidateWord = token.substring( prefix.length() );
                if ( suffixes.isEmpty() ) {
                    // then we test the word
                    if ( this.dictionary.isWord( candidateWord ) ) {
                        hasPrefix = true;
                    }
                }
                else {
                    // we need to strip the suffix as well to find the word
                    for ( String suffix : suffixes ) {
                        if ( suffix.length() < candidateWord.length() // guard prevents array out of bounds exception
                                && this.dictionary.isWord( candidateWord.substring( 0, ( candidateWord.length() - suffix.length() ) ) ) ) {
                            hasPrefix = true;
                            hasSuffix = true;
                        }
                    }
                }
            }
        }
        else {
            // no prefix, but we have a suffix
            for ( String suffix : suffixes ) {
                if ( this.dictionary.isWord( token.substring( 0, ( token.length() - suffix.length() ) ) ) ) {
                    hasSuffix = true;
                }
            }
        }

        return hasPrefix || hasSuffix;
    }

    // there has to be an easier way of doing this check
    /**
     * Checks each candidate and attempts to remove any obvious oversplitting.
     * This may be obsoleted (at least in part) by code to screen neologisms
     *
     * @param candidates a list of {@code Deque}s containing candidate splits
     */
    void cleanup ( ArrayList<ArrayDeque<String>> candidates ) {
        for ( int i = 0; i < candidates.size(); i++ ) {
            // check we need to look at it
            ArrayDeque<String> candidate = candidates.get( i );
            if ( candidate.size() > 1 ) {
                String[] words = candidate.toArray( new String[0] );
                ArrayDeque<String> replacement = new ArrayDeque<>();
                // now look for concatenations of adjacent strings
                // that are dictionary words
                int chainLength = 0;
                StringBuilder test = null;
                for ( int j = 0; j < ( words.length - 1 ); j++ ) {
                    // deal with the first word
                    if ( chainLength == 0 ) {
                        test = new StringBuilder( words[j] );
                        chainLength++;
                        replacement.addLast( words[j] );
                    }
                    // now add the next
                    test.append( words[j + 1] );
                    if ( this.dictionary.isWord( test.toString() )
                            || isNeologism( test.toString() ) ) {
                        chainLength++;
                        replacement.removeLast();
                        replacement.addLast( test.toString() );
                    }
                    else {
                        chainLength = 0;
                        if ( j == ( words.length - 2 ) ) {
                            // i.e. this is the last iteration and the concatentation
                            // of the final two words is not a legitimate dictionary word
                            // so we must add the last word
                            replacement.addLast( words[j + 1] );
                        }
                    }
                }

//                // DEBUG
//                System.out.println("-- replacement -- ");
//                for (String s : replacement) {
//                    System.out.print(s + " ");
//                }
//                System.out.println("\n-- replacement -- \n");
//                // DEBUG
                // now we need to replace the original candidate, if
                // any changes have been made
                if ( ! candidate.equals( replacement ) ) {
                    candidates.set( i, replacement );
                }
            }
        }
    }
}
