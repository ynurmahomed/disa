<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<style>
	#vlResultsTable {
		font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
		border-collapse: collapse;
		width: 100%;
	}
	
	#vlResultsTable td, #vlResultsTable th {
		border: 1px solid #ddd;
		padding: 8px;
	}
	
	#vlResultsTable tr:nth-child(even) {
		background-color: #f2f2f2;
	}
	
	#vlResultsTable tr:hover {
		background-color: #ddd;
	}
	
	#vlResultsTable th {
		padding-top: 12px;
		padding-bottom: 12px;
		text-align: left;
		background-color: #1aac9b;
		color: white;
	}
	
	.submit-btn {
		flex: 1;
		margin: 10px 15px;
	}
	
	.submit-btn input {
		color: #fff;
		background: #1aac9b;
		padding: 8px;
		width: 12.8em;
		font-weight: bold;
		text-shadow: 0 0 .3em black;
		font-size: 9pt;
		border-radius: 5px 5px;
	}
</style>

<h2><openmrs:message code="disa.list.viral.load.results"/></h2>
<br />
<b class="boxHeader"><spring:message code="disa.list.viral.load.results" /></b>
<fieldset>
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
	        <c:if test="${vlData.viralLoadStatus == 'NOT_PROCESSED'}">
	        	<th><spring:message code="disa.map.nid" /></th>
	        </c:if>
		</tr>	
		<c:forEach items="${vlDataLst}" var="vlData">
		    <tr>   
			        <td>${vlData.nid}</td>
			        <td>${vlData.firstName} ${vlData.lastName}</td>
			        <td>${vlData.gender}</td>
			        <td>${vlData.dateOfBirth}</td>
			        <td>${vlData.requestId}</td>
			        <td>${vlData.viralLoadResultCopies}</td>
			        <td>${vlData.viralLoadResultLog}</td>
			        <td>${vlData.hivViralLoadResult}</td>
			        <c:if test="${vlData.viralLoadStatus == 'NOT_PROCESSED'}">
			        	<td><a href='mapPatientIdentifierForm.form?nid=<c:out value="${vlData.nid}"/>&name=<c:out value="${vlData.firstName} ${vlData.lastName}"/>&gender=<c:out value="${vlData.gender}"/>&age=<c:out value="${vlData.dateOfBirth}"/>&requestId=<c:out value="${vlData.requestId}"/>&vlCopies=<c:out value="${vlData.viralLoadResultCopies}"/>&vlLogs=<c:out value="${vlData.viralLoadResultLog}"/>&vlCoded=<c:out value="${vlData.hivViralLoadResult}"/>'><spring:message code="disa.map.nid" /></a></td>
			        </c:if>		        
		    </tr>
		</c:forEach>
	</table>
	<br />
	
	<div class="submit-btn" align="center">
		<input type="submit"
			value='<spring:message code="general.previous"/>'
			name="previous"  onclick="history.back()"/>
		<input type="submit"
			value='<spring:message code="disa.btn.export"/>'
			name="exportViralLoadResults" />
	</div>
</fieldset>

<%@ include file="/WEB-INF/template/footer.jsp"%>