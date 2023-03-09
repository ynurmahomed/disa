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

<br />

<div>
	<b class="boxHeader">
		<spring:message code="disa.pesquisa.resultados.laboratoriais11" />
	</b>
	<div class="box">
		<%@ include file="../common/searchForm.jsp" %>
	</div>
</div>

<div id="alert-box">
	<c:if test="${not empty flashMessage}">
		<div id="openmrs_msg">
			<b>${flashMessage}</b>
		</div>
	</c:if>
</div>

<c:if test="${not empty disaPage.resultList}">
	<div>
		<div class="box">
			<table id="vlResultsTable" class="vlResultsTable" style="width:100%; font-size:12px;">
				<thead>
					<tr>
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
							<spring:message code="disa.referring.request.id" />
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
							<spring:message code="disa.analysis.date.time" />
						</th>
						<th>
							<spring:message code="disa.authorised.date.time" />
						</th>
						<th>
							<spring:message code="disa.viralload.result.copy" />
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
					<c:forEach items="${disaPage.resultList}" var="vlData">
						<tr>
							<td>${vlData.requestingFacilityName}</td>
							<td>${vlData.requestingDistrictName}</td>
							<td>${vlData.healthFacilityLabCode}</td>
							<td>${vlData.referringRequestID}</td>
							<td>${vlData.nid}</td>
							<td>${vlData.firstName} ${vlData.lastName}</td>
							<td>${vlData.gender}</td>
							<td>${vlData.ageInYears}</td>
							<td>${vlData.requestId}</td>
							<td>${vlData.processingDate.substring(0,10)}</td>
							<td>${vlData.viralLoadResultDate.substring(0,10)}</td>
							<td>${vlData.finalViralLoadResult}</td>
							<td>${vlData.viralLoadStatus}</td>
							<td>${vlData.createdAt.substring(0,10)}</td>
							<td>${vlData.updatedAt.substring(0,10)}</td>
							<td>${vlData.notProcessingCause}</td>
							<td class="actions" style="text-align: center;">
								<c:if test="${vlData.viralLoadStatus != 'PROCESSED'}">

									<openmrs:message
											code="disa.manage.actions" />

									<div class="actions-tooltip" role="tooltip">
										<div class="arrow" data-popper-arrow></div>
										<ul>
											<c:if test="${vlData.viralLoadStatus == 'NOT_PROCESSED'}">
												<openmrs:hasPrivilege privilege="Reagendar resultados no Disa Interoperabilidade">
													<li>
														<a href="#" data-requestid="${vlData.requestId}"
															class="reschedule-vl">
															<spring:message code="disa.viralload.reschedule" />
														</a>
													</li>
												</openmrs:hasPrivilege>
												<c:if test="${vlData.notProcessingCause == 'NID_NOT_FOUND'}">
													<openmrs:hasPrivilege privilege="Mapear pacientes no Disa Interoperabilidade">
														<li>
															<a href='managelabresults/${vlData.requestId}/map.form'>
																<spring:message code="disa.map.nid" />
															</a>
														</li>
													</openmrs:hasPrivilege>
												</c:if>
											</c:if>
											<openmrs:hasPrivilege privilege="Realocar resultados no Disa Interoperabilidade">
												<li>
													<a href="managelabresults/${vlData.requestId}/reallocate.form">
														<spring:message code="disa.viralload.reallocate" />
													</a>
												</li>
											</openmrs:hasPrivilege>
											<openmrs:hasPrivilege privilege="Remover resultados no Disa Interoperabilidade">
												<li>
													<a href="#" data-requestid="${vlData.requestId}"
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
				<input type="button" value='<spring:message code="disa.btn.export"/>'
					name="exportViralLoadResults" onclick="window.location.href = '${exportUri}'" />
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

