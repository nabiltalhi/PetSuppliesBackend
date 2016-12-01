/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.petsupplies.controller;

import com.petsupplies.controller.exceptions.NonexistentEntityException;
import com.petsupplies.controller.exceptions.RollbackFailureException;
import com.petsupplies.model.Account;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.petsupplies.model.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author ntalhi
 */
public class AccountJpaController1 implements Serializable {

    public AccountJpaController1(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Account account) throws RollbackFailureException, Exception {
        if (account.getSessionCollection() == null) {
            account.setSessionCollection(new ArrayList<Session>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Session> attachedSessionCollection = new ArrayList<Session>();
            for (Session sessionCollectionSessionToAttach : account.getSessionCollection()) {
                sessionCollectionSessionToAttach = em.getReference(sessionCollectionSessionToAttach.getClass(), sessionCollectionSessionToAttach.getId());
                attachedSessionCollection.add(sessionCollectionSessionToAttach);
            }
            account.setSessionCollection(attachedSessionCollection);
            em.persist(account);
            for (Session sessionCollectionSession : account.getSessionCollection()) {
                Account oldAccountIdOfSessionCollectionSession = sessionCollectionSession.getAccountId();
                sessionCollectionSession.setAccountId(account);
                sessionCollectionSession = em.merge(sessionCollectionSession);
                if (oldAccountIdOfSessionCollectionSession != null) {
                    oldAccountIdOfSessionCollectionSession.getSessionCollection().remove(sessionCollectionSession);
                    oldAccountIdOfSessionCollectionSession = em.merge(oldAccountIdOfSessionCollectionSession);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Account account) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Account persistentAccount = em.find(Account.class, account.getId());
            Collection<Session> sessionCollectionOld = persistentAccount.getSessionCollection();
            Collection<Session> sessionCollectionNew = account.getSessionCollection();
            Collection<Session> attachedSessionCollectionNew = new ArrayList<Session>();
            for (Session sessionCollectionNewSessionToAttach : sessionCollectionNew) {
                sessionCollectionNewSessionToAttach = em.getReference(sessionCollectionNewSessionToAttach.getClass(), sessionCollectionNewSessionToAttach.getId());
                attachedSessionCollectionNew.add(sessionCollectionNewSessionToAttach);
            }
            sessionCollectionNew = attachedSessionCollectionNew;
            account.setSessionCollection(sessionCollectionNew);
            account = em.merge(account);
            for (Session sessionCollectionOldSession : sessionCollectionOld) {
                if (!sessionCollectionNew.contains(sessionCollectionOldSession)) {
                    sessionCollectionOldSession.setAccountId(null);
                    sessionCollectionOldSession = em.merge(sessionCollectionOldSession);
                }
            }
            for (Session sessionCollectionNewSession : sessionCollectionNew) {
                if (!sessionCollectionOld.contains(sessionCollectionNewSession)) {
                    Account oldAccountIdOfSessionCollectionNewSession = sessionCollectionNewSession.getAccountId();
                    sessionCollectionNewSession.setAccountId(account);
                    sessionCollectionNewSession = em.merge(sessionCollectionNewSession);
                    if (oldAccountIdOfSessionCollectionNewSession != null && !oldAccountIdOfSessionCollectionNewSession.equals(account)) {
                        oldAccountIdOfSessionCollectionNewSession.getSessionCollection().remove(sessionCollectionNewSession);
                        oldAccountIdOfSessionCollectionNewSession = em.merge(oldAccountIdOfSessionCollectionNewSession);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = account.getId();
                if (findAccount(id) == null) {
                    throw new NonexistentEntityException("The account with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Account account;
            try {
                account = em.getReference(Account.class, id);
                account.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The account with id " + id + " no longer exists.", enfe);
            }
            Collection<Session> sessionCollection = account.getSessionCollection();
            for (Session sessionCollectionSession : sessionCollection) {
                sessionCollectionSession.setAccountId(null);
                sessionCollectionSession = em.merge(sessionCollectionSession);
            }
            em.remove(account);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Account> findAccountEntities() {
        return findAccountEntities(true, -1, -1);
    }

    public List<Account> findAccountEntities(int maxResults, int firstResult) {
        return findAccountEntities(false, maxResults, firstResult);
    }

    private List<Account> findAccountEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Account.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Account findAccount(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Account.class, id);
        } finally {
            em.close();
        }
    }

    public int getAccountCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Account> rt = cq.from(Account.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
