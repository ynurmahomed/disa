<form:form commandName="searchForm" method="GET">
	<div class="searchFields">
		<div>
			<label for="startDate">
				<openmrs:message code="disa.start.date" />:
			</label>
			<form:input path="startDate"  size="10" id="startDate" onclick="showCalendar(this);" autocomplete="off" autofocus="true"/>
			<spring:hasBindErrors name="searchForm">
				<c:if test="${errors.hasFieldErrors('startDate')}">
					<form:errors path="startDate" cssClass="error"/>
				</c:if>
			</spring:hasBindErrors>
		</div>
		<div>
			<label for="endDate">
				<openmrs:message code="disa.end.date" />:
			</label>
			<form:input path="endDate" size="10" id="endDate" onclick="showCalendar(this);" autocomplete="off"/>
			<spring:hasBindErrors name="searchForm">
				<c:if test="${errors.hasFieldErrors('endDate')}">
					<form:errors path="endDate" cssClass="error"/>
				</c:if>
			</spring:hasBindErrors>
		</div>
		<div class="divider"></div>
		<div>
			<label for="requestId">
				<openmrs:message code="disa.requestId" />:
			</label>
			<form:input path="requestId" size="22" maxlength="26" id="requestId"/>
		</div>
		<div>
			<label for="referringId">
				<openmrs:message code="disa.referring.request.id" />:
			</label>
			<form:input path="referringId" size="22" maxlength="16" id="referringId"/>
		</div>
		<div>
			<label for="vlState">
				<openmrs:message code="disa.frm.status" />:
			</label>
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
		<div>
			<label for="notProcessingCause">
				<openmrs:message code="disa.notProcessingCause" />:
			</label>
			<form:select path="notProcessingCause">
				<form:option value="ALL">
					<openmrs:message code="disa.notProcessingCause.ALL" />
				</form:option>
				<form:option value="NID_NOT_FOUND">
					<openmrs:message code="disa.notProcessingCause.NID_NOT_FOUND" />
				</form:option>
				<form:option value="NO_RESULT">
					<openmrs:message code="disa.notProcessingCause.NO_RESULT" />
				</form:option>
				<form:option value="FLAGGED_FOR_REVIEW">
					<openmrs:message code="disa.notProcessingCause.FLAGGED_FOR_REVIEW" />
				</form:option>
				<form:option value="DUPLICATE_NID">
					<openmrs:message code="disa.notProcessingCause.DUPLICATE_NID" />
				</form:option>
				<form:option value="DUPLICATED_REQUEST_ID">
					<openmrs:message code="disa.notProcessingCause.DUPLICATED_REQUEST_ID" />
				</form:option>
			</form:select>
		</div>
		<div class="divider"></div>
		<div>
			<label for="nid">
				<openmrs:message code="disa.nid" />:
			</label>
			<form:input path="nid" size="22" maxlength="31" id="nid"/>
		</div>
		<div>
			<label for="selSisma">
				<openmrs:message code="disa.sisma.code" />:
			</label>
			<form:select path="vlSisma" id="selSisma">
				<form:option value="ALL">
					<openmrs:message code='disa.sisma.code.ALL' />
				</form:option>
				<form:options items="${sismaCodes}"/>
			</form:select>
		</div>
		<div>
			<button type="submit">
				<openmrs:message code="general.search"/>
			</button>
		</div>
	</div>
</form:form>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/calendar.js" />