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
  var filePicked = document.getElementById("chat-file-picked");
  var sendButton = document.getElementById("chat-send-btn");

  if (!conversationItems.length || !drawer || !drawerBackdrop || !closeDrawerButton || !drawerTitle
      || !messageList || !form || !messageInput || !fileInput || !sendButton) {
    return;
  }

  var stompClient = null;
  var activeConversationId = "";
  var activeRequestId = "";
  var activeSubscriptions = [];
  var appendedMessageIds = {}; // Track message IDs đã được append để tránh duplicate
  var reconnectTimer = null;
  var reconnectDelay = 2000;
  var isConnected = false;
  var isConnecting = false;
  var healthCheckTimer = null;
  var pendingMessages = [];
  var isManualDisconnect = false;

  function clearReconnectTimer() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
  }

  function stopHealthCheck() {
    if (healthCheckTimer) {
      clearInterval(healthCheckTimer);
      healthCheckTimer = null;
    }
  }

  function startHealthCheck() {
    stopHealthCheck();

    healthCheckTimer = setInterval(function () {
      var isDrawerOpen = drawer && drawer.classList && drawer.classList.contains("open");
      if (!activeConversationId || !isDrawerOpen) {
        return;
      }

      if (!isConnected || !stompClient || !stompClient.connected) {
        connectWebSocket(activeConversationId);
      }
    }, 5000);
  }

  function flushPendingMessages() {
    if (!isConnected || !stompClient || !stompClient.connected || pendingMessages.length === 0) {
      return;
    }

    while (pendingMessages.length > 0) {
      var pendingPayload = pendingMessages.shift();
      _doSend(pendingPayload);
    }
  }

  function clearActiveSubscriptions() {
    if (!activeSubscriptions || activeSubscriptions.length === 0) {
      activeSubscriptions = [];
      return;
    }

    for (var i = 0; i < activeSubscriptions.length; i++) {
      try {
        if (activeSubscriptions[i] && typeof activeSubscriptions[i].unsubscribe === "function") {
          activeSubscriptions[i].unsubscribe();
        }
      } catch (e) {
        // Ignore unsubscribe failures from stale subscriptions.
      }
    }
    activeSubscriptions = [];
  }

  setComposerEnabled(false);

  function connectWebSocket(conversationId) {
    if (typeof SockJS === "undefined" || typeof Stomp === "undefined") {
      return;
    }

    var targetConversationId = conversationId || activeConversationId;
    if (!targetConversationId) {
      return;
    }

    if (stompClient && (stompClient.connected || isConnecting)) {
      return;
    }

    isManualDisconnect = false;
    isConnecting = true;

    var socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);
    stompClient.debug = function () {};
    stompClient.heartbeat.outgoing = 20000;
    stompClient.heartbeat.incoming = 20000;

    socket.onclose = function () {
      if (isManualDisconnect) {
        return;
      }
      clearActiveSubscriptions();
      isConnecting = false;
      isConnected = false;

      if (!targetConversationId) {
        return;
      }

      clearReconnectTimer();
      reconnectTimer = setTimeout(function () {
        reconnectDelay = Math.min(reconnectDelay * 2, 30000);
        connectWebSocket(targetConversationId);
      }, reconnectDelay);
    };

    stompClient.connect({}, function () {
      isConnecting = false;
      isConnected = true;
      activeConversationId = targetConversationId;
      reconnectDelay = 2000;
      clearReconnectTimer();
      startHealthCheck();

      subscribeConversation(targetConversationId);
      flushPendingMessages();
    }, function () {
      if (isManualDisconnect) {
        return;
      }

      stompClient = null;
      isConnecting = false;
      isConnected = false;

      if (!targetConversationId) {
        return;
      }

      clearReconnectTimer();
      reconnectTimer = setTimeout(function () {
        reconnectDelay = Math.min(reconnectDelay * 2, 30000);
        connectWebSocket(targetConversationId);
      }, reconnectDelay);
    });
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

  function _doSend(payload) {
    if (!payload) {
      return;
    }

    if (!isConnected || !stompClient || !stompClient.connected) {
      pendingMessages.push(payload);
      if (activeConversationId) {
        connectWebSocket(activeConversationId);
      }
      return;
    }

    stompClient.send("/app/clarification/send", {}, JSON.stringify(payload));
  }

  function sendChatMessage(text, attachments) {
    if (!activeRequestId) {
      return;
    }

    var payload = {
      requestId: activeRequestId,
      content: text,
      attachments: attachments || []
    };

    _doSend(payload);
  }

   function parseMessagePayload(rawContent) {
     if (!rawContent) return { text: "", attachments: [] };

     var PREFIX = "CHAT_V1::";
     var LEGACY_PREFIX = "CHAT_JSON::";
     var SEPARATOR = "\n--FILES--\n";

     if (rawContent.indexOf(LEGACY_PREFIX) === 0) {
       return { text: rawContent.substring(LEGACY_PREFIX.length), attachments: [] };
     }

     if (rawContent.indexOf(PREFIX) !== 0) {
       return { text: rawContent, attachments: [] };
     }

     var payload = rawContent.substring(PREFIX.length);
     var sepIdx = payload.indexOf(SEPARATOR);

     if (sepIdx < 0) {
       return { text: unescapeStr(payload), attachments: [] };
     }

     var textPart = payload.substring(0, sepIdx);
     var attachmentPart = payload.substring(sepIdx + SEPARATOR.length);

     var attachments = [];
     var lines = attachmentPart.split("\n");
     for (var i = 0; i < lines.length; i++) {
       var line = lines[i];
       if (!line || line.trim() === "") continue;
       var parts = splitEscapedLine(line, '|');
       if (parts.length >= 2) {
         attachments.push({
           name: unescapeStr(parts[0]),
           url: unescapeStr(parts[1]),
           type: parts.length >= 3 ? unescapeStr(parts[2]) : "application/octet-stream"
         });
       }
     }

     return { text: unescapeStr(textPart), attachments: attachments };
   }

   function unescapeStr(val) {
     if (!val) return "";
     var res = "";
     var escaped = false;
     for (var i = 0; i < val.length; i++) {
       var ch = val.charAt(i);
       if (escaped) {
         if (ch === 'n') res += '\n';
         else res += ch;
         escaped = false;
         continue;
       }
       if (ch === '\\') {
         escaped = true;
         continue;
       }
       res += ch;
     }
     return res;
   }

   function splitEscapedLine(val, sep) {
     var parts = [];
     var cur = "";
     var escaped = false;
     for (var i = 0; i < val.length; i++) {
       var ch = val.charAt(i);
       if (escaped) {
         cur += ch;
         escaped = false;
         continue;
       }
       if (ch === '\\') {
         cur += ch;
         escaped = true;
         continue;
       }
       if (ch === sep) {
         parts.push(cur);
         cur = "";
         continue;
       }
       cur += ch;
     }
     parts.push(cur);
     return parts;
   }

    function appendMessage(message) {
     // Check duplicate message ID
     var messageId = message.id;
     if (messageId && appendedMessageIds[messageId]) {
       return; // Message đã được append, bỏ qua
     }
     if (messageId) {
       appendedMessageIds[messageId] = true;
     }

     var article = document.createElement("article");
     article.className = "chat-message";

     // Hỗ trợ cả 2 format: MessageDTO từ staff và ChatMessageView từ clarification
     var senderId = (message.sender && message.sender.id) ? message.sender.id
                    : (message.senderId || "");
     var senderName = (message.sender && message.sender.fullName) ? message.sender.fullName
                      : (message.senderName || "Người dùng");
     
     // Parse CHAT_V1 content nếu có
     var rawContent = message.content || message.text || "";
     var parsedData = parseMessagePayload(rawContent);
     var text = parsedData.text;
     
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

     // Lấy attachments từ parsed data hoặc message.attachments
     var attachments = (parsedData.attachments && parsedData.attachments.length > 0)
                       ? parsedData.attachments
                       : (message.attachments || []);

     if (Array.isArray(attachments) && attachments.length > 0) {
       var imageAttachments = [];
       var otherAttachments = [];

       // Phân loại attachment - image vs others
       for (var i = 0; i < attachments.length; i++) {
         var incomingAttachment = attachments[i];
         if (!incomingAttachment || !incomingAttachment.url) continue;
         
         var isImage = /\.(jpg|jpeg|png|gif|webp|bmp|svg)$/i.test(incomingAttachment.url);
         if (isImage) {
           imageAttachments.push(incomingAttachment);
         } else {
           otherAttachments.push(incomingAttachment);
         }
       }

       // Render images trực tiếp
       if (imageAttachments.length > 0) {
         var imageContainer = document.createElement("div");
         imageContainer.className = "chat-image-container";
         
         for (var j = 0; j < imageAttachments.length; j++) {
           var img = document.createElement("img");
           img.src = imageAttachments[j].url;
           img.alt = imageAttachments[j].name || "Hình ảnh";
           img.className = "chat-inline-image";
           img.style.maxWidth = "100%";
           img.style.borderRadius = "8px";
           img.style.marginTop = "8px";
           imageContainer.appendChild(img);
         }
         
         article.appendChild(imageContainer);
       }

       // Render other files as links
       if (otherAttachments.length > 0) {
         var list = document.createElement("ul");
         list.className = "chat-attachment-list";

         for (var k = 0; k < otherAttachments.length; k++) {
           var fileAttachment = otherAttachments[k];
           var item = document.createElement("li");
           var link = document.createElement("a");
           link.href = fileAttachment.url;
           link.target = "_blank";
           link.rel = "noopener noreferrer";
           link.textContent = fileAttachment.name || "Tệp đính kèm";
           item.appendChild(link);
           list.appendChild(item);
         }

         if (list.children.length > 0) {
           article.appendChild(list);
         }
       }
     }

     var timeEl = document.createElement("div");
     timeEl.className = "chat-message-time";
     timeEl.textContent = timeLabel;
     article.appendChild(timeEl);

     // Thêm data-mid attribute để track message đã append
     if (messageId) {
       article.setAttribute("data-mid", messageId);
     }

     messageList.appendChild(article);
     messageList.scrollTop = messageList.scrollHeight;
  }

  function formatChatTime(rawTime) {
    try {
      var d = new Date(rawTime);
      if (isNaN(d.getTime())) return rawTime;
      
      var day = String(d.getDate()).padStart(2, '0');
      var month = String(d.getMonth() + 1).padStart(2, '0');
      var year = d.getFullYear();
      var hours = String(d.getHours()).padStart(2, '0');
      var minutes = String(d.getMinutes()).padStart(2, '0');
      var seconds = String(d.getSeconds()).padStart(2, '0');
      
      return day + '/' + month + '/' + year + ' ' + hours + ':' + minutes + ':' + seconds;
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

    stopHealthCheck();
    clearReconnectTimer();
    clearActiveSubscriptions();
    isManualDisconnect = true;
    if (stompClient && stompClient.connected) {
      try {
        stompClient.disconnect(function () {});
      } catch (e) {
        // Ignore disconnect errors from closed transports.
      }
    }
    stompClient = null;
    isConnected = false;
    isConnecting = false;
  }

  function clearMessages() {
    messageList.innerHTML = "";
    appendedMessageIds = {}; // Reset tracking khi clear messages
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
    if (!enabled) {
      clearSelectedFilesLabel();
    }
  }

  function clearSelectedFilesLabel() {
    if (!filePicked) {
      return;
    }
    filePicked.textContent = "";
    filePicked.hidden = true;
  }

  function renderSelectedFilesLabel(files) {
    if (!filePicked) {
      return;
    }

    if (!files || files.length === 0) {
      clearSelectedFilesLabel();
      return;
    }

    var names = [];
    for (var i = 0; i < files.length; i++) {
      names.push(files[i].name || "tep");
    }

    var text = files.length === 1
      ? "Đã chọn 1 tệp: " + names[0]
      : "Đã chọn " + files.length + " tệp: " + names.join(", ");

    if (text.length > 180) {
      text = text.substring(0, 177) + "...";
    }

    filePicked.textContent = text;
    filePicked.hidden = false;
  }

  function setDrawerConversationState(isOpen) {
    var statusPill = document.getElementById("chat-drawer-status-pill");
    if (!statusPill) {
      return;
    }

    if (isOpen) {
      statusPill.textContent = "Đang mở";
      statusPill.classList.remove("closed");
      setComposerEnabled(true);
      messageInput.placeholder = "Nhập tin nhắn...";
      return;
    }

    statusPill.textContent = "Đã đóng";
    statusPill.classList.add("closed");
    setComposerEnabled(false);
    messageInput.placeholder = "Cuộc hội thoại đã đóng";
  }

   function subscribeConversation(conversationId) {
     if (!stompClient || !stompClient.connected || !conversationId) {
       return;
     }

     clearActiveSubscriptions();

     // Message handler chung cho cả 2 topics
     var messageHandler = function (frame) {
       if (!frame || !frame.body) {
         return;
       }
       try {
         var data = JSON.parse(frame.body);
         appendMessage(data);
       } catch (e) {
         // Ignore malformed frame to keep UI usable.
       }
     };

     // Subscribe vào topic clarification (khi student gửi)
     activeSubscriptions.push(stompClient.subscribe("/topic/clarification/" + conversationId, messageHandler));

     // Subscribe thêm vào topic conversation (khi staff gửi)
     activeSubscriptions.push(stompClient.subscribe("/topic/conversation/" + conversationId, messageHandler));
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
    var isOpen = (item.getAttribute("data-is-open") || "true") === "true";

    if (!conversationId || !requestId) {
      return;
    }

    activeConversationId = conversationId;
    activeRequestId = requestId;
    clearSelectedFilesLabel();
    fileInput.value = "";

    for (var idx = 0; idx < conversationItems.length; idx++) {
      conversationItems[idx].classList.remove("active");
    }
    item.classList.add("active");

    drawerTitle.textContent = subject;
    openDrawer();
    setDrawerConversationState(isOpen);
    renderEmptyMessage("Đang tải hội thoại...");

    loadConversation(conversationId)
      .then(function (payload) {
        if (!payload || payload.conversationId !== activeConversationId) {
          return;
        }

        activeRequestId = payload.requestId || activeRequestId;
        drawerTitle.textContent = payload.subject || subject;
        setDrawerConversationState(payload.open !== false);
        renderMessages(payload.messages || []);
        if (payload.open !== false) {
          if (stompClient && stompClient.connected) {
            subscribeConversation(activeConversationId);
          } else {
            connectWebSocket(activeConversationId);
          }
        }
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

  fileInput.addEventListener("change", function () {
    renderSelectedFilesLabel(fileInput.files);
  });

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
        clearSelectedFilesLabel();
      })
      .catch(function () {
        alert("Không thể gửi tệp đính kèm. Vui lòng thử lại.");
      })
      .finally(function () {
        sendButton.disabled = false;
      });
  });

})();





