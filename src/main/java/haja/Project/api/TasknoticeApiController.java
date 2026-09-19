package haja.Project.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import haja.Project.api.dto.ApiResponse;
import haja.Project.api.dto.TasknoticeRequestDto;
import haja.Project.api.dto.TasknoticeResponseDto;
import haja.Project.domain.*;
import haja.Project.service.*;
import haja.Project.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController // Rest API를 처리하는 Controller 임을 의미
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tasknotice")
public class TasknoticeApiController {

    private final TasknoticeService tasknoticeService;
    //private final UserService userService;
    private final MemberService memberService;
    private final Tasknotice_TagService tasknotice_tagService;
    private final TagService tagService;
    private final TaskService taskService;

    @Operation(summary = "과제 공지사항 생성")
    @PostMapping("tasknotice")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TasknoticeResponseDto.TasknoticeInfo> createTasknotice(
            @RequestBody @Valid TasknoticeRequestDto.Create request) {
        return ApiResponse.from(tasknoticeService.create(SecurityUtil.getCurrentMemberId(), request));
    }

    //수정하기 버튼 -> 이전에 썼던 내용들 그대로 return
    @Operation(summary = "id로 과제 공지사항 조회")
    @GetMapping("tasknotice/{id}")
    public TasknoticeDto tasknotice(
            @PathVariable("id") Long id){

            TasknoticeDto d = new TasknoticeDto(tasknoticeService.findOne(id)); //이렇게 하면 Json이 그대로 오는것을 확인함
            //그니까 수정하기 버튼 누르면 이렇게 주고
            //수정완료 버튼 누르면 request날라온거 set으로 ㄱㄱ
            if (taskService.isSubmit(d.id)) d.isSubmit = true;
            else d.isSubmit = false;

            // 태그 추가
            List<Tasknotice_Tag> tags = tasknotice_tagService.findByTasknoticeId(d.id);
            for (Tasknotice_Tag tag : tags) {
                d.tag.add(tag.getTag().getName());
            }

            return d;

    }

    //수정완료 버튼
    @Operation(summary = "과제 공지사항 수정")
    @PutMapping("tasknotice/{id}")
    public UpdateResponse tasknotice(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateRequest request){
        if(memberService.findById(SecurityUtil.getCurrentMemberId()).get().getAuthority() == Authority.ROLE_ADMIN) {
            Tasknotice tn;
            tn = tasknoticeService.findOne(id);

            //updateTime 도 하나 만들어야할듯
            tn.setDeadline(request.getDeadline()); // postman으로 날짜받는거
            tn.setTarget(request.getTarget());
            tn.setTitle(request.getTitle());
            tn.setExplanation(request.getExplanation());
            tn.setLink(request.getLink());
            Long tn_id = tasknoticeService.save(tn);
            //이러면 이제 과제공지글을 생성이 된거고 이 때 request로 tag_id받고 아래에 tasknoticetag로직 추가
            //request로 tag_id를 어떻게받을까 List로 받아서 for문으로 돌리자

            List<Tasknotice_Tag> tts = tasknotice_tagService.findByTasknoticeId(id); //여기부터 tasknotice_tag삭제 후 생성
            List<ttDTO> result = tts.stream()
                    .map(t -> new ttDTO(t))
                    .collect(Collectors.toList());

            for (int i = 0; i < result.size(); i++) {
                Tasknotice_Tag tt = tasknotice_tagService.findOne(result.get(i).id);
                tasknotice_tagService.delete(tt);
            }

            List<String> tags = request.tags;
            if (tags != null) {
                for (String tag_name : tags) {
                    if (tagService.findByName(tag_name) == null) {
                        Tag tag = new Tag();
                        tag.setName(tag_name);
                        tagService.save(tag);

                        Tasknotice_Tag tasknotice_tag = new Tasknotice_Tag();
                        tasknotice_tag.setTasknotice(tasknoticeService.findOne(tn_id));
                        tasknotice_tag.setTag(tag);
                        tasknotice_tagService.save(tasknotice_tag);
                    } else {
                        Tasknotice_Tag tasknotice_tag = new Tasknotice_Tag();
                        tasknotice_tag.setTasknotice(tasknoticeService.findOne(tn_id));
                        tasknotice_tag.setTag(tagService.findByName(tag_name));
                        tasknotice_tagService.save(tasknotice_tag);
                    }
                }
            }
            return new UpdateResponse(tn_id);
        }
        else
            return null;
    }

