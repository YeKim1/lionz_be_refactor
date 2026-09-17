package haja.Project.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class NoticeTest {
    @Test
    void retainsFieldsAndRelationships() {
        Notice entity = new Notice();
        var id = 7L;
        entity.setId(id);
        var member = new Member();
        entity.setMember(member);
        var target = Part.BE;
        entity.setTarget(target);
        var title = "title";
        entity.setTitle(title);
        var explanation = "body";
        entity.setExplanation(explanation);
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        entity.setDate(date);
        var deadline = LocalDateTime.of(2026, 9, 2, 12, 0);
        entity.setDeadline(deadline);
        var like = 3L;
        entity.setLike(like);
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getMember()).isEqualTo(member);
        assertThat(entity.getTarget()).isEqualTo(target);
        assertThat(entity.getTitle()).isEqualTo(title);
        assertThat(entity.getExplanation()).isEqualTo(explanation);
        assertThat(entity.getDate()).isEqualTo(date);
        assertThat(entity.getDeadline()).isEqualTo(deadline);
        assertThat(entity.getLike()).isEqualTo(like);
    }
}
