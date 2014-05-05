/*
 * Copyright 2009 Yannick Versley / Univ. Tuebingen
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

package elkfed.wn;

import java.util.HashMap;

/**
 *
 * @author yannick
 */
public enum RelationType {
    // nouns
    Antonym,
    Hypernym,
    Instance_Hyper,
    Hyponym,
    Instance_Hypo,
    Member_Holonym,
    Substance_Holonym,
    Part_Holonym,
    Member_Meronym,
    Substance_Meronym,
    Part_Meronym,
    Attribute,
    Derivationally_Related,
    Topic_Domain,
    Topic_Member,
    Region_Domain,
    Region_Member,
    Usage_Domain,
    Usage_Member,
    // verbs
    Entailment,
    Cause,
    Also_see,
    Similar_to,
    Verb_group,
    // adjectives
    Participle,
    Pertainym;
    private static final HashMap<String,RelationType> _mapping;
    static {
        _mapping=new HashMap<String,RelationType>();
        _mapping.put("!",Antonym);
        _mapping.put("@",Hypernym);
        _mapping.put("@i",Instance_Hyper);
        _mapping.put("~",Hyponym);
        _mapping.put("~i",Instance_Hypo); // TBD: check
        _mapping.put("#m",Member_Holonym);
        _mapping.put("#s",Substance_Holonym);
        _mapping.put("#p",Part_Holonym);
        _mapping.put("%m",Member_Meronym);
        _mapping.put("%s",Substance_Meronym);
        _mapping.put("%p",Part_Meronym);
        _mapping.put("=",Attribute);
        _mapping.put("+",Derivationally_Related);
        _mapping.put(";c",Topic_Domain);
        _mapping.put("-c",Topic_Member);
        _mapping.put(";r",Region_Domain);
        _mapping.put("-r",Region_Member);
        _mapping.put(";u", Usage_Domain);
        _mapping.put("-u", Usage_Member);
        _mapping.put("*",Entailment);
        _mapping.put(">",Cause);
        _mapping.put("^",Also_see);
        _mapping.put("$",Verb_group);
        _mapping.put("&",Similar_to);
        _mapping.put("<",Participle);
        _mapping.put("\\",Pertainym);
    }
    public static RelationType getWNRelation(String key) {
        return _mapping.get(key);
    }
}
