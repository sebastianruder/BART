/*
 *   Copyright 2009-2011
 *   Yannick Versley / CiMeC Univ. Trento / Univ. Tuebingen
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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.jasper.servlet.JspServlet;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;

import webcorp.tokens.StupidTokenizer;
import elkfed.config.ConfigProperties;
import elkfed.coref.CorefResolver;
import elkfed.main.XMLAnnotator;
import elkfed.main.xml.CorefExperimentDocument;
import elkfed.mmax.tabular.BIOColumn;
import elkfed.mmax.tabular.Column;
import elkfed.mmax.tabular.TabularImport;
import elkfed.mmax.tabular.TagColumn;
import elkfed.parse.malt.MALTWrapper;

/** runs BART's web demo/REST webservice for German
 * in an embedded Jetty web server
 * @author yannick
 */
public class BARTServer_deu {

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

    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
        try {
            Server server = new Server(8125);
            HandlerCollection handlers = new HandlerCollection();
            server.setHandler(handlers);
            RequestLogHandler requestLogHandler = new RequestLogHandler();
            NCSARequestLog requestLog = new NCSARequestLog("bart_server.log");
            requestLogHandler.setRequestLog(requestLog);
            WebAppContext root = new WebAppContext("webdemo_jsp", "/");
            ShowText_deu servlet = new ShowText_deu();
            MALTWrapper malt=new MALTWrapper("tiger_conf.mco");
            TabularImport ti;
            List<Column> columns=new ArrayList<Column>();
            columns.add(new TagColumn("pos","tag",2));
            columns.add(new BIOColumn("enamex","tag",4));
            ti=new TabularImport(0,columns);

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
            StupidTokenizer tok=new StupidTokenizer("de");
            ctx.setAttribute("malt",malt);
            ctx.setAttribute("tok", tok);
            ctx.setAttribute("tab_import", ti);
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
