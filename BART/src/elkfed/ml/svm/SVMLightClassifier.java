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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import elkfed.config.ConfigProperties;
import elkfed.ml.FeatureDescription;
import elkfed.ml.Instance;
import elkfed.ml.OfflineClassifier;
import elkfed.util.ShellCommand;

/**
 *
 * @author yannick
 */
public class SVMLightClassifier implements OfflineClassifier {
    protected File _modelFile;
    protected File _dictFile;
    protected SVMLightTK _native;
    double _bias;
    protected List<FeatureDescription> _fds;
    private static Logger _logger=Logger.getAnonymousLogger();
    
    /** Creates a new instance of SVMLightClassifier */
    public SVMLightClassifier(File modelFile, File dictFile)
    throws FileNotFoundException {
        _modelFile=modelFile;
        _dictFile=dictFile;
        _bias=getBias(modelFile);
        try {
            _native=new SVMLightTK(modelFile.getAbsolutePath());
        }
        catch (UnsatisfiedLinkError e)
        {
            System.err.println("Not using native interface: "+e.getMessage());
        }
        catch (NoClassDefFoundError e)
        { /* IGNORE - Java does this if we can't load native IF
                      and as a result we cannot load the class */ }
    }
    
    protected double getBias(File modelFile)
    throws FileNotFoundException {
        try {
            System.err.println("loading model:"+modelFile);
            BufferedReader br=new BufferedReader(new FileReader(modelFile));
            String line=br.readLine();
            if (! "SVM-light Version V5.01-TK-1.2".equals(line)) {
                _logger.warning("Cannot read model file");
                return 0.0;
            }
            
            // skip the next 19 lines
            for (int i=0; i<19; i++) line=br.readLine();
            
            int pos=line.indexOf(" # threshold b");
            if (pos==-1) {
                throw new RuntimeException("cannot find threshold value");
            }
            System.err.format("***** Threshold = %f\n",
                    Double.parseDouble(line.substring(0,pos)));
            return Double.parseDouble(line.substring(0,pos));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read model",e);
        }
    }
    
    public void setHeader(List<FeatureDescription> fds) {
        _fds=fds;
    }
    
    public void classify(List<? extends Instance> problems, List output)
    {
        if (_native!=null)
            classify_native(problems,output,null);
        else
            classify_external(problems,output,null);
    }
    
    public void classify(List<? extends Instance> problems, List output,
            List<Double> confidence) {
        if (_native!=null)
            classify_native(problems,output,confidence);
        else
            classify_external(problems,output,confidence);
    }

    public void classify_native(List<? extends Instance> problems, List output,
            List<Double> confidences)
    {
        StringBuilder sb=new StringBuilder();
        SVMLightInstanceWriter w=new SVMLightInstanceWriter(new StringWriter(),
                _dictFile,true);
        w.setHeader(_fds);
        for (Instance inst: problems)
        {
            w.write(inst,sb);
            double result=_native.classify(sb.toString());
            output.add(result>0);
            if (confidences!=null)
                confidences.add(Math.abs(result));
            sb.setLength(0);
        }
    }
    
    public void classify_external(List<? extends Instance> problems, List output,
            List<Double> confidences)
    {
        try {
            File tmpFile=File.createTempFile("svmlight",".data");
            File tmpFile2=File.createTempFile("svmlight",".out");
            SVMLightInstanceWriter w=new SVMLightInstanceWriter(new FileWriter(tmpFile),
                    _dictFile,true);
            w.setHeader(_fds);
            for (Instance inst: problems) {
                w.write(inst);
            }
            w.close();
            runClassify(tmpFile,tmpFile2);
            BufferedReader br=new BufferedReader(new FileReader(tmpFile2));
            String line=br.readLine();
            while (line != null) {
                double result=Double.parseDouble(line);
                output.add(result>0);
                if (confidences!=null)
                    confidences.add(Math.abs(result));
                line=br.readLine();
            }
            if (output.size()!=problems.size()) {
                throw new RuntimeException(String.format("SVMClassify returned %d values, expected %d",
                        output.size(), problems.size()));
            }
            tmpFile.delete();
            tmpFile2.delete();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IOException",e);
        }
    }
        
    private void runClassify(File tmpFile, File tmpFile2) throws IOException {
        try {
            String cmd=
                String.format("%s %s %s %s",
                    ConfigProperties.getInstance().getSVMLightClassify(),
                    tmpFile.getAbsolutePath(),
                    _modelFile.getAbsolutePath(),
                    tmpFile2.getAbsolutePath());
            ShellCommand.runShellCommand(cmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Interrupted",e);
        }
    }
    
    public static void runLearner(String modelName, String options)
    throws IOException {
        File trainFile=new File(ConfigProperties.getInstance().getModelDir(),
                modelName+".data");
        File modelFile=new File(ConfigProperties.getInstance().getModelDir(),
                modelName+".svmltk");
        if (options==null) options="";
        String cmd=
        String.format("%s %s %s %s",
                ConfigProperties.getInstance().getSVMLightLearn(),
                options,
                trainFile.getAbsolutePath(),
                modelFile.getAbsolutePath());
        System.err.println(cmd);
        try {
            ShellCommand.runShellCommand(cmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Interrupted",e);
        }
    }
}
