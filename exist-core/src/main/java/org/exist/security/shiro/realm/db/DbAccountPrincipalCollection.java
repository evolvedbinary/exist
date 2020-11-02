/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.db;

import org.apache.shiro.subject.PrincipalCollection;

import java.util.*;

public class DbAccountPrincipalCollection implements PrincipalCollection {

    final org.exist.security.shiro.realm.db.beans.Account account;

    DbAccountPrincipalCollection(final org.exist.security.shiro.realm.db.beans.Account account) {
        this.account = account;
    }

    @Override
    public Object getPrimaryPrincipal() {
        return DbRealm.getUsernameAtRealmPrincipal(account.getName());
    }

    @Override
    public <T> T oneByType(final Class<T> type) {
        if (String.class.equals(type)) {
            return (T) account.getName();
        } else if (int.class.equals(type) || Integer.class.equals(type)) {
            return (T) Integer.valueOf(account.getId().intValue());
        } else {
            return null;
        }
    }

    @Override
    public <T> Collection<T> byType(final Class<T> type) {
        if (String.class.equals(type)) {
            return Arrays.asList((T) account.getName());
        } else if (int.class.equals(type) || Integer.class.equals(type)) {
            return Arrays.asList((T) Integer.valueOf(account.getId().intValue()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List asList() {
        return Arrays.asList(getPrimaryPrincipal(), account.getName(), account.getId().intValue());
    }

    @Override
    public Set asSet() {
        final Set set = new HashSet();
        set.add(getPrimaryPrincipal());
        set.add(account.getName());
        set.add(account.getId());
        return set;
    }

    @Override
    public Collection fromRealm(final String realmName) {
        if (DbRealm.REALM_NAME.equals(realmName)) {
            return asList();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Set<String> getRealmNames() {
        final Set<String> set = new HashSet<>();
        set.add(DbRealm.REALM_NAME);
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
