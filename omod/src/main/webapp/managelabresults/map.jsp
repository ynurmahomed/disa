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

<script type="text/javascript">
	const nidTarv = i => i.identifierType.uuid === 'e2b966d0-1d5f-11e0-b929-000c29ad1d07';
	window.addEventListener('DOMContentLoaded', () => {
		new DataTable("#disaPatient", {
			dom: 't',
			columns: [
				{data:"nid", orderable:false, width: "30%"},
				{data:"name", orderable:false, width: "30%"},
				{data:"gender", orderable:false, width: "16%"},
				{data:"age", orderable:false, width: "16%"},
				{render: ()  => null, orderable: false, width: "8%"}
			]
		});
		const table = new DataTable('#patientListTable', {
			search: {
				search: "${searchSuggestion}",
			},
			initComplete: () => {
				document.querySelector('div.dataTables_filter input').focus();
			},
			dom: '<"disa-table-search"f>trip<"clear">l',
			pagingType: "simple",
			language: {
				url: "${pageContext.request.contextPath}/moduleResources/disa/js/datatables.net/1.13.2/i18n/${locale}.json"
			},
			processing: true,
			serverSide: true,
			ajax: (data, callback, settings) => {
				if (data.search.value) {
					DWRPatientService.findCountAndPatients(data.search.value, data.start, data.length, true, (res) => {
						callback({data: res.objectList, recordsTotal: res.count, recordsFiltered: res.count});
					});
				} else {
					callback({data: [], recordsTotal: 0});
				}
			},
			columns: [
				{
					data: "nid", 
					render: (data, type, row) =>  {
						const link = document.createElement("a");
						link.target = "_blank";
						link.href = `/openmrs/patientDashboard.form?patientId=\${row.patientId}`;
						link.innerText = row.identifier;
						return link.outerHTML;
					},
					orderable: false,
					width: "30%"
				},
				{
					data: "name", 
					render: (data, type, row) => `\${row.givenName} \${row.familyName}`,
					orderable: false,
					width: "30%"
				},
				{
					data: "gender",
					orderable: false,
					width: "16%"
				},
				{
					data: "age",
					orderable: false,
					width: "16%"
				},
				{
					render: (data, type, row) => {
						const radio = document.createElement("input");
						radio.type = "radio";
						radio.name = "patientUuid";
						radio.value = row.uuid;
						return radio.outerHTML;
					},
					orderable: false,
					width: "8%",
				}
			]
		});

		table.on("search.dt", () => {
			document.getElementById("search").value = table.search();
		});
	});
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>