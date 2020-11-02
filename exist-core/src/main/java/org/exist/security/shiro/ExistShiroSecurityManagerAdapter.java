/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro;

import org.apache.shiro.realm.Realm;
import org.exist.Database;
import org.exist.EXistException;
import org.exist.config.Configuration;
import org.exist.dom.persistent.DocumentImpl;
import org.exist.security.*;
import org.exist.security.SecurityManager;
import org.exist.security.shiro.realm.internal.*;
import org.exist.storage.BrokerPool;
import org.exist.storage.BrokerPoolService;
import org.exist.storage.DBBroker;
import org.exist.storage.txn.Txn;

import java.util.*;

public class ExistShiroSecurityManagerAdapter implements org.exist.security.SecurityManager, BrokerPoolService {

    private BrokerPool brokerPool;
    private ShiroSecurityManager internalSecurityManager;

    private final Map<String, Session> sessions = new HashMap<>();


    @Override
    public void prepare(final BrokerPool brokerPool) {
        this.brokerPool = brokerPool;
        this.internalSecurityManager = new ShiroSecurityManager(brokerPool);
    }

    @Override
    public void attach(final DBBroker broker, final Txn transaction) {

    }

    @Override
    public Database getDatabase() {
        return brokerPool;
    }

    @Override
    public Database database() {
        return brokerPool;
    }

