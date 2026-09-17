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

class TokenDtoTest {
    @Test
    void preservesTokenPayloadThroughJsonRoundTrip() throws Exception {
        LocalDateTime expiration = LocalDateTime.of(2026, 10, 1, 12, 0);
        TokenDto token = TokenDto.builder().id("7").count(2).grantType("Bearer").accessToken("access")
                .refreshToken("refresh").accessTokenExpiresIn(expiration).build();
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        TokenDto result = mapper.readValue(mapper.writeValueAsString(token), TokenDto.class);
        assertThat(result).extracting("id", "count", "grantType", "accessToken", "refreshToken", "accessTokenExpiresIn")
                .containsExactly("7", 2, "Bearer", "access", "refresh", expiration);
    }
}
