package xyz.elemental.xquery.analyzer;

import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.Transliterator;
import com.ibm.icu.text.UnicodeFilter;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.icu.ICUFoldingFilter;
import org.apache.lucene.analysis.icu.ICUNormalizer2Filter;
import org.apache.lucene.analysis.icu.ICUTransformFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import xyz.elemental.xquery.options.CaseOption;
import xyz.elemental.xquery.options.DiacriticsOption;
import xyz.elemental.xquery.options.MatchOptions;
import xyz.elemental.xquery.options.StemOption;

public class ExistFtSearchAnalyzer extends Analyzer {

    private static final int MAX_TOKEN_LEN = 255;

    private MatchOptions matchOptions;

    public ExistFtSearchAnalyzer(MatchOptions matchOptions) {
        this.matchOptions = matchOptions;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        StandardTokenizer src = new StandardTokenizer();
        src.setMaxTokenLength(MAX_TOKEN_LEN);

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
}
