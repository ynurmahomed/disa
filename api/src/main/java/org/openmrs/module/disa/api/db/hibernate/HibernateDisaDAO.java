/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.disa.api.db.hibernate;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.LocationAttribute;
import org.openmrs.module.disa.FsrLog;
import org.openmrs.module.disa.api.db.DisaDAO;

/**
 * It is a default implementation of  {@link DisaDAO}.
 */
@SuppressWarnings("unchecked")
public class HibernateDisaDAO implements DisaDAO {
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	/**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
	    this.sessionFactory = sessionFactory;
    }
    
	/**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
	    return sessionFactory;
    }
    
	private org.hibernate.Session getCurrentSession() {
		try {
			return this.sessionFactory.getCurrentSession();
		} catch (final NoSuchMethodError ex) {
			try {
				final Method method = this.sessionFactory.getClass().getMethod("getCurrentSession", null);
				return (org.hibernate.Session) method.invoke(this.sessionFactory, null);
			} catch (final Exception e) {
				throw new RuntimeException("Failed to get the current hibernate session", e);
			}
		}
	}

	@Override
	public List<LocationAttribute> getAllLocationAttribute(String valueReference) {
		final String hql = "SELECT  l FROM LocationAttribute l WHERE l.valueReference = :valueReference AND l.voided = 0";
		final Query query = this.getCurrentSession().createQuery(hql).setParameter("valueReference", valueReference);
		return query.list();
	}
	
	@Override
	public Serializable saveFsrLog(FsrLog fsrLog) {
		return this.getCurrentSession().save(fsrLog); 
	}

	@Override
	public Long countFsrLogByRequestId(String requestId) {
		final String hql = "SELECT f FROM FsrLog f WHERE f.requestId = :requestId ";
		final Query query = this.getCurrentSession().createQuery(hql).setParameter("requestId", requestId);
		List list=query.list();
		return list!=null && list.isEmpty()?(Long) list.get(0):0;
	}
}