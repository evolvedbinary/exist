(:
 : eXist-db Open Source Native XML Database
 : Copyright (C) 2001 The eXist-db Authors
 :
 : info@exist-db.org
 : http://www.exist-db.org
 :
 : This library is free software; you can redistribute it and/or
 : modify it under the terms of the GNU Lesser General Public
 : License as published by the Free Software Foundation; either
 : version 2.1 of the License, or (at your option) any later version.
 :
 : This library is distributed in the hope that it will be useful,
 : but WITHOUT ANY WARRANTY; without even the implied warranty of
 : MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 : Lesser General Public License for more details.
 :
 : You should have received a copy of the GNU Lesser General Public
 : License along with this library; if not, write to the Free Software
 : Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 :)
xquery version "3.1";

module namespace npt="http://exist-db.org/test/nested-positional-predicate";

declare namespace test="http://exist-db.org/xquery/xqsuite";

declare variable $npt:TEST_COLLECTION_NAME := "test-positional-nested-doc";

declare variable $npt:DATA :=
    document {
        <xml>
            <a>
                <b>A</b>
            </a>
            <a>
                <b>B</b>
                <c>correct</c>
            </a>
            <a>
                <b>B</b>
                <c>wrong</c>
            </a>
        </xml>
    };

declare
    %test:setUp
function npt:setup() {
    xmldb:create-collection("/db", "test"),
    xmldb:store("/db/test", "test.xml", $npt:DATA)
};

declare
    %test:tearDown
function npt:cleanup() {
    xmldb:remove("/db/test")
};

declare
    %test:assertEquals("<c>correct</c>")
function npt:in-memory() {
    $npt:DATA//c[../preceding-sibling::a[1]/b = 'A']
};

declare
    %test:assertEquals("<c>correct</c>")
function npt:in-database() {
    doc("/db/test.xml")//c[../preceding-sibling::a[1]/b = 'A']
};