(:
 : Copyright (C) 2024 Evolved Binary Ltd
 :
 : This code is proprietary and is not Open Source.
 :
:)

xquery version "3.1";

module namespace ftsearch="http://exist-db.org/xquery/test/ft-search";
declare namespace test="http://exist-db.org/xquery/xqsuite";
declare namespace exq = "http://example.org/XQueryImplementation";

declare
    %test:assertEquals("aa bb")
function ftsearch:simpleSearch() {
    for $w in ('aa bb', 'bbbb', 'ccccc')
        where $w contains text 'aa'
        return $w
};



declare
    %test:assertEquals("bb aa")
function ftsearch:listSearchContext() {
    for $w in ('bbbb', 'ccccc', 'cc bb', 'bb aa')
        where $w contains text 'aa'
        return $w
};

declare
    %test:assertEquals('bb aa, 0.13076457')
function ftsearch:scoreVariable() {
    for $w score $s in ('bbbb', 'ccccc', 'cc bb', 'bb aa')
        where $w contains text 'aa'
        return concat($w, ", ", $s)
};

declare
    %test:assertEquals('bb aa, 0.13076457')
function ftsearch:scoreVariableWithExpr() {
    for $w score $s in ('bbbb', 'ccccc', 'cc bb', 'bb aa')
        where $w contains text {'asdasd', 'asdasdad', fn:concat($w, 'aasdasdads'), 'aa'}
        return concat($w, ", ", $s)
};

declare
    %test:assertTrue
function ftsearch:listSearchContext2() {
    ('bbbb', 'ccccc', 'cc bb', 'bb aa') contains text 'bb'
};

declare
    %test:assertTrue
function ftsearch:ftOrElementInSequence() {
    ('bbbb', 'ccccc', 'cc bb', 'bb aa') contains text 'notInList' ftor "bbbb"
};

declare
    %test:assertTrue
function ftsearch:ftAndElementInSequence() {
    ('bbbb', 'ccccc', 'cc bb', 'bb aa') contains text 'cc' ftand 'bb'
};

declare
    %test:assertFalse
function ftsearch:ftAndElementNotInSequence() {
    ('bbbb', 'ccccc', 'cc bb', 'bb aa') contains text 'cc' ftand 'gg'
};

declare
    %test:assertEquals('cc bb, 0.13076457', 'bb aa, 0.13076457')
function ftsearch:ftOrTest() {
    for $w score $s in ('bbbb', 'ccccc', 'cc bb', 'bb aa')
        where $w contains text 'aa' ftor 'cc'
        return concat($w, ", ", $s)
};

declare
    %test:assertEquals("aa bb")
function ftsearch:ignorePragma() {
    for $w in ('aa bb', 'bbbb', 'ccccc')
        where $w contains text (# exq:use-index #) {'aa' any word}
        return $w
};