<%@ taglib prefix="springform" uri="http://www.springframework.org/tags/form"%>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Manage VL Results" otherwise="/login.htm"/>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css"/>
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/selectize.legacy.css" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/carbon-grid-11.17.0-min.css" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/selectize.min.js" />

<%@ include file="../template/localHeader.jsp"%>

<script type="text/javascript">
    $j(document).ready(() => {

        $j(".facility-search").selectize({
            plugins: ["clear_button"],
            load: async (term, callback) => {
                let results = [];

                if (!term.length) return callback(results);

                try {
                    document.body.style.cursor = 'wait';
                    const url = `/openmrs/module/disa/managevlresults/orgunits/search.form?term=\${term}`;
                    const fetchResponse = await fetch(url);
                    if (fetchResponse.status !== 200) {
                        throw new Error(`Search was not successful.`);
                    }
                    const data = await fetchResponse.json();
                    results = data.map((r) => ({
                        text: `\${r.province} > \${r.district} > \${r.facility}`,
                        value: r.code
                    }));
                } catch (error) {
                    console.log(error);
                } finally {
                    document.body.style.cursor = 'default';
                    callback(results);
                }
            }

        });
    })
</script>

<h2><openmrs:message code="disa.list.viral.load.results.manage"/></h2>
<br />

<b class="boxHeader">
	<spring:message code="disa.viralload.reallocate" /> ${requestId}
</b>


<form:form commandName="reallocateForm" method="POST" cssClass="reallocate-form">
    <fieldset>
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
    </fieldset>
</form:form>


<%@ include file="/WEB-INF/template/footer.jsp"%>
