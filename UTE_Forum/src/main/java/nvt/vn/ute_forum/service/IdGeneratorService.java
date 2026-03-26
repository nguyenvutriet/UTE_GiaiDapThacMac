package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.repository.FileAttachmentRepo;
import nvt.vn.ute_forum.repository.RequestRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdGeneratorService {

    private final RequestRepo requestRepo;
    private final FileAttachmentRepo fileAttachmentRepo;

    private long requestSequence = -1;
    private long fileAttachmentSequence = -1;

    public IdGeneratorService(RequestRepo requestRepo, FileAttachmentRepo fileAttachmentRepo) {
        this.requestRepo = requestRepo;
        this.fileAttachmentRepo = fileAttachmentRepo;
    }

    @Transactional(readOnly = true)
    public synchronized String nextRequestId() {
        if (requestSequence < 0) {
            requestSequence = safeSequence(requestRepo.findMaxRequestSequence());
        }
        requestSequence++;
        return "REQ_" + requestSequence;
    }

    @Transactional(readOnly = true)
    public synchronized String nextFileAttachmentId() {
        if (fileAttachmentSequence < 0) {
            fileAttachmentSequence = safeSequence(fileAttachmentRepo.findMaxFileAttachmentSequence());
        }
        fileAttachmentSequence++;
        return "FA_" + fileAttachmentSequence;
    }

    private long safeSequence(Long value) {
        return value == null ? 0L : value;
    }
}

