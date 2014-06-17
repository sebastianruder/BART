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
import java.util.List;
import java.util.logging.Logger;

import elkfed.knowledge.SemanticClass;
import elkfed.lang.EnglishLanguagePlugin;
import elkfed.lang.MentionType.Features;

/**
 * A class that encapsulates the general functionality of a Discourse Entity.
 *
 * @author Mijail A. Kabadjov
 * @author Philippe J. Goux
 * @version 1.2
 *
 * Hopelessly mangled for the purposes of ELKFED by Massimo
 * August 16th 2007
 */

/*
* This seems to be doing a lot of weird things. Should be tested and debugged properly before we start any kind of entity-mention modelling (Olga)
* E.g. -- Head finder! Unify with existing stuff (MMAX and PRS heads). etc
*/


public class CopyOfDiscourseEntityBackup {

    // Class variables
    private static Integer nextId = 0;
    private static final Logger _logger = Logger.getLogger("elkfed.coref.discourse_entities");
    //Instance variables
    private Integer id;                        //The  unique identifier of this DE
    private Set<Property> types;               //set of heads used to refer to
    //this entity (set of Property objects)
    private Set<Property> attributes;          //set of attributes
    //(= set of Property objects type=attribute)
    private Set<Property> relations;           //set of relations to other DEs
    //(set of Property objects type=relation)
    // TODO
//    private Set<Set<Property>> names;          //set of names to be used by PN and
    private HashMap<String, Set<Property>> names;          //set of names to be used by PN and
    // The_PN(set of Property objects type=name)

    //private Set		kind;	       //set of kinds such as company, person...(set of Property objects type=kind)
    // TODO
    private Vector<Mention> corefChain;       //The co-reference chain (list of Cf objects)
    //private DiscourseModel dModel;           //Pointer to the parent DiscourseModel

    //private WordSenseDisambiguator wsd;	//Provides disambiguation service
    // attributes of first mention
    // stored with DE
    private boolean firstMention_isFirstMention;
    private boolean firstMention_isProperName;
    private boolean anyMention_isFirstMention; // in case discourse entity ever gets
    // realized in first position

    /**
     * Discourse Entity constructor. Evokes a new entity in a Discourse Model.
     * @param identifier The identifier of this entity
     * @param cf The Cf evoking this entity
     * @param dm The pointer to the parent Discourse Model
     */
    public CopyOfDiscourseEntityBackup(Mention cf) {
        //wsd = new ImplementerWSD();
        id = nextId;
        nextId = nextId + 1;
        //dModel = dm;
        types = new HashSet<Property>();
        attributes = new LinkedHashSet<Property>(); // Keep order of inputed elements
        relations = new HashSet<Property>();
        names = new HashMap<String, Set<Property>>();
        corefChain = new Vector<Mention>();
        firstMention_isProperName = cf.getIsFirstMention(); // cannot be done
        // by addCF
        firstMention_isFirstMention = false; // provisional - needs to be set
        // after setUttPos
        addCf(cf);
//        printHeads();
    }


    //Accessors
    /**
     * Gets the id of this Discourse Entity.
     * @return Integer The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Gets the heads used to refer to this Discourse Entity.
     * Returns a Set of objects of class Property.
     * @return Set The Set of heads
     */
    public Set<Property> getTypes() {
        return types;
    }

    /**
     * Gets the attributes of this Discourse Entity.
     * Returns a Set of objects of class Property.
     * @return Set The Set of attributes
     */
    public Set<Property> getAttributes() {
        return attributes;
    }

    /**
     * Gets the relations of this Discourse Entity with other Discourse Entities.
     * Returns a Set of objects of class Property.
     * @return Set The Set of relations
     */
    public Set<Property> getRelations() {
        return relations;
    }

    /**
     * Gets the names of this Discourse Entity.
     * Returns a Set of objects of class Property.
     * @return Set The Set of names
     */
    public HashMap<String, Set<Property>> getNames() {
        return names;
    }

    /**
     * Gets the co-reference chain (list of Cfs) of this Discourse Entity.
     * Returns a Set of objects of class Cf.
     * @return Set The Set of Cfs
     */
    public Vector<Mention> getcorefChain() {
        return corefChain;
    }

