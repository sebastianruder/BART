/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elkfed.webdemo;

import java.io.File;
import java.io.FileInputStream;

import javax.servlet.ServletContext;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.jasper.servlet.JspServlet;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefResolver;
import elkfed.main.XMLAnnotator;
import elkfed.main.xml.CorefExperimentDocument;

/** runs BART's web demo in an embedded Jetty web server
 *
 * @author yannick
 */
public class BARTServer {

    public static CorefResolver make_resolver(String[] args) throws Exception {
        CorefExperimentDocument doc;
        if (args.length == 0) {
            doc = CorefExperimentDocument.Factory.parse(
                    ClassLoader.getSystemResourceAsStream("elkfed/main/" +
                    ConfigProperties.getInstance().getDefaultSystem() + ".xml"));
        } else {
            doc = CorefExperimentDocument.Factory.parse(
                    new FileInputStream(args[0]));
        }
        return XMLAnnotator.createResolver(doc.getCorefExperiment());
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(8125);
            HandlerCollection handlers = new HandlerCollection();
            server.setHandler(handlers);
            RequestLogHandler requestLogHandler = new RequestLogHandler();
            NCSARequestLog requestLog = new NCSARequestLog("bart_server.log");
            requestLogHandler.setRequestLog(requestLog);
            WebAppContext root = new WebAppContext("webdemo_jsp", "/");
            ShowText servlet = new ShowText();
            File bartRoot = ConfigProperties.getInstance().getRoot();
            Tokenizer tokenizer = new TokenizerME(
                    new TokenizerModel(new File(bartRoot,
                    "models/opennlp/EnglishTok.bin.gz")));
            root.getInitParams().put("docs_dir", "webdemo_temp");
            ServletHolder holder = new ServletHolder(servlet);
            JspServlet jsp = new JspServlet();
            ServletHolder jsp_holder = new ServletHolder(jsp);
            ResourceHandler resource_handler = new ResourceHandler();
            resource_handler.setResourceBase("./webdemo_css");
            Log.info("serving " + resource_handler.getBaseResource());
            root.addServlet(holder, "/BARTDemo/ShowText/*");
            root.addServlet(jsp_holder, "*.jsp");
            handlers.addHandler(resource_handler);
            handlers.addHandler(root);
            handlers.addHandler(requestLogHandler);
            server.start();
            ServletContext ctx=servlet.getServletContext();
            ctx.setAttribute("tokenizer", tokenizer);
            ctx.setAttribute("pipeline",
                    ConfigProperties.getInstance().getPipeline());
            ctx.setAttribute("mfact",
                    ConfigProperties.getInstance().getMentionFactory());
            ctx.setAttribute("resolver", make_resolver(args));
            server.join();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
