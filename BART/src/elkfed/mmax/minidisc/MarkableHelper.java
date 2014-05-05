/*
 * Copyright 2007 Yannick Versley / Univ. Tuebingen
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
package elkfed.mmax.minidisc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author versley
 */
public class MarkableHelper {
    private static Logger _logger=Logger.getAnonymousLogger();

    public static String[] parseRange(String string) {
        int pos=string.indexOf("..");
        if (pos==-1) {
            return new String[]{string,string};
        } else {
            return new String[]{string.substring(0,pos),
                string.substring(pos+2)};
        }
    }
    
    public static String[] parseRanges(String string) {
        int pos=string.indexOf(",");
        if (pos==-1) {
            return parseRange(string);
        } else {
            String[] pieces=string.split(",");
            List<String> rs=new ArrayList<String>();
            for (String piece: pieces) {
                String[] r=parseRange(piece);
                for (String r0: r) rs.add(r0);
            }
            return rs.toArray(new String[rs.size()]);
        }

    }

    public static int parseId(String id, String prefix) {
        if (id.startsWith(prefix) &&
                id.charAt(prefix.length())=='_') {
            return Integer.parseInt(id.substring(prefix.length()+1));
        } else if (id.charAt(0)>='0' &&
                id.charAt(0)<='9') {
            _logger.severe("not a markable ID:"+id);
            return Integer.parseInt(id);
        } else {
            throw new IllegalArgumentException("not a markable ID:"+id);
        }
    }

}
