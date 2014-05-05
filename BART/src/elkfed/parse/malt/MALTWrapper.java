/*
 * Wrapper around MALTParser based on MaltExamples code.
 *
 * MaltExamples copyright and license:
 *   Copyright (c) 2007-2008 Johan Hall, Jens Nilsson and Joakim Nivre
 *
 *   Redistribution and use in source and binary forms,
 *   with or without modification, are permitted provided
 *   that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of MaltExamples nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Wrapper Code and (eventual) bugs:
 *   Copyright 2009 Yannick Versley / CiMeC Univ. Trento
 */
package elkfed.parse.malt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.maltparser.Engine;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.Util;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.io.dataformat.DataFormatSpecification;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.trie.TrieSymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

import elkfed.config.ConfigProperties;

/**
 * provides a wrapper to MALTParser which allows us to run it repeatedly without
 * starting a JVM, loading the SVM models etc.
 * 
 * This is based on Examples 5 and 7 from Johan Hall's MaltExamples.
 */
public class MALTWrapper {

	protected MaltParserService service;

	public MALTWrapper(String modelName) {
		try {
			// change to models directory - MALTParser's system of .mco files
			// makes this necessary since it uses relative paths to access them
			String oldDir = System.getProperty("user.dir");
			try {
				service = new MaltParserService();
				System.setProperty("user.dir", new File(ConfigProperties
						.getInstance().getRoot(), "models/parser")
						.getAbsolutePath());
				service.initializeParserModel(String.format(
						"-c %s -m parse -lfi /tmp/malt.log", modelName));
			} finally {
				System.setProperty("user.dir", oldDir);
			}
		} catch (MaltChainedException ex) {
			throw new RuntimeException("Cannot initialize MALTParser", ex);
		}
	}

	public void parseFile(String inFile, String outFile, String charset)
			throws IOException, MaltChainedException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(inFile), charset));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outFile), charset));
		String line = null;
		ArrayList<String> lines = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() == 0) {
				DependencyStructure graph = service.parse(lines
						.toArray(new String[lines.size()]));
				for (int i = 1; i <= graph.getHighestDependencyNodeIndex(); i++) {
					DependencyNode node = graph.getDependencyNode(i);
					if (node != null) {
						for (SymbolTable table : node.getLabelTypes()) {
							writer.write(node.getLabelSymbol(table) + "\t");
						}
						if (node.hasHead()) {
							Edge e = node.getHeadEdge();
							writer.write(e.getSource().getIndex() + "\t");
							if (e.isLabeled()) {
								for (SymbolTable table : e.getLabelTypes()) {
									writer
											.write(e.getLabelSymbol(table)
													+ "\t");
								}
							} else {
								for (SymbolTable table : graph
										.getDefaultRootEdgeLabels().keySet()) {
									writer
											.write(graph
													.getDefaultRootEdgeLabelSymbol(table)
													+ "\t");
								}
							}
						}
						writer.write('\n');
						writer.flush();
					}
				}
				writer.write('\n');
				writer.flush();
				System.out.print(".");
				lines.clear();
			} else {
				lines.add(line);
			}
		}
		reader.close();
		writer.flush();
		writer.close();
		System.out.println();
	}

	public void terminate() throws MaltChainedException {
		String oldDir = System.getProperty("user.dir");
		try {
			service.terminateParserModel();
		} finally {
			System.setProperty("user.dir", oldDir);
		}
	}

	public static void main(String[] args) {
		try {
			MALTWrapper malt = new MALTWrapper(args[0]);
			for (int i = 1; i < args.length; i += 2) {
				malt.parseFile(args[i], args[i + 1], "ISO-8859-15");
			}
			malt.terminate();
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		} catch (MaltChainedException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
}
