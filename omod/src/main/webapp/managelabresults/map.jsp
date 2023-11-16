<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Mapear pacientes no Disa Interoperabilidade" otherwise="/login.htm" redirect="/module/disa/managelabresults.form"/>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/datatables.net/1.13.2/jquery.dataTables.min.css" />

<a href="${lastSearchUri}">Voltar</a>

<h2>
	<openmrs:message code="disa.map.identifiers"/>
</h2>

<br />

<div>
	<div class="boxHeader disaHeader">
		<img src="${pageContext.request.contextPath}/moduleResources/disa/img/disalab.ico" alt="" style="width: 20px; height: 20px;">
		<b>
			<spring:message code="disa.patient" />
		</b>
	</div>
	<div class="box disaBox">
		<table id="disaPatient" class="disa-table disa-table-clear" width="100%">
			<thead>
				<tr>
					<th><spring:message code="disa.nid" /></th>
					<th><spring:message code="general.name" /></th>
					<th><spring:message code="disa.gender" /></th>
					<th><spring:message code="disa.age" /></th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>${labResult.nid}</td>
					<td>${labResult.firstName} ${labResult.lastName}</td>
					<td>${labResult.gender}</td>
					<td>${labResult.getAge()}</td>
					<td></td>
				</tr>
			</tbody>
		</table>
	</div>
</div>

<br/>

<%@ include file="../common/alertBox.jspf" %>

<div>
	<div class="boxHeader">
		<img src="/openmrs/images/openmrs_logo_white.gif" alt="" style="width: 20px; height: 20px;">
		<b>
			<spring:message code="disa.openmrs.list.patients" />
		</b>
	</div>
	<div class="box">
		<form method="post">
			<input id="search" name="search" type="hidden"/>
			<table  id="patientListTable"  class="disa-table disa-table-clear" width="100%">
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
			
			
			<div class="submit-btn center">
				<button type="submit">
					<spring:message code="disa.btn.map"/>
				</button>
			</div>
		</form>
	</div>
</div>

<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/datatables.net/1.13.2/jquery.dataTables.min.js" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/mapIdentifiers.js" />
<script type="text/javascript">
	window.addEventListener("DOMContentLoaded", () => {
		new MapIdentifiers("#disaPatient","#patientListTable", {locale: "${locale}", searchSuggestion: "${searchSuggestion}"});
	});
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>