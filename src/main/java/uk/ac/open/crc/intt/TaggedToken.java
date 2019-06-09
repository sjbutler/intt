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
package uk.ac.open.crc.intt;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single token as a string and a list of 0..n word lists
 * in which the token was found. 
 */
public class TaggedToken {
    private final String content;
    private List<String> wordLists;

    TaggedToken( String content ) {
        this.content = content;
        this.wordLists = new ArrayList<>();
    }
    
    /**
     * Recovers the token string.
     * 
     * @return the value of the token
     */
    public String getContent() {
        return this.content;
    }
    
    /**
     * Recovers a list of 0 to n names of word lists in which the token was found. 
     * @return a list with 0..n members. 
     */
    public List<String> wordLists() {
        return this.wordLists;
    }
    
    boolean add(String listName) {
        return this.wordLists.add(listName);
    }
}
