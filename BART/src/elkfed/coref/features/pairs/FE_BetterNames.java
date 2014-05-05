/*
 * Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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

package elkfed.coref.features.pairs;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author yannick.versley
 */
public class FE_BetterNames implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_BNAMES_IDENT=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "BNamesIdent");
    public static final FeatureDescription<Boolean> FD_BNAMES_PREFIX=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "BNamesPrefix");
    public static final FeatureDescription<Boolean> FD_BNAMES_SUFFIX=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "BNamesSuffix");
    public static final FeatureDescription<Boolean> FD_BNAMES_MIDDLE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "BNamesMiddle");
    public static final FeatureDescription<Boolean> FD_BNAMES_ANY=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "BNamesAny");
    public static final FeatureDescription<Boolean> FD_BNAMES_SOME=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "BNamesSome");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_BNAMES_PREFIX);
        fds.add(FD_BNAMES_SUFFIX);
        fds.add(FD_BNAMES_MIDDLE);
        fds.add(FD_BNAMES_ANY);
    }

    private static Pattern honorific_re=
            Pattern.compile("[a-z]{4,}|.*-[a-z]+|M(?:r|s|rs)\\.|Minister|"
            +"President|Secretary|Premier|General|Judge|Justice|Ambassador|Major|"
            +"Chief|Prince|Maj\\.|Gen\\.|'s");
    public static String[] prepareName(String name) {
        String[] nameParts=name.split(" ");
        int last_h=-1;
        for (int i=0; i<nameParts.length-1; i++) {
            if (honorific_re.matcher(nameParts[i]).matches()) {
                last_h=i;
            }
        }
        last_h += 1;
        String[] result=new String[nameParts.length-last_h];
        for (int i=0; i<result.length; i++) {
            result[i]=nameParts[i+last_h];
        }
        return result;
    }

    public void extractFeatures(PairInstance inst) {
        Mention m_ana=inst.getAnaphor();
        Mention m_ante=inst.getAntecedent();
        if (!m_ana.isEnamex() ||
                !m_ante.isEnamex()) {
        inst.setFeature(FD_BNAMES_PREFIX, false);
        inst.setFeature(FD_BNAMES_SUFFIX, false);
        inst.setFeature(FD_BNAMES_MIDDLE, false);
        inst.setFeature(FD_BNAMES_ANY, false);
            return;
        }
// --- check that they are both Person
        if (!(inst.getAntecedent().getEnamexType().startsWith("per"))) {
        inst.setFeature(FD_BNAMES_PREFIX, false);
        inst.setFeature(FD_BNAMES_SUFFIX, false);
        inst.setFeature(FD_BNAMES_MIDDLE, false);
        inst.setFeature(FD_BNAMES_ANY, false);
            return;
        }
        if (!(inst.getAnaphor().getEnamexType().startsWith("per"))) {
        inst.setFeature(FD_BNAMES_PREFIX, false);
        inst.setFeature(FD_BNAMES_SUFFIX, false);
        inst.setFeature(FD_BNAMES_MIDDLE, false);
        inst.setFeature(FD_BNAMES_ANY, false);
            return;

        }



        String[] ana_parts=prepareName(m_ana.getHeadOrName());
        String[] ante_parts=prepareName(m_ante.getHeadOrName());
        boolean is_prefix=false, is_suffix=false, is_ident=false,
                is_middle=false, is_any=false, is_some=false;
        if (ana_parts.length<=ante_parts.length) {
            int len_diff=ante_parts.length-ana_parts.length;
            is_suffix=true;
            is_prefix=true;
            for (int i=0; i<ana_parts.length; i++) {
                if (!ana_parts[i].equals(ante_parts[i])) {
                    is_prefix=false;
                }
                if (!ana_parts[i].equals(ante_parts[i+len_diff])) {
                    is_suffix=false;
                }
            }
        }
        if (ana_parts.length==ante_parts.length) {
            is_ident=is_prefix;
        }
        if (ana_parts.length==2 &&
                ante_parts.length==3) {
            is_middle=(ana_parts[0].equals(ante_parts[0]) &&
                    ana_parts[1].equals(ante_parts[2]));
        }
        int n_matching=0;
        for (int i=0; i<ana_parts.length; i++) {
            for (int j=0; j<ante_parts.length; j++) {
                if (ana_parts[i].equals(ante_parts[j])) {
                    n_matching++;
                }
            }
        }
        if (n_matching>0) is_any=true;
        if (n_matching>1) is_some=true;
//        inst.setFeature(FD_BNAMES_IDENT, is_ident);
        inst.setFeature(FD_BNAMES_PREFIX, is_prefix);
        inst.setFeature(FD_BNAMES_SUFFIX, is_suffix);
        inst.setFeature(FD_BNAMES_MIDDLE, is_middle);
        inst.setFeature(FD_BNAMES_ANY, is_any);
//        inst.setFeature(FD_BNAMES_SOME, is_some);
    }

public static Boolean AliasBnamesPS(String s1, String s2) {
   String[] ana_parts=prepareName(s1);
   String[] ante_parts=prepareName(s2);
        boolean is_prefix=false, is_suffix=false, is_ident=false,
                is_middle=false, is_any=false, is_some=false;
        if (ana_parts.length<=ante_parts.length) {
            int len_diff=ante_parts.length-ana_parts.length;
            is_suffix=true;
            is_prefix=true;
            for (int i=0; i<ana_parts.length; i++) {
                if (!ana_parts[i].equalsIgnoreCase(ante_parts[i])) {
                    is_prefix=false;
                }
                if (!ana_parts[i].equalsIgnoreCase(ante_parts[i+len_diff])) {
                    is_suffix=false;
                }
            }
        }
        if (ana_parts.length==ante_parts.length) {
            is_ident=is_prefix;
        }
        if (ana_parts.length==2 &&
                ante_parts.length==3) {
            is_middle=(ana_parts[0].equalsIgnoreCase(ante_parts[0]) &&
                    ana_parts[1].equalsIgnoreCase(ante_parts[2]));
        }
     return is_prefix||is_suffix;
   } 

}
