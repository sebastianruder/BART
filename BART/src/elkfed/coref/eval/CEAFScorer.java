/*
  Implements CEAF-scorer (Luo 2007?) via lp_solve
  by Olga Uryupina (modified MUCScorer.java)
 */

package elkfed.coref.eval;

import java.util.ArrayList;
import java.util.List;

import elkfed.config.ConfigProperties;


public class CEAFScorer extends Scorer
{
    /** IMPLEMENTATION DETAIL: the singleton instance */
    private static CEAFScorer singleton;
    
    /** Getter for instance */
    public static synchronized CEAFScorer getInstance()
    {
        if (singleton == null)
        { singleton = new CEAFScorer(); }
        return singleton;
    }
    
    /** Creates a new instance of CEAFScorer */
    private CEAFScorer() {}


    /** debug printing **/
    private int PrintPartitions(EvaluationMarkableSet[] keyPartition,
                               EvaluationMarkableSet[] responsePartition) {
      int i;

      System.err.println("----------- Key partition ------\n");

      for (EvaluationMarkableSet keyEqClass : keyPartition)
        System.err.println(keyEqClass.getMarkableSet().size());

      System.err.println("----------- Response partition ------\n");

      for (EvaluationMarkableSet responseEqClass : responsePartition)
        System.err.println(responseEqClass.getMarkableSet().size());

      return 0;
    }
    private double phi3(EvaluationMarkableSet keyEqClass, EvaluationMarkableSet responseEqClass) {

             final int[] intersectionSize =
             EvaluationHelper.getIntersectionSize(keyEqClass, responseEqClass);
             return intersectionSize[0]+intersectionSize[1];

    }
    private double phi4(EvaluationMarkableSet keyEqClass, EvaluationMarkableSet responseEqClass) {

             final int[] intersectionSize =
             EvaluationHelper.getIntersectionSize(keyEqClass, responseEqClass);
             double overlap=intersectionSize[0]+intersectionSize[1];
             return 2*overlap/(keyEqClass.getMarkableSet().size() +responseEqClass.getMarkableSet().size() );


    }


    private double ceaf_local_score(EvaluationMarkableSet keyEqClass, EvaluationMarkableSet responseEqClass) {

/* overlap measure for two entities -- chose between phi3 and phi4 here */ 
      return phi3(keyEqClass, responseEqClass);
    }

    private double ceaf_cumulative_local_score(EvaluationMarkableSet[] keyPartition, EvaluationMarkableSet[] responsePartition) {

/* 
* assumes two partitions are ordered, 
* outputs the cumulative local score (for the denominator of CEAF scoring)
*/

     int keySize=keyPartition.length;
     int responseSize=responsePartition.length;
     if (keySize != responseSize) return -1;
     double score=0;


     for (int i=0;i < keySize; i++) {
       score+=ceaf_local_score(keyPartition[i], responsePartition[i]);
     }
     return score;
  }



    public List<Score> computeScores() 
    {
        PRScores scores = new PRScores();
        scores.addAll(computePerDocumentScore());
        scores.add(computeTotalScore());
        
        System.out.println("\nResults of CEAF evaluation\n");
        System.out.println(scores);
        
        List<Score> toreturn = new ArrayList<Score>();
        for (PRScore score : scores)
        { toreturn.add(score); }
        
        return toreturn;
    }
    
    private PRScore computeTotalScore()
    {        
        final EvaluationMarkableSet[] keyPartition = 
                EvaluationHelper.getKeyPartition(corpus);
        final EvaluationMarkableSet[] responsePartition = 
                EvaluationHelper.getResponsePartition(corpus);
        if (ConfigProperties.getInstance().getDbgPrint()) 
           PrintPartitions(keyPartition,responsePartition);
        return computeScore(keyPartition, responsePartition, "TOTAL");
    }
    
    /** Computes the score per-document, evaluate significance using chi-square
     *  and prints score evaluation to STDOUT
     */
    private PRScores computePerDocumentScore()
    {
        final EvaluationMarkableSet[][] keyPartition =
                EvaluationHelper.getDocumentClusteredKeyPartition(corpus);
        final EvaluationMarkableSet[][] responsePartition =
                EvaluationHelper.getDocumentClusteredResponsePartition(corpus);


        if (keyPartition.length != responsePartition.length)
        { throw new RuntimeException("Number of documents differ, aborting ..."); }
        else
        {
            PRScores scores = new PRScores();
            for (int doc = 0; doc < keyPartition.length; doc++)
            { 
                scores.add(computeScore(
                      keyPartition[doc], 
                      responsePartition[doc],
                      corpus.get(doc).getNameSpace())
                );
            }
            return scores;
        }
    }
    
