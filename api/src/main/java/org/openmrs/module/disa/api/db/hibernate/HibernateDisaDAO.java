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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.LocationAttribute;
import org.openmrs.Patient;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.SyncLog;
import org.openmrs.module.disa.api.TypeOfResult;
import org.openmrs.module.disa.api.db.DisaDAO;
import org.openmrs.module.disa.api.sync.scheduler.ViralLoadFormSchedulerTask;

/**
 * It is a default implementation of {@link DisaDAO}.
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
	public Serializable saveSyncLog(SyncLog syncLog) {
		return this.getCurrentSession().save(syncLog);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean existsByRequestIdAndTypeOfResult(String requestId, TypeOfResult typeOfResult) {
		final String hql = "SELECT f FROM SyncLog f WHERE f.requestId = :requestId AND f.typeOfResult = :typeOfResult";
		final Query query = this.getCurrentSession()
				.createQuery(hql)
				.setParameter("requestId", requestId)
				.setParameter("typeOfResult", typeOfResult);
		List list = query.list();
		return list != null && !list.isEmpty();
	}

	@Override
	public List<Integer> getPatientByNid(String identifier) {
		final String sql = "SELECT distinct pi.patient_id FROM patient_identifier pi "
				+ "INNER JOIN person pe ON pe.person_id = pi.patient_id "
				+ "INNER JOIN patient p ON p.patient_id = p.patient_id "
				+ "WHERE identifier = '" + identifier + "'"
				+ "AND pi.voided = 0 "
				+ "AND pe.voided = 0 "
				+ "AND p.voided = 0";
		final Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
		List<Object[]> objs = query.list();
		List<Integer> patientIds = new ArrayList<Integer>();

		for (Object aux : objs) {
			patientIds.add((Integer) aux);
		}
		return patientIds;
	}

	@Override
	public List<Patient> getPatientByPatientId(Integer patientId) {
		final String hql = "SELECT p FROM Patient p WHERE p.patientId = :patientId";
		final Query query = this.getCurrentSession().createQuery(hql).setParameter("patientId", patientId);
		return query.list();
	}

	@Override
	public List<SyncLog> getSyncLogsWithEncountersByLabResults(List<LabResult> labResults) {
		if (labResults.isEmpty()) {
			return Collections.emptyList();
		}
		String hql = "SELECT s FROM SyncLog s join FETCH s.encounter where s.requestId in :requestIds";
		List<String> requestIds = labResults.stream().map(LabResult::getRequestId).collect(Collectors.toList());
		Query query = this.getCurrentSession().createQuery(hql).setParameterList("requestIds", requestIds);
		return query.list();
	}

	@Override
	public Long getSyncTaskRepeatInterval() {
		String sql = "select repeat_interval "
				+ "from scheduler_task_config "
				+ "where schedulable_class=:schedulableClass";
		Query query = this.getCurrentSession()
				.createSQLQuery(sql)
				.setParameter("schedulableClass",
						ViralLoadFormSchedulerTask.class.getName());
		Object repeatInterval = query.uniqueResult();
		if (repeatInterval == null) {
			return null;
		}
		return Integer.toUnsignedLong((int) repeatInterval);
	}
}
