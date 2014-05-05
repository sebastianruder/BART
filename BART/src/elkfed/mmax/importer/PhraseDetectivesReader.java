/*
 * PhraseDetectivesReader
 * 
 */

package elkfed.mmax.importer;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Stack;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
//import javax.xml.transform.*;
//import javax.xml.transform.dom.*;
//import javax.xml.transform.stream.*;

import elkfed.mmax.minidisc.*;
import elkfed.mmax.pipeline.*;
import elkfed.webdemo.ShowText;

/**
 * PhraseDetectivesReader
 * Import MAS-XML files into MMAX2
 * 
 * @author Massimo Poesio
 */
public class PhraseDetectivesReader {
    
    private static final String MASXMLDTD = "mas_xml_PD.dtd";

    int mc = 0;
    int wc = 0;
    int setc = 0;
    //int max=0;
    Hashtable meid = new Hashtable();
    Hashtable bspan = new Hashtable();
    Hashtable espan = new Hashtable();
    Hashtable minspan = new Hashtable();
    Hashtable set = new Hashtable();
    Hashtable reference = new Hashtable();
    ArrayList words = new ArrayList();
    Stack npheads = new Stack();
    //ArrayList npheads = new ArrayList();

    private File safeCreateDir (String folderName) {

        File folder = new File(folderName);
        try {
            if (folder.exists()) {
                System.out.println("... Directory " + folderName +
                                       " already exists, cannot create");
                System.exit(0);
            } else {
                if (folder.mkdir()) {
                    System.out.println("... Directory " + folderName +
                                       " created");
                } else {
                    System.out.println("... Directory " + folderName +
                                       " couldn't be created, exiting");
                    System.exit(0);
                }
            }
        } catch (Exception e) {
        }
        return folder;
    }

    private File createMMAX2Dir (String outputFolderName) {
        
        File outputFolder = safeCreateDir(outputFolderName);
        String outputFolderPath = outputFolder.getPath();
        //System.out.println(outputFolderPath);

        String baseDataFolderPath = outputFolderPath.concat("/Basedata");
        File baseDataFolder = safeCreateDir(baseDataFolderPath);
        String markablesFolderPath = outputFolderPath.concat("/markables");
        File markablesFolder = safeCreateDir(markablesFolderPath);

        File wordfile =new File(baseDataFolderPath.concat("/words.dtd"));
        try {
                // Create file
                FileWriter fstream = new FileWriter(wordfile);
                BufferedWriter out = new BufferedWriter(fstream);

                out.write("<!ELEMENT words (word*)>");
                out.newLine();
                out.write("<!ELEMENT word (#PCDATA)>");
                out.newLine();
                out.write("<!ATTLIST word id ID #REQUIRED>");
                out.newLine();
                out.close();
        } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
        }

