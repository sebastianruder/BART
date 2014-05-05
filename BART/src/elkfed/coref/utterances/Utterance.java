/* Utterance.java
 *
 * Utterance code from GUITAR
 *
 * Mangled beyond any recognition by MP
 *
 */

/**************************************************************************
*  GuiTAR - A General Tool for Anaphora Resolution
*  Copyright (C) 2004-2007
*  Mijail Kabadjov, Massimo Poesio, Olivia Sanchez-Graillet, Philippe Goux
*
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with this program; if not, write to the Free Software Foundation, Inc.,
*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
**************************************************************************/

package elkfed.coref.utterances;

import java.util.ArrayList;
import java.util.List;
import elkfed.coref.mentions.Mention;
import edu.stanford.nlp.trees.Tree;
//import org.w3c.dom.*;
//import uk.ac.essex.malexa.nlp.dp.GuiTAR.util.*;

/**
 * A class that encapsulates the general functionality of an Utterance.
 * Eventually this class might have additional functionality, but for the moment it acts as a Segment,
 * the main difference being that its children are Cfs not Utterances nor Segments.
 * @author Mijail A. Kabadjov
 * @version 1.1
 */

 
public class Utterance implements Comparable<Utterance> {
    
    /* In GUITAR Utterance  is an extension of segment - 
       not needed for the moment */
    
    /** 
     * Only associate sentences with utterances
     */
    private Tree    _sentenceTree;          // pointer to StanfordNLP-style 
                                            // tree
    private ArrayList<Mention> _CFs;        // List of CFs for  utterance
    private int     _leftBoundary;          // left boundary of utterance   
    private int     _rightBoundary;         // right boundary 

    /**
     * Initialises the instance variables.
     * @param node The syntax Tree associated with this utterance
     * @param seg The segment to which this utterance belongs
     */
    public Utterance(Tree tree) {
        
        _sentenceTree = tree;
        _CFs = new ArrayList<Mention>();
        _leftBoundary = -1;
        _rightBoundary = -1;
        //super(seg);
    }
    
    /**
     * Retrieves the Tree of this utterance.
     * @return Tree 
     */
    public Tree getTree() {
        return _sentenceTree;
    }


    /**
     * Adds a new Cf to this utterance.
     * @param child The Cf to be added
     */
    public void addCF(Mention CF) {
        _CFs.add(CF);
    }
    
    /**
     * Return the CFs list
     * @param empty
     */
    public ArrayList<Mention> getCFs() {
        return _CFs;
    }   
    
    /**
     * Left boundary
     * @param empty
     */
    public int getLeftBoundary() {
        return _leftBoundary;
    }
    public void setLeftBoundary(int lb) {
        _leftBoundary = lb;
    }
    
    /**
     * Right boundary
     * @param empty
     */
    public int getRightBoundary() {
        return _rightBoundary;
    }
    public void setRightBoundary(int rb) {
        _rightBoundary = rb;
    }
    
    /**
     * Sorting
     */
    public int compareTo(Utterance utt) {
        if (_leftBoundary < utt.getLeftBoundary()) {
            return -1;
        } 
        else if (_leftBoundary > utt.getLeftBoundary()) {
            return 1;
        } else {
            return 0;
        }
    }

} //end class Utterance
