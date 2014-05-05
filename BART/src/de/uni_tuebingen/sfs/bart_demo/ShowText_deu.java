/*
 * Copyright 2008 Yannick Versley / Univ. Tuebingen
 * Copyright 2009 Yannick Versley / CiMeC Univ. Trento
 * Copyright 2011 Yannick Versley / Univ. Tuebingen
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
package de.uni_tuebingen.sfs.bart_demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.cscott.jutil.DisjointSet;

import org.maltparser.core.exception.MaltChainedException;
import org.xml.sax.SAXException;

import webcorp.tokens.StupidTokenizer;
import webcorp.tokens.Token;
import elkfed.coref.CorefResolver;
import elkfed.coref.mentions.Mention;
import elkfed.coref.mentions.MentionFactory;
import elkfed.coref.util.Clustering;
import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.minidisc.Exporter;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableHelper;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.tabular.BIOColumn;
import elkfed.mmax.tabular.Column;
import elkfed.mmax.tabular.SentenceColumn;
import elkfed.mmax.tabular.TabularExport;
import elkfed.mmax.tabular.TagColumn;
import elkfed.parse.malt.MALTWrapper;
import elkfed.util.ShellCommand;

import static elkfed.util.Strings.sanitize_unicode;

/**
 * 
 * @author yannick
 */
public class ShowText_deu extends HttpServlet {
	public static String URL_PREFIX = "/BARTDemo/";
	
	private final static String konn_root;
	private final static String semeval_root;
	
