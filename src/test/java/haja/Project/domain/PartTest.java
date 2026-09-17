package haja.Project.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PartTest {
    @Test
    void preservesSerializedNames() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertThat(Part.values()).extracting(Enum::name).containsExactly("FE", "BE", "ALL");
        for (Part value : Part.values()) {
            assertThat(mapper.writeValueAsString(value)).isEqualTo("\"" + value.name() + "\"");
            assertThat(mapper.readValue("\"" + value.name() + "\"", Part.class)).isSameAs(value);
        }
    }
}
