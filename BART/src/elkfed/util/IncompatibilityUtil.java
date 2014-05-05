/*
 * IncompatibilityUtil.java
 *
 * Created on August 31, 2007, 2:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.util;

import edu.brandeis.cs.steele.wn.PointerTarget;
import edu.brandeis.cs.steele.wn.PointerType;
import elkfed.coref.discourse_entities.Property;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Vlad
 */
public class IncompatibilityUtil {
    
    /**Creates the logger*/
    protected static Logger _log=Logger.getAnonymousLogger();
    
    private static HashMap<String,Integer> synCache  = new HashMap<String,Integer>();
    private static Integer groupID=0;
      
    /** IMPLEMENTATION DETAIL: the singleton instance */
    private static IncompatibilityUtil singleton; 
    
    /** Creates a new instance of IncompatibilityUtil */
    public IncompatibilityUtil() {}
        
    
     /** Getter for instance */
    public static synchronized IncompatibilityUtil getInstance()
    {
        if (singleton == null)
        { singleton = new IncompatibilityUtil(); }
        return singleton;
    }
    
    /*** Get rid of strings that occur in both mentions
    * Used to get rid of modifiers that do not provide information */ 
        private void listDuplicateReduction(List<String> l1, List<String> l2)
            {
             List<String> remove = new ArrayList<String>();

            for(String s1 : l1)
                for(String s2 : l2)
                   if(s1.equals(s2))
                    remove.add(s1);
                 

             for(String s1 : remove)   
             {      l1.remove(s1);
                    l2.remove(s1);    }
            }    
        

       private static int iIndent = 0;
       private static boolean bExtended = true;
       
        /** Go through wordnet  */
        private static void traverse(PointerTarget sense, PointerType pointerType)  {

		String sIndent = "";
		for (int i=0; i<iIndent; i++)
			sIndent += " ";

		sIndent += " =>";
            _log.log(Level.INFO,sIndent+sense.getDescription());

            PointerTarget[] parents = sense.getTargets(pointerType);
           for (int i = 0; i < parents.length; ++i) {
		iIndent++;
               traverse(parents[i], pointerType);
		iIndent--;
            }

        }
       
        /**
         * Get pos for prenominal expressions 
         */
     public List<Character> setModifiers_DE_POS(Set<Property> prop)
     {
         List<Character> pos = new ArrayList<Character>();  
         for(Property p : prop)
                 {
                    pos.add( p.getPOS() );
                 }
        
        return pos; 
     }
         
   /**
    *  Extract the strings for each property of the discourse entity 
    */
     public ArrayList<ArrayList<String>> setModifiers_DE(Set<Property> prop,byte type)
       {
           ArrayList<ArrayList<String>> modifiers = new ArrayList<ArrayList<String>>();
           
            for(Property p : prop)
            {
                /**Attribute type is 2, Relation type is 3*/
                if(p.getType() == 2)
                {
                    
                    _log.log(Level.INFO,"Attribute for "   + " | " + p.getPredicate()+ " | ");
                    
                    ArrayList<String> temp = new ArrayList<String>();
                    temp.add(p.getPredicate());
                    temp.add(null);

                    modifiers.add(temp);
                    
                }
                else if (p.getType() == 3)
                {
                     _log.log(Level.INFO,"Relation for "   + " | " + p.getArgument()+ " is " + p.getPredicate() + " | ");
                     
                    ArrayList<String> temp = new ArrayList<String>();
                    temp.add(p.getArgument());
                    temp.add(p.getPredicate());
                    
                     modifiers.add(temp);
                     //TODO need to also return predicate
                }
            }          
            return modifiers;
       }
     
     /**
      * Associates each syn- and ant- set with a integer identifier
      * Puts a new word in the cache, associating a unique integer as the value for each word in the target words 'similar to' set.
      *Each antonym of the word is given a value of negative (-) of the unique integer
      */
     public void cacheDB(List<String> synSet, List<String> antSet)
     {
         //make sure caching is working correctly
     boolean DEBUG = true;

     if(DEBUG ) _log.log(Level.INFO,"Size " + synSet.size() );
     if(synSet.size() >0)
       if(!synCache.containsKey(synSet.get(0).toLowerCase()))   
       {
       if(DEBUG) _log.log(Level.INFO,"Adding " + synSet.get(0).toLowerCase() + " to cache");
           for(String s : synSet)   
             synCache.put(s.toLowerCase(),groupID);
     
           for(String a : antSet)
             synCache.put(a.toLowerCase(),-1*groupID);
           
           groupID++;
       }
       else
           _log.log(Level.INFO,"cache already contains " + synSet.get(0).toLowerCase());
     }
     
