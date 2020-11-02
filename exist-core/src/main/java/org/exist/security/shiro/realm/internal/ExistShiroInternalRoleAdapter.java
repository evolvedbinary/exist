/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.exist.Database;
import org.exist.config.Configuration;
import org.exist.config.ConfigurationException;
import org.exist.security.*;
import org.exist.security.realm.Realm;
import org.exist.storage.DBBroker;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ExistShiroInternalRoleAdapter implements Group {

    private final Database database;
    private final DefaultSecurityManager shiroSecurityManager;
    private final InternalRole internalRole;

    public ExistShiroInternalRoleAdapter(final Database database, final DefaultSecurityManager shiroSecurityManager, final InternalRole internalRole) {
        this.database = database;
        this.shiroSecurityManager = shiroSecurityManager;
        this.internalRole = internalRole;
    }

    @Override
    public boolean isManager(final Account account) {
        // TODO(AR) how should we model group managers with Shiro? Permissions or additional roles?
        if (account.hasDbaRole()) {
            return true;
        }
        return account.getPrimaryGroup().equals(internalRole.getRoleName());
    }

    @Override
    public void addManager(final Account account) throws PermissionDeniedException {
        // TODO(AR) implement
        throw new UnsupportedOperationException("TODO(AR) NOT YET IMPLEMENTED");
    }

    @Override
    public void addManagers(final List<Account> managers) throws PermissionDeniedException {
        // TODO(AR) implement
        throw new UnsupportedOperationException("TODO(AR) NOT YET IMPLEMENTED");
    }

    @Override
    public List<Account> getManagers() throws PermissionDeniedException {
        return null;
    }

    @Override
    public void removeManager(Account account) throws PermissionDeniedException {
        // TODO(AR) implement
        throw new UnsupportedOperationException("TODO(AR) NOT YET IMPLEMENTED");
    }

    @Override
    public void assertCanModifyGroup(Account account) throws PermissionDeniedException {
        // TODO(AR) implement
        throw new UnsupportedOperationException("TODO(AR) NOT YET IMPLEMENTED");
    }

    @Override
    public int getId() {
        return internalRole.getId();
    }

    @Override
    public Realm getRealm() {
        final Collection<org.apache.shiro.realm.Realm> realms = shiroSecurityManager.getRealms();
        for (final org.apache.shiro.realm.Realm realm : realms) {
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalRealmAdapter(database, shiroSecurityManager, (InternalRealm) realm);
            }
        }
        return null;
    }

    @Override
    public String getRealmId() {
        return InternalRealm.REALM_NAME;
    }

    @Override
    public void save() throws ConfigurationException, PermissionDeniedException {
        // TODO(AR) implement
    }

    @Override
    public void save(DBBroker broker) throws ConfigurationException, PermissionDeniedException {
        // TODO(AR) implement
    }

    @Override
    public void setMetadataValue(SchemaType schemaType, String value) {
        // TODO(AR) implement
    }

    @Override
    public String getMetadataValue(SchemaType schemaType) {
        // TODO(AR) implement
        return null;
    }

    @Override
    public Set<SchemaType> getMetadataKeys() {
        // TODO(AR) implement
        return null;
    }

    @Override
    public void clearMetadata() {
        // TODO(AR) implement
    }

    @Override
    public String getName() {
        return internalRole.getRoleName();
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof Group)) {
            return false;
        }

        // TODO(AR) this could likely be improved
        final Group other = (Group) obj;
        return getRealmId().equals(other.getRealmId())
                && getName().equals(other.getName());
    }
}
