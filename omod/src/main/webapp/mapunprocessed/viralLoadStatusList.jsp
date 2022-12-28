<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require anyPrivilege="true" otherwise="/login.htm" redirect="/module/disa/mapunprocessed/viralLoadStatusList.form"/>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/calendar.js"/>

<%@ include file="../template/localHeader.jsp"%>

<h2><openmrs:message code="disa.pesquisa.nids.resultados.nao.processados11"/></h2>
<br />

<div>
	<b class="boxHeader"><spring:message code="disa.select.viral.load.status" /></b>
	<div class="box">
		<form:form commandName="searchForm" method="post">
			<div class="searchFields">
				<div>
					<label for="startDate">
						<openmrs:message code="disa.start.date" /><span class="required">*</span>:
					</label>
					<form:input path="startDate"  size="10" id="startDate" onclick="showCalendar(this);" autocomplete="off"/>
					<spring:hasBindErrors name="searchForm">
						<c:if test="${errors.hasFieldErrors('startDate')}">
							<form:errors path="startDate" cssClass="error"/>
						</c:if>
					</spring:hasBindErrors>
				</div>
				<div>
					<label for="endDate">
						<openmrs:message code="disa.end.date" /><span class="required">*</span>:
					</label>
					<form:input path="endDate" size="10" id="endDate" onclick="showCalendar(this);" autocomplete="off"/>
					<spring:hasBindErrors name="searchForm">
						<c:if test="${errors.hasFieldErrors('endDate')}">
							<form:errors path="endDate" cssClass="error"/>
						</c:if>
					</spring:hasBindErrors>
				</div>
				<div>
					<button type="submit">
						<openmrs:message code="general.next"/>
					</button>
				</div>
			</div>
		</form:form>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>