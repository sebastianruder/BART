/*
 * SoonDecoder.java
 *
 * Created on July 12, 2007, 5:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
* Uses a single binary feature (to be specified within this file) + 
* the soon-style decoder to build chains 
* (needed only for evaluating various features, baselines, etc)
*
* @author olga
*/


package elkfed.coref.algorithms.dummy;

import elkfed.coref.features.pairs.*;
import elkfed.coref.algorithms.soon.*;
import elkfed.coref.mentions.Mention;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.OfflineClassifier;
import elkfed.ml.TriValued;
import java.util.ArrayList;
import java.util.List;
import static elkfed.lang.EnglishLinguisticConstants.*;


public class SoonDummyDecoder extends LocalDecoder {

    List<PairFeatureExtractor> _fes;
    OfflineClassifier _model;

    public SoonDummyDecoder(List<PairFeatureExtractor> fes,
            OfflineClassifier model) {
        _fes=fes;
        _model=model;
        ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes) {
            fe.describeFeatures(fds);
        }
        fds.add(PairInstance.FD_POSITIVE);
        //_model.setHeader(fds);
    }


    public int resolveSingle(List<Mention> mentions, int ana) {
    	// ana: current index
        Mention m_i=mentions.get(ana);
        // m_i: current mention

        for (int j=ana-1; j>=0; j--) {
           Mention m_j=mentions.get(j);


           PairInstance inst=new PairInstance(m_i, m_j);
           for (PairFeatureExtractor fe: _fes) {
              fe.extractFeatures(inst);
           }
           // checks if anaphor and antecedent are coreferent
           // uses setID --> unclear
           inst.setFeature(PairInstance.FD_POSITIVE,
               inst.getAnaphor().isCoreferent(inst.getAntecedent()));
//           if (inst.getFeature(FE_Yago.FD_YAGO_TYPEOF) || inst.getFeature(FE_Yago.FD_YAGO_MEANS)) {
//          if (inst.getFeature(FE_Yago.FD_YAGO_TYPEOF) || inst.getFeature(FE_Yago.FD_YAGO_MEANS) || inst.getFeature(FE_Wiki.FD_WIKI1_MATCH)) {

//           if (inst.getFeature(FE_Appositive.FD_IS_APPOSITIVE)) {
//           if (inst.getFeature(FE_AppositiveParse.FD_IS_APPOSITIVE_PRS)) {
//           if (inst.getFeature(FE_Appositive_iCab.FD_IS_APPOSITIVE_ICAB)) {
//           if (inst.getFeature(FE_Compatible.FD_IS_COMPATIBLE_ANAPRO)) {
           //if (inst.getFeature(FE_CoRef.FD_IS_COREF)) {
           if (inst.getFeature(FE_StringMatch.FD_IS_STRINGMATCH)) {
//           if (inst.getFeature(FE_Span.FD_IS_SPAN)) {


/*
           if (inst.getFeature(FE_CoRef.FD_IS_COREF) && 
  (inst.getAntecedent().getMarkableString().toLowerCase().matches(NONREF_NP) ||
   inst.getAnaphor().getMarkableString().toLowerCase().matches(NONREF_NP))) 
{
*/

//           if (inst.getFeature(FE_SpeakerAlias.FD_SPALIAS_PRONE)==TriValued.TRUE) { 

//           if (inst.getFeature(FE_Yago.FD_YAGO_TYPEOF)) {
//           if (inst.getFeature(FE_Wiki.FD_WIKI1_MATCH)) {
/*

String fsval="[";
if (inst.getFeature(FE_Wiki.FD_WIKI1_MATCH)) {
fsval+= " +wikimatch<" + inst.getAnaphor().getMarkable().getAttributeValue("wiki1") +">";
}

if (inst.getFeature(FE_Yago.FD_YAGO_MEANS)) fsval+= " +yagomeans";
if (inst.getFeature(FE_Yago.FD_YAGO_TYPEOF)) fsval+= " +yagotype";
fsval+="]";


                    if (inst.getFeature(PairInstance.FD_POSITIVE)==true) {
                        System.out.println("True positive " +
inst.getAnaphor().getMarkable().getID() + " and " +
inst.getAntecedent().getMarkable().getID() + " (" +
inst.getAnaphor().getMarkableString() + "), (" +
inst.getAntecedent().getMarkableString() + ")" 
);
                    }else{
                        System.out.println("False positive " +
inst.getAnaphor().getMarkable().getID() + " and " +
inst.getAntecedent().getMarkable().getID() + " (" +
inst.getAnaphor().getMarkableString() + "), (" +
inst.getAntecedent().getMarkableString() + ")" +fsval 
);
                    }
 */

                    if (inst.getFeature(PairInstance.FD_POSITIVE)==true) {
                        System.out.println("True positive " +
//inst.getAnaphor().getMarkable().getID() + " at " +
//inst.getAnaphor().getMarkable().getLeftmostDiscoursePosition() + "," +
//inst.getAnaphor().getMarkable().getRightmostDiscoursePosition() + " and " +
//inst.getAntecedent().getMarkable().getID() + " at " +
//inst.getAntecedent().getMarkable().getLeftmostDiscoursePosition() + "," +
//inst.getAntecedent().getMarkable().getRightmostDiscoursePosition() + " and " +
 " (" +
inst.getAnaphor().getMarkableString() + "), (" +
inst.getAntecedent().getMarkableString() + ")" 
);
                    }else{
                        System.out.println("False positive " +
//inst.getAnaphor().getMarkable().getID() + " and " +
//inst.getAntecedent().getMarkable().getID() + " (" +
inst.getAnaphor().getMarkableString() + "), (" +
inst.getAntecedent().getMarkableString() + ")"  
);
                    }

/*
                    if (inst.getFeature(PairInstance.FD_POSITIVE)==false) {
                        System.out.println("False positive (" +
inst.getAnaphor().getMarkableString() + "), (" +
inst.getAntecedent().getMarkableString() + ")"  
);
}
*/
             return j;
           }
        }
        return -1;
    }
}
