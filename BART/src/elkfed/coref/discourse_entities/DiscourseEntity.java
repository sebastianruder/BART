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



import elkfed.coref.mentions.*;
import edu.stanford.nlp.trees.Tree;
import elkfed.config.ConfigProperties;
import elkfed.lang.LanguagePlugin;
import elkfed.lang.NodeCategory;

import java.util.*;
import java.util.logging.Logger;

import elkfed.knowledge.SemanticClass;
import elkfed.lang.EnglishLanguagePlugin;
import elkfed.lang.MentionType.Features;

/**
 * @author Julian Baumann
 */

   /*
    * minimal DiscourseEntity, should work for StringMatch by iterating over mentions
    */

public class DiscourseEntity {
	
	private static int nextID = 0;
	private int ID;
	
	private boolean firstMention_isFirstMention;
	
	private List<Mention> mentions;
   
   
	public DiscourseEntity(Mention m) {
		ID = nextID;
		nextID++;
		mentions = new ArrayList<Mention>();
		mentions.add(m);
	}
	
	public List<Mention> getMentions() {
		return mentions;
	}
	
	
	
	public void merge(Mention ante) {
		
		DiscourseEntity deAnte = ante.getDiscourseEntity();
		for (Mention m : deAnte.getMentions()) {
			mentions.add(m);
			m.setDiscourseEntity(this);
		}
	}
	
	 public void set_firstMention_isFirstMention(boolean isFirstMention) {
	        firstMention_isFirstMention = isFirstMention;
	    }
}