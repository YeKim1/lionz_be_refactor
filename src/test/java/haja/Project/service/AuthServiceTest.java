package haja.Project.service;

import haja.Project.api.dto.*;
import haja.Project.domain.*;
import haja.Project.jwt.TokenProvider;
import haja.Project.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock AuthenticationManagerBuilder authenticationManagerBuilder;
    @Mock AuthenticationManager authenticationManager;
    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TokenProvider tokenProvider;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock MemberService memberService;
    @InjectMocks AuthService service;

    private final MemberRequestDto request = new MemberRequestDto("user@example.com", "password");
    private final Authentication authentication = new UsernamePasswordAuthenticationToken("7", "encoded");
    private final TokenDto tokens = TokenDto.builder().accessToken("new-access").refreshToken("new-refresh")
            .accessTokenExpiresIn(LocalDateTime.of(2026, 10, 1, 12, 0)).build();

    @Test
    void signupEncodesPasswordAndSavesOrdinaryMember() {
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(memberRepository.save(any(Member.class))).thenAnswer(call -> call.getArgument(0));
        assertThat(service.signup(request).getEmail()).isEqualTo("user@example.com");
        ArgumentCaptor<Member> saved = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("encoded");
        assertThat(saved.getValue().getAuthority()).isEqualTo(Authority.ROLE_USER);
        assertThat(saved.getValue().getImage().getImg_name()).isEqualTo("DefaultProfile.png");
    }

    @Test
    void rejectsDuplicateSignupBeforeEncodingOrSaving() {
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(true);
        assertThatThrownBy(() -> service.signup(request)).hasMessage("이미 가입되어 있는 유저입니다");
        verifyNoInteractions(passwordEncoder);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void loginAuthenticatesTracksCountAndPersistsTokenPair() throws Exception {
        AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(new ObjectPostProcessor<Object>() {
            @Override
            public <O> O postProcess(O object) { return object; }
        });
        builder.parentAuthenticationManager(authenticationManager);
        builder.build();
        service = new AuthService(builder, memberRepository, passwordEncoder, tokenProvider, refreshTokenRepository, memberService);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateTokenDto(authentication)).thenReturn(tokens);
        Member member = Member.builder().id(7L).build(); member.setCount(2);
        when(memberService.findByEmail(request.getEmail())).thenReturn(Optional.of(member));
        assertThat(service.login(request)).isSameAs(tokens);
        assertThat(tokens.getCount()).isEqualTo(2);
        verify(authenticationManager).authenticate(argThat(a -> a.getName().equals(request.getEmail()) && a.getCredentials().equals("password")));
        verify(memberService).updateCount(member, 2);
        verify(memberService).setTokenCount(member, tokens.getAccessTokenExpiresIn());
        verify(refreshTokenRepository).save(argThat(t -> t.getKey().equals("7") && t.getValue().equals("new-refresh") && t.getAccessToken().equals("new-access")));
    }

    @Test
    void failedLoginDoesNotIssueTokensOrChangeMember() throws Exception {
        AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(new ObjectPostProcessor<Object>() {
            @Override
            public <O> O postProcess(O object) { return object; }
        });
        builder.parentAuthenticationManager(authenticationManager);
        builder.build();
        service = new AuthService(builder, memberRepository, passwordEncoder, tokenProvider, refreshTokenRepository, memberService);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));
        assertThatThrownBy(() -> service.login(request)).isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(tokenProvider, memberService, refreshTokenRepository);
    }

    private TokenRequestDto reissueRequest() {
        TokenRequestDto dto = new TokenRequestDto(); dto.setAccessToken("old-access"); dto.setRefreshToken("old-refresh");
        return dto;
    }

    @Test
    void rejectsInvalidRefreshTokenBeforeLookingUpOwner() {
        assertThatThrownBy(() -> service.reissue(reissueRequest())).hasMessage("Refresh Token 이 유효하지 않습니다.");
        verify(tokenProvider, never()).getAuthentication(any());
        verifyNoInteractions(refreshTokenRepository, memberService);
    }

    @Test
    void rejectsLoggedOutUser() {
        when(tokenProvider.validateToken("old-refresh")).thenReturn(true);
        when(tokenProvider.getAuthentication("old-access")).thenReturn(authentication);
        when(refreshTokenRepository.findByAccessToken("old-access")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.reissue(reissueRequest())).hasMessage("로그아웃 된 사용자입니다.");
        verify(tokenProvider, never()).generateTokenDto(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rejectsMismatchedRefreshToken() {
        when(tokenProvider.validateToken("old-refresh")).thenReturn(true);
        when(tokenProvider.getAuthentication("old-access")).thenReturn(authentication);
        when(refreshTokenRepository.findByAccessToken("old-access"))
                .thenReturn(Optional.of(new RefreshToken("7", "different", "old-access")));
        assertThatThrownBy(() -> service.reissue(reissueRequest())).hasMessage("토큰의 유저 정보가 일치하지 않습니다.");
        verify(tokenProvider, never()).generateTokenDto(any());
        verifyNoInteractions(memberService);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void reissuesAndRotatesStoredTokensAndExpiration() {
        RefreshToken stored = new RefreshToken("7", "old-refresh", "old-access");
        Member member = Member.builder().id(7L).build();
        when(tokenProvider.validateToken("old-refresh")).thenReturn(true);
        when(tokenProvider.getAuthentication("old-access")).thenReturn(authentication);
        when(refreshTokenRepository.findByAccessToken("old-access")).thenReturn(Optional.of(stored));
        when(tokenProvider.generateTokenDto(authentication)).thenReturn(tokens);
        when(memberService.findById(7L)).thenReturn(Optional.of(member));
        assertThat(service.reissue(reissueRequest())).isSameAs(tokens);
        assertThat(stored).extracting("key", "value", "accessToken").containsExactly("7", "new-refresh", "new-access");
        verify(refreshTokenRepository).save(stored);
        verify(memberService).setTokenCount(member, tokens.getAccessTokenExpiresIn());
    }
}
