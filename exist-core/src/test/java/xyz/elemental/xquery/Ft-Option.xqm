xquery version "3.1";

module namespace ftsearch="http://exist-db.org/xquery/test/ft-option";
declare namespace test="http://exist-db.org/xquery/xqsuite";

declare ft-option  using wildcards using diacritics sensitive;
declare ft-option  using stemming;

declare
    %test:assertEquals('bikes', 'mošt')
function ftsearch:optionsTest() {

    for $w in ('bikes', 'mošt', 'HellO')
        where $w contains text 'mo.t'ftor 'bike'
        return $w
};