<%@ taglib prefix="springform" uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Realocar resultados no Disa Interoperabilidade" otherwise="/login.htm" redirect="/module/disa/managelabresults/${requestId}/reallocate.form"/>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/selectize.legacy.css" />


<h2><openmrs:message code="disa.list.viral.load.results.manage"/></h2>
<br />

<div>
	<b class="boxHeader">
		<spring:message code="disa.viralload.reallocate" /> ${requestId}
	</b>
	<div class="box">
        <div id="error_msg">
        </div>
		<form:form commandName="reallocateForm" method="POST" cssClass="reallocate-form">
            <div class="field">
                <div class="label">
                    <label for="healthFacilityLabCode">
                        <openmrs:message code="disa.requesting.facility.name" />:
                    </label>
                </div>
                <div class="input">
                    <form:select path="healthFacilityLabCode" id="healthFacilityLabCode" class="facility-search">
                        <form:option value="${orgUnit.code}">${orgUnit.province} > ${orgUnit.district} > ${orgUnit.facility}</form:option>
                    </form:select>
                    <form:errors path="healthFacilityLabCode" cssClass="error"/>
                </div>
            </div>
            <div class="submit-btn center">
                <button type="button" onclick="history.back()">
                    <spring:message code="general.previous"/>
                </button>
                <button type="submit">
                    <openmrs:message code='disa.viralload.reallocate'/>
                </button>
            </div>
        </form:form>
	</div>
</div>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/selectize/0.15.2/selectize.min.js" />
<%@ include file="../common/translations.jspf" %>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/reallocateForm.js" />
<script type="text/javascript">
    $j(document).ready(() => {
        ReallocateForm("#reallocateForm", {requestId: "${requestId}"});
    });
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
