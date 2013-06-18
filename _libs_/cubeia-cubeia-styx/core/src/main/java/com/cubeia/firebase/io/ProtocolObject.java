/**
 * Copyright 2009 Cubeia Ltd  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cubeia.firebase.io;

/**
 * Any object that should be serializable via the Styx wire protocol
 * should implement this interface. Files implementing this protocol
 * are typically automatically generated.
 */
public interface ProtocolObject extends Visitable {
    /**
     * An object needs a unique classId.
     */
    int classId();

    /**
     * Serialisation method.
     */
    void save(PacketOutputStream ps) throws java.io.IOException;

    /**
     * Deserialization method.
     */
    void load(PacketInputStream ps) throws java.io.IOException;
   
}
