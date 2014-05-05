/*
 * Copyright 2007 Project ELERFED
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

import static elkfed.util.Strings.*;
import static elkfed.coref.eval.PRScore.round;

/** A list of p/r/f1 scores
 *
 * @author ponzo
 */
public class PRScores extends ArrayList<PRScore>
{ 
    private static final String SEPARATOR = getNTimes('-',72) + "\n";
    
    private final StringBuffer buffer;
    
    public PRScores()
    {
        super();
        this.buffer = new StringBuffer();
    }
    
    @Override
    public String toString()
    {
        buffer.setLength(0);
        buffer.
                append(toPaddedString("ID",22)).append(getNTimes(' ', 2)).
                append(toPaddedString("RECALL",8)).
                append(toPaddedString("PRECIS",8)).
                append(toPaddedString("F_1",8)).append("\n").append(SEPARATOR);
        for (PRScore score : this)
        {
            buffer.
                append(toPaddedString(score.getId(),22)).append(getNTimes(' ', 2)).
                append(toPaddedString(round(score.getRecall()),8)).
                append(toPaddedString(round(score.getPrecision()),8)).
                append(toPaddedString(round(score.getScore()),8)).
                append("\n");
        }
        return buffer.append(SEPARATOR).toString();
    }
    
    public static void main(String[] args)
    {
        PRScores scores = new PRScores();
        scores.add(new PRScore(0.6, 0.3, "A"));
        scores.add(new PRScore(0.1, 0.8, "B"));
        scores.add(new PRScore(0.8, 0.4, "C"));
        
        System.out.println(scores);
    }
}