        File markablefile =new File(markablesFolderPath.concat("/markables.dtd"));
        try {
                // Create file
                FileWriter fstream = new FileWriter(markablefile);
                BufferedWriter out = new BufferedWriter(fstream);

                out.write("<!ELEMENT markables (markable*)>>");
                out.newLine();
                out.write("<!ATTLIST markable id ID #REQUIRED>");
                out.newLine();
                out.close();
        } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
        }

        return(outputFolder);

    }

     public final Document parseFile(String fileName) {

            System.out.println("Parsing XML file... " + fileName);
            DocumentBuilder docBuilder;
            Document doc = null;
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setIgnoringElementContentWhitespace(true);
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                System.err.println("Wrong parser configuration: " + e.getMessage());
                return null;
            }
            File sourceFile = new File(fileName);
            try {
                doc = docBuilder.parse(sourceFile);
            } catch (SAXException e) {
                System.err.println("Wrong XML file structure: " + e.getMessage());
                return null;
            } catch (IOException e) {
                System.err.println("Could not read source file: " + e.getMessage());
            }
            System.out.println("XML file parsed");
            return doc;
        }

       private void traverse(Node root) {

            Node topnode = root;
            String topnodename = topnode.getNodeName();
            NodeList children = topnode.getChildNodes();

            Node wtxtn;
            String wtxt = "";
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    // do nothing?
                } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                    String childtype = child.getNodeName();

                    if (childtype.equals("W")) {
                        wtxtn = child.getFirstChild();
                        if (wtxtn.getNodeType() == Node.TEXT_NODE) {
                            wtxt = wtxtn.getNodeValue();
                            wtxt = wtxt.substring(0, wtxt.length());
                           // System.out.println(wtxt);
                        }
                        if (!((wtxt.equalsIgnoreCase("&amp;"))
                                || (wtxt.equalsIgnoreCase("&gt;"))
                                || (wtxt.equalsIgnoreCase("&lt;"))
                                || (wtxt.equalsIgnoreCase("&quot;"))
                                || (wtxt.equalsIgnoreCase("&apos;")))) {
                            if (wtxt.equalsIgnoreCase("&")) {
                                wtxt = "&amp;";
                            }
                            if (wtxt.equalsIgnoreCase(">")) {
                                wtxt = "&gt;";
                            }
                            if (wtxt.equalsIgnoreCase("<")) {
                                wtxt = "&lt;";
                            }
                            if (wtxt.equalsIgnoreCase("\"")) {
                                wtxt = "&quot;";
                            }
                            if (wtxt.equalsIgnoreCase("'")) {
                                wtxt = "&apos;";
                            }
                            // DEBUG
                            // System.out.println("    Word "+Integer.toString(wc)+" :"+wtxt);
                            words.add("  <word id=\"word_".concat(Integer.toString(wc)).concat("\">").concat(wtxt).concat("</word>"));
                            wc++;
                            //  System.out.println(words);

                        }

                    } else if (childtype.equalsIgnoreCase("ne")) {
                        String neid = child.getAttributes().getNamedItem("id").getNodeValue();

                        String meid_s = "markable_".concat(Integer.toString(mc));
                        mc++;
                        /* $meid{$neid} = $meid; */
                        meid.put(neid, meid_s);

                        int bspan_i = wc;
                        /* $bspan{$neid} = $bspan; */
                        bspan.put(neid, bspan_i);
                        // DEBUG
                        // System.out.println("NE  "+neid+" opening: "+Integer.toString(bspan_i));
                        traverse(child);
                        int espan_i = wc - 1;
                        /* $espan{$neid} = $espan; */
                        espan.put(neid, espan_i);
                        //boolean empty = false;
                        String minspan_s = "";

                        if (!(npheads.isEmpty())) {
                            minspan_s = npheads.peek().toString();
                            //minspan_s = npheads.get(0).toString();
                            npheads.pop();
                            //npheads.remove(0);
                            // DEBUG
                            // System.out.println("  Popping nphead for ne "+neid+": "+minspan_s);
                        } else {
                            minspan_s = "word_".concat(Integer.toString(espan_i));
                            // DEBUG
                            // System.out.println("  No nphead for ne "+neid+": creating new one, "+minspan_s);
                            //empty = true;
                        }

                        minspan.put(neid,minspan_s);
                        /*
                         if (!empty) {
                            minspan.put(neid, minspan_s);
                        }
                         *
                         */
                        // DEBUG
                        // System.out.println("NE  "+neid+" closing, span: "+Integer.toString(bspan_i)+".."+Integer.toString(espan_i)+", head: "+minspan_s);

                    } else if (childtype.equalsIgnoreCase("nphead")) {
                        int bspan_i = wc;
                        // DEBUG
                        // System.out.println("  nphead opening: "+Integer.toString(bspan_i));
                        traverse(child);
                        int espan_i = wc - 1;
                        // DEBUG
                        // System.out.println("  nphead closing, span: "+Integer.toString(bspan_i)+".."+Integer.toString(espan_i));
                        if (bspan_i == espan_i) {

                            /*push (@npheads, "word_$bspan");*/
                            String head = "word_";
                            head = head.concat(Integer.toString(bspan_i));
                            // DEBUG
                            // System.out.println("  Pushing nphead with span: "+Integer.toString(bspan_i)+".."+Integer.toString(espan_i));
                            npheads.push(head);

                        } else {
                            /*push (@npheads, "word_".$bspan."..word_".$espan);*/
                            String head = "word_";
                            head.concat(Integer.toString(bspan_i));
                            head.concat("..word_");
                            head.concat(Integer.toString(espan_i));
                            npheads.push(head);
                            // DEBUG
                            // System.out.println("  Pushing nphead with span: "+Integer.toString(bspan_i)+".."+Integer.toString(espan_i));
                        }

                    } else if (childtype.equals("PDante")) {

                        String anaphor = child.getAttributes().getNamedItem("id").getNodeValue();                   
                        NodeList Interpretationlist = child.getChildNodes();
                        //interpretation node OR SKIP!!                       

                        for (int k = 0; k < Interpretationlist.getLength(); k++) {
                                   // Added check here to skip spurious nodes and skips
                            if ((Interpretationlist.item(k).getNodeType() == Node.ELEMENT_NODE)
                                    &&
                                (Interpretationlist.item(k).getNodeName().equals("interpretation"))
                                ) {

                                Node interpretation=Interpretationlist.item(k);
                                //anchor  nodes are the  subnodes of interpretation
                                NodeList anchornodelist= interpretation.getChildNodes();

                                String ante="";
                                int maxx=-999999999;
                                Node maxAnchor = null;

                                for (int j = 0; j < anchornodelist.getLength(); j++) {

                                    Node anchornode = anchornodelist.item(j);
                                    if ((anchornode.getNodeType() == Node.ELEMENT_NODE)
                                            &&   // added check here
                                        (anchornode.getNodeName().equals("anchor"))) {
                                        int ann = Integer.valueOf((anchornode.getAttributes()).getNamedItem("ann").getNodeValue());
                                        int my_agr = Integer.valueOf((anchornode.getAttributes()).getNamedItem("agr").getNodeValue());
                                        int my_disagr = Integer.valueOf((anchornode.getAttributes()).getNamedItem("disagr").getNodeValue());
                                        int ancvalue = Integer.valueOf(ann + my_agr - my_disagr);

                                        if(maxx<ancvalue){
                                            maxx=ancvalue;
                                            maxAnchor = anchornode;
                                        /*
                                         Node anchorType = anchornode.getAttributes().getNamedItem("type");
                                        if (anchorType.getNodeValue().equals("DO")) {
                                            anaphorReference = "old";
                                            if((anchornode.getAttributes().getNamedItem("ante"))!=null){
                                                ante =
                                                anchornode.getAttributes().getNamedItem("ante").getNodeValue();
                                            } else {
                                                ante=anaphor;
                                            }
                                        } else if (anchorType.getNodeValue().equals("DN")) {
                                            anaphorReference = "new";

                                        } else if (anchorType.getNodeValue().equals("NR")) {
                                            anaphorReference = "non_referring";
                                        } else if (anchorType.getNodeValue().equals("PR")) {
                                            anaphorReference = "non_referring";
                                        }
                                         *
                                         */
                                        }
                                        /* Moved this out of the loop
                                         String  anchor= ante;
                                        if (!(set.isEmpty())) {
                                            if (set.containsKey(anchor)) {
                                                set.put(anaphor, set.get(anchor));
                                            } else {
                                                set.put(anchor, setc);
                                                set.put(anaphor, setc);
                                                setc++;
                                            }
                                        } else {
                                            set.put(anchor, setc);
                                            set.put(anaphor, setc);
                                            setc++;
                                        }
                                        break;
                                         *
                                         */
                                    }
                                }
                                // HERE DO THE ANTE ETC UPDATE
                                if (maxAnchor == null) {
                                    System.err.println("No anchor found in PDAnte element for anaphor: "
                                                       +anaphor);
                                } else {
                                    Node anchorType = maxAnchor.getAttributes().getNamedItem("type");
                                    if (anchorType.getNodeValue().equals("DO")) {   // discourse old: has ante
                                        // NB for the moment reference value not used
                                        reference.put(anaphor, "old");
                                        if((maxAnchor.getAttributes().getNamedItem("ante"))!=null){
                                            ante =
                                                maxAnchor.getAttributes().getNamedItem("ante").getNodeValue();
                                            if (set.isEmpty()) {
                                                set.put(ante, setc);
                                                set.put(anaphor, setc);
                                                setc++;
                                            } else {
                                                if (set.containsKey(ante)) {
                                                    set.put(anaphor, set.get(ante));
                                                } else {
                                                    set.put(ante, setc);
                                                    set.put(anaphor, setc);
                                                    setc++;
                                                }
                                            }
                                        } else {
                                            System.err.println("DO anchor without ante specification for anaphor: "+anaphor);
                                            set.put(anaphor, setc);
                                            setc++;
                                            // I DON'T UNDERSTAND THIS
                                            // ante=anaphor;
                                        }
                                    } else if (anchorType.getNodeValue().equals("DN")) {
                                        reference.put(anaphor, "new");
                                        set.put(anaphor, setc);
                                        setc++;
                                        // In ARRAU, non-referring nominals are present in the phrase_level
                                        // but not in the coref level. For the moment let's focus on the
                                        // coref level. Note that if there is no bucket in set for a
                                        // markable it doesn't get written out
                                    } else if (anchorType.getNodeValue().equals("NR")) {
                                        reference.put(anaphor, "non_referring");
                                    } else if (anchorType.getNodeValue().equals("PR")) {
                                        reference.put(anaphor, "non_referring");
                                    } else {
                                        System.out.println("Unknown type of interpretation found for anaphor: "
                                                           +anaphor);
                                    }
                                }
                            } // Ignore skips for the moment
                        }
                    } else if (childtype.equalsIgnoreCase("header")) {
                        // do nothing
                    } else {
                        traverse(child);
                    }
                }
            }

       }

       private String removeFileExtension (String filename) {

           //
           if (null != filename) {
               if (filename.contains(".")) {
                   return filename.substring(0, filename.lastIndexOf("."));
               } else {
                   return filename;
               }
           } else {
               return null;
           }

           // get index of last period
           /*
            final int lastPeriodPos = filename.lastIndexOf('.', 1);
           if (lastPeriodPos == -1) {
                // No period after first character - return name as it was passed in
                return filename;
           } else {
                // Remove the last period and everything after it
                return filename.substring(0, lastPeriodPos);
           }
            * 
            */
       }

       private void write_basedata(File dir, String docId) {

           String basedata_file = 
               dir.getPath().concat("/Basedata/").concat(removeFileExtension(docId)).concat("-mmax2_words.xml");
           System.out.println("  Creating basefile file: " + basedata_file);
            try {
                // Create file
                FileWriter fstream = new FileWriter(basedata_file);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                out.newLine();
                out.write("<!DOCTYPE words SYSTEM \"words.dtd\">");
                out.newLine();
                out.write("<words>");

                String word;
                while (!(words.isEmpty())) {
                    word = words.get(0).toString();

                    out.newLine();

                    out.write(word);
                    words.remove(0);

                }
                out.newLine();
                out.write("</words>");


                out.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

        }

        private void write_markables(File dir, String docId, String level) {

            String markable_file = "";
            markable_file = dir.getPath().concat("/markables/").concat(removeFileExtension(docId)).concat("-mmax2_coref_level.xml");

            System.out.println("  Creating markable level file: " + markable_file);

            try {
                // Create file
                FileWriter fstream = new FileWriter(markable_file);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                out.newLine();
                out.write("<!DOCTYPE markables SYSTEM \"markables.dtd\">");
                out.newLine();
                out.write("<markables xmlns=\"www.eml.org/NameSpaces/");
                out.write(level);
                out.write("\">");
                out.newLine();
                Enumeration e = bspan.keys();
                while (e.hasMoreElements()) {
                    String neid = (String) e.nextElement();

                    if (set.containsKey(neid)) {    // member of a coref chain

                        int bspan_i = (Integer) bspan.get(neid);
                        int espan_i = (Integer) espan.get(neid);
                        String meid_s = (String) meid.get(neid);

                        String markable = "  <markable id=\"";
                        markable = markable.concat(meid_s).concat("\" span=\"word_").concat(Integer.toString(bspan_i)).concat("..word_").concat(Integer.toString(espan_i)).concat("\"");
                        if (minspan.containsKey(neid)) { // found a head
                            markable =
                                markable.concat(" min_ids=\"").concat((String) minspan.get(neid)).concat("\"");
                        }
                    
                        markable = markable.concat(" coref_set=\"set_").concat(Integer.toString((Integer) set.get(neid))).concat("\"");
                    
                        // for debugging purposes, add the PhDet ID as well
                        markable = markable.concat(" phrase_det_id=\"").concat(neid).concat("\"");

                        markable = markable.concat(" mmax_level=\"coref\" />");
                        out.write(markable);
                        out.newLine();
                    }
                }

                out.write("</markables>");
                out.newLine();

                //Close the output stream
                out.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }
        }

        private void write_mmax(File dir, String docId) {
            
            String mmax_file
                    = dir.getPath().concat("/").concat(removeFileExtension(docId)).concat("-mmax2.mmax");

            System.out.println("  Creating MMAX2 file: " + mmax_file);

            try {
                // Create file
                FileWriter fstream = new FileWriter(mmax_file);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                out.newLine();
                out.write("<mmax_project>");
                out.newLine();
                out.write("<turns></turns>");
                out.newLine();
                out.write("<words>" + removeFileExtension(docId).concat("-mmax2_words.xml") +"</words>");
                //out.write("<words>" + docId.concat("-game_cleaned-mmax2_words.xml") +"</words>");
                out.newLine();
                out.write("<gestures></gestures>");
                out.newLine();
                out.write("<keyactions></keyactions>");
                out.newLine();
                out.write("<views><stylesheet>muc_style.xsl</stylesheet></views>");
                out.newLine();
                out.write("</mmax_project>");
                out.newLine();
                out.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }


        }

    private void process_file(File inputFolder, File outputFolder, String fileName) {

            String xmlFileName = inputFolder.getPath().concat("/").concat(fileName);
            //String xmlFileName =
            //       dir.getPath().concat("/").concat(fileName).concat("-game_cleaned.xml");
            System.out.println("Extracting basedata and markables from : " + xmlFileName);

            // parse XML file -> XML document will be build
            Document doc = parseFile(xmlFileName);

            // get root node of xml tree structure
            Node root = doc.getDocumentElement();
            
            // find markables and sentences in original doc
            //
            // start by zeroing everything
            mc = 0;
            wc = 0;
            setc = 0;
            meid.clear();
            bspan.clear();
            espan.clear();
            minspan.clear();
            set.clear();
            reference.clear();
            words.clear();
            npheads.clear();
            traverse(root);

            // write node and its child nodes into System.out
            write_basedata(outputFolder, fileName);
            write_markables(outputFolder, fileName, "coref");
            write_mmax(outputFolder, fileName);

    }

    private void sentenceLevel (File directory , String docId){

        String mmax2DocId = removeFileExtension(docId).concat("-mmax2");
        System.out.println("Adding extra levels to : " + mmax2DocId);
        MiniDiscourse Mini=  MiniDiscourse.load(directory,mmax2DocId);
        //SentenceDetector SenDet=new SentenceDetector("./models/opennlp/EnglishSD.bin.gz");
        //SenDet.annotate(Mini);

        // running Pipeline
        // ParserPipeline the new default
        ParserPipeline pipeline = new ParserPipeline();
        //DefaultPipeline My_Default = new DefaultPipeline();
        ShowText.run_pipeline(pipeline, Mini);
        Mini.saveAllLevels();

    }


    public static void main(String[] args) {
                        /* take folder name as input, saves files in
                         * folder with attached _MMAX2
                         */

        PhraseDetectivesReader phdetreader = new PhraseDetectivesReader();

        String inputFolderName = args[0];      // get input
        System.out.println("Input Folder: " + inputFolderName);
        File   inputFolder = new File(inputFolderName);
        //File folder = new File("/home/fereshteh/BART2/BART/datatest");
        if (!inputFolder.isDirectory()) {
            System.out.println(inputFolderName + " not a directory");
            System.exit(0);
        }

        File[] listOfMASXMLFiles = inputFolder.listFiles();
        if (listOfMASXMLFiles.length == 0) {
            System.out.println(inputFolderName + " is empty");
            System.exit(0);
        }
            // this creates the separate directory and two subdirs Basedata
            // and markables
        String outputFolderName = inputFolderName.concat("_MMAX2");
        System.out.println("Output Folder: " + outputFolderName);
        File outputFolder = phdetreader.createMMAX2Dir(outputFolderName);
        
        
        for (int i = 0; i < listOfMASXMLFiles.length; i++) {
            if (listOfMASXMLFiles[i].isFile() &&
                    !(listOfMASXMLFiles[i].getName().equalsIgnoreCase(MASXMLDTD))) {
                String fileName=listOfMASXMLFiles[i].getName();
                // why did Fereshteh do what follows?
                //String docId=f.substring(0,((f.length()-17)));

                // and let's not even think about this ...
                //String directory="/home/fereshteh/BART2/BART/datatest";

                System.out.println("Processing file: " + fileName);

                // create base file and markables
                phdetreader.process_file(inputFolder,outputFolder,fileName);
                //new ParseXMLFile(new File(directory),docId);

                // add additional levels
                phdetreader.sentenceLevel(outputFolder,fileName);
                // new SentenceLevel(new File(directory),docId);
            }
	}
        
      
    }

}
