package org.openmrs.module.disa.api;

import java.time.LocalDateTime;
import java.util.Date;

/**
 *
 * @author machabane
 *
 */
public abstract class LabResult {

	private long id;

	private String finalResult;

	private String requestId;

	private String location;

	private String createdBy;

	private LocalDateTime createdAt;

	private String updatedBy;

	private LocalDateTime updatedAt;

	private String nid;

	private String firstName;

	private String lastName;

	private Date dateOfBirth;

	private String gender;

	private String healthFacilityLabCode;

	private String encounter;

	private Integer encounterId;

	private String pregnant;

	private String breastFeeding;

	private String reasonForTest;

	private LocalDateTime harvestDate;

	private String harvestType;

	private LocalDateTime dateOfSampleReceive;

	private LocalDateTime processingDate;

	private SampleType sampleType;

	private LocalDateTime labResultDate;

	private String aprovedBy;

	private String labComments;

	private String requestingFacilityName;

	private String requestingDistrictName;

	private String requestingProvinceName;

	private LabResultStatus labResultStatus;

	private NotProcessingCause notProcessingCause;

	private String artRegimen;

	private String primeiraLinha;

	private String segundaLinha;

	private String dataDeInicioDoTARV;

	private String synchronizedBy;

	private Integer ageInYears;

	private TypeOfResult typeOfResult;

	private LocalDateTime registeredDateTime;

	protected LabResult() {
	}

	protected LabResult(long id) {
		this.id = id;
	}

	public String getSynchronizedBy() {
		return synchronizedBy;
	}

	public void setSynchronizedBy(String synchronizedBy) {
		this.synchronizedBy = synchronizedBy;
	}

	public String getDataDeInicioDoTARV() {
		return dataDeInicioDoTARV;
	}

	public void setDataDeInicioDoTARV(String dataDeInicioDoTARV) {
		this.dataDeInicioDoTARV = dataDeInicioDoTARV;
	}

	public String getPrimeiraLinha() {
		return primeiraLinha;
	}

	public void setPrimeiraLinha(String primeiraLinha) {
		this.primeiraLinha = primeiraLinha;
	}

	public String getSegundaLinha() {
		return segundaLinha;
	}

	public void setSegundaLinha(String segundaLinha) {
		this.segundaLinha = segundaLinha;
	}

	public String getArtRegimen() {
		return artRegimen;
	}

	public void setArtRegimen(String artRegimen) {
		this.artRegimen = artRegimen;
	}

	public LabResultStatus getLabResultStatus() {
		return labResultStatus;
	}

	public void setLabResultStatus(LabResultStatus labResultStatus) {
		this.labResultStatus = labResultStatus;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getNid() {
		return nid;
	}

	public void setNid(String nid) {
		this.nid = nid;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getHealthFacilityLabCode() {
		return healthFacilityLabCode;
	}

	public void setHealthFacilityLabCode(String healthFacilityLabCode) {
		this.healthFacilityLabCode = healthFacilityLabCode;
	}

	public String getEncounter() {
		return encounter;
	}

	public void setEncounter(String encounter) {
		this.encounter = encounter;
	}

	public String getPregnant() {
		return pregnant;
	}

	public void setPregnant(String pregnant) {
		this.pregnant = pregnant;
	}

	public String getBreastFeeding() {
		return breastFeeding;
	}

	public void setBreastFeeding(String breastFeeding) {
		this.breastFeeding = breastFeeding;
	}

	public String getReasonForTest() {
		return reasonForTest;
	}

	public void setReasonForTest(String reasonForTest) {
		this.reasonForTest = reasonForTest;
	}

	public String getHarvestType() {
		return harvestType;
	}

	public void setHarvestType(String harvestType) {
		this.harvestType = harvestType;
	}

	public LocalDateTime getDateOfSampleReceive() {
		return dateOfSampleReceive;
	}

	public void setDateOfSampleReceive(LocalDateTime dateOfSampleReceive) {
		this.dateOfSampleReceive = dateOfSampleReceive;
	}

	public LocalDateTime getProcessingDate() {
		return processingDate;
	}

	public void setProcessingDate(LocalDateTime processingDate) {
		this.processingDate = processingDate;
	}

	public SampleType getSampleType() {
		return sampleType;
	}

	public void setSampleType(SampleType sampleType) {
		this.sampleType = sampleType;
	}

	public LocalDateTime getLabResultDate() {
		return labResultDate;
	}

	public void setLabResultDate(LocalDateTime labResultDate) {
		this.labResultDate = labResultDate;
	}

	public String getAprovedBy() {
		return aprovedBy;
	}

	public void setAprovedBy(String aprovedBy) {
		this.aprovedBy = aprovedBy;
	}

	public String getLabComments() {
		return labComments;
	}

	public void setLabComments(String labComments) {
		this.labComments = labComments;
	}

	public String getRequestingFacilityName() {
		return requestingFacilityName;
	}

	public void setRequestingFacilityName(String requestingFacilityName) {
		this.requestingFacilityName = requestingFacilityName;
	}

	public NotProcessingCause getNotProcessingCause() {
		return notProcessingCause;
	}

	public void setNotProcessingCause(NotProcessingCause notProcessingCause) {
		this.notProcessingCause = notProcessingCause;
	}

	public String getRequestingDistrictName() {
		return requestingDistrictName;
	}

	public void setRequestingDistrictName(String requestingDistrictName) {
		this.requestingDistrictName = requestingDistrictName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getAge() {
		return ageInYears;
	}

	public String getRequestingProvinceName() {
		return requestingProvinceName;
	}

	public void setRequestingProvinceName(String requestingProvinceName) {
		this.requestingProvinceName = requestingProvinceName;
	}

	public void setAgeInYears(Integer ageInYears) {
		this.ageInYears = ageInYears;
	}

	public Integer getAgeInYears() {
		return ageInYears;
	}

	public TypeOfResult getTypeOfResult() {
		return typeOfResult;
	}

	protected void setTypeOfResult(TypeOfResult typeOfResult) {
		this.typeOfResult = typeOfResult;
	}

	public String getFinalResult() {
		return finalResult;
	}

	public void setFinalResult(String finalResult) {
		this.finalResult = finalResult;
	}

	public boolean isPending() {
		return LabResultStatus.PENDING == labResultStatus;
	}

	public boolean isNotProcessed() {
		return LabResultStatus.NOT_PROCESSED == labResultStatus;
	}

	public boolean isProcessed() {
		return LabResultStatus.PROCESSED == labResultStatus;
	}

	public LocalDateTime getHarvestDate() {
		return harvestDate;
	}

	public void setHarvestDate(LocalDateTime specimenDateTime) {
		this.harvestDate = specimenDateTime;
	}

	public LocalDateTime getRegisteredDateTime() {
		return registeredDateTime;
	}

	public void setRegisteredDateTime(LocalDateTime registeredDateTime) {
		this.registeredDateTime = registeredDateTime;
	}

	public Integer getEncounterId() {
		return encounterId;
	}

	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}
}
