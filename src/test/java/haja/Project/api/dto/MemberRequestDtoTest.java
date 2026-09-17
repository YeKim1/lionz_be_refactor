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

class MemberRequestDtoTest {
    @Test
    void convertsSignupRequestToEncodedUserWithDefaultImage() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode("secret")).thenReturn("encoded");
        Member member = new MemberRequestDto("user@example.com", "secret").toMember(encoder);
        assertThat(member).extracting("email", "password", "authority")
                .containsExactly("user@example.com", "encoded", Authority.ROLE_USER);
        assertThat(member.getImage()).extracting("img_link", "img_name", "img_path")
                .containsExactly("https://lionz.kro.kr/member/img/DefaultProfile.png", "DefaultProfile.png", "/home/img/DefaultProfile.png");
        verify(encoder).encode("secret");
    }
    @Test
    void createsUnauthenticatedCredentials() {
        var authentication = new MemberRequestDto("user@example.com", "secret").toAuthentication();
        assertThat(authentication.getPrincipal()).isEqualTo("user@example.com");
        assertThat(authentication.getCredentials()).isEqualTo("secret");
        assertThat(authentication.isAuthenticated()).isFalse();
    }
    @Test
    void bindsProfileUpdateRequests() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertThat(mapper.readValue("{\"comment\":\"hello\"}", MemberRequestDto.UpdateComment.class).getComment()).isEqualTo("hello");
        assertThat(mapper.readValue("{\"password\":\"new\"}", MemberRequestDto.UpdatePassword.class).getPassword()).isEqualTo("new");
    }
}
