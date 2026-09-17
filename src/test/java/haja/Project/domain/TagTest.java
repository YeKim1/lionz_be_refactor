package haja.Project.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class TagTest {
    @Test
    void retainsFieldsAndRelationships() {
        Tag entity = new Tag();
        var id = 7L;
        entity.setId(id);
        var name = "spring";
        entity.setName(name);
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo(name);
    }
}
