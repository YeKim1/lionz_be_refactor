package haja.Project.api.dto;

import haja.Project.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiResponseTest {
    @Test
    void wrapsPayloadWithoutChangingIt() throws Exception {
        List<String> data = List.of("one", "two");
        ApiResponse<List<String>> response = ApiResponse.from(data);
        assertThat(response.getData()).isSameAs(data);
        assertThat(new ObjectMapper().writeValueAsString(response)).isEqualTo("{\"data\":[\"one\",\"two\"]}");
    }
    @Test
    void supportsNullPayload() { assertThat(ApiResponse.from(null).getData()).isNull(); }
}
