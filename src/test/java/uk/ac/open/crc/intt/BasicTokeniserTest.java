/*
 * Copyright (C) 2010-2015 The Open University
 * Copyright (C) 2017-2019 Simon Butler
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
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Ignore;
import java.util.stream.Collectors;

/**
 * Simple tests of the fundamental functionality of {@code BasicTokeniser}. 
 *
 */
public class BasicTokeniserTest {
    // ensure any tests added exercise deterministic functionality only.
    IdentifierNameTokeniser tokeniser;
    IdentifierNameTokeniserFactory factory;

    @Before
    public void setUp() {
        factory = new IdentifierNameTokeniserFactory();
        factory.setRecursiveSplitOn();
        tokeniser = factory.create();
    }

    @Test
    public void singleCharacterName() {
        String test = "z";

        List<String> tokens = tokeniser.tokenise(test);
	assertThat("Only one token expected", tokens.size(), is(1) );
        assertThat("Failed to split \"z\" correctly", tokens.get(0), is(equalTo("z")));
    }

    @Test
    public void twoCharacterNames() {
        String test = "ds";

        List<String> tokens = tokeniser.tokenise(test);

	assertThat("Only one token expected", tokens.size(), is(1) );
	assertThat("Failed to split \"ds\" correctly", tokens.get(0), is(equalTo("ds")));
    }

    @Test
    public void leadingSeparatorChars() {
        String test = "$test";

        List<String> tokens = tokeniser.tokenise(test);

	assertThat(String.format("Only one token expected, but found %d tokens - %s",
				 tokens.size(),
				 tokens.stream().collect(Collectors.joining(", ", "[", "]")) ),
		   tokens.size(),
		   is(1) );
        assertThat("Failed to strip leading \'$\'", tokens.get(0), is(equalTo("test")));

        test = "$$test";
        tokens = tokeniser.tokenise(test);
	assertThat("Only one token expected", tokens.size(), is(1));
        assertThat("Failed to strip leading \'$$\'", tokens.get(0), is(equalTo("test")));

        test = "__test";
        tokens = tokeniser.tokenise(test);
	assertThat("Only one token expected", tokens.size(), is(1));
        assertThat("Failed to strip leading \'_\'", tokens.get(0), equalTo("test"));
    }

    @Test
    public void trailingSeparatorChars() {
        String test = "test$";

        List<String> tokens = tokeniser.tokenise(test);
	assertThat("Only one token expected", tokens.size(), is(1));
        assertThat("Failed to strip trailing \'$\'", tokens.get(0), is(equalTo("test")));

        test = "test$$";
        tokens = tokeniser.tokenise(test);
	assertThat("Only one token expected", tokens.size(), is(1));
        assertThat("Failed to strip trailing \'$$\'", tokens.get(0), is(equalTo("test")));

        test = "test__";
        tokens = tokeniser.tokenise(test);
        assertThat("Only one token expected", tokens.size(), is(1));
        assertThat("Failed to strip trailing \'__\'",  tokens.get(0), is(equalTo("test")));

        test = "test_";
        tokens = tokeniser.tokenise(test);
        assertThat("Only one token expected", tokens.size(), is(1));
        assertThat("Failed to strip trailing \'_\' from test_",  tokens.get(0), is(equalTo("test")));
    }

    // embedded separators
    @Test
    public void embeddedSeparator() {

        String test = "TEST_CONSTANT";

        List<String> tokens = tokeniser.tokenise(test);
	
	assertThat("Two tokens expected", tokens.size(), is(2));
        assertThat("Split constant incorrectly", 
                tokens, 
		contains("TEST", "CONSTANT"));

    }
    
    @Test
    public void embeddedSeparatorTwo() {
        String test = "TEST_CONSTANT_SECOND";
        List<String> tokens = tokeniser.tokenise(test);
	assertThat("Three tokens expected", tokens.size(), is(3));
        assertThat("Split three part constant incorrectly", 
		tokens, 
		contains("TEST", "CONSTANT", "SECOND"));
    }
    
    
    @Test
    public void embeddedSeparatorThree() {
        String test = "one_two_three$four";
        List<String> tokens = tokeniser.tokenise(test);
	assertThat("Four tokens expected", tokens.size(), is(4));
        assertThat("Split lower case with embedded separator incorrectly",
                tokens,
		contains("one", "two", "three", "four"));
    }


