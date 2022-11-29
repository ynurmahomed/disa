<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/calendar.js"/>

<%@ include file="template/localHeader.jsp"%>

<h2><openmrs:message code="disa.pesquisa.resultados.laboratoriais11"/></h2>
<br />

<b class="boxHeader"><spring:message code="disa.pesquisa.resultados.laboratoriais11" /></b>
<fieldset>
	<form:form commandName="searchForm" method="post">
	    <table>
    		<tr>
    		    <td><label for="requestId"><openmrs:message code="disa.requestId" />:</label></td>
	            <td><form:input path="requestId" size="22" maxlength="16" id="requestId"/></td>
	            <td><label for="nid"><openmrs:message code="disa.nid" />:</label></td>
	            <td><form:input path="nid" size="24" maxlength="21" id="nid"/></td>
	            <td><label for="selSisma"><openmrs:message code="disa.sisma.code" />:</label></td>
				<td>
					<form:select path="vlSisma" id="selSisma" items="${sismaCodes}"/>
		      	</td>
		      	<td><label for="startDate"><openmrs:message code="disa.start.date" />:</label></td>
	            <td><form:input path="startDate"  size="10" id="startDate" onclick="showCalendar(this);" autocomplete="off"/>
					<form:errors path="startDate" cssClass="error"/>
	            <br /></td>
		      	<td><label for="endDate"><openmrs:message code="disa.end.date"/>:</label></td>
            	<td><form:input path="endDate" size="10" id="endDate" onclick="showCalendar(this);" autocomplete="off"/>
					<form:errors path="endDate" cssClass="error"/>
            	<br />
            	</td>
            </tr>
            <tr>
    			<td><label for="referringId"><openmrs:message code="disa.referring.request.id" />:</label></td>
	            <td><form:input path="referringId" size="22" maxlength="16" id="referringId"/><br /></td>
			    <td><label for="vlState"><openmrs:message code="disa.frm.status"/>:</label></td>
	            <td>
		            <form:select path="vlState" id="selValue">
		            	<form:option value="ALL"><openmrs:message code="disa.viral.load.status.ALL"/></form:option>
		         		<form:option value="PROCESSED"><openmrs:message code="disa.viral.load.status.PROCESSED"/></form:option>
		         		<form:option value="NOT_PROCESSED"><openmrs:message code="disa.viral.load.status.NOT_PROCESSED"/></form:option>
		         		<form:option value="PENDING"><openmrs:message code="disa.viral.load.status.PENDING"/></form:option>
		      		</form:select>
		      	</td>
          </tr>
		</table>
        <div class="submit-btn">
            <input id="subValue" type="submit" value='<openmrs:message code="general.next"/>'>
	    </div>
	</form:form>
</fieldset>

<%@ include file="/WEB-INF/template/footer.jsp"%>