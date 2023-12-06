declare namespace sql = "http://exist-db.org/xquery/sql";
declare namespace util = "http://exist-db.org/xquery/util";

declare variable $account_id as element(sql:account_id) := <sql:account_id xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:sql="http://exist-db.org/xquery/sql" sql:type="INT" xs:type="xs:integer">668</sql:account_id>;
declare variable $wines as element(sql:result) := util:expand(doc("/db/adam.sql-wines.xml")/sql:result);  (: NOTE(AR) intentionally uses util:expand#1 to load the data into memory to replicate the approach of the SQL Module :)
declare variable $account-data as document-node(element(data))+ := collection("/db/adam.account-data");




(# exist:time logger-name=winequest logging-level=info log-message-prefix=OUTER #) {


<sql:result count="{$wines/@count}">{
    for $wine in $wines/sql:row
    
    (: formulation alternative 1 :)
(:    let $item := $account-data//item[wq:match(.,$wine)] (\: get custom fields :\):) (: This works and is more flexible, but is slow :)

    (: formulation alternative 2 :)
    (:  :let $item := $account-data//item[@record=$wine/sql:Record or @mdb_prod_id=$wine/sql:mdb_prod_id] (: get custom fields :) :)
    
    (: formulation alternative 3 :)
    let $item-r := $account-data//item[@record eq $wine/sql:Record] (: get custom fields :)
    let $item-p := $account-data//item[@mdb_prod_id eq $wine/sql:mdb_prod_id] (: get custom fields :)
    let $item := $item-r | $item-p
    
    (:  index checks - added by AP :)
    (:  :let $item- := $account-data//data[@account_id eq $account_id]//item[@record eq $wine/sql:Record] :)
    
    return 
        <sql:row>{(
            let $corp-item := $item[ancestor::data/@account_id eq $account_id]
            let $property-item := $item[ancestor::data/@org_id eq $wine/sql:org_id]
            (: AP - if neither item exists, we short-circuit the override of individual fields :)
            let $overridden-fields := if (($property-item,$corp-item)[1]) then
                for $field in $item/element()
                group by $field-name := $field/name()
                let $corp-field := $corp-item/*[name() eq $field-name]
                let $property-field := $property-item/*[name() eq $field-name]
                let $override-field := ($property-field, $corp-field)[1]
                return
                    if ($override-field) then 
                        element {$field-name} {(
                            attribute custom {},
                            $override-field/ancestor::data/@*,
                            $override-field/ancestor::data/fields/@address, 
                            if ($property-field and $corp-field) then attribute corp-value {$corp-field} else (),
                            $override-field/node()
                        )}
                    else ()
                else ()
            return
                $overridden-fields,
            $wine/*
        )}
        </sql:row>
    }</sql:result>



}

