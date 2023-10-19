const INITIAL_INTERVAL = 30_000;
let repeatInterval = INITIAL_INTERVAL;

/**
 * Periodically checks sync status in a fixed interval.
 * If there is an ongoing sync this interval is increased. Once
 * it is finished the interval is reset to its initial value.
 */
async function updateSyncStatus() {
  const lastExecution = document.querySelector(".last-execution");
  const currentExecution = document.querySelector(".current-execution");

  try {
    const res = await fetch(`/openmrs/module/disa/syncstatus.form`);
    if (res.status !== 200) {
      throw new Error("Could not fetch sync status");
    }
    const syncStatus = await res.json();
    lastExecution.innerHTML = syncStatus.lastExecution;
    if (syncStatus.currentExecution) {
      repeatInterval = 1_000;
      currentExecution.innerHTML = syncStatus.currentExecution;
      lastExecution.classList.add("hidden");
      currentExecution.classList.remove("hidden");
    } else {
      repeatInterval = INITIAL_INTERVAL;
      currentExecution.classList.add("hidden");
      lastExecution.classList.remove("hidden");
    }
    setTimeout(updateSyncStatus, repeatInterval);
  } catch (error) {
    console.log("Could not fetch sync status: ", error);
  }
}

updateSyncStatus();
