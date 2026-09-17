package haja.Project.api;

import haja.Project.domain.*;
import haja.Project.service.*;
import haja.Project.repository.*;
import haja.Project.api.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock AuthService authService;
    @Mock MemberService memberService;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @InjectMocks AuthController controller;
    private MockMvc mvc;
    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("7", "unused"));
    }
    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    private static final String CREDENTIALS = "{\"email\":\"user@example.com\",\"password\":\"secret\"}";
    @Test
    void signsUpUsingRequestCredentials() throws Exception {
        when(authService.signup(any())).thenReturn(new MemberResponseDto("user@example.com"));
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content(CREDENTIALS))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("user@example.com"));
        verify(authService).signup(argThat(r -> r.getEmail().equals("user@example.com") && r.getPassword().equals("secret")));
    }
    @Test
    void logsInAndReturnsTokenPayload() throws Exception {
        when(authService.login(any())).thenReturn(TokenDto.builder().accessToken("access").refreshToken("refresh").grantType("Bearer").count(1).build());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(CREDENTIALS))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh")).andExpect(jsonPath("$.count").value(1));
        verify(authService).login(argThat(r -> r.getEmail().equals("user@example.com") && r.getPassword().equals("secret")));
    }
    @Test
    void reissuesUsingStoredRefreshToken() throws Exception {
        when(refreshTokenRepository.findByAccessToken("old")).thenReturn(Optional.of(new RefreshToken("7", "stored", "old")));
        when(authService.reissue(any())).thenReturn(TokenDto.builder().accessToken("new").build());
        mvc.perform(post("/auth/reissue").contentType(MediaType.APPLICATION_JSON).content("{\"accessToken\":\"old\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("new"));
        verify(authService).reissue(argThat(r -> r.getAccessToken().equals("old") && r.getRefreshToken().equals("stored")));
    }
    @Test
    void missingStoredTokenDoesNotCallReissueService() {
        AuthController.accessDTO request = new AuthController.accessDTO(); request.setAccessToken("missing");
        assertThatThrownBy(() -> controller.reissue(request)).isInstanceOf(NoSuchElementException.class);
        verifyNoInteractions(authService);
    }
    @Test
    void rejectsMissingRequestBody() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

}
