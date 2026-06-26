package nvt.vn.ute_forum.command;

import nvt.vn.ute_forum.service.ClarificationConversationService;

public class CloseConversationCommand implements ICommand{

    private ClarificationConversationService clarificationConversationService;
    private String conversationId;

    public CloseConversationCommand(ClarificationConversationService clarificationConversationService, String conversationId) {
        this.clarificationConversationService = clarificationConversationService;
        this.conversationId = conversationId;
    }

    @Override
    public void execute() {
        clarificationConversationService.closeConversation(conversationId);
    }
}
