<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" /></a></li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/viralLoadForm") %>'>class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/disa/viralLoadForm.form"><spring:message code="disa.manage" /></a>
	</li>
	
	<li
		<c:if test='<%= request.getRequestURI().contains("/viralLoadList") %>'>class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/disa/viralLoadList.form"><spring:message code="disa.manage" /></a>
	</li>
	
	<li
		<c:if test='<%= request.getRequestURI().contains("/editNotProcessedForm") %>'>class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/disa/editNotProcessedForm.form"><spring:message code="disa.manage" /></a>
	</li>
	
	<!-- Add further links here -->
</ul>
