(:
 : Copyright (C) 2024 Evolved Binary Ltd
 :
 : This code is proprietary and is not Open Source.
 :
:)

xquery version "3.1";

module namespace ftsearch="http://exist-db.org/xquery/test/ft-search";
declare namespace test="http://exist-db.org/xquery/xqsuite";

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
    for $w in ('bbbb', 'ccccc', ('cc bb', 'bb aa'))
        where $w contains text 'aa'
        return $w
};