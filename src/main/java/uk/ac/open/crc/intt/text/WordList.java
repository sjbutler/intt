/*
 * Copyright (C) 2019 Simon Butler.
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
package uk.ac.open.crc.intt.text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a set of words.  
 * 
 */
public class WordList {
    private final String tag;
    private final Set<String> words;
    
    public WordList( String tag, List<String> words ) {
        this.tag = tag;
        this.words = new HashSet<>(words);
    }
    
    public WordList( String tag, Set<String> words ) {
        this.tag = tag;
        this.words = words;
    }
    
    public boolean isWord( String word ) {
        return this.words.contains( word.toLowerCase() );
    }
    
    public String tag() {
        return this.tag;
    }
    
    public int size() {
        return this.words.size();
    }
    
    public boolean isEmpty() {
        return this.words.isEmpty();
    }
}