     /**
      *  Compute the compatibility of modifiers from wordnet cache
      * DONE: make sure its returning the right value if there are multiple modifiers
      */
     public double compareCompatWN(ArrayList<String> e1, ArrayList<String> e2)
     {
         //TODO make sure that lists are getting compared in cache correctly
       _log.log(Level.INFO,"Comparing WN compatibility");
        ArrayList<Double> results = new ArrayList<Double>();
        double value = 0.5;
        
        for(String s1 : e1)
        {
            _log.log(Level.INFO,"Comparing + " + s1);
          if(synCache.containsKey(s1))
          {    int tempGroup = synCache.get(s1);
               _log.log(Level.INFO," Syn contained key " + s1);
           for(String s2 : e2)
            {
               _log.log(Level.INFO," with " + s2);
               if(synCache.containsKey(s2))
               {
                   _log.log(Level.INFO," Syn contained key " + s2);
                   if(synCache.get(s2) == tempGroup)
                     {
                      //Perfectly compatible
                         value=1.0;
                         results.add(value);
                     }
                     else if(synCache.get(s2) == -1* tempGroup)
                     {
                       _log.log(Level.INFO,"Perfectly incompatible");
                        //perfectly incompatible
                         value=0.0;
                         return value;
                     }
           }
           }
          }
        
        }
        
        Double ret = calcCompatibility_MultipleModifiers(results);
        
        return ret;
     }
     
     
     /**
      * Combine the different values assigned from the seperate comparisons into one representative value
      */
     public double calcCompatibility_MultipleModifiers(ArrayList<Double> results)
     {
       double ret = -1;
       boolean allZero = true, 
               noOnes = true,
               oneZero = false;
       for(Double temp : results)
       {
         if(temp == 1.0)
         {   
              allZero = false;
              noOnes = false;
         }   
         else if (temp == 0.5)
            {
            allZero = false;
           
            }
        else if (temp == 0.0)
                oneZero = true;
         
       }
       
        if(results.size() == 0){} //if no modifiers keep what we already assigned
        else{     
                if(allZero || oneZero){
              ret = 0.0;//  inst.setFeature(FD_IS_INCOMPATIBLE,0.0); sb.append("Assingin 0\n");
                }
                else if (noOnes && !allZero){
              ret = 0.5;//   inst.setFeature(FD_IS_INCOMPATIBLE,0.5);sb.append("Assingin 0.5\n");
                }
                else if(!noOnes){
               ret = 1.0;//     inst.setFeature(FD_IS_INCOMPATIBLE,1.0);sb.append("Assingin 1\n");
                }
        }
     return ret;
     
     }
     
     /**
      * Extract nnp/jj/nn/etc from the parse tree string
      */
     public ArrayList<String> setModifiers(String mod, String type, Boolean postPre)
    {
        StringBuffer buff = new StringBuffer();
        ArrayList<String> modBuffer = new ArrayList<String>();

     //  buff.append("Starting " + type);
        boolean nnp = false;
        for(String tok : mod.replaceAll("\\)", ") ").split("\\s+")) 
        {
                if ((tok.matches(".+\\)")) && nnp)
                {
                    tok=tok.replaceAll("\\)", "");
                    // tok is an nnp
                    
                    //add check for date
                    if(type.equals("cd"))
                    {
                            if(tok.matches("[0-9]{4}"))
                            {
                            modBuffer.add(tok);
                           buff.append(type + " " + tok + "\n");
                            }
                    }   
                    
                    else
                    {
                    modBuffer.add(tok);
                   buff.append(type + " " + tok + "\n");
                    }
                }
                //either nnp or jj
                 if (tok.matches("\\(" + type) || tok.matches("\\[\\(" + type))
                {
                    nnp = true;
                }
                else
                {
                    nnp = false;
                }
        }    
        
        return modBuffer;
    }
     
    public ArrayList<String> getStringList (ArrayList<ArrayList<String>> s, int i)
   {
   ArrayList<String> temp = new ArrayList<String>();
   for(ArrayList a : s)
   {
    temp.add((String) a.get(i));
    _log.log(Level.INFO,"added "+ (String) a.get(i));
   }
   
    return temp;
   }
       
     
     public static void main(String[] args)
     {/*
          HashMap<String,Integer> temp  = new HashMap<String,Integer>();
          temp.put("red",1);
         temp.put("maroon",1);
         
         int i = temp.get("blue");
         if(temp.containsKey("maroon"))
             if(temp.get("maroon") == i)
                 _log.log(Level.INFO,"in same set");
             else 
                 _log.log(Level.INFO,"differen set");
      
         */
       List<String> synSet = new ArrayList<String>();
         synSet.add("american");
         
       if(!synCache.containsKey(synSet.get(0).toLowerCase()))   
       {
       _log.log(Level.INFO,"Adding " + synSet.get(0).toLowerCase() + " to cache");
           for(String s : synSet)   
           { synCache.put(s.toLowerCase(),groupID);
            _log.log(Level.INFO,"added " + s.toLowerCase());
           }
             groupID++;
       }
       else
           _log.log(Level.INFO,"cache already contains " + synSet.get(0).toLowerCase());
         
         
         if(!synCache.containsKey(synSet.get(0).toLowerCase()))   
       {
       _log.log(Level.INFO,"Adding " + synSet.get(0).toLowerCase() + " to cache");
           for(String s : synSet)   
             synCache.put(s.toLowerCase(),groupID);
     
             groupID++;
       }
       else
           _log.log(Level.INFO,"cache already contains " + synSet.get(0).toLowerCase());
         
        
     }
}
    
