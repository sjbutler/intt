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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a mechanism for expanding modal verb abbreviations. The API is
 * prone to revision.
 *
 */
class ModalExpansion {

    private static final Logger LOGGER = LoggerFactory.getLogger( ModalExpansion.class );

    private static ModalExpansion instance = null;

    static ModalExpansion getInstance () {
        if ( instance == null ) {
            instance = new ModalExpansion();
        }

        return instance;
    }

    // ----------------
    private final HashMap<String, List<String>> store;

    private ModalExpansion () {
        this.store = new HashMap<>();

        try {
            InputStream inStream
                    = this.getClass().getResourceAsStream( "text/contractions.txt" );
            try ( BufferedReader in
                    = new BufferedReader( new InputStreamReader( inStream ) ) ) {
                String line;

                // read file
                while ( ( line = in.readLine() ) != null ) {
                    String[] tokens = line.split( "," );
                    // sanity check
                    if ( tokens.length == 2 ) {
                        String[] modalTokens = tokens[1].split( " " );
                        ArrayList<String> modalPhrase = new ArrayList<>();
                        modalPhrase.add( modalTokens[0] );
                        modalPhrase.add( modalTokens[1] );
                        store.put( tokens[0], modalPhrase );
                    }
                }
            }
        }
        catch ( IOException ioEx ) {
            LOGGER.error(
                    "problem encountered instantiating Modal Expansion component: {}",
                    ioEx.getMessage() );
        }
    }

    boolean isExpandable ( String candidate ) {
        return this.store.containsKey( candidate.toLowerCase() );
    }

    boolean containsExpandable ( List<String> candidates ) {
        return candidates.stream().anyMatch( candidate -> {
            return isExpandable( candidate );
        } ); 
    }

    // simplest, but repetitive
    List<String> getExpansionFor ( String candidate ) {
        return this.store.get( candidate.toLowerCase() );
    }

}
