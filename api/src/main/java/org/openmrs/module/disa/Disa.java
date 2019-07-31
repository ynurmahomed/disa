package org.openmrs.module.disa;

import java.util.Date;

/**
 * 
 * @author machabane
 *
 */
@SuppressWarnings("unused")
public class Disa {
	
	private int id;
	
	private String uuid;
	
	private String createdBy;
	
	private String createdAt;
	
	private String updatedBy;
	
	private String updatedAt;
	
	private String entityStatus;
	
	private String nid;
	
	private Date dateOfBirth;
	
	private String healthFacilityLabCode;
	
	private String encounter;
	
	private String pregnant;
	
	private String breastFeeding;
	
	private String reasonForTest;
	
	private String harvestDate;
	
	private String harvestType;
	
	private String dateOfSampleReceive;
	
	private String processingDate;
	
	private String sampleType;
	
	private String viralLoadResultCopies;
	
	private String viralLoadResultLog;
	
	private String viralLoadResultDate;
	
	private String aprovedBy;
	
	private String labComments;
	
	private String hivViralLoadResult;
	
	private String requestingFacilityName;

	public String getNid() {
		return nid;
	}

	public String getHealthFacilityLabCode() {
		return healthFacilityLabCode;
	}
	
	public String getEncounter() {
		return encounter;
	}

	public String getPregnant() {
		return pregnant;
	}

	public String getBreastFeeding() {
		return breastFeeding;
	}

	public String getReasonForTest() {
		return reasonForTest;
	}

	public String getHarvestDate() {
		return harvestDate;
	}

	public String getHarvestType() {
		return harvestType;
	}

	public String getDateOfSampleReceive() {
		return dateOfSampleReceive;
	}

	public String getProcessingDate() {
		return processingDate;
	}

	public String getSampleType() {
		return sampleType;
	}

	public String getViralLoadResultCopies() {
		return viralLoadResultCopies;
	}

	public String getViralLoadResultLog() {
		return viralLoadResultLog;
	}

	public String getAprovedBy() {
		return aprovedBy;
	}

	public String getLabComments() {
		return labComments;
	}
	
	public String getHivViralLoadResult() {
		return hivViralLoadResult;
	}
	
	public String getRequestingFacilityName() {
		return requestingFacilityName;
	}
}
