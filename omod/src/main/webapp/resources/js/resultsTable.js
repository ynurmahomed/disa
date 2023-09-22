let table;

const columns = {
  ID: 0,
  FACILITY_NAME: 1,
  DISTRICT_NAME: 2,
  FACILITY_CODE: 3,
  NID: 4,
  FULL_NAME: 5,
  GENDER: 6,
  AGE: 7,
  REQUEST_ID: 8,
  HARVEST_DATE: 9,
  RESULT_DATE: 10,
  FINAL_RESULT: 11,
  TYPE_OF_RESULT: 12,
  STATUS: 13,
  CREATED_AT: 14,
  UPDATED_AT: 15,
  NOT_PROCESSING_CAUSE: 16,
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
  const systemDeveloper = user.roles.find((r) => r.name === "System Developer");
  if (systemDeveloper) {
    return true;
  }
  return user.privileges.find((p) => p.name === privilege);
}

async function handleReschedule(event) {
  event.preventDefault();

  const anchor = event.currentTarget;
  const id = anchor.dataset.id;
  const headers = { "Content-Type": "application/json" };
  const options = { method: "POST", headers };

  try {
    document.body.style.cursor = "wait";

    const response = await fetch(
      `managelabresults/${id}/reschedule.form`,
      options
    );

    if (response.status === 200) {
      addFlashMessage(t["disa.viralload.reschedule.successful"]);
      table.draw(false);
    } else {
      throw new Error(`Reschedule was not successful.`);
    }
  } catch (error) {
    console.error(error);
    alert(t["disa.unexpected.error"]);
  } finally {
    document.body.style.cursor = "default";
  }
}

