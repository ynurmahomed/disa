<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>

<%@ include file="template/localHeader.jsp"%>

<openmrs:htmlInclude
	file="/scripts/jquery/dataTables/css/dataTables_jui.css" />
<openmrs:htmlInclude
	file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />

<script type="text/javascript">
	$j(document).ready(function() {
		$j('#vlResultsTable').dataTable({
			"iDisplayLength" : 10
		});
	})
</script>

<h2><openmrs:message code="disa.list.viral.load.results"/></h2>
<br />

<c:if test="${not empty vlDataLst}">
	<b class="boxHeader"><spring:message code="disa.list.viral.load.results" /></b>
	<fieldset>
		<form method="post" action="viralLoadResultsList.form">
			<table  id="vlResultsTable" class="display" width="100%" cellpadding="2" cellspacing="0"
				style="font-size: 13px;">
				<thead>
					<tr>
						<th><spring:message code="disa.nid" /></th>
						<th><spring:message code="general.name" /></th>
						<th><spring:message code="disa.gender" /></th>
						<th><spring:message code="disa.age" /></th>
						<th><spring:message code="disa.request.id" /></th>
						<th><spring:message code="disa.viralload.result.copy" /></th>
						<th><spring:message code="disa.viralload.result.log" /></th>
						<th><spring:message code="disa.viralload.result.coded" /></th>
				        <th><spring:message code="disa.status" /></th>
				        <th><spring:message code="disa.not.processing.cause" /></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${vlDataLst}" var="vlData">
					    <tr>   
					        <td>${vlData.nid}</td>
					        <td>${vlData.firstName} ${vlData.lastName}</td>
					        <td>${vlData.gender}</td>
					        <td>${vlData.getAge()}</td>
					        <td>${vlData.requestId}</td>
					        <td>${vlData.viralLoadResultCopies}</td>
					        <td>${vlData.viralLoadResultLog}</td>
					        <td>${vlData.hivViralLoadResult}</td>
					        <td>${vlData.viralLoadStatus}"</td>
					        <td>${vlData.notProcessingCause}"</td>
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

<c:if test="${empty vlDataLst}">
	<div id="openmrs_msg">
		<b> <spring:message code="disa.no.viral.load" /></b>
		<b> <spring:message code="disa.viral.load.status.${vlState}" /></b>
	</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>