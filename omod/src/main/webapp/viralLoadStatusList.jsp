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
	<form method="post">
	    	<table>
    		<tr>
	            <td><label for="startDate"><openmrs:message code="disa.start.date" />:</label></td>
	            <td><input type="text" size=10 name="startDate" id="startDate" onClick="showCalendar(this);" >
					<c:if test="${not empty errorStartDateRequired}">
						<span class="error"> <spring:message
								code="${errorStartDateRequired}"
								text="${errorStartDateRequired}" />
						</span>
					</c:if>
	            <br /></td>
            </tr>
            <tr>
	            <td><label for="endDate"><openmrs:message code="disa.end.date"/>:</label></td>
            	<td><input type="text" size=10 name="endDate" id="endDate" onClick="showCalendar(this);" >
					<c:if test="${not empty errorEndDateRequired}">
						<span class="error"> <spring:message
								code="${errorEndDateRequired}"
								text="${errorEndDateRequired}" />
						</span>
					</c:if>
            	<br />
            	</td>
            </tr>
<!--             <tr>
	            <td><label for="vlState"><openmrs:message code="disa.viral.load.status"/>:</label></td>
	            <td>
		            <select id="selValue" name="vlState"> 
		         		<option value="PROCESSED"><openmrs:message code="disa.viral.load.status.PROCESSED"/></option>
		         		<option value="NOT_PROCESSED"><openmrs:message code="disa.viral.load.status.NOT_PROCESSED"/></option>
		      		</select>
		      	</td>
    			</tr> -->
		</table>
        <div class="submit-btn">
            <input id="subValue" type="submit" value='<openmrs:message code="general.next"/>'>
	        </div>
	</form>
</fieldset>

<%@ include file="/WEB-INF/template/footer.jsp"%>