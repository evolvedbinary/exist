package xyz.elemental.xquery.analyzer;

import com.ibm.icu.text.Transliterator;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.icu.ICUTransformFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import xyz.elemental.xquery.options.*;

public class ExistFtSearchAnalyzer extends Analyzer {

    private static final int MAX_TOKEN_LEN = 255;

    private MatchOptions matchOptions;
    private final boolean queryTokenizer;

    public ExistFtSearchAnalyzer(MatchOptions matchOptions) {
        this.queryTokenizer = false;
        this.matchOptions = matchOptions;
    }

    /**
     * Create analyzer which is optimizer for query compilation.
     * For example when we use wildcard, we want to present wildcards in tokens.
     * @param matchOptions
     * @param queryAnalyzer
     */
    public ExistFtSearchAnalyzer(MatchOptions matchOptions, boolean queryAnalyzer) {
        this.queryTokenizer = queryAnalyzer;
        this.matchOptions = matchOptions;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        Tokenizer src = createTokenizer();

        TokenStream tok = src;
        tok = new EnglishPossessiveFilter(tok); //remove 's

        if(matchOptions.getCaseOption() == CaseOption.CASE_INSENSITIVE) {
            tok = new LowerCaseFilter(tok);
        }

        if (matchOptions.getStemOption() == StemOption.STEMMING) {
            if(matchOptions.getCaseOption() == CaseOption.CASE_SENSITIVE) {
                throw new RuntimeException("Stemming is not supported with 'case sensitive' option");
            }
            tok = new PorterStemFilter(tok);
        }

        var transliteratorRule = matchOptions.getDiacriticsOption() == DiacriticsOption.INSENSITIVE ?
                ":: NFD; :: [:Nonspacing Mark:] Remove; :: NFC ;" : //Diacritics
                ":: NFD; :: NFC ;"; // Only denormalize and then normalize.
                                    // I think that's probably the best behaviour.
                                    // https://unicode.org/reports/tr15/#Norm_Forms

            var transliterator = Transliterator.createFromRules("temp",
                    transliteratorRule
                    , Transliterator.FORWARD);
        tok = new ICUTransformFilter(tok, transliterator);

        return new TokenStreamComponents(r -> {
            src.setReader(r);
        },
                tok
        );
    }

    private Tokenizer createTokenizer() {
        if(queryTokenizer && matchOptions.getWildcardOption() == WildcardOption.WILDCARDS) {
            return new WhitespaceTokenizer();
        }else  {
            var tok = new StandardTokenizer();
            tok.setMaxTokenLength(MAX_TOKEN_LEN);
            return tok;
        }
    }
}
