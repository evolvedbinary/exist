/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.db;

import org.apache.shiro.authc.Account;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.exist.security.shiro.ExpirableAccount;
import org.exist.security.shiro.LockableAccount;
import org.exist.security.shiro.realm.db.beans.GroupReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DbAccount implements Account, LockableAccount, ExpirableAccount {

    private final org.exist.security.shiro.realm.db.beans.Account account;
    private final HashedPassword hashedPassword;

    public DbAccount(final org.exist.security.shiro.realm.db.beans.Account account) {
        this.account = account;
        this.hashedPassword = new HashedPassword(account.getPassword());
    }

    @Override
    public PrincipalCollection getPrincipals() {
        return new DbAccountPrincipalCollection(account);
    }

    @Override
    public Object getCredentials() {
        return hashedPassword;
    }

    @Override
    public Collection<String> getRoles() {
        final List<GroupReference> groupReferences = account.getGroup();
        final List<String> roles = new ArrayList<>(groupReferences.size());
        for (final GroupReference groupReference : groupReferences) {
            roles.add(groupReference.getName());
        }
        return roles;
    }

    @Override
    public Collection<String> getStringPermissions() {
        return null;
    }

    @Override
    public Collection<Permission> getObjectPermissions() {
        return null;
    }

    @Override
    public boolean isCredentialsExpired() {
        return account.isExpired();
    }

    @Override
    public void setCredentialsExpired() {
        //TODO(AR) implement
        throw new UnsupportedOperationException("TODO(AR) implement");
    }

    @Override
    public boolean isLocked() {
        return !account.isEnabled();
    }

    @Override
    public void setLocked(final boolean locked) {
        //TODO(AR) implement
        throw new UnsupportedOperationException("TODO(AR) implement");
    }
}
