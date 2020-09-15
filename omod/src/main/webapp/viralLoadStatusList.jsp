<%@ taglib prefix="springform"
	uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>

<%@ include file="template/localHeader.jsp"%>

<h2><openmrs:message code="disa.list.viral.load.results"/></h2>
<br />

<b class="boxHeader"><spring:message code="disa.select.viral.load.status" /></b>
<fieldset>
	<form method="post">
	
	    <p>
	        <div class="row">
	            <label for="vlState"><openmrs:message code="disa.viral.load.status"/>:</label>
	            <select id="selValue" name="vlState"> 
	         		<option value="PROCESSED"><openmrs:message code="disa.viral.load.status.processed"/></option>
	         		<option value="NOT_PROCESSED"><openmrs:message code="disa.viral.load.status.not_processed"/></option>
	      		</select>
	        </div><br>
	        <div class="submit-btn">
	            <input id="subValue" type="submit" value='<openmrs:message code="general.next"/>'>
	        </div>
	    </p>
	
	</form>
</fieldset>
<%@ include file="/WEB-INF/template/footer.jsp"%>