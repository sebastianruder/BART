package webcorp.tokens;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.AutomatonProvider;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

public class StupidTokenizer {
	private static final AutomatonProvider provider = new DatatypesAutomatonProvider();

	private static RunAutomaton compileAutomaton(String s) {
		System.err.println("compileAutomaton:" + s);
		Automaton a = new RegExp(s).toAutomaton(provider);
		return new RunAutomaton(a);
	}

	public static final RunAutomaton tok_re;
	public static final RunAutomaton num_re;
	public static final Pattern pre_ord_re = Pattern.compile(
			"de[rmns]|das|die|im|zum|am|beim|(sein|ihr|jed|dies)e[mnrs]?",
			Pattern.CASE_INSENSITIVE);
	public static final Pattern konj_re = Pattern.compile("und|oder|bis");
	public static final RunAutomaton ord_noun_re = compileAutomaton("Auflage|Bericht|Domain|Entwurf|Etage|Etappe|"
			+ "Meisterschaft|Generation|Gipfel|Jahr(es)?|Jahrhunderts?|"
			+ "Kammer|Kl|Klasse|Kongreß|Konferenz|Liga|^Mal|Minute|Mitarbeiter|"
			+ "Mitglied|OG|Obergeschoss|Order|Platz|"
			+ "Quartal|Programm|Rang|Runde|Semester|"
			+ "Senat|Sinfonie|Sitzung|Stelle|"
			+ "Stock|Stockwerk|Strafkammer|Studie|Symphonie|"
			+ "Tag|Tagung|Update|Veranstaltung|"
			+ "Verordnung|Version|Vertrag|Wettbewerb|Zivilkammer|"
			+ "Berliner|Bremer|Hamburger|"
			+ "Internationale|Internationalen|Deutschen|"
			+ "Januar|Februar|März|April|Mai|Juni|Juli|"
			+ "August|September|Oktober|November|Dezember|[0-9]+");
	public static final RunAutomaton abk_re = compileAutomaton("[A-Za-z]|Jan|Feb|Mar|Apr|Jun|Jul|Aug|Oct|Nov|Dec|"
			+ "Mo|Di|Mi|Do|Fr|Sa|"
			+ "<L>+(str|korr|fg|b[rg])|"
			+ ".+/[mM]in|Abs|Anm|AZ|Az|B/s|Bd|[Bb]etr|BGBl|bzw|"
			+ "Chr|Co|Corp|Dipl|Dr|Engl|[Hh]rsg|II|Inc|Jh(dt)?|[Jj]r|Ltd|Kl|Max|"
			+ "Mill|Min|Mio|Mr|Mrd|Mw[Ss]t|Nr|Par|Pf|[Pp]hil|Pol|Proc|Prof|Red|"
			+ "[Ss]ch|St|Std|.*Str|[Tt]el|[Tt]h|[Tt]heol|Ver|[Vv]gl|Vol|Zi|[Zz]it|"
			+ "[A-ZÄÖÜ]\\.-[A-ZÄÖÜ]|al|bspw?|bzw|ca|co|div|entspr|etc|evtl|excl|"
			+ "exkl|<Nd>*ff|ggf|incl|inkl|jr|lt|max|min|mind|op|resp|soc|sog|usf|usw|"
			+ "vs|wg|www|zzgl");
	public static final RunAutomaton lc_name_re = compileAutomaton("taz|dpa|1822direkt|3dfx|adidas|h2g2|heise|i-mode|id|t@x|tesion|"
			+ "mediantis|debitel|speedlink");
	static {
		// prefixes such as 1000-, 16:9-, 2/3-, "Hochzeits"-, (Hochzeits-), 1.-
		// ABER: nicht 75/Carl-
		String prefix_re = "(<L>|<Nd>)+(\\.|[:/])?((<L>|[0-9])+)?-|\\((<L>|<Nd>)+-\\)|\\\"(<L>|<Nd>)+\\\"-";
		// 16jähriger, 100mal, -mal
		String word_re = String
				.format(
						"(<L>\\.(<L>\\.)+|((<L>|dell|nell)')?(%s)*<L>+(-|/Innen])?|<Nd>+-?<Lu>?<Ll>+-?|[']<Ll>+)",
						prefix_re);
		String number_s = "(<Nd>+(\\.<Nd>{3})*(,<Nd>+)?|II|III|IV|V|X+V?I*)";
		// Telefonnummern, Uhrzeiten
		String numeric_s = "<Nd>+(:<Nd>+)+|0<Nd>{2,}/<Nd>+(-<Nd>+)*|<Nd>{3,}-(<Nd>+-)?(0|<Nd>{3,})|[12]?[0-9][:\\.](15|[02-6][05])(,[0-9]{2})?";
		// U-235, Q2
		String xy_re = String
				.format(
						"<Lu>+-?(%s|%s)|www\\.<L>+\\.<L>{2,4}|http://(<L>|<Nd>|[/%%])+",
						number_s, numeric_s);
		String token_re = String.format("%s|%s|%s|%s|\\.{2,}|<P>", xy_re,
				word_re, number_s, numeric_s);
		tok_re = compileAutomaton(token_re);
		num_re = compileAutomaton(number_s);
	}

