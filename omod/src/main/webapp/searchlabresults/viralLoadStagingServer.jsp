<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require anyPrivilege="true" otherwise="/login.htm" redirect="/module/disa/searchlabresults/viralLoadStagingServer.form"/>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css" />

<%@ include file="../template/localHeader.jsp"%>

<h2><openmrs:message code="disa.pesquisa.resultados.laboratoriais11"/></h2>
<br />

<b class="boxHeader"><spring:message code="disa.pesquisa.resultados.laboratoriais11" /></b>

<%@ include file="../common/searchForm.jsp" %>

<c:if test="${not empty disaList}">
	<b class="boxHeader"><spring:message code="disa.lista.resultados.laboratoriais333" /></b>
	<fieldset>
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
					</tr>
				</c:forEach>
			</tbody>
		</table>
		<br/>
		<div class="submit-btn center">
			<input type="button"
				value='<spring:message code="disa.btn.export"/>'
				name="exportViralLoadResults" onclick="window.location.href = '${exportUri}'"/>
		</div>
	</fieldset>
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
	$j(document).ready(function() {
		$j('#vlResultsTable').dataTable({
			"iDisplayLength" : 10
		});
	})
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>