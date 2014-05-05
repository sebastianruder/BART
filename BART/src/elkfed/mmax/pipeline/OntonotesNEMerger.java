/*
 * Copyright 2007 Project ELERFED
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
package elkfed.mmax.pipeline;

import elkfed.mmax.util.Markables;
import java.util.ArrayList;
import java.util.List;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;

import java.util.HashMap;
import static elkfed.mmax.MarkableLevels.*;
import static elkfed.lang.EnglishLinguisticConstants.*;

/**
 * does Soon-style merging of gold chunks and gold NER
 * for Ontonotes
 */
public class OntonotesNEMerger extends MarkableCreator {

    /** A reusable list of enamex */
    protected List<Markable> enamexes;

    /** Creates a new instance of Merger */
    public OntonotesNEMerger() {
        super();
        this.enamexes = new ArrayList<Markable>();
    }
    public static final String WANTED_ENTITY = "person|organization|location|gpe|plant";

    /** Merges NP and enamex chunks into a single markable level */
    protected void runComponent() {
        // 0. clean up namely remove leading and trailing genitives and punctuation
        cleanUp();

        /*  1. Fix the candidate markable boundaries following Soon et al.:
         *  "both the noun phrases determined by the noun phrase identification
         *  module and the named entities are merged in such a way that if the
         *  noun phrase overlaps with a named entity, the noun phrase boundaries
         *  will be adjusted to subsume the named entity." 
         *
         *  2. Added embedded enamex are examded to full NP bounday
         *  [president [Clinton]_enamex]_np -----> [president Clinton]_enamex
         *
         */

        this.enamexes = getEnamex();

        ForEachNPChunk:
        for (Markable np : getNPs()) {
            // ForEachNamedEntity:
            for (int ne = 0; ne < enamexes.size(); ne++) {
                final Markable enamex = enamexes.get(ne);
                // if the NE is not one of those we want, skip it
                if (!enamex.getAttributeValue("tag").toLowerCase().matches(WANTED_ENTITY)) {
                    continue;
                }
                // get the two markable spans
                final int[] npSpan = Markables.getInstance().getCorrectBoundaries(np);
                final int[] enamexSpan = Markables.getInstance().getCorrectBoundaries(enamex);
                // if they have the same span, remove the NP    
                // same thing if NP right embed enamex
                if (Markables.getInstance().haveSameSpan(npSpan, enamexSpan)) {
                    continue ForEachNPChunk;
                } // else if they overlap (but not embed) fix *enamex* onset/offset
                else if (Markables.getInstance().rightembed(npSpan, enamexSpan)) {
                    // adjust the enamex boundaries
                    int[] new_span = Markables.span_union(npSpan, enamexSpan);
                    enamex.adjustSpan(new_span[0], new_span[1]);
                    enamexes.set(ne, enamex);
                    continue ForEachNPChunk;
                } // else if they overlap (but not embed) fix *np* onset/offset
                else if (Markables.getInstance().overlap(npSpan, enamexSpan)) {
                    // adjust the noun phrases boundaries
                    int[] new_span = Markables.span_union(npSpan, enamexSpan);
                    np.adjustSpan(new_span[0], new_span[1]);
                }
            }
            // if we made it so far, keep the np
            this.nps.add(np);
        }
    }

    /** Add coreference candidate markables to a document */
    protected void addMarkables() {
        for (Markable markable : nps) {
            addMarkable(markable, DEFAULT_CHUNK_LEVEL);
        }
        for (Markable markable : enamexes) {
            if (!markable.getAttributeValue("tag").toLowerCase().matches(WANTED_ENTITY)) {
                continue;
            }
            addMarkable(markable, DEFAULT_ENAMEX_LEVEL);
        }
    }

    String map_ne_type(String ontonotes_type) {
        String type = ontonotes_type.toLowerCase();
        if (type.equals("gpe")) {
            return "location";
        }
        if (type.equals("plant")) {
            return "location";
        }
        return type;
    }

    /** Add base attributes of a markable to the attribute hashmap */
    protected HashMap<String, String> addBaseAttributes(
            final Markable markable, final HashMap<String, String> attributes, final String type) {
        // is a prenominal or modifier?
        attributes.put(ISPRENOMINAL_ATTRIBUTE, isPrenominal());
        // is it a np chunk or enamex?
        attributes.put(TYPE_ATTRIBUTE, type);
        // np, org, loc, etc.
        if ("enamex".equals(type)) {
            attributes.put(LABEL_ATTRIBUTE, map_ne_type(markable.getAttributeValue(TAG_ATTRIBUTE)));
        }
        return attributes;
    }

