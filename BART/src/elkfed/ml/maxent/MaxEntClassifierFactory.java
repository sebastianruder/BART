/*
 * Copyright 2008 Yannick Versley / Univ. Tuebingen
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
package elkfed.ml.maxent;

import elkfed.config.ConfigProperties;
import elkfed.ml.ClassifierFactory;
import elkfed.ml.InstanceWriter;
import elkfed.ml.OfflineClassifier;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author versley
 */
public class MaxEntClassifierFactory implements ClassifierFactory {

    private static MaxEntClassifierFactory _factory;

    public static MaxEntClassifierFactory getInstance() {
        if (_factory == null) {
            _factory = new MaxEntClassifierFactory();
        }
        return _factory;
    }

    public InstanceWriter getSink(String model_name, String options, String learner) throws IOException {
        return new ClassifierSinkBinary(
                new File(ConfigProperties.getInstance().getModelDir(),
                model_name).getAbsolutePath(),
                ClassifierSinkBinary.comboFromString(options));
    }

    public void do_learning(String model_name, String options, String learner) throws IOException {
        try {
            ParameterEstimatorBinary.do_estimation(
                    new File(ConfigProperties.getInstance().getModelDir(),
                    model_name).getAbsolutePath(),learner);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot do parameter estimation", ex);
        }
    }

    public OfflineClassifier getClassifier(String model_name, String options, String learner) throws IOException {
        try {
            return new ClassifierBinary(new File(ConfigProperties.getInstance().getModelDir(),
                            model_name).getAbsolutePath(),
                            ClassifierSinkBinary.comboFromString(options));
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Cannot load classifier",ex);
        }
    }
}
