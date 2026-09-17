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

class TokenRequestDtoTest {
    @Test
    void bindsBothTokensWithoutMixingThem() throws Exception {
        TokenRequestDto request = new ObjectMapper().readValue("{\"accessToken\":\"access\",\"refreshToken\":\"refresh\"}", TokenRequestDto.class);
        assertThat(request.getAccessToken()).isEqualTo("access");
        assertThat(request.getRefreshToken()).isEqualTo("refresh");
    }
}