    /** Does some string massaging prior to markable level generation */
    protected void cleanUp() {
        // 2. do some clean up
        for (Markable np : getNPs()) {
            cleanMarkableUp(np, currentChunkLevel);
        }
        for (Markable enamex : getEnamex()) {
            cleanMarkableUp(enamex, currentEnamexLevel);
        }
    }

    /** Remove trailing saxon genitives and quotation marks from a markable */
    private void cleanMarkableUp(Markable markable, MarkableLevel level) {
        String firstToken = markable.getDiscourseElements()[0].toLowerCase();
        String lastToken = markable.getDiscourseElements()[markable.getDiscourseElements().length - 1].toLowerCase();

        while (markable != null &&
                (firstToken.equals(SAXON_GENITIVE) ||
                firstToken.matches(PUNCTUATION_MARK) ||
                firstToken.matches(RELATIVE_PRONOUN))) {
            markable = removeLeadingToken(markable, level);
            if (markable != null) {
                firstToken = markable.getDiscourseElements()[0].toLowerCase();
            }
        }

        while (markable != null &&
                (lastToken.equals(SAXON_GENITIVE) ||
                lastToken.matches(PUNCTUATION_MARK))) {
            markable = removeTrailingToken(markable, level);
            if (markable != null) {
                lastToken = markable.getDiscourseElements()[markable.getDiscourseElements().length - 1].toLowerCase();
            }
        }
    }

    /** Used to remove leading tokens from markable */
    private Markable removeLeadingToken(final Markable markable, final MarkableLevel level) {
        // we simply remove stand-alone markables
        if (markable.getDiscourseElements().length == 1) {
            return deleteMarkable(markable, level);
        }

        // we check we do not add nested NP with the same span
        for (Markable previousMarkable : level.getMarkables()) {
            if (previousMarkable.getLeftmostDiscoursePosition() ==
                    markable.getLeftmostDiscoursePosition() + 1 &&
                    previousMarkable.getRightmostDiscoursePosition() ==
                    markable.getRightmostDiscoursePosition()) {
                return deleteMarkable(markable, level);
            }
        }

        markable.adjustSpan(markable.getLeftmostDiscoursePosition() + 1,
                markable.getRightmostDiscoursePosition());

        return markable;
    }

    /** Used to remove trailing tokens from markable */
    private Markable removeTrailingToken(final Markable markable, final MarkableLevel level) {
        // we simply remove stand-alone markables
        if (markable.getDiscourseElements().length == 1) {
            return deleteMarkable(markable, level);
        }

        // we check we do not add nested NP with the same span
        for (Markable previousMarkable : level.getMarkables()) {
            if (previousMarkable.getLeftmostDiscoursePosition() ==
                    markable.getLeftmostDiscoursePosition() &&
                    previousMarkable.getRightmostDiscoursePosition() ==
                    markable.getRightmostDiscoursePosition() - 1) {
                return deleteMarkable(markable, level);
            }
        }

        markable.adjustSpan(markable.getLeftmostDiscoursePosition(),
                markable.getRightmostDiscoursePosition() - 1);

        return markable;
    }

    /** Returns complement of an ArrayList, elements in a which are NOT in b */
    private String[] complement(final ArrayList<String> a, final ArrayList<String> b) {
        if (a == null) {
            return null;
        }
        if (b == null) {
            return (String[]) a.toArray(new String[a.size()]);
        }

        final ArrayList<String> c = new ArrayList<String>();
        for (String elem : a) {
            if (!b.contains(elem)) {
                c.add(elem);
            }
        }
        return (String[]) c.toArray(new String[c.size()]);
    }

    private Markable deleteMarkable(final Markable markable, final MarkableLevel level) {
        try {
            level.deleteMarkable(markable);
        } catch (NullPointerException npe) {
            // MMAX2 deleteMarkable crappola
            // I hate
            // (1) to catch npe
            // (2) to handle with nothing
        }
        return null;
    }

    protected String isPrenominal() {
        return "false";
    }
}
