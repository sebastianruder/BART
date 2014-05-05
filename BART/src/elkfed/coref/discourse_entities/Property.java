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

package elkfed.coref.discourse_entities;

//import elkfed.knowledge.Sense; // NB the interface not the Implementer
//import uk.ac.essex.malexa.nlp.dp.GuiTAR.la.Sense;

import elkfed.knowledge.SemanticClass;

/**
 * A class that encapsulates the general functionality of a Property (of a Discourse Entity).
 * @author Mijail A. Kabadjov 
 * (Slightly revised by Massimo Poesio for inclusion in ELKFED)
 * @version 1.1
 */
public class Property {

	//Constants
	public static final byte TYPE = 1;
	public static final byte ATTRIBUTE = 2;
	public static final byte RELATION = 3;
	public static final byte NAME = 3;


	//Instance variables
	private byte		type;		//Range of valid values : {type, attribute, relation}
        //private Sense		predicate;	//type=type/attr -> predicate=sense, otherwise predicate="the relation" (e.g. of)
	private String          predicate;
        private String	        argument;	//only when type=relation, 
                                                // used to be: points to another DE
                                                // now: just string
        private char          pos;
        
	public Property ( byte tp, String pred) {
		type = tp;
		predicate = pred;
	}
        
        public Property ( byte tp, String pred, char POS ) {
		type = tp;
		predicate = pred;
                pos = POS;
	}
        
        /* More sophisticated version with senses
        public Property ( byte tp, Sense pred ) {
		type = tp;
		predicate = pred;
	}
         **/

	public Property ( String pred, String arg ) {
		type = Property.RELATION; //This is for relations only
		predicate = pred;
		argument = arg;
	}
        
        /* More sophisticated version with Discourse Entities
         *
        public Property ( String pred, DiscourseEntity arg ) {
		type = Property.RELATION; //This is for relations only
		predicate = pred;
		argument = arg;
	}
         */

	public byte getType() {
		return type;
	}

	public String getPredicate() {
		return predicate;
	}

	public String getArgument() {
		return argument;
	}

	public boolean equals( Object o ) {
                Property other = ((Property)o);
		return getPredicate().equals( ((Property)o).getPredicate() ) 
                 &&
                (getArgument()==null || getArgument().equals(other.getPredicate()));
	}

	/** It seems that this method has to be overriden as well, in order for the the set framework to work ***/
	public int hashCode() {
		return ( getPredicate().hashCode() );
	}


	public String toString() {
		return( "Pty-" + type + "-" + predicate );
	}

    public String giveString() {
		return( predicate );
	}

        public char getPOS(){
            return pos;
        }
} //end class Property

