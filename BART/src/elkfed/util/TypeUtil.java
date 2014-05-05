/*
 * Copyright 2007 EML Research
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package elkfed.util;

/**
 * TypeUtil isolates type-unsafety so that code that which uses it for
 * legitimate reasons can stay warning-free.
 *
 * @author John V. Sichi
 */
public class TypeUtil<T>
{
    /**
     * Casts an object to a type.
     *
     * @param o object to be cast
     * @param typeDecl conveys the target type information; the actual value is
     *                 unused and can be null since this is all just stupid
     *                 compiler tricks
     *
     * @return the result of the cast
     */
    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(Object o, TypeUtil<T> typeDecl)
    {
        return (T) o;
    }
}

// End TypeUtil.java