	static {
		String scripts_dir=System.getProperty("elkfed.scripts_root");
		if (scripts_dir==null) {
			konn_root= "/home/yannickv/proj/konnektor";
			semeval_root="/home/yannickv/proj/semeval/converter";
		} else {
			konn_root=scripts_dir+"/scripts";
			semeval_root=konn_root;
		}
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

	public static MiniDiscourse findDocument(List<MiniDiscourse> docs,
			String docId) {
		for (MiniDiscourse doc1 : docs) {
			if (doc1.getNameSpace().equals(docId)) {
				return doc1;
			}
		}
		return null;
	}

	public static void displayNavigation(MiniDiscourse doc, PrintWriter out) {
		out.println("<div class=\"menu\">");
		for (String s : new String[] { "semclass", "mention_type", "markables" }) {
		    out.format(
			       "<a class=\"naviButton\" href=\"#\" onClick=\"renderDoc('%s','%s')\">%s</a> - ",
			       doc.getNameSpace(), s, s);
		}
		out.format(
			   "<a class=\"naviButton\" href=\"#\" onClick=\"renderCoref('%s')\">coreference</a><br>",
			   doc.getNameSpace());

		out.println("</div>");
	}

	public static void displayDocument(MiniDiscourse doc, PrintWriter out,
			File resourceDir, String fmt) throws SAXException,
			TransformerConfigurationException {
		displayNavigation(doc, out);
		StreamResult streamResult = new StreamResult(out);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
				.newInstance();
		StreamSource xsl_source = new StreamSource();
		if (fmt==null) fmt="markables";
		xsl_source.setSystemId(new File(resourceDir, "generic_de_"+fmt+".xsl"));
		TransformerHandler hd = tf.newTransformerHandler(xsl_source);
		// TransformerHandler hd = tf.newTransformerHandler(new
		// StreamSource("generic.xsl"));
		hd.setResult(streamResult);
		String[] wanted_levels = new String[] { "section", "sentence", "markable", "pos"};
		Exporter ex = new Exporter(Arrays.asList(wanted_levels));
		ex.convertFile(doc, hd);
	}

	public static void displayCoref(MiniDiscourse doc, PrintWriter out,
			File resourceDir) throws SAXException,
			TransformerConfigurationException {
		displayNavigation(doc, out);
		StreamResult streamResult = new StreamResult(out);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
				.newInstance();
		StreamSource xsl_source = new StreamSource();
		xsl_source.setSystemId(new File(resourceDir, "generic_lang.xsl"));
		TransformerHandler hd = tf.newTransformerHandler(xsl_source);
		// TransformerHandler hd = tf.newTransformerHandler(new
		// StreamSource("generic.xsl"));
		hd.setResult(streamResult);
		String[] wanted_levels = new String[] { "response", "markable" };
		Exporter ex = new Exporter(Arrays.asList(wanted_levels));
		ex.convertFile(doc, hd);
		out.println("<h4>Coreference chain</h4>");
		out.println("<div class=\"minidisc\" id=\"coref-chain\"></div>");
	}

	public static void exportCoref(MiniDiscourse doc, PrintWriter out,
			File resourceDir) throws SAXException,
			TransformerConfigurationException {
		StreamResult streamResult = new StreamResult(out);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
				.newInstance();
		StreamSource xsl_source = new StreamSource();
		xsl_source.setSystemId(new File(resourceDir, "export.xsl"));
		TransformerHandler hd = tf.newTransformerHandler(xsl_source);
		hd.setResult(streamResult);
		String[] wanted_levels = new String[] { "sentence", "response", "pos" };
		Exporter ex = new Exporter(Arrays.asList(wanted_levels));
		ex.convertFile(doc, hd);
	}

	public static void populateDocsArray(List<MiniDiscourse> docs) {
		// do nothing
	}

	protected void resolve_coref(MiniDiscourse doc1) throws IOException {
		CorefResolver cr = (CorefResolver) getServletContext().getAttribute(
				"resolver");
		MentionFactory mfact = (MentionFactory) getServletContext()
				.getAttribute("mfact");
		DiscourseUtils.deleteResponses(doc1);
		Map<Mention, Mention> antecedents = new HashMap<Mention, Mention>();
		List<Mention> mentions = mfact.extractMentions(doc1);
		DisjointSet<Mention> partition = cr.decodeDocument(mentions,
				antecedents);
		Clustering.addClustersToMMAX(partition, antecedents, doc1);
	}
	

	protected String slurpPostData(HttpServletRequest request,
			String defaultEncoding) throws IOException {
		ServletInputStream inputStream = request.getInputStream();
		InputStreamReader reader;
		String encoding = request.getHeader("Encoding");
		if (encoding == null) {
			String ctype = request.getHeader("Content-type");
			if (ctype != null && ctype.contains("charset=")) {
				String rest = ctype.substring(ctype.indexOf("charset=") + 8);
				if (rest.contains(";")) {
					rest = rest.substring(0, rest.indexOf(';'));
				}
				encoding = rest;
			}
		}
		if (encoding == null) {
			encoding = defaultEncoding;
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
			throws IOException {
		return slurpPostData(request, "UTF-8");
	}

	protected MiniDiscourse importDocument(String input_text) {
		return importDocument(input_text, false);
	}
	
	protected boolean do_parsing(String basename) throws IOException {
		long before_prepare = System.currentTimeMillis();
		int retval;
		try {
			retval = ShellCommand.runShellCommand("python", konn_root
					+ "/malt_wrapper.py", "prepare", basename + ".txt");
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		if (retval != 0) {
			Logger.getLogger(ShowText_deu.class.getName()).log(
					Level.SEVERE,
					"preprocess returned non-zero status:" + retval);
			return false;
		}
		long after_textpro = System.currentTimeMillis();
		Logger.getLogger(ShowText_deu.class.getName()).log(
				Level.INFO,
				String.format("Prepare took %d ms", after_textpro
						- before_prepare));
		long before_malt = System.currentTimeMillis();
		MALTWrapper malt = (MALTWrapper) getServletContext().getAttribute(
				"malt");
		try {
			malt.parseFile(basename + ".txt.conll-in", basename + ".conll",
					"ISO-8859-15");
		} catch (MaltChainedException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		long after_malt = System.currentTimeMillis();
		Logger.getLogger(ShowText_deu.class.getName()).log(
				Level.INFO,
				String.format("MALTParser took %d ms", after_malt
						- before_malt));
		return true;
	}


	protected MiniDiscourse importDocument(String input_text, boolean is_textpro) {
		File mmaxDir = new File(getServletContext()
				.getInitParameter("docs_dir"));
		String docId = UUID.randomUUID().toString();
		String basename = new File(mmaxDir, "parsing" + File.separator + docId)
				.getAbsolutePath();
		try {
			Process p;
			int retval;
			long before_tokenize = System.currentTimeMillis();
			StupidTokenizer tok = (StupidTokenizer) getServletContext()
					.getAttribute("tok");
			List<Token> toks = tok.tokenize(input_text, 0);
			Writer wr = new OutputStreamWriter(new FileOutputStream(basename
					+ ".txt"), Charset.forName("ISO-8859-15"));
			boolean in_sent = false;
			for (int i = 0; i < toks.size(); i++) {
				String w = toks.get(i).value;
				if (toks.get(i).isSentStart() && in_sent) {
					wr.write("\n");
				}
				wr.write(w);
				wr.write("\n");
				in_sent = true;
			}
			wr.close();
			
			retval = ShellCommand.runShellCommand("python", konn_root
					+ "/malt_wrapper.py", "prepare", basename + ".txt");
			if (retval != 0) {
				Logger.getLogger(ShowText_deu.class.getName()).log(
						Level.SEVERE,
						"preprocess returned non-zero status:" + retval);
				return null;
			}
			long after_textpro = System.currentTimeMillis();
			Logger.getLogger(ShowText_deu.class.getName()).log(
					Level.INFO,
					String.format("Tokenize+Prepare took %d ms", after_textpro
							- before_tokenize));
			long before_malt = System.currentTimeMillis();
			MALTWrapper malt = (MALTWrapper) getServletContext().getAttribute(
					"malt");
			malt.parseFile(basename + ".txt.conll-in", basename + ".conll",
					"ISO-8859-15");
			long after_malt = System.currentTimeMillis();
			Logger.getLogger(ShowText_deu.class.getName()).log(
					Level.INFO,
					String.format("MALTParser took %d ms", after_malt
							- before_malt));

			retval = ShellCommand.runShellCommand("python", 
					semeval_root+"/conll2mmax.py", mmaxDir.getAbsolutePath(),
					docId);
			if (retval != 0) {
				Logger.getLogger(ShowText_deu.class.getName()).log(
						Level.SEVERE,
						"preprocess returned non-zero status:" + retval);
			}
			MiniDiscourse doc = MiniDiscourse.load(mmaxDir, docId);
			long after_import = System.currentTimeMillis();
			Logger.getLogger(ShowText_deu.class.getName()).log(
					Level.INFO,
					String.format("Import (whole) took %d ms", after_import
							- before_tokenize));
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
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */
	@SuppressWarnings("unchecked")
	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		ServletOutputStream out_stream=response.getOutputStream();
		PrintWriter out = new PrintWriter(out_stream);
		List<MiniDiscourse> docs = (List<MiniDiscourse>) request.getSession()
				.getAttribute("docs");
		if (docs == null) {
			docs = new ArrayList<MiniDiscourse>();
			populateDocsArray(docs);
			request.getSession().setAttribute("docs", docs);
		}
		String extraPath = request.getPathInfo();
		try {
			if (extraPath.startsWith("/addDoc")) {
				String inputText = slurpPostData(request);
				MiniDiscourse doc = importDocument(inputText);
				docs.add(doc);
				displayDocument(doc, out, new File(getServletContext()
						.getRealPath("WEB-INF/classes/elkfed/webdemo")), null);
			} else if (extraPath.startsWith("/listDocs")) {
				for (MiniDiscourse doc : docs) {
					out.format(
									"<a href=\"#\" onClick=\"renderDoc('%s','markables')\">%s</a><br>",
									doc.getNameSpace(), stringForDocument(doc));
				}
			} else if (extraPath.startsWith("/renderDoc")) {
				String docId = request.getParameter("docId");
				String fmt = request.getParameter("fmt");
				MiniDiscourse doc = findDocument(docs, docId);
				displayDocument(doc, out, new File(getServletContext()
						.getRealPath("WEB-INF/classes/elkfed/webdemo")), fmt);
			} else if (extraPath.startsWith("/renderCoref")) {
				String docId = request.getParameter("docId");
				MiniDiscourse doc = findDocument(docs, docId);
				MarkableLevel resp = doc.getMarkableLevelByName("response");
				if (resp.getMarkables().size() == 0) {
					resolve_coref(doc);
				}
				displayCoref(doc, out, new File(getServletContext()
						.getRealPath("WEB-INF/classes/elkfed/webdemo")));
			} else if (extraPath != null && extraPath.startsWith("/process/")) {
				String[] args = extraPath.substring(9).split("/");
				MiniDiscourse doc;
				if (args.length > 0 && args[0].equals("tab")) {
					String inputText = slurpPostData(request, "ISO-8859-15");
					doc = importDocument(inputText, true);
				} else {
					String inputText = slurpPostData(request);
					doc = importDocument(inputText, false);
				}
				resolve_coref(doc);
				if (args.length > 1 && args[1].equals("tab")) {
					exportTabular(doc, out);
				} else {
					exportCoref(doc, out, new File(getServletContext()
							.getRealPath("WEB-INF/classes/elkfed/webdemo")));
				}
			} else {
				out.println("<html>");
				out.println("<head>");
				out.println("<title>Servlet ShowText</title>");
				out
						.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/BARTDemo/styles.css\">");
				out
						.println("<script type=\"text/javascript\" src=\"/BARTDemo/functions.js\"></script>");
				out.println("</head>");
				out.println("<body onLoad=\"prepare_nodes()\">");
				out.println("<h1>Show Text</h1>");
				// out.println("extraPath="+extraPath+"<br>");
				String input_text = request.getParameter("text");
				if (input_text != null) {
					out.format("input text=<pre>%s</pre>", input_text);
					MiniDiscourse doc;
					doc = importDocument(input_text);
					// MiniDiscourse doc1=MiniDiscourse.load(new
					// File("/space/versley/Elkfed/OntoNotes-MMAX/train/"),
					// "wsj_1056");
					docs.add(doc);
				} else if (extraPath != null
						&& extraPath.startsWith("/displayDoc/")) {
					String docId = extraPath.substring(12);
					MiniDiscourse doc1 = findDocument(docs, docId);
					if (doc1 == null) {
						out.println(String.format(
								"(displayDoc: document %s not found)<br>",
								docId));
					} else {
						displayDocument(doc1, out,
								new File(getServletContext().getRealPath(
										"WEB-INF/classes/elkfed/webdemo")),
								null);
					}
				} else if (extraPath != null
						&& extraPath.startsWith("/corefDoc/")) {
					String docId = extraPath.substring(10);
					MiniDiscourse doc1 = findDocument(docs, docId);
					resolve_coref(doc1);
					displayCoref(doc1, out, new File(getServletContext()
							.getRealPath("WEB-INF/classes/elkfed/webdemo")));
				}
				out.println("<h2>Documents in session</h2>");
				for (MiniDiscourse doc1 : docs) {
					out
							.format(
									"<a href=\"/BARTDemo/ShowText/displayDoc/%s\">%s</a><br>",
									doc1.getNameSpace(),
									stringForDocument(doc1));
				}
				out.println("</body>");
				out.println("</html>");
			}

		} catch (SAXException ex) {
			Logger.getLogger(ShowText_deu.class.getName()).log(Level.SEVERE,
					null, ex);
		} catch (TransformerConfigurationException ex) {
			Logger.getLogger(ShowText_deu.class.getName()).log(Level.SEVERE,
					null, ex);
		} finally {
			out.close();
		}
	}

	// <editor-fold defaultstate="collapsed"
	// desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void exportTabular(MiniDiscourse doc, PrintWriter out)
			throws IOException {
		out.println("# FILE: " + doc.getNameSpace());
		out.println("# FIELDS: token\ttokenstart\ttokenend\t"
				+ "sentence\tpos\tlemma\tentity\tcoref");
		List<Column> cols = new ArrayList<Column>();
		cols.add(new SentenceColumn("sentence", 3));
		cols.add(new TagColumn("pos", "tag", 4));
		cols.add(new TagColumn("lemma", "tag", 5));
		cols.add(new BIOColumn("entity", "tag", 6));
		cols.add(new BIOColumn("response", "coref_set", 7));
		TabularExport te = new TabularExport(0, cols, 1);
		te.do_export(doc, out);
	}
	// </editor-fold>
}
