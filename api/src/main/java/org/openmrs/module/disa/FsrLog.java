package org.openmrs.module.disa;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "fsr_log")
public class FsrLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Integer id;

	@Column(name = "patient_id")
	private Integer patientId;

	@Column(name = "encounter_id")
	private Integer encounterId;

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

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public Integer getEncounterId() {
		return encounterId;
	}

	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
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
}