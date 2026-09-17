package haja.Project.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class Tasknotice_TagTest {
    @Test
    void retainsFieldsAndRelationships() {
        Tasknotice_Tag entity = new Tasknotice_Tag();
        var id = 7L;
        entity.setId(id);
        var tasknotice = new Tasknotice();
        entity.setTasknotice(tasknotice);
        var tag = new Tag();
        entity.setTag(tag);
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getTasknotice()).isEqualTo(tasknotice);
        assertThat(entity.getTag()).isEqualTo(tag);
    }
}
