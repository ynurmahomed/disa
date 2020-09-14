<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>

<%@ include file="template/localHeader.jsp"%>

<h2><openmrs:message code="disa.list.viral.load.results"/></h2>
<br />

<c:if test="${not empty vlDataLst}">
	<b class="boxHeader"><spring:message code="disa.list.viral.load.results" /></b>
	<fieldset>
		<form method="get" action="mapPatientIdentifierForm.form">
			<table  id="vlResultsTable">
				<tr>
					<th><spring:message code="disa.nid" /></th>
					<th><spring:message code="general.name" /></th>
					<th><spring:message code="disa.gender" /></th>
					<th><spring:message code="disa.age" /></th>
					<th><spring:message code="disa.request.id" /></th>
					<th><spring:message code="disa.viralload.result.copy" /></th>
					<th><spring:message code="disa.viralload.result.log" /></th>
					<th><spring:message code="disa.viralload.result.coded" /></th>
			        <c:if test="${vlState == 'NOT_PROCESSED'}">
			        	<th><spring:message code="disa.map.nid" /></th>
			        </c:if>
				</tr>	
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
					        <c:if test="${vlData.viralLoadStatus == 'NOT_PROCESSED'}">
					        	<td><a href='mapPatientIdentifierForm.form?nid=<c:out value="${vlData.nid}"/>'><spring:message code="disa.map.nid" /></a></td>
					        </c:if>		        
				    </tr>
				</c:forEach>
			</table>
		</form>
		<br />
		<form method="post" action="viralLoadResultsList.form">
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
		<c:if test="${vlState == 'PROCESSED'}">
			<b> <spring:message code="disa.viral.load.status.processed" /></b>
		</c:if>
		<c:if test="${vlState == 'NOT_PROCESSED'}">
			<b> <spring:message code="disa.viral.load.status.not_processed" /></b>
		</c:if>
	</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>