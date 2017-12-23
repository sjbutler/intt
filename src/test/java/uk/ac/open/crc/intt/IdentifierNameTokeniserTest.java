/*
    Copyright (C) 2017 Simon Butler

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

import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for the {@code IdentifierNameTokeniser} class.  
 * 
 * NB only very rudimentary tests are implemented.
 * 
 */
public class IdentifierNameTokeniserTest {

    private static IdentifierNameTokeniser tokeniser;    
    
    @BeforeClass
    public static void setUp() {
	tokeniser = new IdentifierNameTokeniserFactory().create();
    }
    
    @Test
    public void twoTokenTest() {
	List<String> tokens = tokeniser.tokenise( "somethingSimple");
	assertThat("Null token list returned by tokeniser", 
		tokens, 
		notNullValue());
	assertThat("Returned token list is empty",
		tokens, 
		not(empty()));
	assertThat("Returned token list does not have two members", 
		tokens, 
		hasSize(2));
    }
}
