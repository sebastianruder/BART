/*
 * Copyright 2008 Yannick Versley / Univ. Tuebingen
 * Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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
package org.livememories.coref;

import elkfed.coref.CorefResolver;
import elkfed.coref.mentions.Mention;
import elkfed.coref.mentions.MentionFactory;
import elkfed.coref.util.Clustering;
import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.minidisc.Exporter;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.tabular.BIOColumn;
import elkfed.mmax.tabular.Column;
import elkfed.mmax.tabular.SentenceColumn;
import elkfed.mmax.tabular.TabularExport;
import elkfed.mmax.tabular.TabularImport;
import elkfed.mmax.tabular.TagColumn;
import elkfed.parse.malt.MALTWrapper;
import elkfed.util.ShellCommand;
import java.io.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.cscott.jutil.DisjointSet;
import org.maltparser.core.exception.MaltChainedException;
import org.xml.sax.SAXException;

/**
 *
 * @author yannick
 */
public class ShowText_ita extends HttpServlet {
    private static boolean is_txpinput=true;
    private static boolean no_parsing=true;
    public static String URL_PREFIX = "/BARTDemo/";

    public static String stringForDocument(MiniDiscourse doc) {
        StringBuffer buf = new StringBuffer();
        for (String tok : doc.getTokens()) {
            buf.append(tok);
            buf.append(" ");
            if (buf.length() >= 40) {
                buf.append("...");
                break;
            }
        }
        return buf.toString();
    }

    public static MiniDiscourse findDocument(List<MiniDiscourse> docs, String docId) {
        for (MiniDiscourse doc1 : docs) {
            if (doc1.getNameSpace().equals(docId)) {
                return doc1;
            }
        }
        return null;
    }

    public static void displayNavigation(MiniDiscourse doc, PrintWriter out) {
        out.println("<div class=\"menu\">");
        for (String s : new String[]{"chunks", "enamex", "markables"}) {
            out.format("<a href=\"#\" onClick=\"renderDoc('%s','%s')\">%s</a> - ",
                    doc.getNameSpace(), s, s);
        }
        out.format("<a href=\"#\" onClick=\"renderCoref('%s')\">coreference</a><br>",
                doc.getNameSpace());

        out.println("</div>");
    }

    public static void displayDocument(MiniDiscourse doc, PrintWriter out,
            File resourceDir, String fmt)
            throws SAXException, TransformerConfigurationException {
        displayNavigation(doc, out);
        StreamResult streamResult = new StreamResult(out);
        SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        StreamSource xsl_source = new StreamSource();
        xsl_source.setSystemId(new File(resourceDir, "generic_lang.xsl"));
        TransformerHandler hd = tf.newTransformerHandler(xsl_source);
        //TransformerHandler hd = tf.newTransformerHandler(new StreamSource("generic.xsl"));
        hd.setResult(streamResult);
        String[] wanted_levels;
        if ("chunks".equalsIgnoreCase(fmt)) {
            wanted_levels = new String[]{
                        "section", "sentence", "chunk", "pos"
                    };
        } else if ("enamex".equalsIgnoreCase(fmt)) {
            wanted_levels = new String[]{
                        "section", "sentence", "enamex", "pos"
                    };
        } else {
            wanted_levels = new String[]{
                        "section", "sentence", "markable", "pos"
                    };
        }
        Exporter ex = new Exporter(Arrays.asList(wanted_levels));
        ex.convertFile(doc, hd);
    }

    public static void displayCoref(MiniDiscourse doc, PrintWriter out,
            File resourceDir)
            throws SAXException, TransformerConfigurationException {
        displayNavigation(doc, out);
        StreamResult streamResult = new StreamResult(out);
        SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        StreamSource xsl_source = new StreamSource();
        xsl_source.setSystemId(new File(resourceDir, "generic_lang.xsl"));
        TransformerHandler hd = tf.newTransformerHandler(xsl_source);
        //TransformerHandler hd = tf.newTransformerHandler(new StreamSource("generic.xsl"));
        hd.setResult(streamResult);
        String[] wanted_levels = new String[]{
            "response", "markable"
        };
        Exporter ex = new Exporter(Arrays.asList(wanted_levels));
        ex.convertFile(doc, hd);
        out.println("<h4>Coreference chain</h4>");
        out.println("<div class=\"minidisc\" id=\"coref-chain\"></div>");
    }

