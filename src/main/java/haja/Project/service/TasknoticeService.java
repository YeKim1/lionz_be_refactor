package haja.Project.service;

import haja.Project.api.dto.TasknoticeRequestDto;
import haja.Project.domain.Tasknotice;
import haja.Project.repository.TasknoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TasknoticeService {

    private final TasknoticeRepository tasknoticeRepository;
    private final MemberService memberService;
    private final Tasknotice_TagService tasknotice_TagService;

    @Transactional
    public Long save(Tasknotice tasknotice){
        tasknoticeRepository.save(tasknotice);
        return tasknotice.getId();
    }

    public Tasknotice findOne(Long id){
        return tasknoticeRepository.findOne(id);
    }

    public List<Tasknotice> findAll() {
        return tasknoticeRepository.findAll();
    }

    public List<Tasknotice> findPartAll() {
        return tasknoticeRepository.findPartAll();
    }

    public List<Tasknotice> findFe() { return tasknoticeRepository.findFe(); }

    public List<Tasknotice> findBe() { return tasknoticeRepository.findBe(); }

    public List<Tasknotice> findByWord(String word){
        return tasknoticeRepository.findByWord(word);
    }

    @Transactional      // 과제공지글 수정
    public Long update(Tasknotice tasknotice){
        tasknoticeRepository.save(tasknotice);
        return tasknotice.getId();
    }

    @Transactional
    public void delete(Long id) {
        tasknoticeRepository.deleteTasknotice(id);
    }


    @Transactional
    public Tasknotice create(Long memberId, TasknoticeRequestDto.Create request) {
        Tasknotice tasknotice = Tasknotice.builder()
                .member(memberService.findById(memberId).get())
                .title(request.getTitle())
                .explanation(request.getExplanation())
                .date(LocalDateTime.now())
                .deadline(request.getDeadline())
                .target(request.getTarget())
                .link(request.getLink())
                .build();
        tasknoticeRepository.save(tasknotice);
        addTags(tasknotice, request.getTags());
        return tasknotice;
    }

    private void addTags(Tasknotice tasknotice, List<String> tags) {
        tasknotice_TagService.attachTags(tasknotice, tags);
    }
}