    @Test
    public void embeddedContiguousSeparatorsOne() {

        String test = "TEST__CONSTANT";

        List<String> tokens = tokeniser.tokenise(test);
	assertThat("Expected two tokens", tokens.size(), is(2));
        assertThat("Split incorrectly round two contiguous separators",
                tokens, 
		contains("TEST", "CONSTANT"));

    }
    
        @Test
    public void embeddedContiguousSeparatorsTwo() {

	String test = "TEST$_$CONSTANT";

        List<String> tokens = tokeniser.tokenise(test);
	assertThat("Expected two tokens", tokens.size(), is(2));
        assertThat("Split constant incorrectly around '$_$'",
		tokens,
		contains("TEST", "CONSTANT"));
    }
    
    // word division - Camel case
    @Test
    public void camelCase() {
        String test = "normalCamelCaseIdentifier";

        List<String> tokens = tokeniser.tokenise(test);

	assertThat("Expected four tokens", tokens.size(), is(4));
        assertThat("Split camelCase incorrectly",
		tokens, 
		contains("normal", "Camel", "Case", "Identifier"));
    }


    @Test
    public void camelCaseUcAbbr() {
        String test = "upperCaseABBR";

        List<String> tokens = tokeniser.tokenise(test);
	assertThat("trailing UC abbreviation split incorrectly: "
		+ "3 tokens expected",
		tokens.size(), 
		is(3));
        assertThat("trailing UC abbreviation split incorrectly",
                tokens,
		contains("upper", "Case", "ABBR"));

    }
        
    @Test
    public void camelCaseUcAbbrEmbedded() {
	String test = "upperCaseABBREmbedded";

        List<String> tokens = tokeniser.tokenise(test);

	assertThat("UC abbreviation in camel case split incorrectly: "
		+ "4 tokens expected",
		tokens.size(),
		is(4));
        assertThat("UC abbreviation in camel case split incorrectly",
		tokens, 
                contains("upper", "Case", "ABBR", "Embedded"));
    }


    @Test
    public void camelCaseLeadingUC() {
        String test = "NormalCamelCaseIdentifier";

        List<String> tokens = tokeniser.tokenise(test);
        assertThat("Split camelCase with leading UC letter incorrectly: "
		+ "4 tokens expected",
		tokens.size(),
		is(4));
        assertThat("Split camelCase with leading UC letter incorrectly",
                tokens,
		contains("Normal", "Camel", "Case", "Identifier"));
    }

    @Test
    public void camelCaseLeadingUCAbbr() {
        String test = "HTMLEditorKit";

        List<String> tokens = tokeniser.tokenise(test);
        assertThat("Split camelCase with leading abbreviation incorrectly: "
		+ "3 tokens expected",
		tokens.size(),
		is(3));
        assertThat("Split camelCase with leading abbreviation incorrectly",
		tokens,
                contains("HTML", "Editor", "Kit"));
    }

    // digits

    // demonstrates that conventional boundaries
    // are sufficient when the digit is
    // embedded in the abbreviation
    @Test
    public void unrecognisedDigitAbbreviation() {
        String test = "areaG3FTriangle";

        List<String> tokens = tokeniser.tokenise(test);

        assertThat("Failed to split unrecognised embedded digit abbreviation "
		+ "correctly: expected 3 tokens",
		tokens.size(),
		is(3));
        assertThat("Failed to split unrecognised embedded digit abbreviation correctly",
                tokens, 
		contains("area", "G3F", "Triangle"));
    }

    @Test
    public void swingNameTest() {
        List<String> tokens = tokeniser.tokenise("JPanel");
	
	assertThat("Failed to split JPanel as expected: 3 tokens expected",
		tokens.size(),
		is(2));
        assertThat("Failed to split JPanel",
                tokens,
		contains("J", "Panel"));
    }
    
    
    @Test
    public void twoSoftWords() {
        List<String> tokens = tokeniser.tokenise("Redobuff");

        assertThat("Failed to split Redobuff correctly: expected 2 tokens",
		tokens.size(),
		is(2));
        assertThat("Failed to split Redobuff",
		tokens,
		contains("Redo", "buff")); 
    }

    @Test
    public void sandwichedTwoSoftWords() {
        List<String> tokens = tokeniser.tokenise("megaRedobuffSync");

        assertThat("Failed to split megaRedobuffSync correctly: expected 4 tokens",
		tokens.size(),
		is(4));
        assertThat("Failed to split Redobuff",
		tokens,
                contains("mega", "Redo", "buff", "Sync"));
    }
    
}
