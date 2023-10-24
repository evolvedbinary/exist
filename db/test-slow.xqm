xquery version "3.1";

import module namespace ccu = "https://ns.eidohealthcare.com/existdb/craft-connector-util" at "xmldb:exist:///db/EIDO/data/edit/craft-connector-util.xqm";

declare variable $docnum := "A02";

declare variable $docname-components := map {
    "source": $ccu:source.craft-connector,
    "code": $docnum,
    "language": "en",
    "library": "uk",
    "view": "full",
    "version": "2.0",
    "version-identifiers": [2, 0],
    "docnum": "procedure.A02_en_uk_full_2.0"
};

declare %private function local:clone-document($document as element(document)?, $new-docnum as xs:string) as element(document)? {
  $document ! <document docnum="{$new-docnum}" subscribed="{./@subscribed}" visible="{./@visible}" phrasechange="{./@phrasechange}">
                  {
                    ./*
                  }
              </document>
};

(:~
 : Given a document element extract the version identifiers from its docnum attribute.
 :
 : @parm a document element.
 :
 : @return the version identifiers if present, else an empty sequence.
 :)
declare %private function local:document-version-identifiers($document as element(document)) as array(xs:integer)? {
    $document/@docnum ! ccu:extract-components-from-docname(.)?version-identifiers
};

let $start := util:system-time() return

<result>
    <start>{$start}</start>
    <count>{
count(
    for $customer in collection("/db/EIDO/data/Customers")
                      //customer[empty(details/template/@type)]
                    
                    (: NOTE(AR) - example of slow query, should in theory use the range index (but does not force it through explicit `range:contains`) :)
                    
                    /portal[exists(specialty/document[@subscribed eq 'Y' and ccu:comparable($docname-components, ccu:extract-components-from-docname(@docnum))])]
                    
                    (: this is spectacularly fast with legacy format, but doesn't work with CC format
                    /portal[exists(specialty/document[@docnum eq $docnum and @subscribed eq 'Y'])]
                    :)
                    
                    (: NOTE(AR) - alternative formulation of above - multiple predicates instead of 'and'. Tries to see if there is a difference between using one predicate with 'and' compared to using two predicates  :)
                    (:
                    /portal[exists(specialty/document[ccu:comparable($docname-components, ccu:extract-components-from-docname(@docnum))][@subscribed eq 'Y'])]
                    :)
                    
                    (: NOTE(AR) - examples same as above but without the @subscribed to show performance difference :)
                    (:
                    /portal[exists(specialty/document[ccu:comparable($docname-components, ccu:extract-components-from-docname(@docnum)) (:  and @subscribed eq 'Y' :) ])]
                    :)
                    
                    (: NOTE(AR) - example where we try to force the range index use through explicit `range:contains` :)
                    (:)
                    /portal[exists(specialty/document[range:contains(@docnum, $docnum)][ccu:comparable($docname-components, ccu:extract-components-from-docname(@docnum))][@subscribed eq 'Y']  )]
                    :)
                
    return
        $customer
)
    }</count>
    {
        let $end := util:system-time() return (
            <end>{$end}</end>,
            <elapsed>{$end - $start}</elapsed>
        )
    }
</result>

