/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.db;

import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.exist.EXistException;
import org.exist.dom.persistent.LockedDocument;
import org.exist.dom.persistent.NodeProxy;
import org.exist.security.PermissionDeniedException;
import org.exist.security.shiro.realm.RealmUtil;
import org.exist.security.shiro.realm.internal.ExistShiroInternalAccountAdapter;
import org.exist.stax.IEmbeddedXMLStreamReader;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.lock.Lock;
import org.exist.storage.txn.Txn;
import org.exist.xmldb.XmldbURI;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Optional;

public class DbRealm extends org.apache.shiro.realm.AuthenticatingRealm {

    public static final String REALM_NAME = "existdb.db";

    private static final String ACCOUNTS_COLLECTION_NAME = "accounts";
    private static final String GROUPS_COLLECTION_NAME = "groups";

    private final BrokerPool brokerPool;
    private final XmldbURI realmCollectionUri;
    private final XmldbURI accountsCollectionUri;
    private final XmldbURI groupsCollectionUri;
    private final ExistShiroInternalAccountAdapter systemAccount;  // TODO(AR) this should not be a class from the `internal` realm

    public DbRealm(final BrokerPool brokerPool, final XmldbURI realmCollectionUri, final ExistShiroInternalAccountAdapter systemAccount) {
        super();
        setName(REALM_NAME);

        this.brokerPool = brokerPool;
        this.realmCollectionUri = realmCollectionUri;
        this.accountsCollectionUri = realmCollectionUri.append(ACCOUNTS_COLLECTION_NAME);
        this.groupsCollectionUri = realmCollectionUri.append(GROUPS_COLLECTION_NAME);
        this.systemAccount = systemAccount;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws AuthenticationException {
        if (token == null) {
            throw new org.apache.shiro.authc.AuthenticationException("Provided token is null");
        }

        DbAccount account = null;
        if (token instanceof UsernamePasswordToken) {

            // TODO(AR) how can we cache these lookups to reduce database lookups of data which hasn't changed - Shiro has a Caching Realm
            // try and lookup the account in the database
            account = loadAccountFromDb(((UsernamePasswordToken)token).getUsername());
        } else {
            throw new UnsupportedTokenException("The DbRealm does not support tokens of type: " + token.getClass().getName());
        }

        if (account != null){
            if (account.isLocked()) {
                throw new LockedAccountException("Account [" + account + "] is locked.");
            }

            if (account.isCredentialsExpired()) {
                throw new ExpiredCredentialsException("The credentials for account [" + account + "] are expired");
            }
        }

        return account;
    }

    private @Nullable DbAccount loadAccountFromDb(final String username) throws AuthenticationException {
        try (final DBBroker broker = brokerPool.get(Optional.of(systemAccount));
                final Txn transaction = brokerPool.getTransactionManager().beginTransaction()) {

            try (final LockedDocument lockedDocument = broker.getXMLResource(accountsCollectionUri.append(username + ".xml"), Lock.LockMode.READ_LOCK)) {
                if (lockedDocument == null) {
                    // user document
                    return null;
                }

                try (final IEmbeddedXMLStreamReader streamReader = broker.newXMLStreamReader(new NodeProxy(lockedDocument.getDocument()), true)) {

                    // TODO(AR) reuse various JAXB classes (check docs) for performance or create a pool
                    final JAXBContext jaxbContext = JAXBContext.newInstance(
                            org.exist.security.shiro.realm.db.beans.Account.class,
                            org.exist.security.shiro.realm.db.beans.GroupReference.class,
                            org.exist.security.shiro.realm.db.beans.Metadata.class);
                    final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    final JAXBElement<org.exist.security.shiro.realm.db.beans.Account> accountElement =
                            unmarshaller.unmarshal(streamReader, org.exist.security.shiro.realm.db.beans.Account.class);


                    transaction.commit();

                    return new DbAccount(accountElement.getValue());
                }
            }

        } catch (final EXistException | PermissionDeniedException | IOException | XMLStreamException | JAXBException e) {
            throw new AuthenticationException("Unable to load account [" + username + "] from database: " + e.getMessage(), e);
        }
    }

    // TODO(AR) move this somewhere common
    static String getUsernameAtRealmPrincipal(final String username) {
        return RealmUtil.getUsernameAtRealmPrincipal(username, REALM_NAME);
    }
}
