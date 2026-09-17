package haja.Project.service;

import haja.Project.domain.*;
import haja.Project.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {
    private final MemberRepository repository = mock(MemberRepository.class);
    private final CustomUserDetailsService service = new CustomUserDetailsService(repository);

    @ParameterizedTest
    @EnumSource(Authority.class)
    void mapsIdPasswordAndAuthority(Authority authority) {
        Member member = Member.builder().id(7L).email("user@example.com").password("encoded").authority(authority).build();
        when(repository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        var result = service.loadUserByUsername(member.getEmail());
        assertThat(result.getUsername()).isEqualTo("7");
        assertThat(result.getPassword()).isEqualTo("encoded");
        assertThat(result.getAuthorities()).extracting("authority").containsExactly(authority.name());
    }

    @Test
    void rejectsMissingUser() {
        when(repository.findByEmail("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class).hasMessage("missing -> 데이터베이스에서 찾을 수 없습니다.");
    }
}
