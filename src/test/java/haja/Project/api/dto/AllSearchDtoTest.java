package haja.Project.api.dto;

import haja.Project.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AllSearchDtoTest {
    @Test
    void mapsEverySearchCategoryInOrder() {
        LocalDateTime date = LocalDateTime.of(2026, 9, 17, 12, 0);
        Notice notice = new Notice(); notice.setId(1L); notice.setTitle("notice"); notice.setExplanation("body");
        notice.setDate(date); notice.setDeadline(date.plusDays(1)); notice.setTarget(Part.ALL);
        Tasknotice tasknotice = new Tasknotice(); tasknotice.setId(2L); tasknotice.setTitle("assignment");
        tasknotice.setExplanation("instructions"); tasknotice.setDate(date); tasknotice.setDeadline(date.plusDays(2)); tasknotice.setTarget(Part.BE);
        Task task = new Task(); task.setId(3L); task.setLink("link"); task.setExplanation("submission");
        Task second = new Task(); second.setId(4L);
        Tasknotice_Tag tag = new Tasknotice_Tag(); tag.setId(5L);
        var result = AllSearchDto.Response.of(List.of(notice), List.of(tasknotice), List.of(task, second), List.of(tag));
        assertThat(result.getNotice().get(0)).extracting("id", "title", "explanation", "date", "target", "deadline")
                .containsExactly(1L, "notice", "body", date, Part.ALL, date.plusDays(1));
        assertThat(result.getNotice().get(0).getTag()).isEmpty();
        assertThat(result.getTasknotice().get(0)).extracting("id", "title", "explanation", "date", "target", "deadline")
                .containsExactly(2L, "assignment", "instructions", date, Part.BE, date.plusDays(2));
        assertThat(result.getTask()).extracting("id").containsExactly(3L, 4L);
        assertThat(result.getTask().get(0)).extracting("link", "explanation").containsExactly("link", "submission");
        assertThat(result.getTasknotice_tag()).extracting("id").containsExactly(5L);
        notice.setTitle("changed");
        assertThat(result.getNotice().get(0).getTitle()).isEqualTo("notice");
    }
    @Test
    void returnsEmptyListsForEmptySearch() {
        var result = AllSearchDto.Response.of(List.of(), List.of(), List.of(), List.of());
        assertThat(result.getNotice()).isEmpty(); assertThat(result.getTasknotice()).isEmpty();
        assertThat(result.getTask()).isEmpty(); assertThat(result.getTasknotice_tag()).isEmpty();
    }
}
