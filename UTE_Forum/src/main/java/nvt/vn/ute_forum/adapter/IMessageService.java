package nvt.vn.ute_forum.adapter;

import nvt.vn.ute_forum.model.Message;

public interface IMessageService {

    public Message saveMessage(String content, String conversationId, String emailSender);



}