    public boolean firstMention_isFirstMention() {
        return firstMention_isFirstMention;
    }

    public boolean firstMention_isProperName() {
        return firstMention_isProperName;
    }

    public boolean anyMention_isFirstMention() {
        return anyMention_isFirstMention;
    }

    //Mutators
    /**
     * A method that updates the state of a Discourse Entity. 
     * It stands for an "access" of the DE, and
     * hence it is called after anaphora resolution has taken place. 
     * It expects a Cf object as a parameter and
     * then updates the coreference chain of this DE, and reads head and modifiers 
     * of this Cf to update this information of the DE.
     * @param cf The new Cf (realisation of this entity)
     */
    public void addCf(Mention cf) {
        if (corefChain.contains(cf)) {
            System.out.println("Mention " + cf.getHeadString() + " already included in entity " + this.getId());
        } else {
            corefChain.add(cf);
            if (corefChain.size() > 1) {
                System.out.println("Adding mention " + cf.getHeadString() + " to entity " + getId() +
                        " (" + corefChain.get(0).getHeadString());
                System.out.println("New length of coref chain for " + getId() + ": " + corefChain.size());
            }

            boolean english_mode=(ConfigProperties.getInstance().getLanguagePlugin()
                    instanceof EnglishLanguagePlugin);
            if (english_mode) {

                if (!(cf.computeAppType(cf) == null)) {
                    types.add(new Property(Property.TYPE, cf.computeAppType(cf)));
                    //       System.out.println("ADDING--1 " + cf.toString() + " |" + cf.computeAppType(cf));
                }
                if (!(cf.computePredicationAttr(cf) == null)) {
                    attributes.add(new Property(Property.ATTRIBUTE, cf.computePredicationAttr(cf)));
                    //   System.out.println("ADDING--2 " + " " + cf.computePredicationAttr(cf));
                }
                if (!(cf.computePredicationType(cf) == null)) {
                    types.add(new Property(Property.TYPE, cf.computePredicationType(cf)));
                    //     System.out.println("ADDING--3 " + " " + cf.computePredicationType(cf));
                }
            }
            if (cf.getPronoun()) {
                Property head_type = null;
                Set<Property> attrs = null;
                Set<Property> rels = null;
                if (cf.getFeatures().contains(Features.isFirstSecondPerson)) {
                    types.add(new Property(Property.TYPE, "person"));
                    if (!cf.getNumber()) {
                        types.add(new Property(Property.TYPE, "organization"));
                    }
                } else if (english_mode && (
                        cf.getHeadString().equals("It") || cf.getHeadString().equals("it") ||
                        cf.getHeadString().equals("Its") || cf.getHeadString().equals("its") ||
                        cf.getHeadString().equals("itself"))) {
                    types.add(new Property(Property.TYPE, "object"));
                    types.add(new Property(Property.TYPE, "organization"));
                } else if (english_mode && (
                        cf.getHeadString().equals("They") || cf.getHeadString().equals("they") ||
                        cf.getHeadString().equals("Their") || cf.getHeadString().equals("their"))) {
                    types.add(new Property(Property.TYPE, "person"));
                    types.add(new Property(Property.TYPE, "object"));
                    types.add(new Property(Property.TYPE, "organization"));
                } else if (cf.getSemanticClass() == SemanticClass.MALE) {
                    types.add(new Property(Property.TYPE, "man"));
                } else if (cf.getSemanticClass() == SemanticClass.FEMALE) {
                    types.add(new Property(Property.TYPE, "woman"));
                } else if (cf.getSemanticClass() == SemanticClass.PERSON) {
                    types.add(new Property(Property.TYPE, "person"));
                } else if (cf.getSemanticClass() == SemanticClass.UNKNOWN) {
                    types.add(new Property(Property.TYPE, "UNKNOWN"));
                } else {
                    types.add(new Property(Property.TYPE, cf.getSemanticClass().toString().toLowerCase()));
                }
                addHead(head_type);
                addAttributes(attrs);
                addRelations(rels);
            }

            if (!cf.getPronoun()) {             // if  Mention not a pronoun
                Property head_type = null;
                Set<Property> attrs = null;
                Set<Property> rels = null;
                HashMap<String, String> nms = null;
                // NominalGroup is a specialization of CF with pre and post modifiers and heads
                // DEBUG
                //System.out.print("Creating Discourse Entity for mention:");
                //System.out.println( cf.getMarkableString());

                //NominalGroup np = (NominalGroup) cf;
                //if proper name --> get name structure
                //if not --> compute head, attr and rels

//                String appositionType = cf.computeAppType(cf);

//                if (cf.getProperName() || cf.isEnamex()) {

//                }
                if (cf.getHeadString().equals("January") || cf.getHeadString().equals("February") ||
                        cf.getHeadString().equals("March") || cf.getHeadString().equals("April") ||
                        cf.getHeadString().equals("May") || cf.getHeadString().equals("June") ||
                        cf.getHeadString().equals("July") || cf.getHeadString().equals("August") ||
                        cf.getHeadString().equals("September") || cf.getHeadString().equals("October") ||
                        cf.getHeadString().equals("November") || cf.getHeadString().equals("December") ||
                        cf.getHeadString().equals("Monday") || cf.getHeadString().equals("Tuesday") ||
                        cf.getHeadString().equals("Wednesday") || cf.getHeadString().equals("Thursday") ||
                        cf.getHeadString().equals("Friday") || cf.getHeadString().equals("Saturday") ||
                        cf.getHeadString().equals("Sunday")) {
                    types.add(new Property(Property.TYPE, cf.getHeadString().toLowerCase()));
                } else if ((cf.getProperName() || cf.isEnamex()) && cf.getSemanticClass() == SemanticClass.MALE) {
                    nms = cf.getNameStructure();
                    types.add(new Property(Property.TYPE, "man"));
                    addNames(nms);
                } else if ((cf.getProperName() || cf.isEnamex()) && cf.getSemanticClass() == SemanticClass.FEMALE) {
                    nms = cf.getNameStructure();
                    types.add(new Property(Property.TYPE, "woman"));
                    addNames(nms);
                } else if ((cf.getProperName() || cf.isEnamex()) && SemanticClass.isaPerson(cf.getSemanticClass())) {
                    nms = cf.getNameStructure();
                    types.add(new Property(Property.TYPE, "person"));
                    addNames(nms);
                } else if ((cf.getProperName() || cf.isEnamex()) && cf.getSemanticClass().toString().equals("GPE")) {
                    addNamesNoPerson(cf);
                    types.add(new Property(Property.TYPE, "place"));
                } else if ((cf.getProperName() || cf.isEnamex()) && cf.getSemanticClass() == SemanticClass.LOCATION) {
                    addNamesNoPerson(cf);
                    types.add(new Property(Property.TYPE, "location"));
                } else if ((cf.getProperName() || cf.isEnamex()) && cf.getSemanticClass() == SemanticClass.ORGANIZATION) {
                    addNamesNoPerson(cf);
                    types.add(new Property(Property.TYPE, "organization"));
                } else if ((cf.getProperName() || cf.isEnamex()) && cf.getSemanticClass() == SemanticClass.UNKNOWN) {
                    addNamesNoPerson(cf);
                    types.add(new Property(Property.TYPE, "UNKNOWN"));

                } else {
//                    head_type = computeHead(cf); //Get head
                    head_type = computeHeadLemma(cf);
                    attrs = computeAttributes(cf); //Get attrs
                }

                // TO DO
                // nms =   getNames(cf, head, attrs); //Get nms
                rels = computeInitialRelations(cf);   // Get relations (for now,
                // just those expressed by
                // postmodifiers

                //DEBUG System.out.println("\t- Head Type:\t\t\t\t" + head_type.getPredicate());
                // Not sure what the type is.


                // Not sure what the type is.
                if (attrs != null) {
                    if (attrs.size() > 0) {
                        _logger.fine("\t- Attribute Set:\t" + attrs);
                    }
                }

                addHead(head_type);
                addAttributes(attrs);
                addRelations(rels);


                // update anyMention_isFirstMention
                this.anyMention_isFirstMention = cf.getIsFirstMention();
            } //end if not a pronoun
        }
    }