    public static void exportCoref(MiniDiscourse doc, PrintWriter out,
            File resourceDir)
            throws SAXException, TransformerConfigurationException {
        StreamResult streamResult = new StreamResult(out);
        SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        StreamSource xsl_source = new StreamSource();
        xsl_source.setSystemId(new File(resourceDir, "export.xsl"));
        TransformerHandler hd = tf.newTransformerHandler(xsl_source);
        hd.setResult(streamResult);
        String[] wanted_levels = new String[]{
            "sentence","response","pos"
        };
        Exporter ex = new Exporter(Arrays.asList(wanted_levels));
        ex.convertFile(doc, hd);
    }

    public static void populateDocsArray(List<MiniDiscourse> docs) {
        // do nothing
    }
    private static final String LM_PATH;

    static {
        String lm=System.getenv("LM_COREF");
        if (lm==null) {
            LM_PATH="/home/yannick/sources/LM_Coref";
        } else {
            LM_PATH=lm;
        }
    }

    protected void resolve_coref(MiniDiscourse doc1) throws IOException {
        CorefResolver cr = (CorefResolver) getServletContext().getAttribute("resolver");
        MentionFactory mfact = (MentionFactory) getServletContext().getAttribute("mfact");
        DiscourseUtils.deleteResponses(doc1);
        Map<Mention, Mention> antecedents = new HashMap<Mention, Mention>();
        List<Mention> mentions = mfact.extractMentions(doc1);
        DisjointSet<Mention> partition = cr.decodeDocument(mentions, antecedents);
        Clustering.addClustersToMMAX(partition, antecedents, doc1);
    }

