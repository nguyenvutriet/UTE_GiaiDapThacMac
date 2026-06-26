package nvt.vn.ute_forum.command;

import nvt.vn.ute_forum.model.ClarificationConversation;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.service.ClarificationConversationService;

public class OpenConversationCommand implements IConversationCommand{

    private ClarificationConversationService conversationReceiver;
    private String content;
    private Users sender;
    private Request request;
    private String subject;

    public OpenConversationCommand(ClarificationConversationService conversationReceiver, String content, Users sender, Request request, String subject) {
        this.conversationReceiver = conversationReceiver;
        this.content = content;
        this.sender = sender;
        this.request = request;
        this.subject = subject;
    }

    @Override
    public ClarificationConversation execute() {
        return conversationReceiver.createConversation(content, sender, request, subject);
    }
}
