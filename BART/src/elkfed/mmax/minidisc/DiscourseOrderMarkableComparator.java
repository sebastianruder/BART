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

/** orders markables by their start position.
 * if start positions are equal, smaller markables are
 * ordered before larger ones
 *
 * @author versley
 */
public class DiscourseOrderMarkableComparator
    implements java.util.Comparator<Markable> {

    @Override
    public boolean equals(Object o) {
        if (o.getClass() == this.getClass()) {
            return true;
        } else {
            return false;
        }
    }
    public int compare(Markable o1, Markable o2) {

        int cmp_start=o1.getLeftmostDiscoursePosition() -
                o2.getLeftmostDiscoursePosition();
        if (cmp_start!=0) return cmp_start;
        int cmp_end=o1.getRightmostDiscoursePosition()-
                o2.getRightmostDiscoursePosition();
        if (cmp_end!=0) return cmp_end;
// changed by olga here: to allow discontinuous markables from the same span

        int[] h1=o1.getHoles();
        int[] h2=o2.getHoles();
        int sz1=0; if (h1!=null) sz1=h1.length;
        int sz2=0; if (h2!=null) sz2=h2.length;
  
        int j;
        for (j=0; j<sz1; j++) {
          if (j>=sz2) return 1;
          if (h1[j]<h2[j]) return -1;
          if (h2[j]>h1[j]) return 1;
        }
        if (sz1==0 && sz2>0) return -1;
        return 0;

    }

    @Override
    public int hashCode() {
        int hash = 0xe1e4fed;
        return hash;
    }
    
}
