package elkfed.coref.algorithms.sieve;

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
	
	public void printMention(Mention m) {
		System.out.println("HeadLemma: " +  m.getHeadLemma());
		System.out.println("Head: " + m.getHeadString() );
		System.out.println("Words: " + m.getDiscourseEntity().getWordsString());
		System.out.println("SetID: " + m.getSetID());
		System.out.println("DeID: " + m.getDiscourseEntity().getID());
		System.out.println("\n");
		Markable markable = m.getMarkable();
		MiniDiscourse doc = markable.getMarkableLevel().getDocument();
        MarkableLevel lemmas = doc.getMarkableLevelByName("lemma");

	}
	
	public void printDiscourseEntity(DiscourseEntity de) {
		//hier könnt ihr Attribute ausgeben lassen
	}
	
	public void printEvaluation() {
		for(Mention m: mentions) {
			
			System.out.print(String.format("%s: ", m.getMarkable().getID()));
			
			if (antecedents.containsKey(m)) {
				if (m.isCoreferent(antecedents.get(m))) {
					System.err.print("TRUE! ");
					
				}
				else {
					System.err.print("FALSE! ");
				}
				System.out.println(String.format("Antecedent of '%s': '%s'(%s) with %s", 
												 m.getMarkable().toString(),	
												 antecedents.get(m).getMarkable().toString(),
												 antecedents.get(m).getMarkable().getID(),
												 sieves.get(m)												
												 ));
				System.out.print("MENTION: ");
				printMention(m);
				System.out.print("ANTECEDENT: ");
				printMention(antecedents.get(m));
				// print postmodifiers
				
				
			} else {
				if (!(m.getSetID() == null)) {
					System.err.println("\nFalse");
				}
				System.out.println(String.format("No Antecedent for '%s'",
												 m.getMarkable().toString()));				
				
				printMention(m);
				
			}
		}
		

	}
	public static void printSievePerformance() {
		System.out.println("Sieve\tlinksPerSieve\tcorrectLinksPerSieve");
		for (String sieve: linksPerSieve.keySet()) {
			System.out.println(String.format("%s\t%d\t%d", sieve, linksPerSieve.get(sieve),
														correctLinksPerSieve.get(sieve)));
		}
	}
	
}
