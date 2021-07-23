<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/calendar.js"/>

<%@ include file="template/localHeader.jsp"%>

<h2><openmrs:message code="disa.list.viral.load.results.staging.server"/></h2>
<br />

<b class="boxHeader"><spring:message code="disa.list.viral.load.results.staging.server" /></b>
<fieldset>
	<form method="post">
	    <table>
    		<tr>
	            <td><label for="startDate"><openmrs:message code="disa.requestId" />:</label></td>
	            <td><input type="text" size=16 name="requestId" id="requestId" >
					<c:if test="${not empty errorRequestIdRequired}">
						<span class="error"> <spring:message
								code="${errorRequestIdRequired}"
								text="${errorRequestIdRequired}" />
						</span>
					</c:if>
	            <br /></td>
            </tr>
		</table>
        <div class="submit-btn">
            <input id="subValue" type="submit" value='<openmrs:message code="general.next"/>'>
	    </div>
	</form>
</fieldset>

<%@ include file="/WEB-INF/template/footer.jsp"%>