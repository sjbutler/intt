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

import java.util.List;
import uk.ac.open.crc.intt.text.WordListReader;

/**
 * Provides a subset of the bytecode opcode mnemonics from 
 * the page at {@link http://en.wikipedia.org/wiki/Java_bytecode_instruction_listings}.
 *
 * <p>
 * The subset consists of opcodes where those with underscores have been 
 * split and the major part of the name retained. In practice, the numeric 
 * suffixes are removed from the {@code aload} series etc. and the 
 * {@code if_acmpeq} series are reduced to {@code acmpeq}. This is experimental
 * and the list may be subject to further revision. For example, any mnemonics
 * composed of 2 or more dictionary words that form a phrase might be omitted. 
 * Single words found in other dictionaries might also omitted. 
 * </p>
 * 
 */
class SimplifiedOpcodeDictionary extends OpcodeDictionary {
    private static SimplifiedOpcodeDictionary instance = null;
    
    synchronized static SimplifiedOpcodeDictionary getInstance() {
        if ( instance == null ) {
            WordListReader reader = new WordListReader( "simplified-opcodes.txt" );
            instance = new SimplifiedOpcodeDictionary( reader.asList() );
        }
        
        return instance;
    }
    
    
    private SimplifiedOpcodeDictionary( List<String> opcodes ) {
        super( opcodes, "simplified_opcode" );
    }
    
}
