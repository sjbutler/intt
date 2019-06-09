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
 * A dictionary of full Java bytecode mnemonics. These include underscores
 * so are intended to be used prior to tokenisation and are part of the notion 
 * of a protected token, which hasn't been implemented.
 *
 */
class FullOpcodeDictionary extends OpcodeDictionary {
    private static FullOpcodeDictionary instance = null;
    
    /** 
     * Retrieves the instance of this class.
     * @return the instance of the class
     */
    synchronized static FullOpcodeDictionary getInstance() {
        if ( instance == null ) {
            WordListReader reader = new WordListReader( "mnemonics-java8.txt" );
            instance = new FullOpcodeDictionary( reader.asList() );
        }
        
        return instance;
    }
    
    
    private FullOpcodeDictionary( List<String> opcodes ) {
        super( opcodes, "full_opcode" );
    }
    
}
