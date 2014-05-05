package elkfed.mmax.util;
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

import elkfed.mmax.*;
import java.util.ArrayList;
import java.util.List;

import java.util.regex.Pattern;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MarkableQuery;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.minidisc.Markable;

import static elkfed.mmax.MarkableLevels.DEFAULT_COREF_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_MARKABLE_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_SECTION_LEVEL;

/** A class of util methods for working with 
 *  features specific to coreference documents
 *
 * @author ponzetsp
 */
public class CorefDocuments {

    /** The coreference set attribute */
    public static final String COREF_SET_ATTRIBUTE = "coref_set";
    /** The section name attribute */
    public static String SECTION_NAME_ATTRIBUTE = "name";
    /** IMPLEMENTATION DETAIL: the singleton instance */
    private static CorefDocuments instance;

    /** Getter for instance */
    public static synchronized CorefDocuments getInstance() {
        if (instance == null) {
            instance = new CorefDocuments();
        }
        return instance;
    }

    /** This class cannot be instantiated from outside (being a singleton) */
    private CorefDocuments() {
    }

    public Markable getText(MiniDiscourse doc) {
        MarkableLevel lvl = doc.getMarkableLevelByName(DEFAULT_SECTION_LEVEL);
        MarkableQuery q = new MarkableQuery(lvl);
        q.addAttRE(SECTION_NAME_ATTRIBUTE, Pattern.compile("t(?:e)?xt"));
        List<Markable> text_segs = q.execute(lvl);
        if (text_segs.isEmpty()) {
            return null;
        } else {
            return text_segs.get(0);
        }
    }

    /** Gets the headers of a MUC document */
    public List<Markable> getHeaders(final MiniDiscourse document) {
        final List<Markable> headers =
                new ArrayList<Markable>();
        for (Markable section : DiscourseUtils.getMarkables(document, DEFAULT_SECTION_LEVEL)) {
            if (!section.getAttributeValue(SECTION_NAME_ATTRIBUTE).equals("trailer") &&
                    !section.getAttributeValue(SECTION_NAME_ATTRIBUTE).equals("text")) {
                headers.add(section);
            }
        }
        return headers;
    }

    /** Checks whether the markable by the pipeline of processing components
     *  correctly identifies at least one element of a coreference chain
     */
    public Markable markableIsaCorefElement(final MiniDiscourse doc, final Markable markable) {
        // for each token in the markable
        for (String discourseElementID : markable.getDiscourseElementIDs()) {
            // get coref chains element at that position and iterate
            final List<Markable> corefElements =
                    doc.getMarkableLevelByName(DEFAULT_COREF_LEVEL).
                    getMarkablesAtDiscourseElementID(
                    discourseElementID, MiniDiscourse.DISCOURSEORDERCMP);
            for (Markable corefElement : corefElements) {
                if (corefOverlapsMarkable(doc, corefElement, markable) != null) {
                    return corefElement;
                }
            }
        }
        // if we made it so far, return null as we did not find anything relevant
        return null;
    }

    /** Checks whether the element of a coreference chain has been correctly
     *  identified as a markable by the pipeline of processing components
     */
    public Markable corefElementIsaMarkable(final MiniDiscourse doc, final Markable corefElem) {

        // for each token in the coreference chain element
        for (String discourseElementID : corefElem.getDiscourseElementIDs()) {
            // get extracted markables at that position and iterate
            final List<Markable> markables =
                    doc.getMarkableLevelByName(DEFAULT_MARKABLE_LEVEL).
                    getMarkablesAtDiscourseElementID(
                    discourseElementID, MiniDiscourse.DISCOURSEORDERCMP);
            for (Markable markable : markables) {
                if (corefOverlapsMarkable(doc, corefElem, markable) != null) {
                    return markable;
                }
            }
        }
        // if we made it so far, return null as we did not find anything relevant
        return null;
    }

    /** Checks whether a "key" and "response" markable overlap */
    public Markable corefOverlapsMarkable(final MiniDiscourse doc,
            final Markable corefMarkable, final Markable markable) {
        // get the discourse positions of the response and coreference markables
        final int[] corefBorders =
                Markables.getInstance().getCorrectBoundaries(corefMarkable);
        final int[] markableBorders =
                Markables.getInstance().getCorrectBoundaries(markable);
        final int markableLeftBorder = markableBorders[0];
        final int markableRightBorder = markableBorders[1];
        assert markableLeftBorder<=markableRightBorder;
        final boolean corefHasMinIds =
                (corefMarkable.getAttributeValue("min_ids", null) != null);
        final boolean markableHasMinIds =
                (markable.getAttributeValue("min_ids", null) != null);

        // exact match?
        if (Markables.haveSameSpan(markableBorders, corefBorders)) {
            return corefMarkable;
        } // min match?
        else if (corefHasMinIds) {
            // for each valid minimum span
            if (!markableHasMinIds) {
                for (int[] minSpan : Markables.getInstance().getMINspans(doc, corefMarkable)) {
                    if (Markables.embeds(markableBorders, minSpan) &&
                            Markables.embeds(corefBorders, markableBorders)) {
                        return corefMarkable;
                    }
                }
            } else {
                // markableHasMinIds && corefHasMinIds
                for (int[] minSpanC : Markables.getInstance().getMINspans(doc, corefMarkable)) {
                    for (int[] minSpanM : Markables.getInstance().getMINspans(doc, markable)) {
                        // minSpanC and minSpanM have to overlap
                        // minSpanC must be subspan of markableBorders
                        // minSpanM must be subspan of corefBorders
                        if (Markables.embeds(markableBorders, minSpanC) &&
                                Markables.embeds(corefBorders, minSpanM) &&
                                Markables.nonzero_intersection(minSpanC, minSpanM)) {
                            return corefMarkable;
                        }
                    }
                }
            }
        } else if (markableHasMinIds) {
            for (int[] minSpan : Markables.getInstance().getMINspans(doc, markable)) {
                if (Markables.embeds(corefBorders, minSpan) &&
                        Markables.embeds(markableBorders, corefBorders)) {
                    return corefMarkable;
                }
            }
        }
        return null;
    }

}
