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

class MemberResponseDtoTest {
    @Test
    void mapsPublicProfileWithoutPassword() throws Exception {
        Image image = new Image("url", "name", "path");
        Member member = Member.builder().id(7L).email("user@example.com").password("secret")
                .authority(Authority.ROLE_ADMIN).phone_num("01012345678").part(Part.BE).name("name")
                .comment("comment").major("major").student_id("20260001").image(image).build();
        assertThat(MemberResponseDto.of(member).getEmail()).isEqualTo("user@example.com");
        var info = MemberResponseDto.MemberInfo.from(member);
        assertThat(info).extracting("id", "email", "authority", "phone_num", "part", "name", "comment", "major", "student_id", "image", "accessTokenExpiresIn")
                .containsExactly(7L, "user@example.com", Authority.ROLE_ADMIN, "01012345678", Part.BE, "name", "comment", "major", "20260001", image, null);
        assertThat(new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(info)).has("password")).isFalse();
    }
    @Test
    void computesRemainingMinutesForFutureAndExpiredTokens() {
        Member member = new Member();
        for (int minutes : new int[]{30, -30}) {
            LocalDateTime expiration = LocalDateTime.now().plusMinutes(minutes); member.setAccessTokenExpiresIn(expiration);
            long upper = Duration.between(LocalDateTime.now(), expiration).toMinutes();
            long actual = MemberResponseDto.MemberInfo.from(member).getAccessTokenExpiresIn();
            long lower = Duration.between(LocalDateTime.now(), expiration).toMinutes();
            assertThat(actual).isBetween(lower, upper);
        }
    }
}