    /**
     * Merges a given Discourse Entity with this one.
     * Adds the types, attributes, and relations
     * of the former to the latter,
     * and updates the Discourse Model by deleting the former and
     * updating the necessary data structures.
     * @param cfAnte The Cf proposed as an antecedent by the resolveAnaphor() method
     */
    //merge mention of the anaphora in DE of antecedent
    public void merge(Mention cfAnte) {
        if (corefChain.contains(cfAnte)) {
            System.out.println("MergeOut: Mention " + cfAnte.getHeadString() + " already included in entity " + this.getId());
        } else {
            CopyOfDiscourseEntityBackup deAnte = cfAnte.getDiscourseEntity();
            types.addAll(deAnte.getTypes());
            attributes.addAll(deAnte.getAttributes());
            relations.addAll(deAnte.getRelations());
            names = mergeNames(deAnte.getNames(), names);
//            names = mergeNames(names, deAnte.getNames());
            // CHECK USAGE 
            // AND DEFINITION           
            corefChain.add(cfAnte);
        }
    }

    /**
     * Adds a new (if new) head to this Discourse Entity.
     * @param head The head of a Cf
     */
    private void addHead(Property head_type) {
        // need to add check - only add if not already there.
        if (head_type != null) {
            types.add(head_type);
        }
    }

