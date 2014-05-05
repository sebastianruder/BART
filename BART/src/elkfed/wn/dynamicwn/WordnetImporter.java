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
package elkfed.wn.dynamicwn;

import elkfed.wn.RelationType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * imports Princeton Wordnet and similar file formats
 * @author yannick
 */
public class WordnetImporter {

    protected WordnetImpl _wn = new WordnetImpl();
    Map<String, SynsetImpl> _synsets = new HashMap<String, SynsetImpl>();

    class RelationFixup {

        String from;
        String to;
        RelationType type;

        Relation makeRelation() {
            return new Relation(_synsets.get(from),
                    _synsets.get(to),
                    type);
        }
    }
    List<RelationFixup> _rels = new ArrayList<RelationFixup>();

    void loadStream(BufferedReader r) throws IOException {
        while (true) {
            String l = r.readLine();
            if (l == null) {
                break;
            }
            if (l.charAt(0) == ' ') {
                continue;
            }
            String[] parts0 = l.split(" \\| ");
            String[] parts = parts0[0].split(" ");
            // pos + offset -> v00000231
            String pos=parts[2];
            if ("s".equals(pos)) pos="a";
            String synsetId = pos + parts[0];
            SynsetImpl synset = new SynsetImpl();
            _synsets.put(synsetId, synset);
            _wn.addSynset(synset);
            if (parts0.length > 1) {
                synset._gloss = parts0[1];
            }
            int w_cnt = Integer.parseInt(parts[3], 16);
            for (int i = 0; i < w_cnt; i++) {
                LexUnitImpl lu = new LexUnitImpl(synset, parts[4 + 2 * i]);
                _wn.addLexUnit(lu);
            }
            int p_offset = 4 + 2 * w_cnt + 1;
            int p_cnt = Integer.parseInt(parts[p_offset - 1]);
            for (int i = 0; i < p_cnt; i++) {
                // TBD: distinguish between synrels and lexrels
                RelationFixup rel = new RelationFixup();
                rel.from = synsetId;
                rel.to = parts[p_offset + 2] + parts[p_offset + 1];
                rel.type = RelationType.getWNRelation(parts[p_offset]);
                _rels.add(rel);
                p_offset += 4;
            }
        }
    }

    public void do_fixup() {
        for (RelationFixup rel : _rels) {
            SynsetImpl from = _synsets.get(rel.from);
            SynsetImpl to = _synsets.get(rel.to);
            if (to==null) {
                System.out.print("no to:"+rel.to+
                        " (from "+rel.from+")");
            }
            Relation rel2=new Relation(from, to, rel.type);
            _wn.addSynsetRelation(rel2);
        }
        _rels.clear();
    }

    public static void main(String[] args) {
        WordnetImporter wni = new WordnetImporter();
        try {
            for (String fname : args) {
                BufferedReader br=new BufferedReader(new FileReader(fname));
                wni.loadStream(br);
                br.close();
            }
            wni.do_fixup();

            for (LexUnitImpl lu : wni._wn.lexUnitsForWord("dog")) {
                SynsetImpl syn = lu.getSynset();
                System.out.println(syn);
                for (Relation rel: syn.getRels()) {
                    System.out.println(rel._type+" "+rel._to);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
