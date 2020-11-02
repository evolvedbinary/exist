/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

import org.apache.shiro.subject.PrincipalCollection;

import java.util.*;

public class InternalAccountPrincipalCollection implements PrincipalCollection {

    final InternalAccount account;

    InternalAccountPrincipalCollection(final InternalAccount account) {
        this.account = account;
    }

    @Override
    public Object getPrimaryPrincipal() {
        return InternalRealm.getUsernameAtRealmPrincipal(account.getUsername());
    }

    @Override
    public <T> T oneByType(final Class<T> type) {
        if (String.class.equals(type)) {
            return (T) account.getUsername();
        } else if (int.class.equals(type) || Integer.class.equals(type)) {
            return (T) Integer.valueOf(account.getId());
        } else {
            return null;
        }
    }

    @Override
    public <T> Collection<T> byType(final Class<T> type) {
        if (String.class.equals(type)) {
            return Arrays.asList((T) account.getUsername());
        } else if (int.class.equals(type) || Integer.class.equals(type)) {
            return Arrays.asList((T) Integer.valueOf(account.getId()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List asList() {
        return Arrays.asList(getPrimaryPrincipal(), account.getUsername(), account.getId());
    }

    @Override
    public Set asSet() {
        final Set set = new HashSet();
        set.add(getPrimaryPrincipal());
        set.add(account.getUsername());
        set.add(account.getId());
        return set;
    }

    @Override
    public Collection fromRealm(final String realmName) {
        if (InternalRealm.REALM_NAME.equals(realmName)) {
            return asList();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Set<String> getRealmNames() {
        final Set<String> set = new HashSet<>();
        set.add(InternalRealm.REALM_NAME);
        return set;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Iterator iterator() {
        return asList().iterator();
    }
}
