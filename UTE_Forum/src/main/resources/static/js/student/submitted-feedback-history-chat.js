(function () {
  "use strict";

  var root = document.getElementById("chat-root");
  if (!root) {
    return;
  }

  var currentUserId = root.getAttribute("data-current-user-id") || "";
  var conversationItems = root.querySelectorAll(".conversation-item");

  var drawer = document.getElementById("chat-drawer");
  var drawerBackdrop = document.getElementById("chat-drawer-backdrop");
  var closeDrawerButton = document.getElementById("close-chat-drawer-btn");
  var drawerTitle = document.getElementById("chat-drawer-title");

  var messageList = document.getElementById("chat-message-list");
  var form = document.getElementById("chat-send-form");
  var messageInput = document.getElementById("chat-message-input");
  var fileInput = document.getElementById("chat-file-input");
  var sendButton = document.getElementById("chat-send-btn");

  if (!conversationItems.length || !drawer || !drawerBackdrop || !closeDrawerButton || !drawerTitle
      || !messageList || !form || !messageInput || !fileInput || !sendButton) {
    return;
  }

  var stompClient = null;
  var activeConversationId = "";
  var activeRequestId = "";
  var activeSubscription = null;

  setComposerEnabled(false);

  function connectWebSocket() {
    if (typeof SockJS === "undefined" || typeof Stomp === "undefined") {
      return;
    }

    var socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);
    stompClient.debug = function () {};

    stompClient.connect({}, function () {});
  }

  function uploadFiles(files) {
    if (!files || files.length === 0 || !activeRequestId) {
      return Promise.resolve([]);
    }

    var formData = new FormData();
    formData.append("requestId", activeRequestId);
    for (var i = 0; i < files.length; i++) {
      formData.append("files", files[i]);
    }

    return fetch("/api/history/chat/upload", {
      method: "POST",
      body: formData,
      credentials: "same-origin"
    })
      .then(function (response) {
        if (!response.ok) {
          throw new Error("upload-failed");
        }
        return response.json();
      })
      .then(function (payload) {
        if (!payload || !Array.isArray(payload.attachments)) {
          return [];
        }
        return payload.attachments;
      });
  }

  function sendChatMessage(text, attachments) {
    if (!stompClient || !stompClient.connected || !activeRequestId) {
      return;
    }

    var payload = {
      requestId: activeRequestId,
      content: text,
      attachments: attachments || []
    };

    stompClient.send("/app/clarification/send", {}, JSON.stringify(payload));
  }

  function appendMessage(message) {
    var article = document.createElement("article");
    article.className = "chat-message";

    // Hỗ trợ cả 2 format: MessageDTO từ staff và ChatMessageView từ clarification
    var senderId = (message.sender && message.sender.id) ? message.sender.id
                   : (message.senderId || "");
    var senderName = (message.sender && message.sender.fullName) ? message.sender.fullName
                     : (message.senderName || "Người dùng");
    var text = message.content || message.text || "";
    var rawTime = message.createAt || message.createdAt || message.time || null;
    var timeLabel = rawTime ? formatChatTime(rawTime) : "";

    var isMe = senderId && senderId === currentUserId;
    if (isMe) {
      article.className += " mine";
    }

    var header = document.createElement("div");
    header.className = "chat-message-meta";
    var sender = document.createElement("strong");
    sender.textContent = senderName;
    header.appendChild(sender);
    article.appendChild(header);

    if (text) {
      var p = document.createElement("p");
      p.className = "chat-message-text";
      p.textContent = text;
      article.appendChild(p);
    }

    if (Array.isArray(message.attachments) && message.attachments.length > 0) {
      var list = document.createElement("ul");
      list.className = "chat-attachment-list";

      for (var i = 0; i < message.attachments.length; i++) {
        var attachment = message.attachments[i];
        if (!attachment || !attachment.url) {
          continue;
        }
        var item = document.createElement("li");
        var link = document.createElement("a");
        link.href = attachment.url;
        link.target = "_blank";
        link.rel = "noopener noreferrer";
        link.textContent = attachment.name || "Tệp đính kèm";
        item.appendChild(link);
        list.appendChild(item);
      }

      if (list.children.length > 0) {
        article.appendChild(list);
      }
    }

    var timeEl = document.createElement("div");
    timeEl.className = "chat-message-time";
    timeEl.textContent = timeLabel;
    article.appendChild(timeEl);

    messageList.appendChild(article);
    messageList.scrollTop = messageList.scrollHeight;
  }

  function formatChatTime(rawTime) {
    try {
      var d = new Date(rawTime);
      if (isNaN(d.getTime())) return rawTime;
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch(e) {
      return String(rawTime);
    }
  }

  function openDrawer() {
    drawer.classList.add("open");
    drawer.setAttribute("aria-hidden", "false");
    drawerBackdrop.hidden = false;
    document.body.style.overflow = "hidden";
  }

  function closeDrawer() {
    drawer.classList.remove("open");
    drawer.setAttribute("aria-hidden", "true");
    drawerBackdrop.hidden = true;
    document.body.style.overflow = "";
  }

  function clearMessages() {
    messageList.innerHTML = "";
  }

  function renderEmptyMessage(text) {
    clearMessages();
    var empty = document.createElement("div");
    empty.className = "chat-empty compact";
    empty.innerHTML = "<p>" + text + "</p>";
    messageList.appendChild(empty);
  }

  function renderMessages(messages) {
    clearMessages();
    if (!Array.isArray(messages) || messages.length === 0) {
      renderEmptyMessage("Chưa có tin nhắn trong cuộc hội thoại này.");
      return;
    }

    for (var i = 0; i < messages.length; i++) {
      appendMessage(messages[i]);
    }
  }

  function setComposerEnabled(enabled) {
    messageInput.disabled = !enabled;
    fileInput.disabled = !enabled;
    sendButton.disabled = !enabled;
  }

  function subscribeConversation(conversationId) {
    if (!stompClient || !stompClient.connected || !conversationId) {
      return;
    }

    if (activeSubscription) {
      activeSubscription.unsubscribe();
      activeSubscription = null;
    }

    activeSubscription = stompClient.subscribe("/topic/clarification/" + conversationId, function (frame) {
      if (!frame || !frame.body) {
        return;
      }
      try {
        var data = JSON.parse(frame.body);
        appendMessage(data);
      } catch (e) {
        // Ignore malformed frame to keep UI usable.
      }
    });
  }

  function loadConversation(conversationId) {
    return fetch("/api/history/chat/messages?conversationId=" + encodeURIComponent(conversationId), {
      method: "GET",
      credentials: "same-origin"
    }).then(function (response) {
      if (!response.ok) {
        throw new Error("load-failed");
      }
      return response.json();
    });
  }

  function activateConversation(item) {
    if (!item) {
      return;
    }

    var conversationId = item.getAttribute("data-conversation-id") || "";
    var requestId = item.getAttribute("data-request-id") || "";
    var subject = item.getAttribute("data-subject") || "Trao đổi";

    if (!conversationId || !requestId) {
      return;
    }

    activeConversationId = conversationId;
    activeRequestId = requestId;

    for (var idx = 0; idx < conversationItems.length; idx++) {
      conversationItems[idx].classList.remove("active");
    }
    item.classList.add("active");

    drawerTitle.textContent = subject;
    openDrawer();
    setComposerEnabled(true);
    renderEmptyMessage("Đang tải hội thoại...");

    loadConversation(conversationId)
      .then(function (payload) {
        if (!payload || payload.conversationId !== activeConversationId) {
          return;
        }

        activeRequestId = payload.requestId || activeRequestId;
        drawerTitle.textContent = payload.subject || subject;
        renderMessages(payload.messages || []);
        subscribeConversation(activeConversationId);
      })
      .catch(function () {
        renderEmptyMessage("Không tải được cuộc hội thoại. Vui lòng thử lại.");
      });
  }

  for (var i = 0; i < conversationItems.length; i++) {
    conversationItems[i].addEventListener("click", function () {
      activateConversation(this);
    });
  }

  closeDrawerButton.addEventListener("click", closeDrawer);
  drawerBackdrop.addEventListener("click", closeDrawer);

  form.addEventListener("submit", function (event) {
    event.preventDefault();

    if (!activeConversationId || !activeRequestId) {
      return;
    }

    var text = (messageInput.value || "").trim();
    var files = fileInput.files;

    if (!text && (!files || files.length === 0)) {
      return;
    }

    sendButton.disabled = true;

    uploadFiles(files)
      .then(function (attachments) {
        sendChatMessage(text, attachments);
        messageInput.value = "";
        fileInput.value = "";
      })
      .catch(function () {
        alert("Không thể gửi tệp đính kèm. Vui lòng thử lại.");
      })
      .finally(function () {
        sendButton.disabled = false;
      });
  });

  connectWebSocket();
})();





