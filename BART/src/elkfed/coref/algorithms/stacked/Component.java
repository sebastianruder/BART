package elkfed.coref.algorithms.stacked;

import elkfed.ml.FeatureDescription;

public enum Component {
    RANK_PRONOUN(true,0,
        CandidateFilter.PRONOUN,3),
    RANK_SAME_HEAD(true,1,
        CandidateFilter.SAME_HEAD,3),
    RANK_BRIDGING(true,2,
        CandidateFilter.BRIDGING,2),
    CFY_PRONOUN(false,0),
    CFY_APPOSITION(false,1,
        CandidateFilter.APPOSITION,0),
    CFY_SAME_HEAD(false,2,
        CandidateFilter.SAME_HEAD,0),
    CFY_BRIDGING(false,3);
    public static final int num_rankers=3;
    public static final FeatureDescription[][] EMPTY_COMBO =
    {};

    public final boolean is_ranker;
    public final int pos;
    public CandidateFilter filter;
    public final int shortlist_size;
    Component(boolean r, int p, CandidateFilter f, int s) {
        is_ranker=r; pos=p; filter=f; shortlist_size=s; }
    Component(boolean r, int p) {
        is_ranker=r; pos=p; shortlist_size=0; }
}