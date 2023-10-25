<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="Pesquisar resultados no Disa Interoperabilidade" otherwise="/login.htm"
	redirect="/module/disa/managelabresults.form" />

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/disa.css" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/datatables.net/1.13.2/jquery.dataTables.min.css" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/css/buttons/2.3.4/buttons.dataTables.min.css" />

<h2>
	<openmrs:message code="disa.list.viral.load.results.manage" />
</h2>


<p>
	<small>
		<span class="last-execution ${(syncStatus.currentExecution != null || syncStatus.lastExecution == null) ? 'hidden' : ''}">${syncStatus.lastExecution}</span>
		<span class="current-execution ${(syncStatus.currentExecution == null) ? 'hidden' : ''}">${syncStatus.currentExecution}</span>
	</small>
</p>

<div>
	<b class="boxHeader">
		<spring:message code="disa.pesquisa.resultados.laboratoriais11" />
	</b>
	<div class="box">
		<%@ include file="../common/searchForm.jspf" %>
	</div>
</div>

<%@ include file="../common/alertBox.jspf" %>

<c:if test="${not empty disaPage.resultList}">
	<div>
		<div class="box">
			<table id="vlResultsTable" class="disa-table disa-table-results" style="width:100%; font-size:12px;">
				<thead>
					<tr>
						<th>id</th>
						<th>
							<spring:message code="disa.requesting.facility.name" />
						</th>
						<th>
							<spring:message code="disa.requesting.district.name" />
						</th>
						<th>
							<spring:message code="disa.sisma.code" />
						</th>
						<th>
							<spring:message code="disa.nid" />
						</th>
						<th>
							<spring:message code="general.name" />
						</th>
						<th>
							<spring:message code="disa.gender" />
						</th>
						<th>
							<spring:message code="disa.age" />
						</th>
						<th>
							<spring:message code="disa.request.id" />
						</th>
						<th>
							<spring:message code="disa.harvest.date" />
						</th>
						<th>
							<spring:message code="disa.authorised.date.time" />
						</th>
						<th>
							<spring:message code="disa.lab.result" />
						</th>
						<th>
							<spring:message code="disa.lab.result.type" />
						</th>
						<th>
							<spring:message code="disa.status" />
						</th>
						<th>
							<spring:message code="disa.created.at" />
						</th>
						<th>
							<spring:message code="disa.updated.at" />
						</th>
						<th>
							<spring:message code="disa.notProcessingCause" />
						</th>
						<th>
							<spring:message code="disa.manage.table.header" />
						</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${disaPage.resultList}" var="labResult">
						<tr>
							<td>${labResult.id}</td>
							<td>${labResult.requestingFacilityName}</td>
							<td>${labResult.requestingDistrictName}</td>
							<td>${labResult.healthFacilityLabCode}</td>
							<td>${labResult.nid}</td>
							<td>${labResult.firstName} ${labResult.lastName}</td>
							<td>${labResult.gender}</td>
							<td>${labResult.ageInYears}</td>
							<td>
								<c:if test="${labResult.encounterId != null}">
									<a target="_blank" href="/openmrs/module/htmlformentry/htmlFormEntry.form?encounterId=${labResult.encounterId}">
										${labResult.requestId}
									</a>
								</c:if>
								<c:if test="${labResult.encounterId == null}">
									${labResult.requestId}
								</c:if>
							</td>
							<td>${labResult.harvestDate.toString().substring(0,10)}</td>
							<td>${labResult.labResultDate.toString().substring(0,10)}</td>
							<td>${labResult.finalResult}</td>
							<td>${labResult.typeOfResult}</td>
							<td>${labResult.labResultStatus}</td>
							<td>${labResult.createdAt.toString().substring(0,10)}</td>
							<td>${labResult.updatedAt.toString().substring(0,10)}</td>
							<td>${labResult.notProcessingCause}</td>
							<td class="actions" style="text-align: center;">
								<c:if test="${labResult.labResultStatus != 'PROCESSED'}">

									<openmrs:message
											code="disa.manage.actions" />

									<div class="actions-tooltip" role="tooltip">
										<div class="arrow" data-popper-arrow></div>
										<ul>
											<c:if test="${labResult.labResultStatus == 'NOT_PROCESSED'}">
												<openmrs:hasPrivilege privilege="Reagendar resultados no Disa Interoperabilidade">
													<li>
														<a href="#" data-id="${labResult.id}"
															class="reschedule-vl">
															<spring:message code="disa.viralload.reschedule" />
														</a>
													</li>
												</openmrs:hasPrivilege>
												<c:if test="${labResult.notProcessingCause == 'NID_NOT_FOUND'}">
													<openmrs:hasPrivilege privilege="Mapear pacientes no Disa Interoperabilidade">
														<li>
															<a href='managelabresults/${labResult.id}/map.form'>
																<spring:message code="disa.map.nid" />
															</a>
														</li>
													</openmrs:hasPrivilege>
												</c:if>
											</c:if>
											<openmrs:hasPrivilege privilege="Realocar resultados no Disa Interoperabilidade">
												<li>
													<a href="managelabresults/${labResult.id}/reallocate.form">
														<spring:message code="disa.viralload.reallocate" />
													</a>
												</li>
											</openmrs:hasPrivilege>
											<openmrs:hasPrivilege privilege="Remover resultados no Disa Interoperabilidade">
												<li>
													<a href="#" data-id="${labResult.id}"
														class="delete-vl">
														<spring:message code="disa.viralload.delete" />
													</a>
												</li>
											</openmrs:hasPrivilege>
										</ul>
									</div>
								</c:if>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
			<br />
			<div class="submit-btn center">
				<button onclick="window.location.href = '${exportUri}'" >
					<spring:message code="disa.btn.export"/>
				</button>
			</div>
		</div>
	</div>
</c:if>

<c:if test="${empty disaPage.resultList}">
	<div id="openmrs_msg">
		<b>
			<spring:message code="disa.no.viral.load.form" />
		</b>
	</div>
</c:if>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/popperjs__core/2.11.6/popper.min.js" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/JSZip/2.5.0/jszip.min.js" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/datatables.net/1.13.2/jquery.dataTables.min.js" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/buttons/2.3.4/dataTables.buttons.min.js" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/buttons/2.3.4/buttons.colVis.min.js" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/buttons/2.3.4/buttons.html5.min.js" />

<%@ include file="../common/translations.jspf" %>

<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/resultsTable.js" />
<openmrs:htmlInclude file="${pageContext.request.contextPath}/moduleResources/disa/js/syncStatus.js" />

<script type="text/javascript">
	window.addEventListener("DOMContentLoaded", () => {
		ResultsTable("#vlResultsTable", {locale: "${locale}", totalResults: +"${disaPage.totalResults}"});
	});
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
