<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<h2><openmrs:message code="disa.list.viral.load.results"/></h2>

	<table border="1">
	 
		<tr>
				<td>NID</td>
		        <td>Nome</td>
		        <td>Sexo</td>
		        <td>Idade</td>
		        <td>ID(Requisição)</td>
		        <td>Carga Viral(Cópia)</td>
		        <td>Carga Viral(Log)</td>
		        <td>Carga Viral(Coded)</td>  
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
			        	<td><a href='editNotProcessedForm.form?nid=<c:out value="${vlData.nid}"/>&name=<c:out value="${vlData.firstName} ${vlData.lastName}"/>&gender=<c:out value="${vlData.gender}"/>&age=<c:out value="${vlData.dateOfBirth}"/>&requestId=<c:out value="${vlData.requestId}"/>&vlCopies=<c:out value="${vlData.viralLoadResultCopies}"/>&vlLogs=<c:out value="${vlData.viralLoadResultLog}"/>&vlCoded=<c:out value="${vlData.hivViralLoadResult}"/>'>Mapear NID</a></td>
			        </c:if>		        
		    </tr>
		</c:forEach>
		
	</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>