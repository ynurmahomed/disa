<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" /></a></li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/viralLoadStatusList") %>'>class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/disa/viralLoadStatusList.form"><spring:message code="disa.list.viral.load.results" /></a>
	</li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/viralLoadStagingServer") %>'>class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/disa/viralLoadStagingServer.form"><spring:message code="disa.list.viral.load.results.staging.server" /></a>
	</li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/managevlresults") %>'>class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/disa/managevlresults/search.form"><spring:message code="disa.list.viral.load.results.manage" /></a>
	</li>

	<!-- Add further links here -->
</ul>
