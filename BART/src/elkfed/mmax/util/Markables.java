/*
 * Markables.java
 *
 * Created on July 22, 2007, 12:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.mmax.util;

import elkfed.config.ConfigProperties;
import elkfed.lang.LanguagePlugin;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableHelper;
import elkfed.mmax.minidisc.MiniDiscourse;

import static elkfed.lang.EnglishLinguisticConstants.ARTICLE;
import static elkfed.lang.EnglishLinguisticConstants.PUNCTUATION_MARK;
import static elkfed.lang.EnglishLinguisticConstants.SAXON_GENITIVE;

/** A class of util methods for working with 
 *  features specific to coreference documents
 *
 * @author ponzetsp
 */
public class Markables {
    
    /* IMPLEMENTATION DETAIL: the singleton instance */
    private static Markables instance;

    /** Getter for instance */
    public static synchronized Markables getInstance()
    {
        if (instance == null)
        { instance = new Markables(); }
        return instance;
    }

    public static int[] span_union(int[] span1, int[] span2) {
        if (span1[0]<span2[0]) {
            // if there's a hole in there, things get wild...
            assert span1[1]>=span2[0];
            return new int[]{span1[0],Math.max(span1[1],span2[1])};
        } else {
            // if there's a hole in there, things get wild...
            assert span2[1]>=span1[0];
            return new int[]{span2[0],Math.max(span1[1],span2[1])};            
        }
    }
    
    /** This class cannot be instantiated from outside (being a singleton) */
    private Markables() {}
    
    /** Get the MIN string span. The int[][] table returned contains
     *  for each row, a min span pair
     *
     */
    public int[][] getMINspans(
            final MiniDiscourse doc, final Markable corefChainElem)
    {   
        if (corefChainElem.getAttributeValue("min_ids", null) == null)
        {
            final int[][] minSpans = new int[1][2];
            // no minimum string: we simply return the full span
            minSpans[0][0] = corefChainElem.getLeftmostDiscoursePosition();
            minSpans[0][1] = corefChainElem.getRightmostDiscoursePosition();
            return minSpans;
        }
        else
        {
            // we have minimum strings: so get the discourse positions of the
            // MIN string attribute. NOTE: we can have multiple min strings
            //
            // get first the min spans
            final String[] minSpanAttribute =
                    corefChainElem.getAttributeValue("min_ids").split(";");
            final int[][] minSpans = new int[minSpanAttribute.length][2];
            
            // for each minimum span
            for (int span = 0; span < minSpanAttribute.length; span++)
            {
                final String[] spanIDs = MarkableHelper.parseRange(minSpanAttribute[span]);
                minSpans[span][0] =
                    doc.getDiscoursePositionFromDiscourseElementID(spanIDs[0]);
                minSpans[span][1] =        
                    doc.getDiscoursePositionFromDiscourseElementID(spanIDs[1]);
            }
            return minSpans;
        }
    }
    
    /** Gets the correct boundaries of a markable, stripping off trailing and
     *  leading punctuation
     */
    public int[] getCorrectBoundaries(Markable markable)
    {
        int leftBoundary = markable.getLeftmostDiscoursePosition();
        int rightBoundary = markable.getRightmostDiscoursePosition();
        final String[] tokens = markable.getDiscourseElements();
        final LanguagePlugin langPlugin=ConfigProperties.getInstance()
                .getLanguagePlugin();
        
        // we iterate through tokens
        int token = 0;
        // strips leading punctuation and articles
        while (
                    langPlugin.unwanted_left(tokens[token])
               &&
                leftBoundary < rightBoundary
               &&
                token < tokens.length
              )         
        { leftBoundary++; token++; }
        
        // strips trailing punctuation
        token = tokens.length - 1;
        while (
                    langPlugin.unwanted_right(tokens[token])
                &&
                    leftBoundary < rightBoundary
                &&
                    token >= 0
              )
        
        { rightBoundary-- ; token--;}
        int[] span = {leftBoundary, rightBoundary};
        return span;
    }
    
    /** Checks whether two markable spans are the same span.
     * @param span1 a two-element array in which the the first element is the starting position of the
     *        markable and the second element is the ending position of the markable.
     * @param span2 a two-element array in which the the first element is the starting position of the
     *        markable and the second element is the ending position of the markable.
     */
    public static boolean haveSameSpan(int[] span1, int[] span2)
    {        
        return (
                    span1[0] == span2[0]
                &&
                    span1[1] == span2[1]
               );
    }

    /** Checks whether two markable spans overlap BUT DO NOT EMBED */
    public static boolean overlap(Markable m1, Markable m2)
    {
        return overlap(
                new int[]{m1.getLeftmostDiscoursePosition(),m1.getRightmostDiscoursePosition()},
                new int[]{m2.getLeftmostDiscoursePosition(),m2.getRightmostDiscoursePosition()}
        );
    }
    
    /** Checks whether two markable spans overlap BUT DO NOT EMBED 
     * @param span1 a two-element array in which the the first element is the starting position of the
     *        markable and the second element is the ending position of the markable.
     * @param span2 a two-element array in which the the first element is the starting position of the
     *        markable and the second element is the ending position of the markable.
     */
    public static boolean overlap(int[] span1, int[] span2)
    {
        return
        (
            // offset overlapping
            (
                span1[0] < span2[0]
             &&
                span1[1] >= span2[0]
             &&
                // but no embedding
                span1[1] < span2[1]
            )
         ||
            // onset overlapping
            (
                span1[0] <= span2[1]
             &&
                span1[1] > span2[1]
             &&
                // but no embedding
                span1[0] > span2[0]
            )  
        );
    }

    // return if the spans somehow overlap
    public static boolean nonzero_intersection(int[] span1, int[] span2)
    {
        if (span1[0]>span2[1]) return false;
        if (span2[0]>span2[1]) return false;
        return true;
    }

    
    /** Checks whether the first markable embed the second OVERLAPPING ON THE LEFT
     *
     *  e.g. (the) [[Clinton]_ENAMEX administration]_NP 
     *
     * @param span1 a two-element array in which the the first element is the starting position of the
     *        markable and the second element is the ending position of the markable.
     * @param span2 a two-element array in which the the first element is the starting position of the
     *        markable and the second element is the ending position of the markable.
     */
    public boolean leftembed(int[] span1, int[] span2)
    {
        return
        (
                span1[0] == span2[0]
             &&
                span1[1] > span2[1]
        );
    }
    
    
    /** Checks whether the first markable embed the second OVERLAPPING ON THE RIGHT
     *
     *  e.g. (the) [president [Clinton]_ENAMEX]_NP 
     *
     * @param span1 a two-element array in which the the first element is the starting position of the
     *        markable and the second element is the ending position of the markable.
     * @param span2 a two-element array in which the the first element is the starting position of the
     *        markable and the second element is the ending position of the markable.
     */
    public boolean rightembed(int[] span1, int[] span2)
    {
        return
        (
                span1[0] < span2[0]
             &&
                span1[1] == span2[1]
        );
    }
    
    /** span1 is a proper superset of span2 */
    public static boolean embeds(int[] span1, int[] span2) {
        return (span1[0]<=span2[0] &&
                span1[1]>=span2[1]);
    }
}
