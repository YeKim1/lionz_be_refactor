package haja.Project.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class ScheduleTest {
    @Test
    void retainsFieldsAndRelationships() {
        Schedule entity = new Schedule();
        var id = 7L;
        entity.setId(id);
        var title = "title";
        entity.setTitle(title);
        var category = Category.birthday;
        entity.setCategory(category);
        var startdate = LocalDateTime.of(2026, 9, 1, 12, 0);
        entity.setStartdate(startdate);
        var enddate = LocalDateTime.of(2026, 9, 2, 12, 0);
        entity.setEnddate(enddate);
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getTitle()).isEqualTo(title);
        assertThat(entity.getCategory()).isEqualTo(category);
        assertThat(entity.getStartdate()).isEqualTo(startdate);
        assertThat(entity.getEnddate()).isEqualTo(enddate);
    }
}
