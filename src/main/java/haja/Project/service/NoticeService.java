package haja.Project.service;

import haja.Project.api.dto.NoticeRequestDto;
import haja.Project.api.dto.NoticeResponseDto;
import haja.Project.domain.Notice;
import haja.Project.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final MemberService memberService;
    private final Notice_TagService notice_TagService;

    @Transactional
    public Long save(Notice notice) {
        noticeRepository.save(notice);
        return notice.getId();
    }

    public Notice findById(Long id) { return noticeRepository.findById(id); }

    public List<Notice> findAll() { return noticeRepository.findAll(); }

    public List<Notice> findByTarget(String target) { return noticeRepository.findByTarget(target); }

    public List<Notice> findByWord(String word){
        return noticeRepository.findByWord(word);
    }

    @Transactional
    public void update(Long id, String title, String explanation, LocalDateTime deadline) {
        noticeRepository.update(id, title, explanation, deadline);
    }

    @Transactional
    public void delete(Long id) { noticeRepository.delete(id); }

    @Transactional
    public NoticeResponseDto.NoticeInfo create(Long memberId, NoticeRequestDto.Create request) {
        Notice notice = Notice.builder()
                .member(memberService.findById(memberId).get())
                .title(request.getTitle())
                .explanation(request.getExplanation())
                .date(LocalDateTime.now())
                .deadline(request.getDeadline())
                .target(request.getTarget())
                .build();
        noticeRepository.save(notice);
        addTags(notice, request.getTags());
        return NoticeResponseDto.NoticeInfo.from(notice);
    }

    private void addTags(Notice notice, List<String> tags) {
        notice_TagService.attachTags(notice, tags);
    }
}
