package haja.Project.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import haja.Project.domain.Member;
import haja.Project.domain.Tasknotice;
import haja.Project.domain.Part;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class TasknoticeResponseDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TasknoticeInfo {
        Long id;
        Member member;
        String title;
        String explanation;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime date;
        Part target;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime deadline;
        String link;

        public static TasknoticeInfo from(Tasknotice notice) {
            return new TasknoticeInfo(
                    notice.getId(),
                    notice.getMember(),
                    notice.getTitle(),
                    notice.getExplanation(),
                    notice.getDate(),
                    notice.getTarget(),
                    notice.getDeadline(),
                    notice.getLink()
            );
        }
    }
}
