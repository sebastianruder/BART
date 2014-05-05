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

package elkfed.coref.eval;

import java.util.ArrayList;
import java.util.List;

/** Implements the scorer for coreference described in
 *  Vilain, Marc, et al. A Model-Theoretic Coreference Scoring Scheme,
 *  Proceedings of the Message Understanding Conference (MUC-6),
 *  pp. 45-52, November 1995.
 *
 * @author ponzetsp
 */
public class MUCScorer extends Scorer
{
    /** IMPLEMENTATION DETAIL: the singleton instance */
    private static MUCScorer singleton;
    
    /** Getter for instance */
    public static synchronized MUCScorer getInstance()
    {
        if (singleton == null)
        { singleton = new MUCScorer(); }
        return singleton;
    }
    
    /** Creates a new instance of MUCScorer */
    private MUCScorer() {}

    public List<Score> computeScores() 
    {
        PRScores scores = new PRScores();
        scores.addAll(computePerDocumentScore());
        scores.add(computeTotalScore());
        
        System.out.println("\nResults of MUC evaluation\n");
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
        return computeScore(keyPartition, responsePartition, "SCORER MUC-TOTAL");
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
    
    /** Computes the MUC scores */
    private PRScore computeScore(EvaluationMarkableSet[] keyPartition,
                               EvaluationMarkableSet[] responsePartition,
                               String id)
    {
        final double recall = recall(keyPartition, responsePartition);
        final double precision = precision(keyPartition, responsePartition);
        return new PRScore(recall, precision, id);
    }
    
    /**
     * Returns the MUC recall score for the specified key and response
     * partitions.
     *
     * @param keyPartition Partition provided by the key.
     * @param responsePartition Partition provided by the response.
     * @return Recall value for the specified key and response partitions.
     */
    private double recall(
            final EvaluationMarkableSet[] keyPartition,
            final EvaluationMarkableSet[] responsePartition)
    {
        // the overall numerator and denominator
        int numerator = 0;
        int denominator = 0;
        // for each key equivalence class
        for (EvaluationMarkableSet keyEqClass : keyPartition)
        {
            // TO BE COMPUTED: the number of OPT coreference equivalence
            // class members for which ***no response is provided***
            int optElements = EvaluationHelper.getOPTSize(keyEqClass);
            // TO BE COMPUTED: the number of elements in the key
            // equivalence class which are in the response equivalence
            // classes
            int keyElementsInResponse = 0;
            // TO BE COMPUTED: the number of partitions (|p(S)|)
            int numPartitions = 0;
            for (EvaluationMarkableSet responseEqClass : responsePartition)
            {
                // intersectionSize[0]: how many elements in the key are
                //                      included the response class
                // intersectionSize[1]: how many OPT keys have a response
                final int[] intersectionSize =
                    EvaluationHelper.getIntersectionSize(keyEqClass, responseEqClass);
                
                if (intersectionSize[0] != 0)
                {
                    keyElementsInResponse += intersectionSize[0];
                    optElements -= intersectionSize[1];
                    numPartitions++;
                }
            }
            /*  the elements in the key missing from any partition are
            	assumed to be singleton within the partition, EXCEPT WHEN
            	THEY ARE OPTIONAL AND NO RESPONSE HAS BEEN PROVIDED!

            	The number of singletons is given therefore by the size of the
            	key set MINUS the number of elements in any response set
            	MINUS the optional elements with no response
             */
            final int singletons =
                keyEqClass.getMarkableSet().size() - keyElementsInResponse - optElements;
            numPartitions += singletons;
            final int sizeEqClass = keyEqClass.getMarkableSet().size() - optElements;
            
            numerator += (sizeEqClass - numPartitions);
            denominator += (sizeEqClass - 1);
        }
        if (denominator == 0)
        { return 1.0; }
        return (double) numerator / (double) denominator;
    }
    
    /**
     * Precision score for the response partition produced by the
     * clusterer against the key partiaion.  This class is implemented
     * here as the dual of recall, with the roles reversed for the
     * key and response.
     *
     * @param keyPartition Partion of the domain by the key.
     * @param responsePartition Partition of the domain by the response.
     * @return Precision score, in the range <code>[0.0,1.0]</code>.
     */
    private double precision(
        final EvaluationMarkableSet[] keyPartition,
        final EvaluationMarkableSet[] responsePartition)
    { 
        // the overall numerator and denominator
        int numerator = 0;
        int denominator = 0;
        // for each response equivalence class
        for (EvaluationMarkableSet responseEqClass : responsePartition)
        {
            // TO BE COMPUTED: the number of elements in the key
            // equivalence class which are in the response equivalence
            // classes
            int responseElementsInKey = 0;
            // TO BE COMPUTED: the number of partitions (|p(S|)
            int numPartitions = 0;
            for (EvaluationMarkableSet keyEqClass : keyPartition)
            {
                // how many elements in the response are included a key class
                final int intersectionSize =
                    EvaluationHelper.getIntersectionSize(keyEqClass, responseEqClass)[0];
                if (intersectionSize != 0)
                { 
                    responseElementsInKey += intersectionSize;
                    numPartitions++;
                }
            }
            /*  the elements in the response missing from any partition are
                assumed to be singleton within the partition.
                
                The number of singletons is given therefore by the size of the
                response set MINUS the number of elements in any key set
             */
            final int singletons =
                responseEqClass.getMarkableSet().size() - responseElementsInKey;
            numPartitions += singletons;

            numerator += (responseEqClass.getMarkableSet().size() - numPartitions);
            denominator += (responseEqClass.getMarkableSet().size() - 1);
        }
        if (denominator == 0)
        { return 1.0; }
        return (double) numerator / (double) denominator;
    }
}
