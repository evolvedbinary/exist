package xyz.elemental.xquery;


import org.apache.lucene.search.Query;
import xyz.elemental.xquery.options.MatchOptions;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FtQuery {

    private Query luceneQuery;

    private Set<MatchOptions> matchOptions = new HashSet();

    public Query getLuceneQuery() {
        return luceneQuery;
    }

    public void setLuceneQuery(Query luceneQuery) {
        this.luceneQuery = luceneQuery;
    }

    public Set<MatchOptions> getMatchOptions() {
        return matchOptions;
    }

    public void setMatchOptions(Set<MatchOptions> matchOptions) {
        this.matchOptions = matchOptions;
    }
}