    /**
     * Adds (if new) attribute(s) to this Discourse Entity.
     * @param attrs The Set of attributes to be added
     */
    private void addAttributes(Set<Property> attrs) {
        if ((attrs != null) && (attrs.size() > 0)) {
            attributes.addAll(attrs);
        }
    }

    /**
     * Adds (if new) relation(s) to this Discourse Entity.
     * @param rels The Set of relations to be added
     */
    private void addRelations(Set<Property> rels) {
        if ((rels != null) && (rels.size() > 0)) {
            relations.addAll(rels);
        }
    }

    /**
     * Reads the necessary information from a given NP and creates a Property object for its head.
     * @param np The NP to be processed
     * @return Property An object holding the head of the NP
     */
    private Property computeHead(Mention np) {
        //Node headNode = np.getHeadNode();
        String headString = np.getHeadString();
        //System.out.println("head string " + headString );
        if (headString != null) {
            return (new Property(Property.TYPE, headString));
        } //end if head not null
        return null; //only if head is null
    }

        private Property computeHeadLemma(Mention np) {
        //Node headNode = np.getHeadNode();
        String headString = null;
        if (np.getNumber()) {
            headString = np.getHeadString();
        } else {
            headString = np.getHeadOrName();
        }
        //System.out.println("head string " + headString );
        if (headString != null) {
            return (new Property(Property.TYPE, headString));
        } //end if head not null
        return null; //only if head is null
    }

    private Property computeName(String n) {
        //Node headNode = np.getHeadNode();
        //String headString = np.getHeadString();
        //System.out.println("head string " + headString );
        return (new Property(Property.TYPE, n));
    }

    /**
     * Reads the premodifiers from the input Mention and creates Property objects as attributes for every premodifier
     * not part of an embedded NE. Embedded NEs are to be treated as relations to other Discourse Entities.
     * @param np The NP to be processed
     * @return Set A Set of Property Objects; one for every premodifier (attribute)
     */
    private Set<Property> computeAttributes(Mention np) {
        LanguagePlugin lang_plugin =
                ConfigProperties.getInstance().getLanguagePlugin();
        Set<Property> result = new LinkedHashSet<Property>();
        List<Tree> preModifiers = np.getPremodifiers(); // straight from Mention
        //DEBUG
        //System.out.println("Number of premodifiers of "+np.getMarkableString()+" :"+
        //        preModifiers.size());
        char pos = '\0';
        if ((preModifiers != null) && (preModifiers.size() > 0)) {
            for (int i = 0; i < preModifiers.size(); i++) {
                Tree mod = preModifiers.get(i); // Expected structure:
                // (NP (DT the) (JJ last) (NN supper))
                if (mod.isLeaf()) {
                    // this shouldn't happen'
                    System.out.println("WARNING: UNEXPECTED LEAF " + mod.nodeString());
                //result.add(new Property(Property.ATTRIBUTE, mod.nodeString()));
                //result.add(new Property(Property.ATTRIBUTE, getSense(mod.nodeString())));
                } else {
                    NodeCategory ncat = lang_plugin.labelCat(mod.nodeString());
                    if (mod.isPreTerminal()) {
                        if (ncat == NodeCategory.CN ||
                                ncat == NodeCategory.ADJ) {
                            if (ncat == NodeCategory.CN) {
                                pos = 'N';
                            }
                            if (ncat == NodeCategory.ADJ) {
                                pos = 'A';
                            }

                            //System.out.println("Pre terminal node "+ mod.nodeString());
                            Tree wordNode = mod.firstChild();
                            _logger.fine("Adding attribute " + wordNode.nodeString() + " to entity");
                            result.add(new Property(Property.ATTRIBUTE, wordNode.nodeString(), pos));
                        }
                    }
                }
            }
        }
        return result;
    }

