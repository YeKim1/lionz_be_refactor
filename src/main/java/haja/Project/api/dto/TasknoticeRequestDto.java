package haja.Project.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import haja.Project.domain.Part;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class TasknoticeRequestDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Create {
        String title;
        String explanation;
        Part target;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime deadline;
        List<String> tags;
        String link;
    }
}
