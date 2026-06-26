package nvt.vn.ute_forum.adapter;

import nvt.vn.ute_forum.model.ClarificationConversation;
import nvt.vn.ute_forum.model.Message;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.repository.MessageRepo;
import nvt.vn.ute_forum.service.ClarificationConversationService;
import nvt.vn.ute_forum.service.RequestService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MessageAdapter implements IMessageService {

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private ClarificationConversationService clarificationConversationService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private RequestService requestService;

    @Override
    public Message saveMessage(String content, String conversationId, String emailSender) {

        ClarificationConversation conversation = clarificationConversationService.getConversationById(conversationId);
        if(conversation == null){
            return null;
        }

        Users sender = usersService.getByEmail(emailSender);
        if(sender == null){
            return null;
        }

        Message mess = new Message();
        mess.setId(UUID.randomUUID().toString());
        mess.setContent(content);
        mess.setCreateAt(LocalDateTime.now());
        mess.setClarificationConversation(conversation);
        mess.setSender(sender);

        Request request = requestService.getRequestById(conversation.getRequest().getId());
        Users receiver = request.getUser();
        mess.setReceiver(receiver);

        messageRepo.save(mess);
        return mess;
    }
}