<script type="text/javascript">

	let table;

	// Translations
	var viralLoadStatus = {
		'PROCESSED': "<openmrs:message code='disa.viral.load.status.PROCESSED' />",
		'NOT_PROCESSED': "<openmrs:message code='disa.viral.load.status.NOT_PROCESSED' />",
		'PENDING': "<openmrs:message code='disa.viral.load.status.PENDING' />",
	};

	var notProcessingCause = {
		'NID_NOT_FOUND': "<openmrs:message code='disa.notProcessingCause.NID_NOT_FOUND' />",
		'NO_RESULT': "<openmrs:message code='disa.notProcessingCause.NO_RESULT'/>",
		'DUPLICATE_NID': "<openmrs:message code='disa.notProcessingCause.DUPLICATE_NID'/>",
		'FLAGGED_FOR_REVIEW': "<openmrs:message code='disa.notProcessingCause.FLAGGED_FOR_REVIEW'/>",
		'DUPLICATED_REQUEST_ID': "<openmrs:message code='disa.notProcessingCause.DUPLICATED_REQUEST_ID'/>",
	};

	/**
	 * Return the current OpenMRS user from session.
	 */
	async function getCurrentUser() {
		try {
			const response = await fetch("/openmrs/ws/rest/v1/session");
			if (response.status === 200) {
				const json = await response.json();
				return json.user;
			}
		} catch (e) {
			console.error(e);
		}
	}

	function hasPrivilege(user, privilege) {
		const systemDeveloper = user.roles.find(r => r.name === "System Developer");
		if(systemDeveloper) {
			return true;
		}
		return user.privileges.find((p) => p.name === privilege);
	}

	async function handleReschedule(event) {

		event.preventDefault();

		const anchor = event.currentTarget;
		const requestId = anchor.dataset.requestid;
		const headers = { "Content-Type": "application/json" }
		const options = { method: "POST", headers};

		try {

			document.body.style.cursor = 'wait';

			const response = await fetch(`managelabresults/\${requestId}/reschedule.form`, options);

			if (response.status === 200) {
				addFlashMessage("<spring:message code='disa.viralload.reschedule.successful'/>");
				table.draw(false);
			} else {
				throw new Error(`Reschedule was not successful.`)
			}
		} catch (error) {
			console.error(error);
			alert("<spring:message code='disa.unexpected.error'/>");
		} finally {
			document.body.style.cursor = 'default';
		}
	}

	async function handleDelete(event) {

		event.preventDefault();

		const anchor = event.currentTarget;
		const requestId = anchor.dataset.requestid;
		if (confirm(`<spring:message code='disa.viralload.delete.confirmation.javascript'/>`)) {
			try {
				document.body.style.cursor = 'wait';
				const response = await fetch(`managelabresults/\${requestId}.form`, { method: "DELETE" });
				if (response.status === 204) {
					addFlashMessage("<spring:message code='disa.viralload.delete.successful'/>");
					table.draw(false);
				} else {
					throw new Error(`Delete was not successful.`);
				}
			} catch (error) {
				console.error(error);
				alert("<spring:message code='disa.unexpected.error'/>");
			} finally {
				document.body.style.cursor = 'default';
			}
		}
	}

	/**
	 * Create tooltips for table actions.
	 */
	function createTooltips() {
		for (const toggle of document.querySelectorAll(".actions")) {
			const actions = toggle.querySelector(".actions-tooltip");

			// Only show tooltip if actions available
			if (actions) {

				// Makes the popper the same width as the reference.
				const sameWidth = {
					name: "sameWidth",
					enabled: true,
					phase: "beforeWrite",
					requires: ["computeStyles"],
					fn: ({ state }) => {
						state.styles.popper.width = `\${state.rects.reference.width}px`;
					},
					effect: ({ state }) => {
						state.elements.popper.style.width = `\${state.elements.reference.offsetWidth}px`;
					}
				};


				const popperInstance = Popper.createPopper(toggle, actions, {
					modifiers: [sameWidth, Popper.preventOverflow]
				});

				// Show/hide based on hover
				function show() {
					// Make the tooltip visible
					actions.setAttribute('data-show', '');

					// Enable the event listeners
					popperInstance.setOptions((options) => ({
						...options,
						modifiers: [
							...options.modifiers,
							{ name: 'eventListeners', enabled: true },
						],
					}));

					// Update its position
					popperInstance.update();
				}

				function hide () {
					// Hide the tooltip
					actions.removeAttribute('data-show');

					// Disable the event listeners
					popperInstance.setOptions((options) => ({
						...options,
						modifiers: [
							...options.modifiers,
							{ name: 'eventListeners', enabled: false },
						],
					}));
				}

				const showEvents = ['mouseenter', 'focus'];
				const hideEvents = ['mouseleave', 'blur'];

				showEvents.forEach((event) => {
					toggle.addEventListener(event, show);
				});

				hideEvents.forEach((event) => {
					toggle.addEventListener(event, hide);
				});
			}

		}
	}

	/**
	 * Add a message to be temporarily displayed.
	 */
	function addFlashMessage(message) {
		const alertBox = document.getElementById("alert-box");
		// Clear previous message
		if(alertBox.lastElementChild) {
			alertBox.lastElementChild.remove();
		}
		sessionStorage.setItem("flashMessage", message);
	}

	/**
	 * Display a temporary success message if present in sessionStorage.
	 */
	function showFlashMessage() {
		const alertBox = document.getElementById("alert-box");
		const message = sessionStorage.getItem("flashMessage");
		if (message) {
			const openMRSMsg = document.createElement("div");
			openMRSMsg.innerText = message;
			openMRSMsg.id = "openmrs_msg";
			alertBox.appendChild(openMRSMsg);
			sessionStorage.removeItem("flashMessage");
		}
	}

	/**
	 * Should execute everytime the table is drawn.
	 */
	function postDraw() {
		createTooltips();
		// Add handlers for delete link
		for (const a of document.querySelectorAll(".delete-vl")) {
			a.addEventListener('click', handleDelete);
		}

		// Add handlers for reschedule link
		for (const a of document.querySelectorAll(".reschedule-vl")) {
			a.addEventListener('click', handleReschedule);
		}

		showFlashMessage();
	}

	window.addEventListener('DOMContentLoaded', async function () {

		const user = await getCurrentUser();

		// Setup results table
		table = new DataTable('#vlResultsTable', {
			language: {
				url: "${pageContext.request.contextPath}/moduleResources/disa/js/datatables.net/1.13.2/i18n/${locale}.json"
			},
			dom: '<"float-right"B>trip<"clear">l',
			pagingType: 'full_numbers',
			processing: true,
			scrollX: true,
			buttons: [
				{
					extend: 'colvis',
					columns: [0,1,2,3,5,6,7,9,10,11,13,14,15]
				}

			],
			displayStart: (+"${disaPage.pageNumber}" - 1) * +"${disaPage.pageSize}",
			serverSide: true,
			deferLoading: "${disaPage.totalResults}",
			order: [
				// Created at column
				[13, 'desc']
			],
			columnDefs: [
				// Hide id de referenciamento and updated at by default.
				{
					targets: [3,14],
					visible: false
				},
        	],
			columns: [
				{ data: "requestingFacilityName" },
				{ data: "requestingDistrictName" },
				{ data: "healthFacilityLabCode" },
				{ data: "referringRequestID" },
				{ data: "nid" },
				{
					data: "firstName",
					render: (data, type, row, meta) => {
						if (row.lastName) {
							return `\${data} \${row.lastName}`;
						} else {
							return `\${data}`;
						}
					}
				},
				{ data: "gender" },
				{ data: "ageInYears" },
				{ data: "requestId" },
				{
					data: "processingDate",
					render: (data, type, row, meta) => data.substring(0, 10)
				},
				{
					data: "viralLoadResultDate",
					render: (data, type, row, meta) => data.substring(0, 10)
				},
				{ data: "finalViralLoadResult" },
				{
					data: "viralLoadStatus",
					orderable: false,
					render: (data, type, row, meta) => viralLoadStatus[data]
				},
				{
					data: "createdAt",
					render: (data, type, row, meta) => data.substring(0, 10)
				},
				{
					data: "updatedAt",
					render: (data, type, row, meta) => data ? data.substring(0, 10) : null
				},
				{
					data: "notProcessingCause",
					orderable: false,
					render: (data, type, row, meta) => {
						if (data) {
							return notProcessingCause[data];
						}
						return null;
					}
				},
				// Manage column
				{
					data: null,
					className: "actions",
					orderable: false,
					render: ( data, type, row, meta ) => {

						// If processed render nothing
						if (row.viralLoadStatus === "PROCESSED") {
							return null;
						}

						const span = document.createElement("span");

						// Base tooltip element
						const tooltip = document.createElement("div");
						tooltip.className = "actions-tooltip";

						// Arrow
						const arrow = document.createElement("div");
						arrow.className = "arrow";

						// Actions list
						const ul = document.createElement("ul");

						if (row.viralLoadStatus === "NOT_PROCESSED") {

							// Reschedule
							if (hasPrivilege(user, "Reagendar resultados no Disa Interoperabilidade")) {
								const reschedule = document.createElement("li");
								const rescheduleLink = document.createElement("a");
								rescheduleLink.href="#";
								rescheduleLink.className = "reschedule-vl";
								rescheduleLink.dataset.requestid = row.requestId;
								rescheduleLink.appendChild(document.createTextNode("<spring:message code='disa.viralload.reschedule' />"));
								reschedule.appendChild(rescheduleLink);
								ul.appendChild(reschedule);
							}

							// Map NID
							if (row.notProcessingCause == 'NID_NOT_FOUND') {
								if (hasPrivilege(user, "Mapear pacientes no Disa Interoperabilidade")) {
									const map = document.createElement("li");
									const mapLink = document.createElement("a");
									mapLink.href=`managelabresults/\${row.requestId}/map.form`;
									mapLink.appendChild(document.createTextNode("<spring:message code='disa.map.nid' />"));
									map.appendChild(mapLink);
									ul.appendChild(map);
								}
							}
						}

						// Reallocate
						if (hasPrivilege(user, "Realocar resultados no Disa Interoperabilidade")) {
							const reallocate = document.createElement("li");
							const reallocateLink = document.createElement("a");
							reallocateLink.href=`managelabresults/\${row.requestId}/reallocate.form`;
							reallocateLink.appendChild(document.createTextNode("<spring:message code='disa.viralload.reallocate' />"));
							reallocate.appendChild(reallocateLink);
							ul.appendChild(reallocate);
						}

						// Void
						if (hasPrivilege(user, "Remover resultados no Disa Interoperabilidade")) {
							const delete_ = document.createElement("li");
							const deleteLink = document.createElement("a");
							deleteLink.href="#";
							deleteLink.className = "delete-vl";
							deleteLink.dataset.requestid = row.requestId;
							deleteLink.appendChild(document.createTextNode("<spring:message code='disa.viralload.delete' />"));
							delete_.appendChild(deleteLink)
							ul.appendChild(delete_);
						}

						// If no actions available don't display tooltip
						if (!ul.children.length) {
							return null;
						}

						span.appendChild(document.createTextNode("<spring:message htmlEscape='false' code='disa.manage.actions' />"));
						span.appendChild(tooltip);
						tooltip.appendChild(arrow);
						tooltip.appendChild(ul);

						return span.outerHTML;
					}
				},
			],
			ajax: {
				headers: {
					Accept: "application/json"
				},
				url: "managelabresults.form",
				data: (data) => {
					const pageSize = data.length;
					let pageNumber = 1;
					if (data.start > 0 && pageSize !== -1) {
						pageNumber = (data.start / pageSize) + 1;
					}
					const formData = Object.fromEntries(new FormData(searchForm));
					const orderBy = data.columns[data.order[0].column].data;
					const dir = data.order[0].dir;
					const search = data.search.value;
					return {pageNumber, pageSize, ...formData, search, orderBy, dir};
				},
				dataFilter: (data) => {
					const json = JSON.parse(data);
					json.recordsTotal = json.totalResults;
					json.recordsFiltered = json.totalResults;
					json.data = json.resultList;
					return JSON.stringify(json);
				}

			}
		});

		table.on("draw", postDraw);
		// Inial draw is not triggered when using deferLoading, so we call postDraw manually.
		postDraw();

	});
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
