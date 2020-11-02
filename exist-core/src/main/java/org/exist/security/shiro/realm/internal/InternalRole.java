/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

public abstract class InternalRole {
    private final int id;
    private final String roleName;
    private final String description;

    protected InternalRole(final int id, final String roleName, final String description) {
        this.id = id;
        this.roleName = roleName;
        this.description = description;
    }

    public String getPrimaryPrincipal() {
        return InternalRealm.getRoleAtRealmPrincipal(roleName);
    }

    @Override
    public String toString() {
        return InternalRealm.getRoleAtRealmPrincipal(roleName);
    }

    // TODO(AR) temp for integration with eXist-db's current SecurityManager
    int getId() {
        return id;
    }

    // TODO(AR) temp for integration with eXist-db's current SecurityManager
    String getRoleName() {
        return roleName;
    }
}