	public static final Pattern sent_re = Pattern.compile("\\.|!|\\?");
	public static final Pattern maybe_sent_re = Pattern.compile(":");

	public static final int WORD = 0;
	public static final int NUMBER = 1;
	public static final int ABBREV = 2;

	public StupidTokenizer(String lang) {

	}

	public int classifyToken(Token tok) {
		if (num_re.run(tok.value)) {
			return NUMBER;
		}
		if (abk_re.run(tok.value)) {
			return ABBREV;
		} else {
			return WORD;
		}
	}

	private boolean isLower(String s) {
		if (Character.isLowerCase(s.charAt(0))) {
			if (lc_name_re.run(s)) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	/*
	 * if result[pos-1] is a number and result[pos] is a dot, is
	 * result[pos-1:pos+1] an ordinal number?
	 */
	private boolean plausibleOrdinal(int pos, List<Token> result) {
		String s_post;
		if (pos < result.size() - 1) {
			s_post = result.get(pos + 1).value;
		} else {
			s_post = "*END*";
		}
		String s_pre;
		if (pos >= 2) {
			s_pre = result.get(pos - 2).value;
		} else {
			s_pre = "*BEGIN*";
		}
		if (isLower(s_post) || "?!:,/".contains(s_post)
				|| ord_noun_re.run(s_post)) {
			return true;
		} else if (pre_ord_re.matcher(s_pre).matches()) {
			return true;
		}
		return false;
	}

	private boolean plausibleAbbrev(int pos, List<Token> result) {
		String s_post;
		if (pos < result.size() - 1) {
			s_post = result.get(pos + 1).value;
			// System.out.println("post:"+s_post);
		} else {
			s_post = "*END*";
		}
		if (isLower(s_post)) {
			return true;
		}
		return true; // false;
	}

	/*
	 * needed: - attach clitic 's and ' to proper names (McDonald's, Disney's
	 * but not geht's hat's) - attach - to truncated items (-los, -bohrung) but
	 * not to non-truncated ones (-hat, -wird)?
	 */
	public List<Token> tokenize(String input, int offset) {
		List<Token> result = new ArrayList<Token>();
		AutomatonMatcher m = tok_re.newMatcher(input);
		while (m.find()) {
			Token tok = new Token();
			tok.value = m.group(0);
			if (!tok_re.run(tok.value)) {
				System.err.println("Invalid token:" + tok.value);
			}
			// tok.wsp_after = m.group(2);
			tok.start = m.start() + offset;
			tok.end = m.end() + offset;
			result.add(tok);
		}
		// no tokens? no problem.
		if (result.size() == 0)
			return result;
		// Step 1: join abbreviations
		Token tok = result.get(0);
		for (int i = 1; i < result.size(); i++) {
			Token tokNext = result.get(i);
			boolean attach = false;
			if (tok.end == tokNext.start && ".".equals(tokNext.value)) {
				int cls = classifyToken(tok);
				// System.out.format("Classify: %s => %s\n",tok.value,cls);
				switch (cls) {
				case ABBREV:
					attach = plausibleAbbrev(i, result);
					break;
				case NUMBER:
					attach = plausibleOrdinal(i, result);
					break;
				}
			}
			if (attach) {
				tok.value = tok.value + tokNext.value;
				tok.end = tokNext.end;
				tok.wsp_after = tokNext.wsp_after;
				result.remove(i);
				i--;
			} else {
				tok = tokNext;
			}
		}
		// Step 2: detect sentence boundaries
		for (int i = 0; i < result.size() - 1; i++) {
			System.out.println("SBnd: looking at "+result.get(i).value);
			if (sent_re.matcher(result.get(i).value).matches()) {
				result.get(i + 1).flags |= 4;
			} else if (maybe_sent_re.matcher(result.get(i).value).matches()
					&& !isLower(result.get(i + 1).value)) {
				result.get(i + 1).flags |= 4;
			}
		}
		// Step 3: reattach some clitics (Hans', Disney's)
		tok = result.get(0);
		for (int i = 1; i < result.size(); i++) {
			Token tokNext = result.get(i);
			if (tok.end == tokNext.start && tokNext.value.charAt(0) == '\'') {
				boolean attach = false;
				if (tokNext.value.length() == 1) {
					char c = tok.value.charAt(tok.value.length() - 1);
					if (c == 'b' || c == 't' || c == 's') {
						attach = true;
					}
				} else if (tokNext.value.length() == 2
						&& (Character.toLowerCase(tokNext.value.charAt(1)) == 's')) {
					attach = Character.isUpperCase(tok.value.charAt(0));
				}
				// System.err.format("%s|%s\n", tok.value,tokNext.value);
				if (attach) {
					tok.value = tok.value + tokNext.value;
					tok.end = tokNext.end;
					tok.wsp_after = tokNext.wsp_after;
					result.remove(i);
					i--;
				} else {
					tok = tokNext;
				}
			} else {
				tok = tokNext;
			}
		}
		// Step 4: reattach some separated dash-compounds
		// (Telekomwimpel-|schwingenden)
		tok = result.get(0);
		for (int i = 1; i < result.size(); i++) {
			Token tokNext = result.get(i);
			if (tok.value.length() > 3 && tok.value.endsWith("-")
					&& Character.isLetter(tokNext.value.charAt(0))
					&& !konj_re.matcher(tokNext.value).matches()) {
				tok.value = tok.value + tokNext.value;
				tok.end = tokNext.end;
				tok.wsp_after = tokNext.wsp_after;
				result.remove(i);
				i--;
			} else {
				tok = tokNext;
			}
		}
		return result;
	}

	static private String[] test_tokens = { "(Musik-)Geschichte",
			"Anti-Abtreibungs-Gesetz", "öffentlich-rechtlichen" };

	public static void run_test() {
		for (String s : test_tokens) {
			AutomatonMatcher m = tok_re.newMatcher(s);
			boolean f = m.find();
			if (!f) {
				System.err.println("Not recognized: " + s);
				continue;
			}
			if (m.start() != 0 || m.end() != s.length()) {
				System.err.format("Recognized %s, wanted %s\n", m.group(), s);
				continue;
			}
		}
	}

	public static void main(String[] args) {
		StupidTokenizer tok = new StupidTokenizer("de");
		run_test();
		System.out.println(sent_re.matcher(".").matches());
		List<Token> toks = tok
				.tokenize(
						"Peter: Nach dem 12. 1000-Meter-Lauf, sagte die 16jährige, wäre alles- (naja, fast alles) mit dem \"Grenzenlos\"-Modell OK gewesen, mit ca. 12 Metern/Sek. Wissen's, das ist Charly's Traum, und Jonas'...",
						0);
		for (Token s : toks) {
			System.out.println(s.value);
		}
		toks=tok.tokenize("Das ist Peter. Peter mag Erdbeereis. Bananen mag er nicht.", 0);
		System.out.println(toks.get(3).toString()+toks.get(4).flags);
		System.out.println(toks.get(7).toString()+toks.get(8).flags);
	}
}
