/*
 * Copyright (C) 2019 Simon Butler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.open.crc.intt;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests population of the {@code TaggedToken} class by the tokeniser.
 * 
 * Tests for accuracy can be done here.
 */
public class TaggedTokenTest {
    
    private static IdentifierNameTokeniserFactory factory;
    private static IdentifierNameTokeniser tokeniser;
    
    @BeforeClass
    public static void setUp() {
        factory = new IdentifierNameTokeniserFactory();
        
        tokeniser = factory.create();
    }
    
    
    
    @Test
    public void taggedTokenInstantiationTest() {
        String token = "token";
        TaggedToken tt = new TaggedToken(token);
        tt.add("a");
        tt.add("b");
        tt.add("c");
        tt.add("d");
        
        List<String> tagList = tt.wordLists();
        String tags = tagList.stream().collect(Collectors.joining(", ", "[", "]"));
        assertThat(String.format("Instantiation test failed, expected 4 word list tags, found %d tags as \"%s\"", tagList.size(), tags), tagList.size(), is(4));
    }
    
    @Test
    public void singleTokenTest() {
        String token = "Local";
        
        List<TaggedToken> ttNaive = tokeniser.naiveTokenisationWithOrigins(token);
        List<TaggedToken> ttFull = tokeniser.tokeniseWithOrigins(token);
        
        assertThat(
                "Naive tokenisation list is null", 
                ttNaive, 
                notNullValue());
        List<String> naiveTokens = getTokens(ttNaive);
        String tokenList = naiveTokens.stream().collect(Collectors.joining(", ","[","]"));
        assertThat(
                String.format("Naive tokenisation list should have 1 member, found %d as \"%s\"",ttNaive.size(), tokenList), 
                ttNaive.size(), 
                is(1));
        
        
        
        assertThat(
                "Full tokenisation list is null", 
                ttFull, 
                notNullValue());
        List<String> fullTokens = getTokens(ttFull);
        tokenList = fullTokens.stream().collect(Collectors.joining(", ","[","]"));
        assertThat(
                String.format("Full tokenisation list should have 1 member, found %d as \"%s\"",ttFull.size(), tokenList), 
                ttFull.size(), 
                is(1));
        List<String> tagList = ttFull.get(0).wordLists();
        String tags = tagList.stream().collect(Collectors.joining(", ", "[", "]"));
        assertThat(String.format("Found %d tags as \"%s\"", tagList.size(), tags), 
                tagList.size(), 
                is(3));
        
    }
    
    
    private List<String> getTokens(List<TaggedToken> ttList) {
        List<String> tokens = new ArrayList<>();
        ttList.forEach(tt -> tokens.add(tt.getContent()));
        return tokens;
    }
}
