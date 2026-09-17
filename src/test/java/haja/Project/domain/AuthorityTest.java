package haja.Project.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AuthorityTest {
    @Test
    void preservesSerializedNames() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertThat(Authority.values()).extracting(Enum::name).containsExactly("ROLE_USER", "ROLE_ADMIN");
        for (Authority value : Authority.values()) {
            assertThat(mapper.writeValueAsString(value)).isEqualTo("\"" + value.name() + "\"");
            assertThat(mapper.readValue("\"" + value.name() + "\"", Authority.class)).isSameAs(value);
        }
    }
}