    private Set<Property> computeInitialRelations(Mention np) {
        LanguagePlugin lang_plugin =
                ConfigProperties.getInstance().getLanguagePlugin();
        Set<Property> result = new LinkedHashSet<Property>();
        List<Tree> postModifiers = np.getPostmodifiers(); // straight from Mention

        char pos = '\0';
        //DEBUG
        //System.out.println("Number of postmodifiers of "+np.getMarkableString()+" :"+
        //        postModifiers.size());
        if ((postModifiers != null) && (postModifiers.size() > 0)) {
            for (int i = 0; i < postModifiers.size(); i++) {
                Tree mod = postModifiers.get(i); // Expected structure:
                // (NP  (NN software) (PP from (NP India))
                if (mod.isLeaf()) {
                    // this shouldn't happen'
                    System.out.println("WARNING: UNEXPECTED LEAF " + mod.nodeString());
                //result.add(new Property(Property.ATTRIBUTE, mod.nodeString()));
                //result.add(new Property(Property.ATTRIBUTE, getSense(mod.nodeString())));
                } else {
                    if (mod.isPreTerminal()) { // this shouldn't happen either,
                        // but we'll add it to the properties
                        NodeCategory ncat = lang_plugin.labelCat(mod.nodeString());
                        if (ncat == NodeCategory.CN ||
                                ncat == NodeCategory.ADJ) {
                            if (ncat == NodeCategory.CN) {
                                pos = 'N';
                            }
                            if (ncat == NodeCategory.ADJ) {
                                pos = 'A';
                            }
                        }
                    } else {
                        //System.out.println("Type of postmodifier: " + mod.nodeString());
                        NodeCategory ncat = lang_plugin.labelCat(mod.nodeString());
                        if (ncat == NodeCategory.PP) {
                            if (mod.numChildren() == 2) { // (PP (in from) (NP (nnp India)))
                                Tree prepNode = mod.getChild(0);
                                Tree npNode = mod.getChild(1);
                                Tree npHead = massimoHeadFindHack(npNode);
                                if (npHead != null && prepNode != null ) {

              //DEBUG
              //System.out.println("Adding relation "+
             //                  prepNode.firstChild().nodeString()+" "+
            //                  npHead.firstChild().nodeString() );

/* -- no clue what it means, just fixed so that it doesn't crash  (Olga) -- */
         if (prepNode.numChildren()>0) prepNode = prepNode.firstChild();
         result.add(new Property(prepNode.nodeString(),
                    npHead.firstChild().nodeString()));
                                }
                            }
                        } 
                    }
                }
            } //end outer loop
        } //end if premodified
        return result;
    }