async function handleDelete(event) {
  event.preventDefault();

  const anchor = event.currentTarget;
  const id = anchor.dataset.id;

  // Note that requestId is used in the confirm message
  const requestId = anchor.dataset.requestid;
  const message = t["disa.viralload.delete.confirmation.javascript"].replace(
    "${requestId}",
    requestId
  );
  if (confirm(message)) {
    try {
      document.body.style.cursor = "wait";
      const response = await fetch(`managelabresults/${id}.form`, {
        method: "DELETE",
      });
      if (response.status === 204) {
        addFlashMessage(t["disa.viralload.delete.successful"]);
        table.draw(false);
      } else {
        throw new Error(`Delete was not successful.`);
      }
    } catch (error) {
      console.error(error);
      alert(t["disa.unexpected.error"]);
    } finally {
      document.body.style.cursor = "default";
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
          state.styles.popper.width = `${state.rects.reference.width}px`;
        },
        effect: ({ state }) => {
          state.elements.popper.style.width = `${state.elements.reference.offsetWidth}px`;
        },
      };

      const popperInstance = Popper.createPopper(toggle, actions, {
        modifiers: [sameWidth, Popper.preventOverflow],
      });

      // Show/hide based on hover
      function show() {
        // Make the tooltip visible
        actions.setAttribute("data-show", "");

        // Enable the event listeners
        popperInstance.setOptions((options) => ({
          ...options,
          modifiers: [
            ...options.modifiers,
            { name: "eventListeners", enabled: true },
          ],
        }));

        // Update its position
        popperInstance.update();
      }

      function hide() {
        // Hide the tooltip
        actions.removeAttribute("data-show");

        // Disable the event listeners
        popperInstance.setOptions((options) => ({
          ...options,
          modifiers: [
            ...options.modifiers,
            { name: "eventListeners", enabled: false },
          ],
        }));
      }

      const showEvents = ["mouseenter", "focus"];
      const hideEvents = ["mouseleave", "blur"];

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
 * Should execute everytime the table is drawn.
 */
function postDraw() {
  createTooltips();
  // Add handlers for delete link
  for (const a of document.querySelectorAll(".delete-vl")) {
    a.addEventListener("click", handleDelete);
  }

  // Add handlers for reschedule link
  for (const a of document.querySelectorAll(".reschedule-vl")) {
    a.addEventListener("click", handleReschedule);
  }

  showFlashMessage();
}

async function ResultsTable(el, { locale, totalResults }) {
  const user = await getCurrentUser();

  // Setup results table
  table = new DataTable(el, {
    stateSave: true,
    stateSaveParams: (settings, data) => {
      // Always start at first page
      data.start = 0;
      // Always display 10 pages
      data.length = 10;
      // Always order by CREATED_AT column
      data.order = [[+columns.CREATED_AT, "desc"]];
    },
    language: {
      url: `/openmrs/moduleResources/disa/js/datatables.net/1.13.2/i18n/${locale}.json`,
    },
    dom: '<"float-right"B>trip<"clear">l',
    pagingType: "full_numbers",
    processing: true,
    scrollX: true,
    buttons: [
      {
        extend: "colvis",
        columns: [
          columns.FACILITY_NAME,
          columns.DISTRICT_NAME,
          columns.FACILITY_CODE,
          columns.FULL_NAME,
          columns.GENDER,
          columns.REQUEST_ID,
          columns.HARVEST_DATE,
          columns.RESULT_DATE,
          columns.TYPE_OF_RESULT,
          columns.FINAL_RESULT,
          columns.STATUS,
          columns.CREATED_AT,
          columns.UPDATED_AT,
          columns.NOT_PROCESSING_CAUSE,
        ],
      },
    ],
    serverSide: true,
    deferLoading: totalResults,
    order: [[columns.CREATED_AT, "desc"]],
    columnDefs: [
      // Hide updated at by default.
      {
        targets: [columns.UPDATED_AT],
        visible: false,
      },
    ],
    columns: [
      { data: "id" },
      { data: "requestingFacilityName" },
      { data: "requestingDistrictName" },
      { data: "healthFacilityLabCode" },
      {
        data: "nid",
        render: (data) => data || null,
      },
      {
        data: "firstName",
        render: (data, type, row, meta) => {
          if (row.lastName) {
            return `${data} ${row.lastName}`;
          } else {
            return `${data}`;
          }
        },
      },
      { data: "gender" },
      {
        data: "ageInYears",
        render: (data) => data || null,
      },
      {
        data: "requestId",
        render: (data, type, row, meta) => {
          if (row.encounterId) {
            const link = document.createElement("a");
            link.target = "_blank";
            link.href = `/openmrs/module/htmlformentry/htmlFormEntry.form?encounterId=${row.encounterId}`;
            link.innerText = data;
            return link.outerHTML;
          } else {
            return data;
          }
        },
      },
      {
        data: "harvestDate",
        render: (data, type, row, meta) => data?.substring(0, 10) || null,
      },
      {
        data: "labResultDate",
        render: (data, type, row, meta) => data?.substring(0, 10) || null,
      },
      { data: "finalResult" },
      {
        data: "typeOfResult",
        orderable: false,
        render: (data, type, row, meta) => t[`disa.typeOfResult.${data}`],
      },
      {
        data: "labResultStatus",
        orderable: false,
        render: (data, type, row, meta) => t[`disa.viral.load.status.${data}`],
      },
      {
        data: "createdAt",
        render: (data, type, row, meta) => data.substring(0, 10),
      },
      {
        data: "updatedAt",
        render: (data, type, row, meta) =>
          data ? data.substring(0, 10) : null,
      },
      {
        data: "notProcessingCause",
        orderable: false,
        render: (data, type, row, meta) => {
          if (data) {
            return t[`disa.notProcessingCause.${data}`];
          }
          return null;
        },
      },
      // Manage column
      {
        data: null,
        className: "actions",
        orderable: false,
        render: (data, type, row, meta) => {
          // If processed render nothing
          if (row.labResultStatus === "PROCESSED") {
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

          if (row.labResultStatus === "NOT_PROCESSED") {
            // Reschedule
            if (
              hasPrivilege(
                user,
                "Reagendar resultados no Disa Interoperabilidade"
              )
            ) {
              const reschedule = document.createElement("li");
              const rescheduleLink = document.createElement("a");
              rescheduleLink.href = "#";
              rescheduleLink.className = "reschedule-vl";
              rescheduleLink.dataset.id = data.id;
              rescheduleLink.appendChild(
                document.createTextNode(t["disa.viralload.reschedule"])
              );
              reschedule.appendChild(rescheduleLink);
              ul.appendChild(reschedule);
            }

            // Map NID
            if (row.notProcessingCause == "NID_NOT_FOUND") {
              if (
                hasPrivilege(
                  user,
                  "Mapear pacientes no Disa Interoperabilidade"
                )
              ) {
                const map = document.createElement("li");
                const mapLink = document.createElement("a");
                mapLink.href = `managelabresults/${data.id}/map.form`;
                mapLink.appendChild(document.createTextNode(t["disa.map.nid"]));
                map.appendChild(mapLink);
                ul.appendChild(map);
              }
            }
          }

          // Reallocate
          if (
            hasPrivilege(user, "Realocar resultados no Disa Interoperabilidade")
          ) {
            const reallocate = document.createElement("li");
            const reallocateLink = document.createElement("a");
            reallocateLink.href = `managelabresults/${data.id}/reallocate.form`;
            reallocateLink.appendChild(
              document.createTextNode(t["disa.viralload.reallocate"])
            );
            reallocate.appendChild(reallocateLink);
            ul.appendChild(reallocate);
          }

          // Void
          if (
            hasPrivilege(user, "Remover resultados no Disa Interoperabilidade")
          ) {
            const delete_ = document.createElement("li");
            const deleteLink = document.createElement("a");
            deleteLink.href = "#";
            deleteLink.className = "delete-vl";
            deleteLink.dataset.requestid = row.requestId;
            deleteLink.dataset.id = data.id;
            deleteLink.appendChild(
              document.createTextNode(t["disa.viralload.delete"])
            );
            delete_.appendChild(deleteLink);
            ul.appendChild(delete_);
          }

          // If no actions available don't display tooltip
          if (!ul.children.length) {
            return null;
          }

          span.appendChild(document.createTextNode(t["disa.manage.actions"]));
          span.appendChild(tooltip);
          tooltip.appendChild(arrow);
          tooltip.appendChild(ul);

          return span.outerHTML;
        },
      },
    ],
    ajax: {
      headers: {
        Accept: "application/json",
      },
      url: "managelabresults/json.form",
      data: (data) => {
        const pageSize = data.length;
        let pageNumber = data.start / pageSize;
        const formData = Object.fromEntries(new FormData(searchForm));
        const orderBy = data.columns[data.order[0].column].data;
        const dir = data.order[0].dir;
        const search = data.search.value;
        return { pageNumber, pageSize, ...formData, search, orderBy, dir };
      },
      dataFilter: (data) => {
        const json = JSON.parse(data);
        json.recordsTotal = json.totalResults;
        json.recordsFiltered = json.totalResults;
        json.data = json.resultList;
        return JSON.stringify(json);
      },
    },
  });

  table.on("draw", postDraw);
  // Inial draw is not triggered when using deferLoading, so we call postDraw manually.
  postDraw();
}
