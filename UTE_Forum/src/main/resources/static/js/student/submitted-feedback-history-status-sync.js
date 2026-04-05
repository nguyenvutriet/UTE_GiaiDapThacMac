(function () {
  "use strict";

  var syncRoot = document.getElementById("history-status-sync");
  if (!syncRoot) {
    return;
  }

  var requestId = syncRoot.getAttribute("data-request-id") || "";

  var requestIds = {};
  if (requestId) {
    requestIds[requestId] = true;
  }

  var cards = document.querySelectorAll(".feedback-card[data-request-id]");
  for (var i = 0; i < cards.length; i++) {
    var cardRequestId = cards[i].getAttribute("data-request-id");
    if (cardRequestId) {
      requestIds[cardRequestId] = true;
    }
  }

  var topicRequestIds = Object.keys(requestIds);
  if (topicRequestIds.length === 0) {
    return;
  }

  var hasReloadTriggered = false;
  var lastStatusByRequestId = {};

  function reloadCurrentPage() {
    if (hasReloadTriggered) {
      return;
    }
    hasReloadTriggered = true;
    window.location.reload();
  }

  function pollStatus(requestId) {
    return fetch("/api/history/status?requestId=" + encodeURIComponent(requestId), {
      method: "GET",
      credentials: "same-origin"
    }).then(function (response) {
      if (!response.ok) {
        throw new Error("status-fetch-failed");
      }
      return response.json();
    }).then(function (payload) {
      if (!payload || !payload.requestId || !payload.currentStatus) {
        return;
      }

      var prev = lastStatusByRequestId[payload.requestId];
      if (prev && prev !== payload.currentStatus) {
        reloadCurrentPage();
        return;
      }

      lastStatusByRequestId[payload.requestId] = payload.currentStatus;
    }).catch(function () {
      // Ignore polling errors so the page stays usable on deploy.
    });
  }

  function pollAllStatuses() {
    for (var i = 0; i < topicRequestIds.length; i++) {
      pollStatus(topicRequestIds[i]);
    }
  }

  pollAllStatuses();
  setInterval(pollAllStatuses, 5000);
})();

