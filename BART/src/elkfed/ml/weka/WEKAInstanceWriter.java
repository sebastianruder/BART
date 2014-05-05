/*
 * WEKAInstanceWriter.java
 *
 * Created on July 11, 2007, 6:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.ml.weka;

import elkfed.ml.*;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 *
 * @author vae2101
 */
public class WEKAInstanceWriter implements InstanceWriter{
    private final boolean DEBUG=true;
    private final Writer _file;
    private List<FeatureDescription> _fds;
    
    /** Creates a new instance of WEKAInstanceWriter */
    public WEKAInstanceWriter(Writer output) {
       _file = output;
    }
    
   public void setHeader(List<FeatureDescription> fds) throws IOException {
       System.out.println("WEKAInstanceWriter.setHeader");
       System.out.println(fds);

       _fds = fds;
       
       _file.write("@RELATION something\n\n");
       
       for (FeatureDescription fd : fds)
           _file.write(getWEKAHeaderLine(fd));
       
       _file.write("\n\n@DATA\n");
   }
   
   private String getWEKAHeaderLine(FeatureDescription fd) {
       
       String typedec=null;
       
       switch (fd.type) { 
           case FT_BOOL:
               typedec = "{false,true}";
               break;
           case  FT_SCALAR:
               typedec = "NUMERIC";
               break;
           case FT_NOMINAL_ENUM: 
               StringBuffer sb = new StringBuffer();
               
               Object[] csts = fd.cls.getEnumConstants();
               
               sb.append("{");
               for (int i = 0; i < csts.length - 1; i++)
                   sb.append(csts[i].toString()).append(",");
               sb.append(csts[csts.length-1]);
               sb.append("}");
               
               typedec = sb.toString();
               break;
           case FT_STRING: 
               typedec = "STRING";
               break;
           case FT_TREE_STRING:
               typedec = "STRING";
               break;
           default:
               throw new RuntimeException("unsupported feature type");
       }
       
       return "@ATTRIBUTE " + fd.name + " " + typedec + "\n";
              
   }
   
    /** writes an instance to the file */
    public void write(Instance inst) throws IOException{
       
        StringBuffer sb = new StringBuffer();
       
        for (int i = 0; i < _fds.size()-1; i++)
        {
            sb.append(inst.getFeature(_fds.get(i)).toString());
            sb.append(",");
        }
        sb.append(inst.getFeature(_fds.get(_fds.size()-1)).toString());
        sb.append("\n");
        if (DEBUG)
        {
            sb.append("% ").append(inst.getDebugInfo()).append("\n");
        }
       _file.write(sb.toString());
    }

    public void close() throws IOException
    {
        _file.close();
    }

    public void flush() throws IOException
    {
        _file.flush();
    }
}
