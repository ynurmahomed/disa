<%@ taglib prefix="springform" uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>

<%@ include file="../template/localHeader.jsp"%>

<openmrs:htmlInclude
	file="/scripts/jquery/dataTables/css/dataTables_jui.css" />
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
			const response = await fetch(`\${requestId}.form`, options);

			if (response.status === 200) {
				location.reload();
			} else {
				throw new Error(`Reschedule was not successful.`)
			}
		} catch (error) {
			console.error(error);
			alert("<spring:message code='disa.viralload.delete.error'/>");
		}
	}

	async function handleDelete(event) {

		event.preventDefault();

		const anchor = event.currentTarget;
        const requestId = anchor.dataset.requestid;
        if (confirm(`<spring:message code='disa.viralload.delete.confirmation.javascript'/>`)) {
			try {
				document.body.style.cursor = 'wait';
				const response = await fetch(`\${requestId}.form`, { method: "DELETE" });
				if (response.status === 204) {
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
	})
</script>

<h2><openmrs:message code="disa.list.viral.load.results.manage"/></h2>
<br />

<c:if test="${not empty disaList}">
	<b class="boxHeader"><spring:message code="disa.lista.resultados.laboratoriais333" /></b>
	<fieldset>
		<!-- TODO: Handle download. The controller expects disaList in the session -->
		<form method="post" action="/openmrs/module/disa/viralLoadStagingServerResult.form">
			<table  id="vlResultsTable" class="display" width="100%" cellpadding="2" cellspacing="0"
				style="font-size: 13px;">
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
						<th></th>
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
					        <td>${vlData.getAge()}</td>
					        <td>${vlData.requestId}</td>
					        <td>${vlData.processingDate}</td>
					        <td>${vlData.viralLoadResultDate}</td>
					        <td>${vlData.finalViralLoadResult}</td>
					        <td><openmrs:message code="disa.viral.load.status.${vlData.viralLoadStatus}"/></td>
					        <td>${vlData.createdAt}</td>
					        <td>${vlData.updatedAt}</td>
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
			<div class="submit-btn" align="center">
				<input type="button"
					value='<spring:message code="general.previous"/>'
					name="previous"  onclick="history.back()"/>
				<input type="submit"
					value='<spring:message code="disa.btn.export"/>'
					name="exportViralLoadResults" />
			</div>
		</form>
	</fieldset>
</c:if>

<c:if test="${empty disaList}">
	<div id="openmrs_msg">
		<b> <spring:message code="disa.no.viral.load.form" /></b>
	</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>
