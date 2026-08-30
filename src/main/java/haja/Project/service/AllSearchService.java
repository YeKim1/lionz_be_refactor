package haja.Project.service;

import haja.Project.api.dto.AllSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AllSearchService {
    private final NoticeService noticeService;
    private final TasknoticeService tasknoticeService;
    private final TaskService taskService;
    private final Tasknotice_TagService tasknotice_tagService;

    public AllSearchDto.Response search(String word) {
        return AllSearchDto.Response.of(
                noticeService.findByWord(word),
                tasknoticeService.findByWord(word),
                taskService.findByWord(word),
                tasknotice_tagService.findByTagName(word)
        );
    }
}
