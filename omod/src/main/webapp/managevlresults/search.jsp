<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="Manage VL Results" otherwise="/login.htm"/>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/carbon-grid-11.17.0-min.css" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/calendar.js" />

<%@ include file="../template/localHeader.jsp" %>

<h2>
	<openmrs:message code="disa.list.viral.load.results.manage" />
</h2>

<br/>

<b class="boxHeader">
	<spring:message code="disa.pesquisa.resultados.laboratoriais11" />
</b>

<form:form commandName="searchForm" method="GET">
	<fieldset>
		<div class="cds--css-grid">
			<div class="cds-css-grid-column">
				<label for="requestId">
					<openmrs:message code="disa.requestId" />:
				</label>
			</div>
			<div class="cds-css-grid-column cds--col-span-2">
				<form:input path="requestId" size="22" maxlength="16" id="requestId"/>
			</div>
			<div class="cds-css-grid-column">
				<label for="nid">
					<openmrs:message code="disa.nid" />:
				</label>
			</div>
			<div class="cds-css-grid-column cds--col-span-2">
				<form:input path="nid" size="22" maxlength="21" id="nid"/>
			</div>
			<div class="cds-css-grid-column">
				<label for="selSisma">
					<openmrs:message code="disa.sisma.code" />:
				</label>
			</div>
			<div class="cds-css-grid-column">
				<form:select path="vlSisma" id="selSisma" items="${sismaCodes}"/>
			</div>
			<div class="cds-css-grid-column">
				<label for="startDate">
					<openmrs:message code="disa.start.date" />:
				</label>
			</div>
			<div class="cds-css-grid-column">
				<form:input path="startDate"  size="10" id="startDate" onclick="showCalendar(this);" autocomplete="off"/>
			</div>
			<spring:hasBindErrors name="searchForm">
				<c:if test="${errors.hasFieldErrors('startDate')}">
					<div class="cds-css-grid-column cds--col-span-2">
						<form:errors path="startDate" cssClass="error"/>
					</div>
				</c:if>
			</spring:hasBindErrors>
			<div class="cds-css-grid-column">
				<label for="endDate">
					<openmrs:message code="disa.end.date" />:
				</label>
			</div>
			<div class="cds-css-grid-column">
				<form:input path="endDate" size="10" id="endDate" onclick="showCalendar(this);" autocomplete="off"/>
			</div>
			<spring:hasBindErrors name="searchForm">
				<c:if test="${errors.hasFieldErrors('endDate')}">
					<div class="cds-css-grid-column cds--col-span-2">
						<form:errors path="endDate" cssClass="error"/>
					</div>
				</c:if>
			</spring:hasBindErrors>
		</div>
		<div class="cds--css-grid">
			<div class="cds-css-grid-column">
				<label for="referringId">
					<openmrs:message code="disa.referring.request.id" />:
				</label>
			</div>
			<div class="cds-css-grid-column cds--col-span-2">
				<form:input path="referringId" size="22" maxlength="16" id="referringId"/>
			</div>
			<div class="cds-css-grid-column">
				<label for="vlState">
					<openmrs:message code="disa.frm.status" />:
				</label>
			</div>
			<div class="cds-css-grid-column cds--col-span-2">
				<form:select path="vlState" id="selValue">
				<form:option value="ALL">
					<openmrs:message code="disa.viral.load.status.ALL" />
				</form:option>
				<form:option value="PROCESSED">
					<openmrs:message code="disa.viral.load.status.PROCESSED" />
				</form:option>
				<form:option value="NOT_PROCESSED">
					<openmrs:message code="disa.viral.load.status.NOT_PROCESSED" />
				</form:option>
				<form:option value="PENDING">
					<openmrs:message code="disa.viral.load.status.PENDING" />
				</form:option>
			</form:select>
			</div>
		</div>

		<div class="submit-btn">
			<input id="subValue" type="submit" value='<openmrs:message code="general.next"/>'/>
		</div>
	</fieldset>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
