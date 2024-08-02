const nidTarv = (i) =>
  i.identifierType.uuid === "e2b966d0-1d5f-11e0-b929-000c29ad1d07";

function MapIdentifiers(disaEl, patientListEl, { locale, searchSuggestion }) {
  new DataTable(disaEl, {
    dom: "t",
    order: [],
    columns: [
      { data: "nid", orderable: false, width: "30%" },
      { data: "name", orderable: false, width: "30%" },
      { data: "gender", orderable: false, width: "16%" },
      { data: "age", orderable: false, width: "16%" },
      { render: () => null, orderable: false, width: "8%" },
    ],
  });

  const table = new DataTable(patientListEl, {
    search: {
      search: searchSuggestion,
    },
    initComplete: () => {
      document.querySelector("div.dataTables_filter input").focus();
    },
    dom: '<"disa-table-search"f>trip<"clear">l',
    order: [],
    pagingType: "simple",
    language: {
      url: `/openmrs/moduleResources/disa/js/datatables.net/1.13.2/i18n/${locale}.json`,
    },
    processing: true,
    serverSide: true,
    ajax: (data, callback, settings) => {
      if (data.search.value) {
        DWRPatientService.findCountAndPatients(
          data.search.value,
          data.start,
          data.length,
          true,
          (res) => {
            callback({
              data: res.objectList,
              recordsTotal: res.count,
              recordsFiltered: res.count,
            });
          }
        );
      } else {
        callback({ data: [], recordsTotal: 0 });
      }
    },
    columns: [
      {
        data: "nid",
        render: (data, type, row) => {
          const link = document.createElement("a");
          link.target = "_blank";
          link.href = `/openmrs/patientDashboard.form?patientId=${row.patientId}`;
          link.innerText = row.identifier;
          return link.outerHTML;
        },
        orderable: false,
        width: "30%",
      },
      {
        data: "name",
        render: (data, type, row) => `${row.givenName} ${row.middleName} ${row.familyName}`,
        orderable: false,
        width: "30%",
      },
      {
        data: "gender",
        orderable: false,
        width: "16%",
      },
      {
        data: "age",
        orderable: false,
        width: "16%",
      },
      {
        render: (data, type, row) => {
          const radio = document.createElement("input");
          radio.type = "radio";
          radio.name = "patientUuid";
          radio.value = row.uuid;
          return radio.outerHTML;
        },
        orderable: false,
        width: "8%",
      },
    ],
  });

  table.on("search.dt", () => {
    document.getElementById("search").value = table.search();
  });
}
