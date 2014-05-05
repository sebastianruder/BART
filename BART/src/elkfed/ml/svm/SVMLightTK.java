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

package elkfed.ml.svm;

import elkfed.config.ConfigProperties;

/** native interface to SVMLight-TK
 */
class SVMLightTK
{
    static {
	System.load(ConfigProperties.getInstance().getSvmLibrary());
    }
    final private int modelHandle;
    final private double modelThreshold;
    
    /** loads a model from a file */
    public SVMLightTK(String modelFile)
    {
	modelHandle=load_model(modelFile);
	modelThreshold=get_threshold();
    }
    
    /** given an instance in SVMLight-TK format,
     *  returns the classification
     */
    public double classify(String instance)
    {
	return classify_instance(modelHandle, instance);
    }

    public double getThreshold()
    {
	return modelThreshold;
    }
    private static native int load_model(String modelFile);
    private static native double get_threshold();
    private static native double classify_instance(int modelNumber,
						   String instance);

    public static void main(String[] args)
    {
	String test_input="-1 	|BT| (S (NP (NP (NP (DT the)(NN role))(PP (IN of)(NP (NNP Celimene))))(, ,)(VP (VBN played)(NP (-NONE- *))(PP (IN by)(NP (NNP Kim)(NNP Cattrall))))(, ,))(VP (VP (VBN attributed)))) |ET| 4:1 10622:1 12486:1 12494:1 14658:1 15316:1 17357:1 21478:1  |EV|";
	SVMLightTK[] models=new SVMLightTK[3];
	models[0]=new SVMLightTK("model1");
	System.out.println("Threshold: "+models[0].getThreshold());
	models[1]=new SVMLightTK("model2");
	System.out.println("Threshold: "+models[1].getThreshold());
	models[2]=new SVMLightTK("model3");
	System.out.println("Threshold: "+models[2].getThreshold());

	for(int i=0;i<3;i++){
	    System.out.println("SCORE model "+i+": "+
			      models[i].classify(test_input));
	}
    }
}
