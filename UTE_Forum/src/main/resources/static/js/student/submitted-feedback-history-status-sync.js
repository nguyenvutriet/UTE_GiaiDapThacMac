(function () {
  "use strict";

  var syncRoot = document.getElementById("history-status-sync");
  if (!syncRoot) {
    return;
  }

  var requestId = syncRoot.getAttribute("data-request-id");
  if (!requestId) {
    return;
  }

  if (typeof SockJS === "undefined" || typeof Stomp === "undefined") {
    return;
  }

  var hasReloadTriggered = false;

  function reloadCurrentPage() {
    if (hasReloadTriggered) {
      return;
    }
    hasReloadTriggered = true;
    window.location.reload();
  }

  function onStatusEvent(frame) {
    if (!frame || !frame.body) {
      return;
    }

    try {
      var payload = JSON.parse(frame.body);
      if (!payload || payload.requestId !== requestId) {
        return;
      }
      reloadCurrentPage();
    } catch (e) {
      // Ignore malformed frame and keep current page usable.
    }
  }

  var socket = new SockJS("/ws");
  var client = Stomp.over(socket);
  client.debug = function () {};

  client.connect({}, function () {
    client.subscribe("/topic/request-status/" + requestId, onStatusEvent);
  });
})();

