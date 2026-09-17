package haja.Project.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CategoryTest {
    @Test
    void preservesSerializedNames() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertThat(Category.values()).extracting(Enum::name).containsExactly("inhaSche", "centSche", "birthday");
        for (Category value : Category.values()) {
            assertThat(mapper.writeValueAsString(value)).isEqualTo("\"" + value.name() + "\"");
            assertThat(mapper.readValue("\"" + value.name() + "\"", Category.class)).isSameAs(value);
        }
    }
}
