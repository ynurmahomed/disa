<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>

<%@ include file="template/localHeader.jsp"%>

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
	        <td>${selectedPatient.getAge()}</td>
	    </tr>
	</table>
</fieldset>
<br />

<b class="boxHeader"><spring:message code="disa.openmrs.list.patients" /></b>
<fieldset>
	<form method="post" action="addPatient.form">
		<spring:message code="general.search" />:
		<spring:bind path="patient">
			<openmrs_tag:patientField formFieldName="patientId"
				searchLabelCode="Patient.find" initialValue="${status.value.patientId}"
			linkUrl="" callback="" allowSearch="true" />
		<c:if test="${not empty errorPatientRequired}">
			<span class="error"> <spring:message
					code="${errorPatientRequired}"
					text="${errorPatientRequired}" />
			</span>
		</c:if>
		</spring:bind>
		<div class="submit-btn">
			<input type="submit" value='<spring:message code="general.add"/>'
				name="addPatient" id="btn-addPatient" />
		</div>
	</form>

	<c:if test="${not empty errorSelectPatient}">
		<div id="error_msg">
			<span> <spring:message code="${errorSelectPatient}" /></span>
			<br />
		</div>
	</c:if>
	<form method="post" action="mapPatientIdentifierForm.form">
		
		<table  id="vlResultsTable">
			<tr>
				<th><spring:message code="disa.nid" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="disa.gender" /></th>
				<th><spring:message code="disa.age" /></th>
				<th><spring:message code="general.select" /></th>
			</tr>	
			<c:forEach items="${patients}" var="patient">
			    <tr>   
			        <td>${patient.identifiers.iterator().next()}</td>
			        <td>${patient.givenName} ${patient.middleName} ${patient.familyName}</td>
			        <td>${patient.gender}</td>
			        <td>${patient.getAge()}</td>
				    <td>
			    		<input type="hidden" id="nid" name="nid" value="${selectedPatient.nid}" />
	      					<input type="radio" name="patientUuid" id="patientUuid" value="${patient.uuid}" />
				    </td>
			    </tr>
			</c:forEach>
		</table>
		<br />
		<div class="submit-btn" align="center">
			<input type="button" value='<spring:message code="general.previous"/>'
				name="previous"  onclick="history.back()"/>
			<input type="submit" value='<spring:message code="disa.btn.map"/>'
				name="mapIdentifier" id="btn-mapIdentifier" />
		</div>
	</form>
</fieldset>

<%@ include file="/WEB-INF/template/footer.jsp"%>