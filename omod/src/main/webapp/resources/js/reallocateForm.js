function ReallocateForm(el, {requestId}) {
  const form = document.querySelector(el);
  const errorMsgBox = document.getElementById("error_msg");

  // Ask for confirmation before submit
  form.addEventListener("submit", (event) => {
    const options = event.currentTarget.querySelector(
      "#healthFacilityLabCode"
    ).options;
    const orgUnit = options[options.selectedIndex].label;
    const message = t["disa.viralload.reallocate.confirmation.javascript"]
      .replace("${requestId}", requestId)
      .replace("${orgUnit}", orgUnit);
    if (orgUnit.length && !confirm(message)) {
      event.preventDefault();
    }
  });

  $j(".facility-search").selectize({
    placeholder: t["disa.viralload.reallocate.select.placeholder"],
    plugins: ["clear_button"],
    load: async (term, callback) => {
      let results = [];

      errorMsgBox.innerText = "";

      if (!term.length) return callback(results);

      try {
        document.body.style.cursor = "wait";
        const url = `/openmrs/module/disa/orgunits/search.form?term=${term}`;
        const fetchResponse = await fetch(url);
        if (fetchResponse.status !== 200) {
          const error = await fetchResponse.json();
          throw new Error(error.message);
        }
        const data = await fetchResponse.json();
        results = data.map((r) => ({
          text: `${r.province} > ${r.district} > ${r.facility}`,
          value: r.code,
        }));
      } catch (error) {
        errorMsgBox.innerText = error.message;
        console.log(error);
      } finally {
        document.body.style.cursor = "default";
        callback(results);
      }
    },
  });
}
