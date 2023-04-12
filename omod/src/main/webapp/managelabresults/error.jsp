<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css" />

<h2>
	<openmrs:message code="disa.list.viral.load.results.manage" />
</h2>

<br />

<div>
	<b class="boxHeader">
		<spring:message code="disa.pesquisa.resultados.laboratoriais11" />
	</b>
	<div class="box">
		<%@ include file="../common/searchForm.jspf" %>
	</div>
</div>

<%@ include file="../common/alertBox.jspf" %>
