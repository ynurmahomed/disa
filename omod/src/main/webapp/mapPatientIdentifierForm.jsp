<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>

<%@ include file="template/localHeader.jsp"%>

<h2><openmrs:message code="disa.map.identifiers"/></h2>
<br />
<div id="error_msg" hidden="hidden">
	<span> <spring:message code="disa.select.patient" /></span>
</div> 
<br />

<form method="post" action="mapPatientIdentifierForm.form">
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
       					<input type="radio" required="required" name="patientUuid" id="patientUuid" value="${patient.uuid}" />
				    </td>
			    </tr>
			</c:forEach>
		</table>
	</fieldset>
	<br />
		
	<div class="submit-btn" align="center">
		<input type="button" value='<spring:message code="general.previous"/>'
			name="previous"  onclick="history.back()"/>
		<input type="submit" value='<spring:message code="disa.btn.map"/>'
			name="mapIdentifier" id="btn-mapIdentifier" />
	</div>
</form>

<script type="text/javascript">
document.getElementById("patientUuid").oninvalid = function () {
	var divErrorMsg = document.querySelector("#error_msg");
	if (this.checked) {
		divErrorMsg.hidden = "hidden";
	} else {
		divErrorMsg.hidden = "";
		event.preventDefault();
	}
};
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>