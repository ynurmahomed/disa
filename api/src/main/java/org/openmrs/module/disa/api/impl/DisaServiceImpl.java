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
package org.openmrs.module.disa.api.impl;

import org.openmrs.LocationAttribute;
import org.openmrs.api.impl.BaseOpenmrsService;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.disa.FsrLog;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.api.db.DisaDAO;

/**
 * It is a default implementation of {@link DisaService}.
 */
public class DisaServiceImpl extends BaseOpenmrsService implements DisaService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private DisaDAO dao;
	
	/**
     * @param dao the dao to set
     */
    public void setDao(DisaDAO dao) {
	    this.dao = dao;
    }
    
    /**
     * @return the dao
     */
    public DisaDAO getDao() {
	    return dao;
    }

	@Override
	public List<LocationAttribute> getAllLocationAttribute(String valueReference) {
		return dao.getAllLocationAttribute(valueReference);
	}

	@Override
	public Serializable saveFsrLog(FsrLog fsrLog) {
		return dao.saveFsrLog(fsrLog); 
	}
}