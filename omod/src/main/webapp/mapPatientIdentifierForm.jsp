<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<style>
	#vlResultsTable {
		font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
		border-collapse: collapse;
		width: 100%;
	}
	
	#vlResultsTable td, #vlResultsTable th {
		border: 1px solid #ddd;
		padding: 8px;
	}
	
	#vlResultsTable tr:nth-child(even) {
		background-color: #f2f2f2;
	}
	
	#vlResultsTable tr:hover {
		background-color: #ddd;
	}
	
	#vlResultsTable th {
		padding-top: 12px;
		padding-bottom: 12px;
		text-align: left;
		background-color: #1aac9b;
		color: white;
	}
	
	.submit-btn {
		flex: 1;
		margin: 10px 15px;
	}
	
	.submit-btn input {
		color: #fff;
		background: #1aac9b;
		padding: 8px;
		width: 12.8em;
		font-weight: bold;
		text-shadow: 0 0 .3em black;
		font-size: 9pt;
		border-radius: 5px 5px;
	}
</style>

<h2><openmrs:message code="disa.map.identifiers"/></h2>
<br />
<b class="boxHeader"><spring:message code="disa.patient" /></b>
<fieldset>
	<table  id="vlResultsTable">
		<tr>
			<th><spring:message code="disa.nid" /></th>
			<th><spring:message code="general.name" /></th>
			<th><spring:message code="disa.gender" /></th>
			<th><spring:message code="disa.age" /></th>
		</tr>	
	    <tr>   
	        <td>${selectedPatient.nid}</td>
	        <td>${selectedPatient.firstName} ${selectedPatient.lastName}</td>
	        <td>${selectedPatient.gender}</td>
	        <td>${selectedPatient.dateOfBirth}</td>
	    </tr>
	</table>
</fieldset>
<br />

<b class="boxHeader"><spring:message code="disa.openmrs.list.patients" /></b>
<fieldset>
	<table  id="vlResultsTable">
		<tr>
			<th><spring:message code="disa.nid" /></th>
			<th><spring:message code="general.name" /></th>
			<th><spring:message code="disa.gender" /></th>
			<th><spring:message code="disa.age" /></th>
		</tr>	
		<c:forEach items="${patients}" var="patient">
		    <tr>   
			        <td>${patient.identifiers}</td>
			        <td>${patient.givenName} ${patient.middleName} ${patient.familyName}</td>
			        <td>${patient.gender}</td>
			        <td>${patient.birthdate}</td>
		    </tr>
		</c:forEach>
	</table>
</fieldset>
<br />
	
<div class="submit-btn" align="center">
	<input type="submit"
		value='<spring:message code="general.previous"/>'
		name="previous"  onclick="history.back()"/>
	<input type="submit"
		value='<spring:message code="disa.btn.export"/>'
		name="exportViralLoadResults" />
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>