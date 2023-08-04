package org.openmrs.module.disa.api;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.openmrs.Encounter;

@Entity
@Table(name = "disa_sync_log")
public class SyncLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Integer id;

	@Column(name = "patient_id")
	private Integer patientId;

	@OneToOne
	@JoinColumn(name = "encounter_id")
	private Encounter encounter;

	@Column(name = "patient_identifier")
	private String patientIdentifier;

	@Column(name = "request_id")
	private String requestId;

	@Column(name = "creator")
	private Integer creator;

	@Column(name = "date_created")
	private Date dateCreated;

	@Column(name = "type_of_result")
	@Enumerated(EnumType.STRING)
	private TypeOfResult typeOfResult;

	public SyncLog() {
	}

	public SyncLog(String requestId, TypeOfResult typeOfResult) {
		this.requestId = requestId;
		this.typeOfResult = typeOfResult;
	}

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public Encounter getEncounter() {
		return encounter;
	}

	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	public String getPatientIdentifier() {
		return patientIdentifier;
	}

	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Integer getCreator() {
		return creator;
	}

	public void setCreator(Integer creator) {
		this.creator = creator;
	}

	public void setTypOfResult(TypeOfResult typeOfResult) {
		this.typeOfResult = typeOfResult;
	}

	public boolean belongsTo(LabResult labResult) {
		return this.equals(new SyncLog(labResult.getRequestId(), labResult.getTypeOfResult()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
		result = prime * result + ((typeOfResult == null) ? 0 : typeOfResult.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SyncLog other = (SyncLog) obj;
		if (requestId == null) {
			if (other.requestId != null)
				return false;
		} else if (!requestId.equals(other.requestId))
			return false;
		if (typeOfResult != other.typeOfResult)
			return false;
		return true;
	}

}
