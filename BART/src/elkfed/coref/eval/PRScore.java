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

import java.math.BigDecimal;

/** A recall/precision/f score
 *
 * @author ponzo
 */
public class PRScore implements Score 
{
    public static final int SCALE = 3;
    
    private final double recall;

    private final double precision;
    
    private final double f1;
    
    private final String id;
    
    /** Creates a new instance of PRScore */
    public PRScore(final double recall, final double precision, final String id)
    {
        this.recall = recall;
        this.precision = precision;
        this.id = id;
        
        this.f1 = fMeasure(recall, precision);
    }
    public PRScore(final double num, final double r_denum, final double p_denum, final String id)
    {
        if (r_denum==0 || p_denum==0) {this.recall=0; this.precision=0;}
        else {this.recall=num/r_denum; this.precision=num/p_denum;}
        this.id = id;
        
        this.f1 = fMeasure(recall, precision);
    }
    
    public double getRecall()
    { return recall; }
    
    public double getPrecision()
    { return precision; }
    
    public double getScore()
    { return f1; }

    public String getId()
    { return id; }
    
    /** Computes the F-measure for a given precision and recall */
    private double fMeasure(final double recall, final double precision)
    {
        if (precision == 0.0 && recall == 0.0)
        { return 0.0; }
        else
        {
            return      (2.0*precision*recall)
                    /
                        (precision + recall);
        }
    }

    public String toString()
    {
        return new StringBuffer().
                append("ID ").append(getId()).
                append(" -- RECALL ").append(new BigDecimal(getRecall()).setScale(SCALE, BigDecimal.ROUND_UP)).
                append(" -- PRECISION ").append(new BigDecimal(getPrecision()).setScale(SCALE, BigDecimal.ROUND_UP)).
                append(" -- F_1 ").append(new BigDecimal(getScore()).setScale(SCALE, BigDecimal.ROUND_UP)).toString();
    }
    
    public static BigDecimal round(double d)
    { return new BigDecimal(d).setScale(SCALE, BigDecimal.ROUND_UP); }
}
