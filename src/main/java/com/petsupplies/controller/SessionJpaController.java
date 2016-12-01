/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.petsupplies.controller;

import com.petsupplies.controller.exceptions.NonexistentEntityException;
import com.petsupplies.controller.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.petsupplies.model.Account;
import com.petsupplies.model.Session;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author ntalhi
 */
public class SessionJpaController implements Serializable {

    public SessionJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Session session) throws RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Account accountId = session.getAccountId();
            if (accountId != null) {
                accountId = em.getReference(accountId.getClass(), accountId.getId());
                session.setAccountId(accountId);
            }
            em.persist(session);
            if (accountId != null) {
                accountId.getSessionCollection().add(session);
                accountId = em.merge(accountId);
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

    public void edit(Session session) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Session persistentSession = em.find(Session.class, session.getId());
            Account accountIdOld = persistentSession.getAccountId();
            Account accountIdNew = session.getAccountId();
            if (accountIdNew != null) {
                accountIdNew = em.getReference(accountIdNew.getClass(), accountIdNew.getId());
                session.setAccountId(accountIdNew);
            }
            session = em.merge(session);
            if (accountIdOld != null && !accountIdOld.equals(accountIdNew)) {
                accountIdOld.getSessionCollection().remove(session);
                accountIdOld = em.merge(accountIdOld);
            }
            if (accountIdNew != null && !accountIdNew.equals(accountIdOld)) {
                accountIdNew.getSessionCollection().add(session);
                accountIdNew = em.merge(accountIdNew);
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
                Integer id = session.getId();
                if (findSession(id) == null) {
                    throw new NonexistentEntityException("The session with id " + id + " no longer exists.");
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
            Session session;
            try {
                session = em.getReference(Session.class, id);
                session.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The session with id " + id + " no longer exists.", enfe);
            }
            Account accountId = session.getAccountId();
            if (accountId != null) {
                accountId.getSessionCollection().remove(session);
                accountId = em.merge(accountId);
            }
            em.remove(session);
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

    public List<Session> findSessionEntities() {
        return findSessionEntities(true, -1, -1);
    }

    public List<Session> findSessionEntities(int maxResults, int firstResult) {
        return findSessionEntities(false, maxResults, firstResult);
    }

    private List<Session> findSessionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Session.class));
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

    public Session findSession(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Session.class, id);
        } finally {
            em.close();
        }
    }

    public int getSessionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Session> rt = cq.from(Session.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