    /** Computes the CEAF scores */
    private PRScore computeScore(EvaluationMarkableSet[] keyPartition,
                               EvaluationMarkableSet[] responsePartition,
                               String id)
    {

       double recall=0;
       double precision=0;

       int i=0;
       int j=0;

       int keySize=keyPartition.length;
       int responseSize=responsePartition.length;


        //try {

/* ------------------------ init LPSolve problem ------------------------  */

//         LpSolve ceafilp=LpSolve.makeLp(0,keySize*responseSize);
//         ceafilp.setMaxim(); //defaut is to minimize the objective
//         ceafilp.setDebug(false); // no debug printing
//         ceafilp.setVerbose(0); //no whatever printing
///* ------------------------- helpers  ------------------------------- */
//
//         StringBuffer zero2=new StringBuffer(); 
//         StringBuffer one2=new StringBuffer(); 
//
//         for(i=0;i<responseSize;i++) {
//           if (i>0) zero2.append(" ");
//           if (i>0) one2.append(" ");
//           zero2.append(0);
//           one2.append(1);
//         }
//
///* ----------------------- Set objective function ------------------------ */
//
//         StringBuffer objective=new StringBuffer();
//         i=0;
//         for (EvaluationMarkableSet keyEqClass : keyPartition)
//           {
//             j=0;
//             for (EvaluationMarkableSet responseEqClass : responsePartition)
//               {
//                  double cost=ceaf_local_score(keyEqClass, responseEqClass);
//
//                  if (i+j >0 ) {objective.append(" ");}
//                  objective.append(cost);
//
//                  j++;
//               }
//             i++;
//           }
//
//         ceafilp.strSetObjFn(objective.toString());
//
///* 
//* set constraints to enforce 1-to-1 mapping between key and response entities 
//*/
//
//
///* ----------------------- constraints ----------------------------*/    
//
//         for (i=0; i<keySize;i++) {
//            StringBuffer constraint=new StringBuffer();
//            for (j=0; j<i; j++) {
//              if (j>0) constraint.append(" ");
//              constraint.append(zero2);
//            }
//            if (j>0) constraint.append(" ");
//            constraint.append(one2);
//            for (j=i+1; j<keySize; j++) {
//              if (j>0) constraint.append(" ");
//              constraint.append(zero2);
//            }
//
//            ceafilp.strAddConstraint(constraint.toString(),LpSolve.LE,1);
//         }
//
//        for (i=0; i<responseSize;i++) {
//          StringBuffer constraint=new StringBuffer();
//          StringBuffer row=new StringBuffer(); 
//          for(j=0; j<i; j++) {
//            if (j>0) row.append(" ");
//            row.append(0);
//          }
//          if (j>0) row.append(" ");
//          row.append(1);
//          for(j=i+1; j<responseSize; j++) {
//            if (j>0) row.append(" ");
//            row.append(0);
//          }      
//        
//
//          for (j=0; j<keySize; j++) {
//              if (j>0) constraint.append(" ");
//              constraint.append(row);
//          }
//
//          ceafilp.strAddConstraint(constraint.toString(),LpSolve.LE,1);
//        }
//
///* ------------------  Set variables to be binary ---------------------- */
//
//        int varid=1;
//        for (i=0; i<keySize;i++) {
//          for (j=0; j<responseSize;j++) {
//            ceafilp.setBinary(varid,true);
//            varid++;
//          }
//        }
//
///* ----------------------- launch ILP solver ----------------------------*/
//        ceafilp.solve();
//
//
//
//
///* now numerator is the value of (maximized) objective function */
//
//     double numerator=ceafilp.getObjective();
//
//
//
///*  Compute R and P */
//
//      double down_r=ceaf_cumulative_local_score(keyPartition,keyPartition);
//      double down_p=ceaf_cumulative_local_score(responsePartition,responsePartition);
//
//      System.err.println("CEAF stats: num= " + numerator + " r_denum= " + down_r + " p_denum = " + down_p);
// 
//      if (down_r==0 || down_p==0) {
//        recall=0;
//        precision=0;
//      }else{
//        recall=numerator/down_r;
//        precision=numerator/down_p;
//      }
//      // delete the problem and free memory
//      ceafilp.deleteLp();
//   }     
//
//   catch (LpSolveException e) {
//       e.printStackTrace();
//   }


   return new PRScore(recall, precision, "CEAF-"+id);
 }
    
}