/*
 * SplitLinkScorer.java
 *
 * Created on August 7, 2007, 9:35 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.coref.eval;

import elkfed.config.ConfigProperties;
import elkfed.coref.mentions.Mention;
import java.util.List;

/**
 *
 * @author yannick
 */
public class SplitLinkScorer implements LinkScorer {

    LinkScorer _tot_scorer = new SimpleLinkScorer("All Links");
    LinkScorer _pro_scorer = new SimpleLinkScorer("Pronouns");
    LinkScorer _it_scorer = new SimpleLinkScorer("- 'it'");
    LinkScorer _app_scorer = new SimpleLinkScorer("Appositions");
    LinkScorer _nom_scorer = new SimpleLinkScorer("Nominals");
    LinkScorer _nam_scorer = new SimpleLinkScorer("Names");

    public boolean isApposition(Mention m_i, Mention m_j) {
        if (m_i.getSentId() != m_j.getSentId()) {
            return false;
        }
        int delta = m_i.getStartWord() - m_j.getEndWord();
        if (delta >= 0 && delta <= 2 &&
                !(m_j.getPronoun() && delta > 1)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasApposition(List<Mention> mentions, int anaphor) {
        Mention m_i = mentions.get(anaphor);
        for (int k = 0; k < anaphor; k++) {
            Mention m_k = mentions.get(k);
            if (m_i.isCoreferent(m_k) &&
                    isApposition(m_i, m_k)) {
                return true;
            }
        }
        return false;
    }

    public void displayResults() {
        _pro_scorer.displayResults();
        _it_scorer.displayResults();
        _app_scorer.displayResults();
        _nom_scorer.displayResults();
        _nam_scorer.displayResults();
        _tot_scorer.displayResults();
    }

    public void displayResultsShort() {
        System.out.println("                                         Prec Recl F1");
        displayResultsShort(false);
        displayResultsShort(true);
    }

    public void displayResultsShort(boolean ignoring) {
        _pro_scorer.displayResultsShort(ignoring);
        _it_scorer.displayResultsShort(ignoring);
        _app_scorer.displayResultsShort(ignoring);
        _nom_scorer.displayResultsShort(ignoring);
        _nam_scorer.displayResultsShort(ignoring);
        _tot_scorer.displayResultsShort(ignoring);
    }

    public void scoreLink(List<Mention> mentions, int antecedent, int anaphor) {
        Mention m_i = mentions.get(anaphor);
        Mention m_j = mentions.get(antecedent);
        _tot_scorer.scoreLink(mentions, antecedent, anaphor);
        if (m_i.getPronoun()) {
            _pro_scorer.scoreLink(mentions, antecedent, anaphor);
            if (ConfigProperties.getInstance().getLanguagePlugin().isExpletiveWordForm(m_i.getMarkableString())) {
                _it_scorer.scoreLink(mentions, antecedent, anaphor);
            }
        } else if (isApposition(m_i, m_j)) {
            _app_scorer.scoreLink(mentions, antecedent, anaphor);
        } else if (hasApposition(mentions, anaphor) &&
                !m_i.isCoreferent(m_j)) {
            _app_scorer.scoreLink(mentions, antecedent, anaphor);
        } else if (m_i.getProperName()) {
            _nam_scorer.scoreLink(mentions, antecedent, anaphor);
        } else {
            _nom_scorer.scoreLink(mentions, antecedent, anaphor);
        }
    }

    public void scoreNonlink(List<Mention> mentions, int anaphor) {
        Mention m_i = mentions.get(anaphor);
        _tot_scorer.scoreNonlink(mentions, anaphor);
        if (m_i.getPronoun()) {
            _pro_scorer.scoreNonlink(mentions, anaphor);
            if (m_i.getMarkableString().equalsIgnoreCase("it")) {
                _it_scorer.scoreNonlink(mentions, anaphor);
            }
        } else if (hasApposition(mentions, anaphor)) {
            _app_scorer.scoreNonlink(mentions, anaphor);
        } else if (m_i.getProperName()) {
            _nam_scorer.scoreNonlink(mentions, anaphor);
        } else {
            _nom_scorer.scoreNonlink(mentions, anaphor);
        }
    }
}
