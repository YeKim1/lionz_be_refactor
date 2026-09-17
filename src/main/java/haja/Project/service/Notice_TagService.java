package haja.Project.service;

import haja.Project.domain.Notice;
import haja.Project.domain.Notice_Tag;
import haja.Project.repository.Notice_TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class Notice_TagService {

    private final Notice_TagRepository notice_tagRepository;
    private final TagService tagService;

    public List<Notice_Tag> findByNotice(Long id) {
        return notice_tagRepository.findByNotice(id);
    }

    @Transactional
    public Long save(Notice_Tag notice_tag) {
        notice_tagRepository.save(notice_tag);
        return notice_tag.getId();
    }

    @Transactional
    public int deleteByNoticeId(Long notice_id) {
        return notice_tagRepository.deleteByNoticeId(notice_id);
    }

    @Transactional
    public void attachTags(Notice notice, List<String> names) {
        if (names.isEmpty()) return;
        names.forEach(name -> attachTag(notice, name));
    }

    private void attachTag(Notice notice, String name) {
        Notice_Tag notice_tag = Notice_Tag.builder()
                .notice(notice)
                .tag(tagService.findOrCreate(name))
                .build();
        notice_tagRepository.save(notice_tag);
    }
}
