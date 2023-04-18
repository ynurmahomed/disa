/**
 * Add a message to be temporarily displayed.
 */
function addFlashMessage(message) {
    const alertBox = document.getElementById("alert-box");
    // Clear previous message
    if (alertBox.lastElementChild) {
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
