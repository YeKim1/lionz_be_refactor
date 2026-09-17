package haja.Project.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class TasknoticeTest {
    @Test
    void retainsFieldsAndRelationships() {
        Tasknotice entity = new Tasknotice();
        var id = 7L;
        entity.setId(id);
        var member = new Member();
        entity.setMember(member);
        var target = Part.FE;
        entity.setTarget(target);
        var title = "title";
        entity.setTitle(title);
        var explanation = "body";
        entity.setExplanation(explanation);
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        entity.setDate(date);
        var deadline = LocalDateTime.of(2026, 9, 2, 12, 0);
        entity.setDeadline(deadline);
        var link = "https://example.com";
        entity.setLink(link);
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getMember()).isEqualTo(member);
        assertThat(entity.getTarget()).isEqualTo(target);
        assertThat(entity.getTitle()).isEqualTo(title);
        assertThat(entity.getExplanation()).isEqualTo(explanation);
        assertThat(entity.getDate()).isEqualTo(date);
        assertThat(entity.getDeadline()).isEqualTo(deadline);
        assertThat(entity.getLink()).isEqualTo(link);
    }
}
