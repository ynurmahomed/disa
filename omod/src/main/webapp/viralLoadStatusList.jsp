<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/calendar.js"/>

<%@ include file="template/localHeader.jsp"%>

<h2><openmrs:message code="disa.pesquisa.nids.resultados.nao.processados11"/></h2>
<br />

<b class="boxHeader"><spring:message code="disa.select.viral.load.status" /></b>
<fieldset>
	<form:form commandName="searchForm" method="post">
		<table>
    		<tr>
	            <td><label for="startDate"><openmrs:message code="disa.start.date" />:</label></td>
	            <td><form:input path="startDate" type="text" size="10" name="startDate" id="startDate" onclick="showCalendar(this);"/>
					<form:errors path="startDate" cssClass="error"/>
	            <br /></td>
            </tr>
            <tr>
	            <td><label for="endDate"><openmrs:message code="disa.end.date"/>:</label></td>
            	<td><form:input path="endDate" type="text" size="10" name="endDate" id="endDate" onclick="showCalendar(this);"/>
					<form:errors path="endDate" cssClass="error"/>
            	<br />
            	</td>
            </tr>
		</table>
        <div class="submit-btn">
            <input id="subValue" type="submit" value='<openmrs:message code="general.next"/>'>
	        </div>
	</form:form>
</fieldset>

<%@ include file="/WEB-INF/template/footer.jsp"%>