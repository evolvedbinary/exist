package xyz.elemental.xquery.analyzer;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import xyz.elemental.xquery.options.CaseOption;
import xyz.elemental.xquery.options.DiacriticsOption;
import xyz.elemental.xquery.options.MatchOptions;
import xyz.elemental.xquery.options.StemOption;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;



public class ExistFtSearchAnalyzerTest {

    @Test
    public void stemming() throws IOException {
        var options = MatchOptions.defaultMatchOptions();
        options.setStemOption(StemOption.STEMMING);
        ExistFtSearchAnalyzer analyzer = new ExistFtSearchAnalyzer(options);
        var tokenStream = analyzer.tokenStream("fieldName", new StringReader("text bikes mo\u0161t"));
        tokenStream.reset();
        assertThat(getTokens(tokenStream)).containsExactly("text", "bike", "most");
    }

    @Test
    public void noStemming() throws IOException {
        var options = MatchOptions.defaultMatchOptions();
        options.setStemOption(StemOption.NO_STEMMING);
        ExistFtSearchAnalyzer analyzer = new ExistFtSearchAnalyzer(options);
        var tokenStream = analyzer.tokenStream("fieldName", new StringReader("text bikes mo\u0161t"));
        tokenStream.reset();
        assertThat(getTokens(tokenStream)).containsExactly("text", "bikes", "most");
    }

    @Test
    public void defaultStemming() throws IOException {
        var options = MatchOptions.defaultMatchOptions();
        ExistFtSearchAnalyzer analyzer = new ExistFtSearchAnalyzer(options);
        var tokenStream = analyzer.tokenStream("fieldName", new StringReader("text bikes mo\u0161t"));
        tokenStream.reset();
        assertThat(getTokens(tokenStream)).containsExactly("text", "bikes", "most");
    }

    @Test
    public void diacriticsSensitive() throws IOException {
        var options = MatchOptions.defaultMatchOptions();
        options.setDiacriticsOption(DiacriticsOption.SENSITIVE);
        ExistFtSearchAnalyzer analyzer = new ExistFtSearchAnalyzer(options);
        var tokenStream = analyzer.tokenStream("fieldName", new StringReader("text bikes mo\u0161t krc\u030cma"));
        tokenStream.reset();
        assertThat(getTokens(tokenStream)).containsExactly("text", "bikes", "mo\u0161t", "kr\u010Dma");
    }

    @Test
    public void diacriticsInsensitive() throws IOException {
        var options = MatchOptions.defaultMatchOptions();
        options.setDiacriticsOption(DiacriticsOption.INSENSITIVE);
        ExistFtSearchAnalyzer analyzer = new ExistFtSearchAnalyzer(options);
        var tokenStream = analyzer.tokenStream("fieldName", new StringReader("text bikes mo\u0161t kr\u010Dma"));
        tokenStream.reset();
        assertThat(getTokens(tokenStream)).containsExactly("text", "bikes", "most", "krcma");
    }

    @Test
    public void diacriticsDefaults() throws IOException {
        var options = MatchOptions.defaultMatchOptions();
        ExistFtSearchAnalyzer analyzer = new ExistFtSearchAnalyzer(options);
        var tokenStream = analyzer.tokenStream("fieldName", new StringReader("text bikes mo\u0161t krc\u030Cma"));
        tokenStream.reset();
        assertThat(getTokens(tokenStream)).containsExactly("text", "bikes", "most", "krcma");
    }


    @Test
    public void caseSensitive() throws IOException {
        var options = MatchOptions.defaultMatchOptions();
        options.setCaseOption(CaseOption.CASE_SENSITIVE);
        ExistFtSearchAnalyzer analyzer = new ExistFtSearchAnalyzer(options);
        var tokenStream = analyzer.tokenStream("fieldName", new StringReader("Text bIkes mo\u0160t krc\u030CMa"));
        tokenStream.reset();
        assertThat(getTokens(tokenStream)).containsExactly("Text", "bIkes", "moSt", "krcMa");
    }

    @Test
    public void caseInsensitive() throws IOException {
        var options = MatchOptions.defaultMatchOptions();
        options.setCaseOption(CaseOption.CASE_INSENSITIVE);
        ExistFtSearchAnalyzer analyzer = new ExistFtSearchAnalyzer(options);
        var tokenStream = analyzer.tokenStream("fieldName", new StringReader("Text bIkes mo\u0160t krc\u030CMa"));
        tokenStream.reset();
        assertThat(getTokens(tokenStream)).containsExactly("text", "bikes", "most", "krcma");
    }

    @Test
    public void caseDefault() throws IOException {
        var options = MatchOptions.defaultMatchOptions();
        ExistFtSearchAnalyzer analyzer = new ExistFtSearchAnalyzer(options);
        var tokenStream = analyzer.tokenStream("fieldName", new StringReader("Text bIkes mo\u0160t krc\u030CMa"));
        tokenStream.reset();
        assertThat(getTokens(tokenStream)).containsExactly("text", "bikes", "most", "krcma");
    }


    public void printTokenStream(TokenStream tokenStream) {
        var tokens = getTokens(tokenStream);
        System.out.println(String.join(",", tokens));
        for(String s: tokens) {
            for(char c: s.toCharArray()) {
                System.out.print('\'');
                System.out.print(c);
                System.out.print("' ");
            }
            System.out.println(", ");
        }
    }

    public String[] getTokens(TokenStream tokenStream) {
        List<String> tokens = new ArrayList<>();
        try {
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            while (tokenStream.incrementToken()) {
                tokens.add(charTermAttribute.toString());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return tokens.toArray(new String[0]);
    }



}