    @Override
    public void registerAccount(final Account account) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerGroup(final Group group) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Account getAccount(final int id) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                for (final Account account : new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).getAccounts()) {
                    if (account.getId() == id) {
                        return account;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Account getAccount(final String name) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).getAccount(name);
            }
        }

        return null;
    }

    @Override
    public boolean hasAccount(final String name) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).hasAccount(name);
            }
        }

        return false;
    }

    @Override
    public Account addAccount(final Account user) throws PermissionDeniedException {
        // TODO(AR) fix when we add further realms
        throw new PermissionDeniedException("It is not possible to add accounts to the internal realm");
    }

    @Override
    public Account addAccount(final DBBroker broker, final Account account) throws PermissionDeniedException {
        // TODO(AR) fix when we add further realms
        throw new PermissionDeniedException("It is not possible to add accounts to the internal realm");
    }

    @Override
    public boolean deleteAccount(final String name) throws PermissionDeniedException {
        // TODO(AR) fix when we add further realms
        throw new PermissionDeniedException("It is not possible to delete accounts from the internal realm");
    }

    @Override
    public boolean deleteAccount(final Account account) throws PermissionDeniedException {
        // TODO(AR) fix when we add further realms
        throw new PermissionDeniedException("It is not possible to delete accounts from the internal realm");
    }

    @Override
    public boolean updateAccount(final Account account) {
        //TODO(AR) implement
        throw new UnsupportedOperationException("TODO(AR) NOT YET IMPLEMENTED");
    }

    @Override
    public boolean hasUser(final int id) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                for (final Account account : new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).getAccounts()) {
                    if (account.getId() == id) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean updateGroup(final Group group) {
        //TODO(AR) implement
        throw new UnsupportedOperationException("TODO(AR) NOT YET IMPLEMENTED");
    }

    @Override
    public Group addGroup(DBBroker broker, Group group) throws PermissionDeniedException, EXistException {
        // TODO(AR) fix when we add further realms
        throw new PermissionDeniedException("It is not possible to add groups to the internal realm");
    }

    @Override
    public void addGroup(DBBroker broker, String group) throws PermissionDeniedException, EXistException {
        // TODO(AR) fix when we add further realms
        throw new PermissionDeniedException("It is not possible to add groups to the internal realm");
    }

    @Override
    public boolean hasGroup(final String name) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).hasGroup(name);
            }
        }

        return false;
    }

    @Override
    public boolean hasGroup(final Group group) {
        return hasGroup(group.getId());
    }


    @Override
    public boolean hasGroup(final int id) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                for (final Group group : new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).getGroups()) {
                    if (group.getId() == id) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public Group getGroup(final String name) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).getGroup(name);
            }
        }

        return null;
    }

    @Override
    public Group getGroup(final int gid) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                for (final Group group : new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).getGroups()) {
                    if (group.getId() == gid) {
                        return group;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean deleteGroup(final String name) throws PermissionDeniedException {
        // TODO(AR) fix when we add further realms
        throw new PermissionDeniedException("It is not possible to delete groups to the internal realm");
    }

    @Override
    public boolean hasAdminPrivileges(final Account user) {
        return user.hasDbaRole();
    }

    @Override
    public Subject authenticate(final String username, final Object credentials) throws AuthenticationException {

        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                final ExistShiroInternalRealmAdapter realmAdapter = new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm) realm);
                return realmAdapter.authenticate(username, credentials);
            }
        }

        return null;
    }

    @Override
    public Subject getSystemSubject() {
        final Iterator<Realm> it = internalSecurityManager.getRealms().iterator();
        while (it.hasNext()) {
            final Realm realm = it.next();
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalAccountAdapter(brokerPool, internalSecurityManager, ((InternalRealm)realm).getSystemAccount());
            }
        }

        return null;
    }

    @Override
    public Subject getGuestSubject() {
        final Iterator<Realm> it = internalSecurityManager.getRealms().iterator();
        while (it.hasNext()) {
            final Realm realm = it.next();
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalAccountAdapter(brokerPool, internalSecurityManager, ((InternalRealm)realm).getGuestAccount());
            }
        }

        return null;
    }

    @Override
    public Group getDBAGroup() {
        final Iterator<Realm> it = internalSecurityManager.getRealms().iterator();
        while (it.hasNext()) {
            final Realm realm = it.next();
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalRoleAdapter(brokerPool, internalSecurityManager, ((InternalRealm)realm).getDbaRole());
            }
        }

        return null;
    }

    @Override
    public List<Account> getGroupMembers(final String groupName) {
        final List<Account> results = new ArrayList<>();

        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                final ExistShiroInternalRealmAdapter realmAdapter = new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm) realm);
                for (final String member : realmAdapter.findAllGroupMembers(groupName)) {
                    results.add(realmAdapter.getAccount(member));
                }
            }
        }

        return results;
    }

    @Override
    public Collection<Account> getUsers() {
        final List<Account> results = new ArrayList<>();

        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                final ExistShiroInternalRealmAdapter realmAdapter = new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm) realm);
                results.addAll(realmAdapter.getAccounts());
            }
        }

        return results;
    }

    @Override
    public Collection<Group> getGroups() {
        final List<Group> results = new ArrayList<>();

        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                final ExistShiroInternalRealmAdapter realmAdapter = new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm) realm);
                results.addAll(realmAdapter.getGroups());
            }
        }

        return results;
    }

    @Override
    public void registerSession(final Session session) {
        // TODO(AR) what is this for?!?
        sessions.put(session.getId(), session);
    }

    @Override
    public Subject getSubjectBySessionId(final String sessionId) {
        final Session session = sessions.get(sessionId);
        if (session != null) {
            return session.getSubject();
        }
        return null;
    }

    @Override
    public List<String> findUsernamesWhereNameStarts(final String startsWith) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).findUsernamesWhereNameStarts(startsWith);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> findUsernamesWhereUsernameStarts(final String startsWith) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).findUsernamesWhereUsernameStarts(startsWith);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> findAllGroupNames() {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).findAllGroupNames();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> findAllUserNames() {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).findAllUserNames();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> findGroupnamesWhereGroupnameStarts(final String startsWith) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                return (List<String>)new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).findGroupnamesWhereGroupnameStarts(startsWith);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> findAllGroupMembers(final String groupName) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                return (List<String>)new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).findAllGroupNames();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void processParameter(final DBBroker broker, final DocumentImpl document) {
        //TODO(AR) I think this is old Configurator junk
    }

    @Override
    public void processParameterBeforeSave(final DBBroker broker, final DocumentImpl document) {
        //TODO(AR) I think this is old Configurator junk
    }

    @Override
    public String getAuthenticationEntryPoint() {
        //TODO(AR) this likely should not be here as it is HTTP related!
        return "/authentication/login";
    }

    @SuppressWarnings("Unchecked")
    @Override
    public List<String> findGroupnamesWhereGroupnameContains(final String fragment) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                return (List<String>)new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).findGroupnamesWhereGroupnameContains(fragment);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> findUsernamesWhereNamePartStarts(final String startsWith) {
        for (final Realm realm : internalSecurityManager.getRealms()) {

            // TODO(AR) this needs to be expanded to all realms!
            if (realm instanceof InternalRealm) {
                return new ExistShiroInternalRealmAdapter(brokerPool, internalSecurityManager, (InternalRealm)realm).findUsernamesWhereNamePartStarts(startsWith);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public Subject getCurrentSubject() {
        return database().getActiveBroker().getCurrentSubject();
    }

    @Override
    public void preAllocateAccountId(final SecurityManager.PrincipalIdReceiver receiver) {
        // TODO(AR) why do we need this?
    }

    @Override
    public void preAllocateGroupId(final SecurityManager.PrincipalIdReceiver receiver)  {
        // TODO(AR) why do we need this?
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }
}
