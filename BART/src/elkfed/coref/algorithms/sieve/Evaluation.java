package elkfed.coref.algorithms.sieve;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.mentions.Mention;

public class Evaluation {
	
	private List<Mention> mentions;
	public Map<Mention, Mention>  antecedents;
	private Map<Mention, String> sieves;
	
	public Evaluation(List<Mention> mentions) {
		this.mentions = mentions;
		this.antecedents = new HashMap<>();
		this.sieves = new HashMap<>();
	}
	public void setLink(Mention m, Mention ante, String sieve) {
		antecedents.put(m, ante);
		sieves.put(m, sieve);
	}
	
	public void printMention(Mention m) {
		
		System.out.println("Heads: " + m.getHeadString());
		System.out.println("Words: " + m.getDiscourseEntity().getWordsString());
		System.out.println("SetID: " + m.getSetID());
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
				System.out.println(String.format("No Antecedent for '%s'",
												 m.getMarkable().toString()));				
				printMention(m);
				
			}
		}
	}
	
	
}
