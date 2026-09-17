package haja.Project.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class Notice_TagTest {
    @Test
    void retainsFieldsAndRelationships() {
        Notice_Tag entity = new Notice_Tag();
        var id = 7L;
        entity.setId(id);
        var notice = new Notice();
        entity.setNotice(notice);
        var tag = new Tag();
        entity.setTag(tag);
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getNotice()).isEqualTo(notice);
        assertThat(entity.getTag()).isEqualTo(tag);
    }
}
