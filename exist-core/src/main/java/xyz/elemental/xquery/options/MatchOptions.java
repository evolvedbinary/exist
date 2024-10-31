package xyz.elemental.xquery.options;

import java.util.Locale;
import java.util.Objects;

public class MatchOptions {

    private Locale language = Locale.ENGLISH;
    private WildcardOption wildcardOption;
    private ThesaurusOption thesaurusOption;
    private StemOption stemOption;
    private CaseOption caseOption;
    private DiacriticsOption diacriticsOption;
    private StopWordOption stopWordOption;

    public MatchOptions() {
    }

    public static MatchOptions defaultMatchOptions() {
        var n = new MatchOptions();
        n.setCaseOption(CaseOption.CASE_INSENSITIVE);
        n.setDiacriticsOption(DiacriticsOption.INSENSITIVE);
        n.setStemOption(StemOption.NO_STEMMING);
        // TODO - no thesaurus n.setThesaurusOption();
        //TODO - no stop words n.setStopWordOption();
        n.setLanguage(Locale.ENGLISH);// Implementation defined
        n.setWildcardOption(WildcardOption.NO_WILDCARDS);
        return n;
    }

    public MatchOptions copy() {
        var n = new MatchOptions();
        n.setLanguage(language);
        n.setWildcardOption(wildcardOption);
        n.setThesaurusOption(thesaurusOption);
        n.setStemOption(stemOption);
        n.setCaseOption(caseOption);
        n.setDiacriticsOption(diacriticsOption);
        n.setStopWordOption(stopWordOption);
        return n;
    }

    public Locale getLanguage() {
        return language;
    }

    public void setLanguage(Locale language) {
        this.language = language;
    }

    public WildcardOption getWildcardOption() {
        return wildcardOption;
    }

    public void setWildcardOption(WildcardOption wildcardOption) {
        this.wildcardOption = wildcardOption;
    }

    public ThesaurusOption getThesaurusOption() {
        return thesaurusOption;
    }

    public void setThesaurusOption(ThesaurusOption thesaurusOption) {
        this.thesaurusOption = thesaurusOption;
    }

    public StemOption getStemOption() {
        return stemOption;
    }

    public void setStemOption(StemOption stemOption) {
        this.stemOption = stemOption;
    }

    public CaseOption getCaseOption() {
        return caseOption;
    }

    public void setCaseOption(CaseOption caseOption) {
        this.caseOption = caseOption;
    }

    public DiacriticsOption getDiacriticsOption() {
        return diacriticsOption;
    }

    public void setDiacriticsOption(DiacriticsOption diacriticsOption) {
        this.diacriticsOption = diacriticsOption;
    }

    public StopWordOption getStopWordOption() {
        return stopWordOption;
    }

    public void setStopWordOption(StopWordOption stopWordOption) {
        this.stopWordOption = stopWordOption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchOptions that = (MatchOptions) o;
        return Objects.equals(language, that.language) && wildcardOption == that.wildcardOption && Objects.equals(thesaurusOption, that.thesaurusOption) && stemOption == that.stemOption && caseOption == that.caseOption && diacriticsOption == that.diacriticsOption && Objects.equals(stopWordOption, that.stopWordOption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(language, wildcardOption, thesaurusOption, stemOption, caseOption, diacriticsOption, stopWordOption);
    }
}