    private Tree massimoHeadFindHack(Tree npNode) {
        LanguagePlugin lang_plugin = ConfigProperties.getInstance().getLanguagePlugin();
        /*
         * NOTE (yv):
         * We should really have a decent configurable head finder.
         * The "generic" head finder below probably works, but ...
         * this is ugly enough for English, but making it work for
         * English *and* Italian (and possibly other languages)
         * is only something for very enthusiastic people with
         * slight masochistic tendencies.
         */
        //CollinsHeadFinder hf = new CollinsHeadFinder();
        //ModCollinsHeadFinder hf = new ModCollinsHeadFinder();

/* -- trivial -- */
        if (npNode.numChildren() == 0 ) return npNode;
        if (npNode.numChildren() == 1 ) {
          if (npNode.firstChild().numChildren()==0) return npNode;            
          return massimoHeadFindHack(npNode.firstChild());
        }
/* -- coordination -- */
        if (npNode.numChildren() > 2) {
          for (Tree n: npNode.children()) {
           if (lang_plugin.labelCat(n.nodeString()) == NodeCategory.CC)
             return null;
          }
        }
 
/* -- last child is a noun (common/proper) --*/
/* NB: will it work for italian though? */

NodeCategory firstpos=lang_plugin.labelCat(npNode.firstChild().nodeString());
NodeCategory nextpos=lang_plugin.labelCat(npNode.getChild(1).nodeString());
NodeCategory lastpos=lang_plugin.labelCat(npNode.lastChild().nodeString());


       if (lastpos == NodeCategory.CN) return npNode.lastChild();
if (lastpos == NodeCategory.PN) return npNode.lastChild();


/* -- (NP (NP (DT the) (NN man)) (PP (in from) (NP (NNP UNCLE)))) -- */

if (firstpos == NodeCategory.NP && nextpos != NodeCategory.CN)
                    return massimoHeadFindHack(npNode.firstChild());

/* -- misc -- */

        Tree found_head = null;
        int state = 0;
        for (Tree n : npNode.children()) {
          NodeCategory ncat = lang_plugin.labelCat(n.nodeString());
          if (ncat == NodeCategory.CN ||
              ncat == NodeCategory.PN ||
              ncat == NodeCategory.PRO) {
                 state = 4;
                 found_head = n;
              } else if (ncat == NodeCategory.NP && state < 3) {
                    state = 3;
                    found_head = n;
                    } else if (ncat == NodeCategory.ADJ && state < 3) {
                         state = 2;
                         found_head = n;
                    }
              }
              if (found_head != null) {
                  if (state == 3) {
                    return massimoHeadFindHack(found_head);
                  }
                  return found_head;
              }

//    if (ConfigProperties.getInstance().getDbgPrint()) 
     System.err.println("Couldn't find a head for NP:" + npNode.pennString());
    return null;
   }
            
       
    

    /**
     * Adds (if new) name(s) to this Discourse Entity.
     * @param nms The Set of names to be
     *
     */
    private void addNames(HashMap<String, String> nms) {
        if (nms != null) {
            if (nms.get("Role") != null) {
                if (names.get("Role") != null) {
                    names.get("Role").add(computeName(nms.get("Role").toLowerCase()));
                    _logger.finer("ADDED Role " + nms.get("Role"));
                } else {
                    Set<Property> value = new HashSet<Property>();
                    names.put("Role", value);
                    names.get("Role").add(computeName(nms.get("Role").toLowerCase()));
                    _logger.finer("ADDED Role " + nms.get("Role"));
                }
            }
            if (nms.get("Role") == null && names.get("Role") == null) {
                Set<Property> value = new HashSet<Property>();
                names.put("Role", value);
            }
            if (nms.get("Forename") != null) {
                if (names.get("Forename") != null) {
                    names.get("Forename").add(computeName(nms.get("Forename").toLowerCase()));
                    _logger.finer("ADDED Forename " + nms.get("Forename"));
                } else {
                    Set<Property> value = new HashSet<Property>();
                    names.put("Forename", value);
                    names.get("Forename").add(computeName(nms.get("Forename").toLowerCase()));
                    _logger.finer("ADDED Forename " + nms.get("Forename"));
                }
            }
            if (nms.get("Forename") == null && names.get("Forename") == null) {
                Set<Property> value = new HashSet<Property>();
                names.put("Forename", value);
            }
            if (nms.get("Middle") != null) {
                if (names.get("Middle") != null) {
                    names.get("Middle").add(computeName(nms.get("Middle").toLowerCase()));
                    _logger.finer("ADDED Middle " + nms.get("Middle"));
                } else {
                    Set<Property> value = new HashSet<Property>();
                    names.put("Middle", value);
                    names.get("Middle").add(computeName(nms.get("Middle").toLowerCase()));
                    _logger.finer("ADDED Middle " + nms.get("Middle"));
                }
            }
            if (nms.get("Middle") == null && names.get("Middle") == null) {
                Set<Property> value = new HashSet<Property>();
                names.put("Middle", value);
            }
            if (nms.get("Link") != null) {
                if (names.get("Link") != null) {
                    names.get("Link").add(computeName(nms.get("Link").toLowerCase()));
                } else {
                    Set<Property> value = new HashSet<Property>();
                    names.put("Link", value);
                    names.get("Link").add(computeName(nms.get("Link").toLowerCase()));
                }
            }
            if (nms.get("Link") == null && names.get("Link") == null) {
                Set<Property> value = new HashSet<Property>();
                names.put("Link", value);
            }
            if (nms.get("Surname") != null) {
                if (names.get("Surname") != null) {
                    names.get("Surname").add(computeName(nms.get("Surname").toLowerCase()));
                    _logger.finer("ADDED Surname " + nms.get("Surname"));
                } else {
                    Set<Property> value = new HashSet<Property>();
                    names.put("Surname", value);
                    names.get("Surname").add(computeName(nms.get("Surname").toLowerCase()));
                    _logger.finer("ADDED Surname " + nms.get("Surname"));
                }
            }
            if (nms.get("Surname") == null && names.get("Surname") == null) {
                Set<Property> value = new HashSet<Property>();
                names.put("Surname", value);
            }
            if (nms.get("Suffix") != null) {
                if (names.get("Suffix") != null) {
                    names.get("Suffix").add(computeName(nms.get("Suffix").toLowerCase()));
                    _logger.finer("ADDED Suffix " + nms.get("Suffix"));
                } else {
                    Set<Property> value = new HashSet<Property>();
                    names.put("Suffix", value);
                    names.get("Suffix").add(computeName(nms.get("Suffix").toLowerCase()));
                    _logger.finer("ADDED Suffix " + nms.get("Suffix"));
                }
                if (nms.get("Suffix") == null && names.get("Suffix") == null) {
                    Set<Property> value = new HashSet<Property>();
                    names.put("Suffix", value);
                }
            }
        }
    }

