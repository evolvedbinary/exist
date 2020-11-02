/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

import org.apache.shiro.authc.Account;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;

import javax.annotation.Nullable;
import java.util.*;

public abstract class InternalAccount implements Account {

    private final int id;
    private final String username;
    private final String description;
    private final Object credentials;
    private @Nullable InternalRole[] roles;

    protected InternalAccount(final int id, final String username, final String description, final Object credentials) {
        this.id = id;
        this.username = username;
        this.description = description;
        this.credentials = credentials;
    }

    public void addRole(final InternalRole role) {
        if (roles == null) {
            this.roles = new InternalRole[1];
        } else {
            this.roles = Arrays.copyOf(roles, roles.length + 1);
        }
        this.roles[roles.length - 1] = role;
    }

    public void addRoles(final InternalRole[] roles) {
        if (roles == null) {
            this.roles = new InternalRole[roles.length];
        } else {
            this.roles = Arrays.copyOf(roles, this.roles.length + roles.length);
        }
        System.arraycopy(roles, 0, this.roles, this.roles.length - roles.length, roles.length);
    }

    @Override
    public PrincipalCollection getPrincipals() {
        return new InternalAccountPrincipalCollection(this);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Collection<String> getRoles() {
        if (roles == null) {
            return Collections.emptyList();
        }

        final List<String> result = new ArrayList<>(roles.length);
        for (final InternalRole role : roles) {
            result.add(role.getPrimaryPrincipal());
        }
        return result;
    }

    @Override
    public Collection<String> getStringPermissions() {
        return null;  // TODO(AR) implement getObjectPermissions below instead?
    }

    @Override
    public Collection<Permission> getObjectPermissions() {
        return null;  // TODO(AR) implement?
    }

    public abstract boolean isLocked();

    @Override
    public String toString() {
        return InternalRealm.getUsernameAtRealmPrincipal(username);
    }

    // TODO(AR) temp for integration with eXist-db's current SecurityManager
    @Nullable InternalRole[] getInternalRoles() {
        return roles;
    }

    // TODO(AR) temp for integration with eXist-db's current SecurityManager
    int getId() {
        return id;
    }

    // TODO(AR) temp for integration with eXist-db's current SecurityManager
    String getUsername() {
        return username;
    }
}
