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

import java.util.List;
import java.util.stream.Collectors;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * 
 */
public class SeparatorsTest {
   
    private static IdentifierNameTokeniser inttCobol;
    private static IdentifierNameTokeniser inttFantasy;
    private static IdentifierNameTokeniserFactory factory;
    
    @BeforeClass
    public static void setUp() {
        factory = new IdentifierNameTokeniserFactory();
        factory.setSeparatorCharacters("-");
        inttCobol = factory.create();
        factory.setSeparatorCharacters("_-><%");
        inttFantasy = factory.create();
    }
    
    @Test
    public void noSeparatorTest() {
        List<String> tokens = inttCobol.tokenise( "HELLO" );
        assertThat(String.format("Expected a single token. Found: %s", 
                tokens.stream().collect(Collectors.joining(", ", "\"", "\"")) ), 
                tokens, 
                hasSize(1));
    }
    
    @Test
    public void simpleSeparatorTest() {
        List<String> tokens = inttCobol.tokenise( "HELLO-WORLD" );
        assertThat(String.format("Expected two tokens. Found: %s", 
                tokens.stream().collect(Collectors.joining(", ", "\"", "\"")) ), 
                tokens, 
                hasSize(2));
    }
    
    @Test
    public void fantasyTest() {
        List<String> tokens = inttFantasy.tokenise( "good%bye<and>thanks-for_the%fish" );
        assertThat(String.format("Expected seven tokens. Found: %s", 
                tokens.stream().collect(Collectors.joining(", ", "\"", "\"")) ), 
                tokens, 
                hasSize(7));
    }
}
