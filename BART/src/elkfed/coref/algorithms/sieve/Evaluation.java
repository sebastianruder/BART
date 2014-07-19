package elkfed.coref.algorithms.sieve;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.mentions.Mention;
import elkfed.mmax.MarkableLevels;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;

public class Evaluation {
	
	private PrintWriter writer;
	private List<Mention> mentions;
	public Map<Mention, Mention>  antecedents;
	private Map<Mention, String> sieves;
	
	public static Map<String, Integer> correctLinksPerSieve = new HashMap<String, Integer>();
	public static Map<String, Integer> linksPerSieve = new HashMap<String, Integer>();
	
	
	
	public Evaluation(List<Mention> mentions) {
		this.mentions = mentions;
		this.antecedents = new HashMap<>();
		this.sieves = new HashMap<>();
	}
	public void setLink(Mention m, Mention ante, String sieve) {
		antecedents.put(m, ante);
		sieves.put(m, sieve);
		linksPerSieve.put(sieve, (linksPerSieve.containsKey(sieve) ? linksPerSieve.get(sieve): 0) + 1);
		if (m.isCoreferent(ante)) {
			correctLinksPerSieve.put(sieve, (correctLinksPerSieve.containsKey(sieve) ? correctLinksPerSieve.get(sieve): 0) + 1);
		}
	}
	
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
				"%s (%s)\nHeadLemma: %s\nHead: %s\nWords: %s\nSetID: %s\nDeID: %s\n",
				m.getMarkable().toString(),
				m.getMarkable().getID(),
				m.getHeadLemma(),
				m.getHeadString(),
				m.getDiscourseEntity().getWordsString(),
				m.getSetID(),
				m.getDiscourseEntity().getID());
		printAndWrite(string_to_print);
		Markable markable = m.getMarkable();
		MiniDiscourse doc = markable.getMarkableLevel().getDocument();
        MarkableLevel lemmas = doc.getMarkableLevelByName("lemma");
	}
	
	public void printEvaluation() {
		
		// writer appends to file; file should be cleared manually if wanted or new file can be created
		// Sebastian file path: "D:/BART/BART/src/elkfed/coref/algorithms/sieve/log/mmax-100.log"
		// Julian filePath: "/home/julian/git/BART/BART/src/elkfed/coref/algorithms/sieve/log/mmax-100.log"
		try {
		    writer = new PrintWriter(new BufferedWriter(new FileWriter("/home/julian/git/BART/BART/src/elkfed/coref/algorithms/sieve/log/mmax-100.log", true)));
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
	
	public void printAndWrite(String s) {
		System.out.println(s);
		writer.println(s);
	}
	
	public static void printSievePerformance() {
		System.out.println("Sieve\tlinksPerSieve\tcorrectLinksPerSieve");
		for (String sieve: linksPerSieve.keySet()) {
			System.out.println(String.format("%s\t%d\t%d", sieve, linksPerSieve.get(sieve),
														correctLinksPerSieve.get(sieve)));
		}
	}	
}
