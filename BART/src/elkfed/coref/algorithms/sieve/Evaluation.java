package elkfed.coref.algorithms.sieve;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elkfed.config.ConfigProperties;
import elkfed.coref.mentions.Mention;
import elkfed.lang.LanguagePlugin;

/**
 * The Evaluation class
 * 
 * @author Julian, Sebastian, Xenia
 */
public class Evaluation {
	
	protected static final LanguagePlugin langPlugin = ConfigProperties
			.getInstance().getLanguagePlugin();
	private PrintWriter writer; // writer writing the log file
	private List<Mention> mentions; // a list of all mentions
	public Map<Mention, Mention>  antecedents;
	private Map<Mention, String> sieves;
	
	private static Map<String, Integer> correctLinksPerSieve = new HashMap<String, Integer>();
	private static Map<String, Integer> linksPerSieve = new HashMap<String, Integer>();
	
	/**
	 * The constructor of the Evaluation class.
	 * 
	 * @param mentions a list of mentions
	 */
	public Evaluation(List<Mention> mentions) {
		this.mentions = mentions;
		this.antecedents = new HashMap<>();
		this.sieves = new HashMap<>();
	}
	
	/**
	 * 
	 * @param m
	 * @param ante
	 * @param sieve
	 */
	public void setLink(Mention m, Mention ante, String sieve) {
		antecedents.put(m, ante);
		sieves.put(m, sieve);
		linksPerSieve.put(sieve, (linksPerSieve.containsKey(sieve) ? linksPerSieve.get(sieve): 0) + 1);
		if (m.isCoreferent(ante)) {
			correctLinksPerSieve.put(sieve, (correctLinksPerSieve.containsKey(sieve) ? correctLinksPerSieve.get(sieve): 0) + 1);
		}
	}
	
	/**
	 * Prints and writes to a file different features of a mention, such
	 * as its string, id, head lemma, head string, discourse entity, set
	 * id and discourse entity id for evaluation purposes.
	 * 
	 * @param m the mention
	 * @param isAntecedent true or false
	 * @throws IOException if file can't be written
	 */
	public void printMention(Mention m, boolean isAntecedent) throws IOException {
		if (isAntecedent) {
			System.out.print("ANTECEDENT: ");
			writer.print("ANTECEDENT: ");
		}
		else {
			System.out.print("MENTION: ");
			writer.print("MENTION: ");
		}
		String string_to_print = String.format(
				"%s (%s)\nHeadLemma: %s\nHead: %s\nWords: %s\nSetID: %s\nDeID: %s\nHeadGF: %s\n",
				m.getMarkable().toString(),
				m.getMarkable().getID(),
				m.getHeadLemma(),
				m.getHeadString(),
				m.getDiscourseEntity().getWordsString(),
				m.getSetID(),
				m.getDiscourseEntity().getID(),
				langPlugin.getHeadGF(m));
		printAndWrite(string_to_print);
		//Markable markable = m.getMarkable();
		//MiniDiscourse doc = markable.getMarkableLevel().getDocument();
        //MarkableLevel lemmas = doc.getMarkableLevelByName("lemma");
	}
	
	/**
	 * Prints evaluation information, i.e. if a match is correct or false and if
	 * no match for a mention is found, its antecedent if it has one
	 */
	public void printEvaluation() {
		// writer appends to file; file should be cleared manually if wanted or new file can be created
		// Sebastian file path: "D:/BART/BART/src/elkfed/coref/algorithms/sieve/log/mmax-100.log"
		// Julian filePath: "/home/julian/git/BART/BART/src/elkfed/coref/algorithms/sieve/log/mmax-100.log"
		try {
			File log_dir = new File(ConfigProperties.getInstance().getRoot(), "logs");
		    writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(log_dir, ConfigProperties.getInstance().getTestDataId() + ".log"), true)));
		} catch (IOException ex) {
		  System.err.println("IOException!");
		}
		String document = mentions.get(0).getDocument().getNameSpace();
		
		writer.println(String.format("Document:  %s", document));
		
		for(Mention m: mentions) {
			String correct_match = "FALSE";
			System.out.print(String.format("%s: ", m.getMarkable().getID()));
			
			if (antecedents.containsKey(m)) {
				if (m.isCoreferent(antecedents.get(m))) {
					correct_match = "TRUE";	
				}
				String string_to_print = String.format("%s! Antecedent of '%s': '%s'(%s) with %s", 
						 correct_match,
						 m.getMarkable().toString(),	
						 antecedents.get(m).getMarkable().toString(),
						 antecedents.get(m).getMarkable().getID(),
						 sieves.get(m));
				try {
					printAndWrite(string_to_print);
					printMention(m, false); // false to print "MENTION: "
					printMention(antecedents.get(m), true); // true to print "ANTECEDENT: "
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				if (!(m.getSetID() == null)) {
					System.out.print("SetID error! ");
				}
				Mention correct_antecedent = null;
				for (Mention antecedent : mentions) {
					if (antecedent == m) {
						correct_match = "TRUE";
						break;
					}
					else if (m.isCoreferent(antecedent)) {
						correct_antecedent = antecedent;
						break;
					}
				}
				String string_to_print = String.format("%s! No Antecedent for '%s'",
												 correct_match, m.getMarkable().toString());
				try {
					printAndWrite(string_to_print);
					printMention(m, false);
					if (correct_antecedent != null) {
						printMention(correct_antecedent, true);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		writer.println("");
		writer.close();
	}
	
	/**
	 * One function to both write a string to a file and print it to stdout
	 * @param s the string to be written
	 */
	public void printAndWrite(String s) {
		System.out.println(s);
		writer.println(s);
	}
	
	/**
	 * Prints the performance of all sieves, i.e. the amount of links generated
	 * and the amount of correct links
	 */
	public static void printSievePerformance() {
		System.out.println("Sieve\tlinksPerSieve\tcorrectLinksPerSieve\tPrecision");
		for (String sieve: linksPerSieve.keySet()) {
			System.out.println(String.format("%s\t%d\t%d\t%.3f", sieve, linksPerSieve.get(sieve),
														correctLinksPerSieve.get(sieve),
														(double)correctLinksPerSieve.get(sieve)/
														(double)linksPerSieve.get(sieve)));
		}
	}	
}
