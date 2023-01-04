<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="Manage VL Results" otherwise="/login.htm" redirect="/module/disa/managelabresults.form"/>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css" />
<openmrs:htmlInclude
	file="/scripts/jquery/dataTables/css/dataTables_jui.css" />

<%@ include file="../template/localHeader.jsp" %>

<h2>
	<openmrs:message code="disa.list.viral.load.results.manage" />
</h2>

<br/>

<div>
	<b class="boxHeader">
		<spring:message code="disa.pesquisa.resultados.laboratoriais11" />
	</b>
	<div class="box">
		<%@ include file="../common/searchForm.jsp" %>
	</div>
</div>

<div id="alert-box">
	<c:if test="${not empty flashMessage}">
		<div id="openmrs_msg">
			<b>${flashMessage}</b>
		</div>
	</c:if>
</div>

<c:if test="${not empty disaList}">
	<div>
		<div class="box">
			<table  id="vlResultsTable" style="width:100%; font-size:12px;">
				<thead>
					<tr>
						<th><spring:message code="disa.requesting.facility.name"/></th>
						<th><spring:message code="disa.requesting.district.name"/></th>
						<th><spring:message code="disa.sisma.code"/></th>
						<th><spring:message code="disa.referring.request.id"/></th>
						<th><spring:message code="disa.nid" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="disa.gender" /></th>
						<th><spring:message code="disa.age" /></th>
						<th><spring:message code="disa.request.id" /></th>
						<th><spring:message code="disa.analysis.date.time" /></th>
						<th><spring:message code="disa.authorised.date.time" /></th>
						<th><spring:message code="disa.viralload.result.copy" /></th>
						<th><spring:message code="disa.status" /></th>
						<th><spring:message code="disa.created.at" /></th>
						<th><spring:message code="disa.updated.at" /></th>
						<th><spring:message code="disa.not.processing.cause" /></th>
						<th><spring:message code="disa.manage.options" /></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${disaList}" var="vlData">
						<tr>
							<td>${vlData.requestingFacilityName}</td>
							<td>${vlData.requestingDistrictName}</td>
							<td>${vlData.healthFacilityLabCode}</td>
							<td>${vlData.referringRequestID}</td>
							<td>${vlData.nid}</td>
							<td>${vlData.firstName} ${vlData.lastName}</td>
							<td>${vlData.gender}</td>
							<td>${vlData.getAge() == 0 ? "" : vlData.getAge()}</td>
							<td>${vlData.requestId}</td>
							<td>${vlData.processingDate.substring(0,10)}</td>
							<td>${vlData.viralLoadResultDate.substring(0,10)}</td>
							<td>${vlData.finalViralLoadResult}</td>
							<td><openmrs:message code="disa.viral.load.status.${vlData.viralLoadStatus}"/></td>
							<td>${vlData.createdAt.substring(0,10)}</td>
							<td>${vlData.updatedAt.substring(0,10)}</td>
							<td>
								<c:if test="${not empty vlData.notProcessingCause}">
									<openmrs:message code="disa.${vlData.notProcessingCause}"/>
								</c:if>
							</td>
							<td>
								<ul class="actions">
									<c:if test="${vlData.viralLoadStatus == 'NOT_PROCESSED'}">
										<li>
											<a href="#" data-requestid="${vlData.requestId}" class="reschedule-vl">
												<spring:message code="disa.viralload.reschedule" />
											</a>
										</li>
									</c:if>
									<c:if test="${vlData.viralLoadStatus == 'NOT_PROCESSED'}">
										<li>
											<a href="${pageContext.request.contextPath}/module/disa/managelabresults/${vlData.requestId}/reallocate.form">
												<spring:message code="disa.viralload.reallocate" />
											</a>
										</li>
									</c:if>
									<li>
										<a href="#" data-requestid="${vlData.requestId}" class="delete-vl">
											<spring:message code="disa.viralload.delete" />
										</a>
									</li>
								</ul>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
			<br />
			<div class="submit-btn center">
				<input type="button"
					value='<spring:message code="disa.btn.export"/>'
					name="exportViralLoadResults" onclick="window.location.href = '${exportUri}'"/>
			</div>
		</div>
	</div>
</c:if>

<c:if test="${not empty searchForm.startDate && not empty searchForm.endDate}">
	<c:if test="${empty disaList}">
		<div id="openmrs_msg">
			<b> <spring:message code="disa.no.viral.load.form" /></b>
		</div>
	</c:if>
</c:if>

<openmrs:htmlInclude
	file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />

<script type="text/javascript">
	async function handleReschedule(event) {

		event.preventDefault();

		const anchor = event.currentTarget;
		const requestId = anchor.dataset.requestid;
		const headers = {"Content-Type": "application/json"}
		const body = JSON.stringify({viralLoadStatus: "PENDING"});
		const options = { method: "PATCH", headers, body };

		try {

			document.body.style.cursor = 'wait';

			const response = await fetch(`managelabresults/\${requestId}.form`, options);

			if (response.status === 200) {
				sessionStorage.setItem("flashMessage", "<spring:message code='disa.viralload.reschedule.successful'/>");
				location.reload();
			} else {
				throw new Error(`Reschedule was not successful.`)
			}
		} catch (error) {
			console.error(error);
			alert("<spring:message code='disa.viralload.delete.error'/>");
		} finally {
			document.body.style.cursor = 'default';
		}
	}

	async function handleDelete(event) {

		event.preventDefault();

		const anchor = event.currentTarget;
	        const requestId = anchor.dataset.requestid;
        	if (confirm(`<spring:message code='disa.viralload.delete.confirmation.javascript'/>`)) {
				try {
					document.body.style.cursor = 'wait';
					const response = await fetch(`managelabresults/\${requestId}.form`, { method: "DELETE" });
					if (response.status === 204) {
						sessionStorage.setItem("flashMessage", "<spring:message code='disa.viralload.delete.successful'/>");
						location.reload();
					} else {
						throw new Error(`Delete was not successful.`);
					}
				} catch (error) {
					console.error(error);
					alert("<spring:message code='disa.viralload.delete.error'/>");
				} finally {
					document.body.style.cursor = 'default';
				}
        	}
    	}


	$j(document).ready(function() {
		for (const a of document.querySelectorAll(".delete-vl")) {
			a.addEventListener('click', handleDelete);
		}

		for (const a of document.querySelectorAll(".reschedule-vl")) {
			a.addEventListener('click', handleReschedule);
		}

		$j('#vlResultsTable').dataTable({
			"iDisplayLength" : 10
		});

		// Display a temporary success message if present on sessionStorage
		const alertBox = document.getElementById("alert-box");
		const message = sessionStorage.getItem("flashMessage");
		if(message) {
			const openMRSMsg = document.createElement("div");
			openMRSMsg.innerText = message;
			openMRSMsg.id = "openmrs_msg";
			alertBox.appendChild(openMRSMsg);
			sessionStorage.removeItem("flashMessage");
		}
	});
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
