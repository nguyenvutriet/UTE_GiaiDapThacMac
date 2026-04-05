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
      if (!payload || !payload.requestId || !requestIds[payload.requestId]) {
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
    for (var j = 0; j < topicRequestIds.length; j++) {
      client.subscribe("/topic/request-status/" + topicRequestIds[j], onStatusEvent);
    }
  });
})();

