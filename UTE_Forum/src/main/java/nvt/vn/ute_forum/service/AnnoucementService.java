package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.repository.AnnouncementRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnnoucementService {

    @Autowired
    private AnnouncementRepo announcementRepo;

}
