<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Mapear pacientes no Disa Interoperabilidade" otherwise="/login.htm" redirect="/module/disa/managelabresults.form"/>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/datatables.net/1.13.2/jquery.dataTables.min.css" />

<h2>
	<openmrs:message code="disa.map.identifiers"/>
</h2>

<br />

<div>
	<b class="boxHeader">
		<spring:message code="disa.patient" />
	</b>
	<div class="box">
		<table class="disa-table">
			<tr>
				<th><spring:message code="disa.nid" /></th>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="disa.gender" /></th>
				<th><spring:message code="disa.age" /></th>
			</tr>
			<tr>
				<td>${labResult.nid}</td>
				<td>${labResult.firstName} ${labResult.lastName}</td>
				<td>${labResult.gender}</td>
				<td>${labResult.getAge()}</td>
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
				<button type="submit">
					<spring:message code="general.add"/>
				</button>
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
			<table  id="patientListTable"  class="disa-table" width="100%" cellpadding="2" cellspacing="0"
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
							<td>
								<a target="_blank" href="/openmrs/patientDashboard.form?patientId=${patient.patientId}">
									${patient.identifiers.iterator().next()}
								</a>
							</td>
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
				<button type="button" onclick="location.href='${lastSearchUri}'">
					<spring:message code="general.previous"/>
				</button>
				<button type="submit">
					<spring:message code="disa.btn.map"/>
				</button>
			</div>
		</form>
	</div>
</div>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/datatables.net/1.13.2/jquery.dataTables.min.js" />

<script type="text/javascript">
	window.addEventListener('DOMContentLoaded', () => {
		new DataTable('#patientListTable', {
			dom: 'rftip<"clear">l',
			language: {
				url: "${pageContext.request.contextPath}/moduleResources/disa/js/datatables.net/1.13.2/i18n/${locale}.json"
			},
		});
	});
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>