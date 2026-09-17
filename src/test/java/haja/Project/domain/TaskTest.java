package haja.Project.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class TaskTest {
    @Test
    void retainsFieldsAndRelationships() {
        Task entity = new Task();
        var id = 7L;
        entity.setId(id);
        var member = new Member();
        entity.setMember(member);
        var tasknotice = new Tasknotice();
        entity.setTasknotice(tasknotice);
        var link = "https://example.com";
        entity.setLink(link);
        var explanation = "body";
        entity.setExplanation(explanation);
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        entity.setDate(date);
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getMember()).isEqualTo(member);
        assertThat(entity.getTasknotice()).isEqualTo(tasknotice);
        assertThat(entity.getLink()).isEqualTo(link);
        assertThat(entity.getExplanation()).isEqualTo(explanation);
        assertThat(entity.getDate()).isEqualTo(date);
    }
}
