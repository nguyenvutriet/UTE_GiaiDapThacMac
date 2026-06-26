package nvt.vn.ute_forum.command;

import nvt.vn.ute_forum.model.ClarificationConversation;
import org.springframework.stereotype.Service;

@Service
public class CommandInvoker {
    public ClarificationConversation executeCommand(IConversationCommand command) {;
        return command.execute();
    }
}