    private void addNamesNoPerson(Mention cf) {
        if (cf != null) {
            Set<Property> value = new HashSet<Property>();
            names.put("AtomicName", value);
            names.get("AtomicName").add(new Property(Property.NAME, cf.toString().toLowerCase()));

        }
    }

    private HashMap<String, Set<Property>> mergeNames(HashMap<String, Set<Property>> nameStrAnte, HashMap<String, Set<Property>> nameStrAna) {
        if (nameStrAna.get("Role") != null) {
            if (nameStrAnte.get("Role") != null) {
                nameStrAna.get("Role").addAll(nameStrAnte.get("Role"));
            } else {
                Set<Property> value = new HashSet<Property>();
                nameStrAnte.put("Role", value);
                nameStrAna.get("Role").addAll(nameStrAnte.get("Role"));
                nameStrAna.get("Role");
            }
        } else {
            Set<Property> val = new HashSet<Property>();
            nameStrAna.put("Role", val);
            if (nameStrAnte.get("Role") != null) {
                nameStrAna.get("Role").addAll(nameStrAnte.get("Role"));
            } else {
                Set<Property> value = new HashSet<Property>();
                nameStrAnte.put("Role", value);
                nameStrAna.get("Role").addAll(nameStrAnte.get("Role"));
            }
        }

        if (nameStrAna.get("Forename") != null) {
            if (nameStrAnte.get("Forename") != null) {
                nameStrAna.get("Forename").addAll(nameStrAnte.get("Forename"));
            } else {
                Set<Property> value = new HashSet<Property>();
                nameStrAnte.put("Forename", value);
                nameStrAna.get("Forename").addAll(nameStrAnte.get("Forename"));
                nameStrAna.get("Forename");
            }
        }
        if (nameStrAna.get("Middle") != null) {
            if (nameStrAnte.get("Middle") != null) {
                nameStrAna.get("Middle").addAll(nameStrAnte.get("Middle"));
            } else {
                Set<Property> value = new HashSet<Property>();
                nameStrAnte.put("Middle", value);
                nameStrAna.get("Middle").addAll(nameStrAnte.get("Middle"));
            }
        }
        if (nameStrAna.get("Link") != null) {
            if (nameStrAnte.get("Link") != null) {
                nameStrAna.get("Link").addAll(nameStrAnte.get("Link"));
            } else {
                Set<Property> value = new HashSet<Property>();
                nameStrAnte.put("Link", value);
                nameStrAna.get("Link").addAll(nameStrAnte.get("Link"));
            }
        }
        if (nameStrAna.get("Surname") != null) {
            if (nameStrAnte.get("Surname") != null) {
                nameStrAna.get("Surname").addAll(nameStrAnte.get("Surname"));
            } else {
                Set<Property> value = new HashSet<Property>();
                nameStrAnte.put("Surname", value);
                nameStrAna.get("Surname").addAll(nameStrAnte.get("Surname"));
                nameStrAna.get("Surname");
            }
        }
        if (nameStrAna.get("Suffix") != null) {
            if (nameStrAnte.get("Suffix") != null) {
                nameStrAna.get("Suffix").addAll(nameStrAnte.get("Suffix"));
            } else {
                Set<Property> value = new HashSet<Property>();
                nameStrAnte.put("Suffix", value);
                nameStrAna.get("Suffix").addAll(nameStrAnte.get("Suffix"));
            }
        }
        if (nameStrAna.get("AtomicName") != null) {
            if (nameStrAnte.get("AtomicName") != null) {
                nameStrAna.get("AtomicName").addAll(nameStrAnte.get("AtomicName"));
            } else {
                Set<Property> value = new HashSet<Property>();
                nameStrAnte.put("AtomicName", value);
                nameStrAna.get("AtomicName").addAll(nameStrAnte.get("AtomicName"));
            }
        }
        return nameStrAna;
    }

