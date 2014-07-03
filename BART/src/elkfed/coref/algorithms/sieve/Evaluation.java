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
		sieves.put(m,  sieve);
		
	}
	
	public void printMention(Mention m) {
		//hier könnt ihr Attribute ausgeben lassen
	}
	
	public void printDiscourseEntity(DiscourseEntity de) {
		//hier könnt ihr Attribute ausgeben lassen
	}
	
	public void printEvaluation() {
		for(Mention m: mentions) {
			
			if (antecedents.containsKey(m)) {
				
				System.out.println(String.format("Antecedent of %s: %s with %s\n", 
												 m.toString(),
												 antecedents.get(m).toString(),
												 sieves.get(m)));
				printMention(m);
				printMention(antecedents.get(m));
				
				
			} else {				
				System.out.println(String.format("No Antecedent for %s\n",
												 m.toString()));				
				printMention(m);
				
			}
		}
	}
	
	
}
