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

/**
 * Provides version constants for use in the Factory and reporting to the user.
 *
 */
final class Version {
    private static final String NAME = "intt";
    private static final String MAJOR = "0";
    private static final String MINOR = "8";
    private static final String PATCH_LEVEL = "8-dev";
    
    static String getNameAndVersion() {
        return String.format( "%s %s", getName(), getVersion() );
    }
    
    static String getName() {
        return NAME;
    }
    
    static String getVersion() {
        return String.format( "%s.%s.%s", MAJOR, MINOR, PATCH_LEVEL );
    }
}
