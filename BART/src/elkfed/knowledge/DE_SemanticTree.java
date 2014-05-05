/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elkfed.knowledge;

/**
 *
 * @author kepa
 */
import java.util.ArrayList;
import java.util.Set;

import edu.berkeley.nlp.syntax.Tree;
import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.discourse_entities.Property;
import elkfed.coref.mentions.Mention;

public class DE_SemanticTree {

    private Tree nodeDE;
    private Tree nodeID;
    private Tree nodeTYPES;
    private Tree nodeATTRS;
    private Tree nodeRELS;

    public Tree GetDE_SemanticTree(Mention m) {

        nodeDE = new Tree<String>("DE", new ArrayList<Tree<String>>());
        nodeID = new Tree<String>("ID", new ArrayList<Tree<String>>());
        nodeTYPES = new Tree<String>("TYPES", new ArrayList<Tree<String>>());
        nodeATTRS = new Tree<String>("ATTRIBUTES", new ArrayList<Tree<String>>());
        nodeRELS = new Tree<String>("RELATIONS", new ArrayList<Tree<String>>());

        DiscourseEntity de = m.getDiscourseEntity();
        Integer id = de.getId();
        Set<Property> de_types = de.getTypes();
        Set<Property> de_attrs = de.getAttributes();
        Set<Property> de_rels = de.getRelations();

        nodeDE.getChildren().add(nodeID);
        nodeID.getChildren().add(new Tree<String>(id.toString()));
        nodeDE.getChildren().add(nodeTYPES);

        if (de_types.isEmpty() && de_attrs.isEmpty() && de_rels.isEmpty()) {
            // DE of a pronoun
            Tree nodeTYPE = new Tree<String>("TYPE", new ArrayList<Tree<String>>());
            nodeTYPES.getChildren().add(nodeTYPE);
            nodeTYPE.getChildren().add(new Tree<String>("ANYTHING"));
        } else {
            for (Property de_type : de_types) {
                Tree nodeTYPE = new Tree<String>("TYPE", new ArrayList<Tree<String>>());
                nodeTYPES.getChildren().add(nodeTYPE);
                nodeTYPE.getChildren().add(new Tree<String>(de_type.toString()));
            }
        }

        if (de_attrs.size() > 0) {
            nodeDE.getChildren().add(nodeATTRS);
            for (Property de_attr : de_attrs) {
                Tree nodeATTR = new Tree<String>("ATTR", new ArrayList<Tree<String>>());
                nodeATTRS.getChildren().add(nodeATTR);
                nodeATTR.getChildren().add(new Tree<String>(de_attr.toString()));
            }
        }

        if (de_rels.size() > 0) {
            nodeDE.getChildren().add(nodeRELS);
            for (Property de_rel : de_rels) {
                Tree nodeREL = new Tree<String>("REL", new ArrayList<Tree<String>>());
                Tree nodePRED = new Tree<String>("PRED", new ArrayList<Tree<String>>());
                Tree nodeARG = new Tree<String>("ARG", new ArrayList<Tree<String>>());
                nodeRELS.getChildren().add(nodeREL);
                nodeREL.getChildren().add(nodePRED);
                nodePRED.getChildren().add(new Tree<String>(de_rel.getPredicate().toString()));
                nodeREL.getChildren().add(nodeARG);
                nodeARG.getChildren().add(new Tree<String>(de_rel.getArgument().toString()));
            }
        }
        return nodeDE;
    }

    private Tree getTypesTree() {
        return nodeTYPES;
    }

    private Tree getAttributeTree() {
        return nodeATTRS;
    }

    private Tree getRelationsTree() {
        return nodeRELS;
    }

    //sub-tress as string for feature extractor 
    //format for tree-kernels?
    private String getTypesTreeString() {
        return nodeTYPES.toString();
    }

    private String getAttributesTreeString() {
        return nodeATTRS.toString();
    }

    private String getRelationsTreeString() {
        return nodeRELS.toString();
    }
}

