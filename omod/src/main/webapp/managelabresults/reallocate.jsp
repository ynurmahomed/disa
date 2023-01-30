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
                <input type="button"
                        value='<spring:message code="general.previous"/>'
                        name="previous"  onclick="history.back()"/>
                <input type="submit" value="<openmrs:message code='disa.viralload.reallocate'/>"/>
            </div>
        </form:form>
	</div>
</div>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/selectize/0.15.2/selectize.min.js" />

<script type="text/javascript">

    const errorMsgBox = document.getElementById("error_msg");

    $j(document).ready(() => {

        $j(".facility-search").selectize({
            placeholder: '<spring:message code="disa.viralload.reallocate.select.placeholder" htmlEscape="false"/>',
            plugins: ["clear_button"],
            load: async (term, callback) => {
                let results = [];

                errorMsgBox.innerText = "";

                if (!term.length) return callback(results);

                try {
                    document.body.style.cursor = 'wait';
                    const url = `/openmrs/module/disa/orgunits/search.form?term=\${term}`;
                    const fetchResponse = await fetch(url);
                    if (fetchResponse.status !== 200) {
                        const error = await fetchResponse.json();
                        throw new Error(error.message);
                    }
                    const data = await fetchResponse.json();
                    results = data.map((r) => ({
                        text: `\${r.province} > \${r.district} > \${r.facility}`,
                        value: r.code
                    }));
                } catch (error) {
                    errorMsgBox.innerText = error.message;
                    console.log(error);
                } finally {
                    document.body.style.cursor = 'default';
                    callback(results);
                }
            }

        });
    })
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
