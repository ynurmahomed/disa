<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>

<openmrs:htmlInclude
	file="/scripts/jquery/dataTables/css/dataTables_jui.css" />

<h2>
	<openmrs:message code="disa.map.identifiers"/>
</h2>

<br />

<div>
	<b class="boxHeader">
		<spring:message code="disa.patient" />
	</b>
	<div class="box">
		<table class="vlResultsTable">
			<tr>
				<th><spring:message code="disa.nid" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="disa.gender" /></th>
				<th><spring:message code="disa.age" /></th>
			</tr>
			<tr>
				<td>${disa.nid}</td>
				<td>${disa.firstName} ${disa.lastName}</td>
				<td>${disa.gender}</td>
				<td>${disa.getAge()}</td>
			</tr>
		</table>
	</div>
</div>

<div>
	<b class="boxHeader">
		<spring:message code="disa.openmrs.list.patients" />
	</b>
	<div class="box">
		<form method="post" action="map/addPatient.form">
			<spring:message code="general.search" />:
			<openmrs_tag:patientField formFieldName="patientId"
				searchLabelCode="Patient.find" linkUrl="" callback="" allowSearch="true" />
			<c:if test="${not empty errorPatientRequired}">
				<span class="error">
					<spring:message code="${errorPatientRequired}" text="${errorPatientRequired}" />
				</span>
			</c:if>
			<div class="submit-btn">
				<input type="submit" value='<spring:message code="general.add"/>'
					name="addPatient" id="btn-addPatient" />
			</div>
		</form>
	</div>
</div>

<div>
	<div class="box">
		<c:if test="${not empty errorSelectPatient}">
			<div id="error_msg">
				<spring:message code="${errorSelectPatient}" />
			</div>
		</c:if>
		<form method="post">
			<table  id="patientListTable"  class="display" width="100%" cellpadding="2" cellspacing="0"
					style="font-size: 13px;">
				<thead>
					<tr>
						<th><spring:message code="disa.nid" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="disa.gender" /></th>
						<th><spring:message code="disa.age" /></th>
						<th><spring:message code="general.select" /></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${patientList}" var="patient">
						<tr>
							<td>${patient.identifiers.iterator().next()}</td>
							<td>${patient.givenName} ${patient.middleName} ${patient.familyName}</td>
							<td>${patient.gender}</td>
							<td>${patient.getAge()}</td>
							<td>
								<input type="radio" name="patientUuid" id="patientUuid" value="${patient.uuid}" />
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
			<br />
			<div class="submit-btn center">
				<input type="button" value='<spring:message code="general.previous"/>'
					name="previous"  onclick="location.href='${lastSearchUri}'"/>
				<input type="submit" value='<spring:message code="disa.btn.map"/>'
					name="mapIdentifier" id="btn-mapIdentifier" />
			</div>
		</form>
	</div>
</div>

<openmrs:htmlInclude
	file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />

<script type="text/javascript">
	$j(document).ready(function() {
		$j('#patientListTable').dataTable({
			"iDisplayLength" : 10
		});
	})
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>