    @Operation(summary = "과제 공지사항 삭제")
    @DeleteMapping("tasknotice/{id}")
    public void deleteTasknotice(@PathVariable("id") Long id) {
        if(memberService.findById(SecurityUtil.getCurrentMemberId()).get().getAuthority() == Authority.ROLE_ADMIN) {
            tasknotice_tagService.deleteByTasknoticeId(id);
            taskService.deleteByTasknotice(id);
            tasknoticeService.delete(id);
        }
    }

    @Operation(summary = "모든 과제 공지사항 조회")
    @GetMapping("tasknotice")
    public Result ReadTasknotice() {
        Member member = memberService.findById(SecurityUtil.getCurrentMemberId()).get();
        List<Tasknotice> tasknotices = tasknoticeService.findAll();
        List<TasknoticeDto> collect = tasknotices.stream()
                .map(t -> new TasknoticeDto(t))
                .collect(Collectors.toList());

        for(TasknoticeDto d: collect) {
            // 제출 미제출
            if (taskService.isSubmit(d.id)) d.isSubmit = true;
            else d.isSubmit = false;

            // 태그 추가
            List<Tasknotice_Tag> tags = tasknotice_tagService.findByTasknoticeId(d.id);
            for (Tasknotice_Tag tag: tags) {
                d.tag.add(tag.getTag().getName());
            }
        }
        return new Result(collect);
    }

    //FE파트 tasknotice조회
    @Operation(summary = "FE 대상 과제 공지사항 조회")
    @GetMapping("tasknotice/FE")
    public Result ReadTasknoticeFe() {
        List<Tasknotice> tasknotices = tasknoticeService.findFe();
        List<TasknoticeDto> collect = tasknotices.stream()
                .map(t -> new TasknoticeDto(t))
                .collect(Collectors.toList());
        return new Result(collect);
    }

    // BE파트 tasknotice조회
    @Operation(summary = "BE 대상 과제 공지사항 조회")
    @GetMapping("tasknotice/BE")
    public Result ReadTasknoticeBe() {
        List<Tasknotice> tasknotices = tasknoticeService.findBe();
        List<TasknoticeDto> collect = tasknotices.stream()
                .map(t -> new TasknoticeDto(t))
                .collect(Collectors.toList());
        return new Result(collect);
    }

    @Operation(summary = "ALL 대상 과제 공지사항 조회")
    @GetMapping("tasknotice/ALL")
    public Result ReadtasknoticeAll() {
        List<Tasknotice> tasknotices = tasknoticeService.findPartAll();
        List<TasknoticeDto> collect = tasknotices.stream()
                .map(t -> new TasknoticeDto(t))
                .collect(Collectors.toList());
        return new Result(collect);
    }

    @Data
    static class UpdateRequest{
        private Long id;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime deadline;
        private Part target;
        //private File image; // 이건 나중에
        private String title;
        private String explanation;
        private String link;
        private List<String> tags;
        private List<Long> data;
    }

    @Data
    static class UpdateResponse{
        private Long id;
        UpdateResponse(Long id){ this.id = id;}
    }

    @Data
    static class ttDTO{   // Tasknotice_Tag(객체에서)의 id만 가져오도록
        private Long id;
        public ttDTO(Tasknotice_Tag tasknotice_tag){
            id = tasknotice_tag.getId();
        }
    }

    @Data
    @AllArgsConstructor
    static class TasknoticeDto {
        private Long id;
        private Member member;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime date;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime deadline;
        //private LocalDateTime updatetime;
        private Part target;
        private Image image;
        private String title;
        private String explanation;
        private String link;
        private List<String> tag;

        private Boolean isSubmit;


        public TasknoticeDto(Tasknotice tasknotice) {
            id = tasknotice.getId();
            member = tasknotice.getMember();
            date = tasknotice.getDate();
            deadline = tasknotice.getDeadline();
            //updatetime = tasknotice.getUpdateTime();
            target = tasknotice.getTarget();
            title = tasknotice.getTitle();
            explanation = tasknotice.getExplanation();
            isSubmit = false;
            tag = new ArrayList<>();
            link = tasknotice.getLink();
        }
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }


}