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
package elkfed.webdemo;

import elkfed.coref.CorefResolver;
import elkfed.coref.mentions.Mention;
import elkfed.coref.mentions.MentionFactory;
import elkfed.coref.util.Clustering;
import elkfed.mmax.Corpus;
import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.minidisc.Exporter;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.pipeline.Pipeline;
import java.io.*;

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
import opennlp.tools.tokenize.Tokenizer;
import org.xml.sax.SAXException;

/**
 *
 * @author yannick
 */
public class ShowText extends HttpServlet {

    public static String URL_PREFIX = "/BARTDemo/";

    public static void run_pipeline(Pipeline pipeline, MiniDiscourse doc) {
        DiscourseUtils.deleteMarkableLevels(doc, pipeline);
        Corpus c = new Corpus();
        c.add(doc);
        pipeline.setData(c);
        pipeline.annotateData();
    }

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
        xsl_source.setSystemId(new File(resourceDir, "generic.xsl"));
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
        xsl_source.setSystemId(new File(resourceDir, "generic.xsl"));
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

    protected void resolve_coref(MiniDiscourse doc1) throws IOException {
        CorefResolver cr = (CorefResolver) getServletContext().getAttribute("resolver");
        MentionFactory mfact = (MentionFactory) getServletContext().getAttribute("mfact");
        DiscourseUtils.deleteResponses(doc1);
        Map<Mention, Mention> antecedents = new HashMap<Mention, Mention>();
        List<Mention> mentions = mfact.extractMentions(doc1);
        DisjointSet<Mention> partition = cr.decodeDocument(mentions, antecedents);
        Clustering.addClustersToMMAX(partition, antecedents, doc1);
    }

    protected String slurpPostData(HttpServletRequest request)
            throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
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

    protected MiniDiscourse importDocument(String input_text) {
        Tokenizer tok = (Tokenizer) getServletContext().getAttribute("tokenizer");
        String[] toks = tok.tokenize(input_text);
        File mmaxDir = new File(getServletContext().getInitParameter("docs_dir"));
        String docId = UUID.randomUUID().toString();
        MiniDiscourse doc = MiniDiscourse.createFromTokens(mmaxDir, docId, toks);
        return doc;
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
                Pipeline pipeline = (Pipeline) getServletContext().getAttribute("pipeline");
                run_pipeline(pipeline, doc);
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
            } else if (extraPath != null && (
                            extraPath.startsWith("/process/") ||
                            extraPath.equals("process")))
            {
                String inputText = slurpPostData(request);
                MiniDiscourse doc = importDocument(inputText);
                Pipeline pipeline = (Pipeline) getServletContext().getAttribute("pipeline");
                run_pipeline(pipeline, doc);
                resolve_coref(doc);
                exportCoref(doc, out,
                        new File(getServletContext().getRealPath("WEB-INF/classes/elkfed/webdemo")));
                doc.deleteAll();
            } else {
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Unrecognized URI</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Unrecognized Path</h1>");
                out.println("The path "+request.getServletPath()+extraPath+
                        " is not bound to anything. To get a friendly interface "+
                        "to BART, please go to <a href=\"/index.jsp\">/index.jsp</a>");
                if (!docs.isEmpty()) {
                    out.println("<h2>Documents in session</h2>");
                    for (MiniDiscourse doc1 : docs) {
                        out.format("<a href=\"/BARTDemo/ShowText/displayDoc/%s\">%s</a><br>",
                                doc1.getNameSpace(), stringForDocument(doc1));
                    }
                }
                out.println("</body>");
                out.println("</html>");
            }

        } catch (SAXException ex) {
            Logger.getLogger(ShowText.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(ShowText.class.getName()).log(Level.SEVERE, null, ex);
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

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
