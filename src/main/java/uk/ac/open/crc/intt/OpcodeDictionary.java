/*
    Copyright (C) 2010-2015 The Open University
    Copyright (C) 2019 Simon Butler

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
import java.util.HashSet;
import java.util.List;

/**
 * A dictionary of Java bytecode opcodes.
 *
 */
class OpcodeDictionary implements Dictionary {

    private HashSet<String> opcodeSet;

    String name = "Opcode Dictionary";

    /**
     * Creates a dictionary using the supplied list of opcodes and
     * named with the supplied name.
     *
     * @param opcodes a list of opcodes
     */
    public OpcodeDictionary ( List<String> opcodes ) {
        this.opcodeSet = new HashSet<>();
        opcodes.stream().forEach( (opcode) -> {
            this.opcodeSet.add( opcode.toLowerCase() );
        } );
    }

    /**
     * Creates a dictionary using the supplied list of opcodes and
     * named with the supplied name.
     *
     * @param opcodes a list of opcodes
     * @param name    a name for the dictionary
     */
    public OpcodeDictionary ( List<String> opcodes, String name ) {
        this( opcodes );
        this.name = name;
    }

    /**
     * Indicates if the name (fragment) is a recognised opcode.
     *
     * @param name An identifier name, or name fragment.
     *
     * @return {@code true} if the fragment is a recognised opcode
     *
     */
    @Override
    public synchronized boolean isWord ( String name ) {
        return opcodeSet.contains( name.toLowerCase() );
    }

     @Override
    public List<String> tags( String word ) {
        List<String> tags = new ArrayList<>();
        
        if ( isWord( word ) ) {
            tags.add( "opcode" );
        }
        
        return tags;
    }
    
    /**
     * Provides a descriptive string containing the name of the dictionary
     * and the number of entries.
     *
     * @return a descriptive string
     */
    @Override
    public String toString () {
        StringBuilder output = new StringBuilder( "name=" );
        output.append( name );
        output.append( ", entries=" );
        output.append( this.opcodeSet.size() );
        output.append( ";" );

        return output.toString();
    }

}
