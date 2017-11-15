
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

package uk.ac.open.crc.intt.text;

import java.util.List;
import org.junit.Test;
import org.junit.Ignore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 * 
 */
public class WordListReaderTest {
    // need to configure Gradle properly to run tests on files in the resources folder in the jar file.
    @Test
    @Ignore
    public void opcodesTest() {
	WordListReader r = new WordListReader("simplified-opcodes.txt");
	
	List<String> opcodes = r.asList();
	
	assertThat(opcodes, notNullValue());
	
	assertThat(opcodes, not(empty()));
    }
}
