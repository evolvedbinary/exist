xquery version "3.1";

let $start := util:system-time() return
<result>
    <start>{$start}</start>
    <reindex>{xmldb:reindex('xmldb:exist:///db/EIDO/data/Customers')}</reindex>
    {
        let $end := util:system-time() return (
            <end>{$end}</end>,
            <elapsed>{$end - $start}</elapsed>
        )
    }
</result>