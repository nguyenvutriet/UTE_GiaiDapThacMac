package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.repository.FileAttachmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileAttachmentService {

    @Autowired
    private FileAttachmentRepo fileAttachmentRepo;

}
