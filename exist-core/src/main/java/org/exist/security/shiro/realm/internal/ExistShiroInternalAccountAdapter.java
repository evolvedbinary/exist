/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.exist.Database;
import org.exist.config.Configuration;
import org.exist.config.ConfigurationException;
import org.exist.security.*;
import org.exist.security.realm.Realm;
import org.exist.storage.DBBroker;

import java.util.Collection;
import java.util.Set;

//TODO(AR) this is a temporary bridge between the Shiro model and the eXist-db model
public class ExistShiroInternalAccountAdapter implements Subject {

    private final Database database;
    private final DefaultSecurityManager shiroSecurityManager;
    private final InternalAccount internalAccount;

    private final Session legacySession;
    private boolean authenticated;
    private int umask = Permission.DEFAULT_UMASK;

    public ExistShiroInternalAccountAdapter(final Database database, final DefaultSecurityManager shiroSecurityManager, final InternalAccount internalAccount) {
        this.database = database;
        this.shiroSecurityManager = shiroSecurityManager;
        this.internalAccount = internalAccount;
        this.legacySession = new Session(this);
    }

    @Override
    public boolean authenticate(final Object credentials) {
        this.authenticated =
                null != shiroSecurityManager.authenticate(
                        new UsernamePasswordToken(internalAccount.getPrincipals().getPrimaryPrincipal().toString(), credentials.toString()));

        return authenticated;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public boolean isExternallyAuthenticated() {
        return false;
    }

    @Override
    public String getSessionId() {
        return legacySession.getId();
    }

    @Override
    public Session getSession() {
        return legacySession;
    }

    @Override
    public void setPrimaryGroup(Group group) throws PermissionDeniedException {
        throw new PermissionDeniedException("NOT IMPLEMENTED");
    }

    @Override
    public void assertCanModifyAccount(Account user) throws PermissionDeniedException {
        throw new PermissionDeniedException("NOT IMPLEMENTED");
    }

    @Override
    public int getUserMask() {
        return umask;
    }

    @Override
    public void setUserMask(final int umask) {
        this.umask = umask;
    }

    @Override
    public Group addGroup(String group) throws PermissionDeniedException {
        throw new PermissionDeniedException("NOT IMPLEMENTED");
    }

    @Override
    public Group addGroup(Group group) throws PermissionDeniedException {
        throw new PermissionDeniedException("NOT IMPLEMENTED");
    }

    @Override
    public void remGroup(String group) throws PermissionDeniedException {
        throw new PermissionDeniedException("NOT IMPLEMENTED");
    }

    @Override
    public String[] getGroups() {
        final InternalRole[] roles = internalAccount.getInternalRoles();
        if (roles == null) {
            return new String[0];
        }

        final String[] groups = new String[roles.length];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = roles[i].getRoleName();
        }
        return groups;
    }

    @Override
    public int[] getGroupIds() {
        final InternalRole[] roles = internalAccount.getInternalRoles();
        if (roles == null) {
            return new int[0];
        }

        final int[] groupIds = new int[roles.length];
        for (int i = 0; i < groupIds.length; i++) {
            groupIds[i] = roles[i].getId();
        }
        return groupIds;
    }

    @Override
    public boolean hasDbaRole() {
        final InternalRole[] roles = internalAccount.getInternalRoles();
        if (roles == null) {
            return false;
        }

        for (int i = 0; i < roles.length; i++) {
            if (roles[i] instanceof DbaRole) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPrimaryGroup() {
        final InternalRole[] roles = internalAccount.getInternalRoles();
        if (roles == null) {
            return null;
        }

        return roles[0].getRoleName();
    }

    @Override
    public Group getDefaultGroup() {
        final InternalRole[] roles = internalAccount.getInternalRoles();
        if (roles == null) {
            return null;
        }

        return new ExistShiroInternalRoleAdapter(database, shiroSecurityManager, roles[0]);
    }

    @Override
    public boolean hasGroup(final String group) {
        final InternalRole[] roles = internalAccount.getInternalRoles();
        if (roles == null) {
            return false;
        }

        for (int i = 0; i < roles.length; i++) {
            if (roles[i].getRoleName().equals(group)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setPassword(String passwd) {
        // TODO(AR) implement
        throw new UnsupportedOperationException("TODO(AR) NOT YET IMPLEMENTED");
    }

    @Override
    public void setCredential(Credential credential) {
        // TODO(AR) implement
        throw new UnsupportedOperationException("TODO(AR) NOT YET IMPLEMENTED");
    }

    @Override
    public String getPassword() {
        return internalAccount.getCredentials().toString();
    }

    @Override
    public String getDigestPassword() {
        return internalAccount.getCredentials().toString();
    }

    @Override
    public void setGroups(String[] groups) {
        // TODO(AR) implement
        throw new UnsupportedOperationException("TODO(AR) NOT YET IMPLEMENTED");
    }

    @Override
    public String getUsername() {
        return internalAccount.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !internalAccount.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        // TODO(AR) remove function or implement
        throw new UnsupportedOperationException("TODO(AR) NOT YET IMPLEMENTED");
    }

    @Override
    public int getId() {
        return internalAccount.getId();
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
        return internalAccount.getUsername();
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
        if (obj == null || !(obj instanceof Subject)) {
            return false;
        }

        // TODO(AR) this could likely be improved
        final Subject other = (Subject) obj;
        return getRealmId().equals(other.getRealmId())
                && getName().equals(other.getName());
    }
}
