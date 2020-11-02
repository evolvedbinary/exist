/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.exist.security.shiro.realm.db.DbRealm;
import org.exist.security.shiro.realm.internal.ExistShiroInternalAccountAdapter;
import org.exist.security.shiro.realm.internal.InternalRealm;
import org.exist.storage.BrokerPool;
import org.exist.xmldb.XmldbURI;

import java.util.Arrays;

// TODO(AR) should be replaced with an implementation of DefaultSecurityManager that can load many realms dynamically at startup
public class ShiroSecurityManager extends DefaultSecurityManager {

    public ShiroSecurityManager(final BrokerPool brokerPool) {
        super();

        //TODO(AR) do we need to set the following somehow
        //setAuthenticationStrategy(FirstSuccessfulStrategy)

        final InternalRealm internalRealm = new InternalRealm();

        final DbRealm dbRealm = new DbRealm(brokerPool, XmldbURI.create("/db/system/security/exist"),
                new ExistShiroInternalAccountAdapter(brokerPool, this, internalRealm.getSystemAccount()));

        setRealms(Arrays.asList(
            internalRealm,
            dbRealm
        ));
    }
}