    public void printPropertySet(Set<Property> setPro) {
        if (setPro != null) {
            for (Property pro : setPro) {
                System.out.print(pro.toString() + " ");
            }
        } else {

            System.out.print("-");
        }
    }

    public void printRelationsSet(Set<Property> setRels) {
        if (setRels != null) {
            for (Property rels : setRels) {
                System.out.print(rels.getPredicate().toString() + "(" +
                        rels.getArgument().toString() + ")  ");
            }
        } else {
            System.out.print("-");
        }
    }

    public void printDiscourseEntity() {
        //       System.out.println("##");
        System.out.println("   ID: " + id.toString());
        System.out.print("   Types: ");
        printPropertySet(types);
        System.out.println();
        System.out.print("   Attrs: ");
        printPropertySet(attributes);
        System.out.println();
        System.out.print("   Relations: ");
        printRelationsSet(relations);
        System.out.println();
        System.out.println("   Names: ");
        printNames(names);
        System.out.println("");
    }

    public void printNames(HashMap<String, Set<Property>> name) {
        if (name.get("Role") != null) {
            System.out.print("       Roles: ");
            for (Property role : name.get("Role")) {
                System.out.print(role.toString() + " ");
                System.out.println();
            }
        }
        if (name.get("Forename") != null) {
            System.out.print("       Forename: ");
            for (Property forename : name.get("Forename")) {
                System.out.print(forename.toString() + " ");
                System.out.println();
            }
        }
        if (name.get("Surname") != null) {
            System.out.print("       Lastname: ");
            for (Property lastname : name.get("Surname")) {
                System.out.print(lastname.toString() + " ");
                System.out.println();
            }
        }
        if (name.get("AtomicName") != null) {
            System.out.print("       AtomicName: ");
            for (Property atomic : name.get("AtomicName")) {
                System.out.print(atomic.toString() + " ");
                System.out.println();
            }
        }
    }

    public void printAttributes() {
        if (types != null && attributes != null) {
            for (Property type : types) {
                for (Property attribute : attributes) {
                    System.out.println("ATTR " + type.toString() +
                            " " + attribute.toString());
                }
            }
        }
    }

    public void printRelations() {
        if (types != null && relations != null) {
            for (Property type : types) {
                for (Property relation : relations) {

                    System.out.println("REL " + type.toString() +
                            " " + relation.getPredicate().toString() +
                            " " + relation.getArgument().toString());

                }
            }
        }
    }

    public void printHeads() {
        if (types != null) {
            System.out.print("HEADS: ");
            for (Property type : types) {
                System.out.print(" " + type.giveString());
//                                System.out.print(type.giveString());
            }
        }
        System.out.println();
    }

    public void set_firstMention_isFirstMention(boolean isFirstMention) {
        firstMention_isFirstMention = isFirstMention;
    }

    public void set_firstMention_isProperName(boolean isProperName) {
        firstMention_isProperName = isProperName;
    }
}