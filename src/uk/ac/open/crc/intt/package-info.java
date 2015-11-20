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

/**
 * A tokeniser and a factory class to create and configure tokeniser instances.
 *
 * <p>
 * INTT was designed to tokenise identifier names extracted from Java source
 * code. In the default configuration, INTT will tokenise identifier names composed
 * from English language words that optionally use the '$' and '_' separator
 * characters. 
 * </p>
 *
 * <h3>Configuration</h3>
 * <p>INTT can be configured to use alternative dictionaries and sets of
 * separator characters making it possible to tokenise identifier names
 * extracted from most programming languages constructed from words and
 * abbreviations in most natural languages. See 
 * {@link uk.ac.open.crc.intt.IdentifierNameTokeniserFactory}
 * for more details.
 * </p>
 *
 * <p>
 * Copyright (C) 2011-2015 <a href="http://www.open.ac.uk/">The Open University</a>
 * </p>
 * 
 * @version 0.7.0-dev
 *
 */

package uk.ac.open.crc.intt;

