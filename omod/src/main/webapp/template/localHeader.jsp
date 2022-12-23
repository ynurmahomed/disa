<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" /></a></li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/mapunprocessed") %>'>class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/disa/mapunprocessed/viralLoadStatusList.form"><spring:message code="disa.list.viral.load.results" /></a>
	</li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/searchlabresults") %>'>class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/disa/searchlabresults/viralLoadStagingServer.form"><spring:message code="disa.list.viral.load.results.staging.server" /></a>
	</li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/managelabresults") %>'>class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/disa/managelabresults.form"><spring:message code="disa.list.viral.load.results.manage" /></a>
	</li>

	<!-- Add further links here -->
</ul>
