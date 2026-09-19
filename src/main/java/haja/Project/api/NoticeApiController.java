package haja.Project.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import haja.Project.api.dto.ApiResponse;
import haja.Project.api.dto.NoticeRequestDto;
import haja.Project.api.dto.NoticeResponseDto;
import haja.Project.domain.*;
import haja.Project.service.MemberService;
import haja.Project.service.NoticeService;
import haja.Project.service.Notice_TagService;
import haja.Project.service.TagService;
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

@RestController
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Notice")
public class NoticeApiController {
    private final NoticeService noticeService;
    private final MemberService memberService;
    private final TagService tagService;
    private final Notice_TagService notice_tagService;

    @Operation(summary = "공지사항 생성", description = "공지사항 생성 페이지에서 공지 생성 완료하기 버튼")
    @PostMapping("notice")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NoticeResponseDto.NoticeInfo> createNotice(@RequestBody @Valid NoticeRequestDto.Create request) {
        return ApiResponse.from(noticeService.create(SecurityUtil.getCurrentMemberId(), request));
    }


    @Operation(summary = "전체 공지사항 조회")
    @GetMapping("notice")
    public Result readNotice() {
        List<Notice> notices = noticeService.findAll();
        List<NoticeDto> collect = notices.stream().map(n -> new NoticeDto(n))
                .collect(Collectors.toList());

        for (NoticeDto dto: collect) {
            List<Notice_Tag> tags = notice_tagService.findByNotice(dto.id);
            for (Notice_Tag tag: tags) {
                dto.tag.add(tag.getTag().getName());
            }
        }
        return new Result(collect);
    }

    @Operation(summary = "파트별 공지사항 조회", description = "target에는 FE, BE, ALL이 들어갑니다")
    @GetMapping("notice/part/{target}")
    public Result readNoticeByPart(@PathVariable("target") String target) {
        List<Notice> notices = noticeService.findByTarget(target);
        List<NoticeDto> collect = notices.stream().map(n -> new NoticeDto(n))
                .collect(Collectors.toList());
        return new Result(collect);
    }

    @Operation(summary = "id로 공지사항 조회")
    @GetMapping("notice/{id}")
    public NoticeDto FindOne(@PathVariable("id") Long id) {
        NoticeDto d = new NoticeDto(noticeService.findById(id));

        List<Notice_Tag> tags = notice_tagService.findByNotice(d.id);
        for (Notice_Tag tag: tags) {
            d.tag.add(tag.getTag().getName());
        }

        return d;
    }

    // 공지사항 생성 리팩토링 후 반영 예정
//    @Operation(summary = "공지사항 수정")
//    @PutMapping("notice/{id}")
//    public NoticeResponse updateNotice(@PathVariable("id") Long id,
//            @RequestBody @Valid NoticeRequest request) {
//        if(memberService.findById(SecurityUtil.getCurrentMemberId()).get().getAuthority() == Authority.ROLE_ADMIN) {
//            noticeService.update(id, request.title, request.explanation, request.deadline);
//            notice_tagService.deleteByNoticeId(id);
//
//            // 중복 코드 없애면 좋을텐데,, 나중에 ㄱㄱ
//            List<String> tags = request.tags;
//            if (tags != null) {
//                for (String tag_name : tags) {
//                    // 없는 태그면 Tag 생성하고 Notice_Tag 생성
//                    if (tagService.findByName(tag_name) == null) {
//                        Tag tag = new Tag();
//                        tag.setName(tag_name);
//                        tagService.save(tag);
//
//                        Notice_Tag notice_tag = new Notice_Tag();
//                        notice_tag.setNotice(noticeService.findById(id));
//                        notice_tag.setTag(tag);
//                        notice_tagService.save(notice_tag);
//
//                    }
//                    // 있는 태그면 Notice_Tag만 생성
//                    else {
//                        Notice_Tag notice_tag = new Notice_Tag();
//                        notice_tag.setNotice(noticeService.findById(id));
//                        notice_tag.setTag(tagService.findByName(tag_name));
//                        notice_tagService.save(notice_tag);
//                    }
//                }
//            }
//
//            return new NoticeResponse(id);
//        }
//        else
//            return null;
//    }

    @Operation(summary = "공지사항 삭제")
    @DeleteMapping("notice/{id}")
    public void deleteNotice(@PathVariable("id") Long id) {
        if(memberService.findById(SecurityUtil.getCurrentMemberId()).get().getAuthority() == Authority.ROLE_ADMIN) {
            notice_tagService.deleteByNoticeId(id);
            noticeService.delete(id);
        }
    }


    @Data
    static class NoticeResponse {
        Long id;
        public NoticeResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class NoticeDto {
        Long id;
        Member member;
        String title;
        String explanation;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime date;
        Part target;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime deadline;
        List<String> tag;


        public NoticeDto(Notice notice) {
            this.id = notice.getId();
            this.member = notice.getMember();
            this.title = notice.getTitle();
            this.explanation = notice.getExplanation();
            this.date = notice.getDate();
            this.deadline = notice.getDeadline();
            this.target = notice.getTarget();
            tag = new ArrayList<>();
        }
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }
}
