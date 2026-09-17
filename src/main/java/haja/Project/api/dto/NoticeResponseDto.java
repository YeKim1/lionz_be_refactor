package haja.Project.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import haja.Project.domain.Member;
import haja.Project.domain.Notice;
import haja.Project.domain.Part;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NoticeResponseDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoticeInfo {
        Long id;
        Member member;
        String title;
        String explanation;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime date;
        Part target;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime deadline;

        public static NoticeInfo from(Notice notice) {
            return new NoticeInfo(
                    notice.getId(),
                    notice.getMember(),
                    notice.getTitle(),
                    notice.getExplanation(),
                    notice.getDate(),
                    notice.getTarget(),
                    notice.getDeadline()
            );
        }
    }
}
