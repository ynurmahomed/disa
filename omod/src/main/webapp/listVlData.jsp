<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="web_form.css">
</head>
<body>
	<%@ page import = "java.util.*, cl.ciochile.victoraravena.*, org.openmrs.module.*, com.google.gson.Gson, com.google.gson.reflect.TypeToken"%> 	
	
		<%
			ApiAuthRest.setURLBase("http://localhost:8085/services/viralloads");
			ApiAuthRest.setUsername("disa");
			ApiAuthRest.setPassword("disa");
			
			List<String> sismaCodes = new ArrayList<String>();
			sismaCodes.add("1040114");
			
			String jsonViralLoadInfo = ApiAuthRest.getRequestGetFsrByStatus("/viral-status", sismaCodes, request.getParameter("vlState"));
			List<Disa> vlDataLst = new Gson().fromJson(jsonViralLoadInfo, new TypeToken<ArrayList<Disa>>() {}.getType());
			request.setAttribute("vlDataLst", vlDataLst);
		%>
		
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
				        	<td><a href='editVl.jsp?nid=<c:out value="${vlData.nid}"/>&name=<c:out value="${vlData.firstName} ${vlData.lastName}"/>&gender=<c:out value="${vlData.gender}"/>&age=<c:out value="${vlData.dateOfBirth}"/>&requestId=<c:out value="${vlData.requestId}"/>&vlCopies=<c:out value="${vlData.viralLoadResultCopies}"/>&vlLogs=<c:out value="${vlData.viralLoadResultLog}"/>&vlCoded=<c:out value="${vlData.hivViralLoadResult}"/>'>Mapear NID</a></td>
				        </c:if>		        
			    </tr>
			</c:forEach>
			
			
		</table>
</body>
</html>