    protected String slurpPostData(HttpServletRequest request,
            String defaultEncoding)
            throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        InputStreamReader reader;
        String encoding=request.getHeader("Encoding");
        if (encoding==null) {
            String ctype=request.getHeader("Content-type");
            if (ctype!=null && ctype.contains("charset=")) {
                String rest=ctype.substring(ctype.indexOf("charset=")+8);
                if (rest.contains(";")) {
                    rest=rest.substring(0,rest.indexOf(';'));
                }
                encoding=rest;
            }
        }
        if (encoding==null) {
            encoding=defaultEncoding;
        }
        reader = new InputStreamReader(inputStream, encoding);
        BufferedReader br = new BufferedReader(reader);
        StringBuffer sb = new StringBuffer();
        String str;
        str = br.readLine();
        while (str != null) {
            sb.append(str);
            sb.append('\n');
            str = br.readLine();
        }
        return sb.toString();
    }

    protected String slurpPostData(HttpServletRequest request)
            throws IOException
    {
        return slurpPostData(request,"UTF-8");
    }
    
    protected MiniDiscourse importTextPro(File mmaxDir, String docId,
            String txpName)
            throws IOException, FileNotFoundException
    {
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(
                new FileInputStream(txpName), "ISO-8859-15"));
            TabularImport ti=TabularImport.readTPHeader(br);
            return ti.do_import(mmaxDir, docId, br);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected MiniDiscourse importDocument(String input_text) {
        return importDocument(input_text, false);
    }

    protected MiniDiscourse importDocument(String input_text, boolean is_textpro) {
        File mmaxDir = new File(getServletContext().getInitParameter("docs_dir"));
        String docId = UUID.randomUUID().toString();
        if (is_txpinput) docId="doc_txp";
        String basename=
                    new File(mmaxDir,
                        "parsing"+File.separator+docId).getAbsolutePath();
        try {
            Process p;
            int retval;
            long before_textpro=System.currentTimeMillis();
            if (is_textpro) {
                Writer wr=new OutputStreamWriter(
                        new FileOutputStream(basename+".txt.txp"),
                        Charset.forName("ISO-8859-15"));
                wr.write(input_text);
                wr.close();
            } else {
                if (is_txpinput) {
//just copy textpro into a tmp file
                  Writer wr=new OutputStreamWriter(
                          new FileOutputStream(basename+".txt.txp"),
                          Charset.forName("ISO-8859-15"));
                  wr.write(input_text);
                  wr.close();
                }else{ 
//run textpro
                Writer wr=new OutputStreamWriter(
                        new FileOutputStream(basename+".txt"),
                        Charset.forName("ISO-8859-15"));
                wr.write(input_text);
                wr.close();
                String textpro_root=System.getenv("TEXTPRO");
                retval=ShellCommand.runShellCommand(
                    "perl",textpro_root+"/textpro","-l","italian",
                    "-c","token+tokenstart+tokenend+sentence+pos+lemma+entity",
                    basename+".txt");
                if (retval!=0) {
                    Logger.getLogger(ShowText_ita.class.getName()).log(Level.SEVERE,
                            "preprocess returned non-zero status:"+retval);
                    return null;
                }
                long after_textpro=System.currentTimeMillis();
                Logger.getLogger(ShowText_ita.class.getName()).log(Level.INFO,
                        String.format("TextPro took %d ms",after_textpro-before_textpro));
            }
            }
            // we import the TextPro layers but don't use the MiniDiscourse object
            // so we can load the new levels from add_parses in a later step.

            MiniDiscourse doc0=importTextPro(mmaxDir,docId,basename+".txt.txp");


            doc0.saveAllLevels();
            if (no_parsing==false) {
            retval=ShellCommand.runShellCommand(
                    "bash",LM_PATH+"/bin/preprocess_step1b.sh",
                    mmaxDir.getAbsolutePath(), docId);
            if (retval!=0) {
                Logger.getLogger(ShowText_ita.class.getName()).log(Level.SEVERE,
                        "preprocess returned non-zero status:"+retval);
                return null;
            }
            long before_malt=System.currentTimeMillis();
            MALTWrapper malt=(MALTWrapper)getServletContext().getAttribute("malt");
//            malt.parseFile(basename+".conll-in", basename+".conll", "UTF-8");
            malt.parseFile(basename+".conll-in", basename+".conll", "ISO-8859-15");
            long after_malt=System.currentTimeMillis();
            Logger.getLogger(ShowText_ita.class.getName()).log(Level.INFO,
                    String.format("MALTParser took %d ms",after_malt-before_malt));


              retval=ShellCommand.runShellCommand(
                "bash",LM_PATH+"/bin/preprocess_step3a.sh",
                mmaxDir.getAbsolutePath(),
                docId);
            if (retval!=0) {
                Logger.getLogger(ShowText_ita.class.getName()).log(Level.SEVERE,
                        "preprocess returned non-zero status:"+retval);
            }
         }   
            MiniDiscourse doc = MiniDiscourse.load(mmaxDir, docId);
            TabularImport ti=(TabularImport)getServletContext().getAttribute("tab_import");
            BufferedReader rd=new BufferedReader(new FileReader(basename+".txt.txp"));
            ti.do_import(doc, rd);
            long after_import=System.currentTimeMillis();
            Logger.getLogger(ShowText_ita.class.getName()).log(Level.INFO,
                    String.format("Import (whole) took %d ms",after_import-before_textpro));
            return doc;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } catch (MaltChainedException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        List<MiniDiscourse> docs =
                (List<MiniDiscourse>) request.getSession().getAttribute("docs");
        String extraPath = request.getPathInfo();
        if (docs == null) {
            docs = new ArrayList<MiniDiscourse>();
            populateDocsArray(docs);
            request.getSession().setAttribute("docs", docs);
        }
        try {
            if (extraPath.startsWith("/addDoc")) {
                String inputText = slurpPostData(request);
                MiniDiscourse doc = importDocument(inputText);
                docs.add(doc);
                displayDocument(doc, out,
                        new File(getServletContext().getRealPath("WEB-INF/classes/elkfed/webdemo")), null);
            } else if (extraPath.startsWith("/listDocs")) {
                for (MiniDiscourse doc : docs) {
                    out.format("<a href=\"#\" onClick=\"renderDoc('%s','markables')\">%s</a><br>",
                            doc.getNameSpace(), stringForDocument(doc));
                }
            } else if (extraPath.startsWith("/renderDoc")) {
                String docId = request.getParameter("docId");
                String fmt = request.getParameter("fmt");
                MiniDiscourse doc = findDocument(docs, docId);
                displayDocument(doc, out,
                        new File(getServletContext().getRealPath("WEB-INF/classes/elkfed/webdemo")),
                        fmt);
            } else if (extraPath.startsWith("/renderCoref")) {
                String docId = request.getParameter("docId");
                MiniDiscourse doc = findDocument(docs, docId);
                MarkableLevel resp = doc.getMarkableLevelByName("response");
                if (resp.getMarkables().size() == 0) {
                    resolve_coref(doc);
                }
                displayCoref(doc, out,
                        new File(getServletContext().getRealPath("WEB-INF/classes/elkfed/webdemo")));
            } else if (extraPath != null && extraPath.startsWith("/process/")) {
                String[] args=extraPath.substring(9).split("/");
                MiniDiscourse doc;
                if (args.length>0 && args[0].equals("tab")) {
                    String inputText = slurpPostData(request,"ISO-8859-15");
                    doc = importDocument(inputText, true);
                } else {
                    String inputText = slurpPostData(request);
                    doc = importDocument(inputText,false);
                }
              if (is_txpinput==false)  resolve_coref(doc);
                if (args.length>1 && args[1].equals("tab")) {
                    exportTabular(doc,out);
                } else {
                    exportCoref(doc, out,
                            new File(getServletContext().getRealPath("WEB-INF/classes/elkfed/webdemo")));
                }
                if (is_txpinput == false) {doc.deleteAll();}
            } else {
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Servlet ShowText</title>");
                out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/BARTDemo/styles.css\">");
                out.println("<script type=\"text/javascript\" src=\"/BARTDemo/functions.js\"></script>");
                out.println("</head>");
                out.println("<body onLoad=\"prepare_nodes()\">");
                out.println("<h1>Show Text</h1>");
                //out.println("extraPath="+extraPath+"<br>");
                String input_text = request.getParameter("text");
                if (input_text != null) {
                    out.format("input text=<pre>%s</pre>",
                            input_text);
                    MiniDiscourse doc;
                    doc = importDocument(input_text);
                    //MiniDiscourse doc1=MiniDiscourse.load(new File("/space/versley/Elkfed/OntoNotes-MMAX/train/"), 
                    //       "wsj_1056");
                    docs.add(doc);
                } else if (extraPath != null && extraPath.startsWith("/displayDoc/")) {
                    String docId = extraPath.substring(12);
                    MiniDiscourse doc1 = findDocument(docs, docId);
                    if (doc1 == null) {
                        out.println(String.format("(displayDoc: document %s not found)<br>", docId));
                    } else {
                        displayDocument(doc1, out,
                                new File(getServletContext().getRealPath("WEB-INF/classes/elkfed/webdemo")), null);
                    }
                } else if (extraPath != null && extraPath.startsWith("/corefDoc/")) {
                    String docId = extraPath.substring(10);
                    MiniDiscourse doc1 = findDocument(docs, docId);
                    resolve_coref(doc1);
                    displayCoref(doc1, out,
                            new File(getServletContext().getRealPath("WEB-INF/classes/elkfed/webdemo")));
                }
                out.println("<h2>Documents in session</h2>");
                for (MiniDiscourse doc1 : docs) {
                    out.format("<a href=\"/BARTDemo/ShowText/displayDoc/%s\">%s</a><br>",
                            doc1.getNameSpace(), stringForDocument(doc1));
                }
                out.println("</body>");
                out.println("</html>");
            }

        } catch (SAXException ex) {
            Logger.getLogger(ShowText_ita.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(ShowText_ita.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void exportTabular(MiniDiscourse doc, PrintWriter out)
            throws IOException
    {
        if (is_txpinput) return;
        out.println("# FILE: "+doc.getNameSpace());
        out.println("# FIELDS: token\ttokenstart\ttokenend\t"+
                "sentence\tpos\tlemma\tentity\tcoref");
        List<Column> cols=new ArrayList<Column>();
        cols.add(new SentenceColumn("sentence", 3));
        cols.add(new TagColumn("pos", "tag", 4));
        cols.add(new TagColumn("lemma", "tag", 5));
        cols.add(new BIOColumn("entity", "tag", 6));
        cols.add(new BIOColumn("response","coref_set",7));
        TabularExport te=new TabularExport(0, cols, 1);
        te.do_export(doc, out);
    }
    // </editor-fold>
}
