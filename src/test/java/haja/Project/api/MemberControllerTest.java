package haja.Project.api;

import haja.Project.api.dto.MemberRequestDto;
import haja.Project.api.dto.MemberResponseDto.MemberInfo;
import haja.Project.domain.Image;
import haja.Project.domain.Member;
import haja.Project.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Standalone MVC tests cover routing/JSON; direct tests cover controller delegation.
// Security filters and database configuration are deliberately outside this unit test scope.
@ExtendWith(MockitoExtension.class)
class MemberControllerTest {
    @Mock MemberService memberService;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks MemberController memberController;
    @TempDir Path tempDir;
    private MockMvc mockMvc;
    private Member member;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));
        member = Member.builder().id(1L).email("member@example.com")
                .name("홍길동").password("secret").build();
        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsCurrentMemberWithoutPassword() throws Exception {
        when(memberService.findById(1L)).thenReturn(Optional.of(member));
        mockMvc.perform(get("/member"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("member@example.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
        verify(memberService).findById(1L);
    }

    @Test
    void looksUpRequestedIdInsteadOfAuthenticatedId() throws Exception {
        Member other = Member.builder().id(2L).email("other@example.com").build();
        when(memberService.findById(2L)).thenReturn(Optional.of(other));
        mockMvc.perform(get("/member/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.email").value("other@example.com"));
        verify(memberService).findById(2L);
    }

    @Test
    void returnsAllMemberProfiles() throws Exception {
        when(memberService.findAll()).thenReturn(List.of(member, Member.builder().id(2L).build()));
        mockMvc.perform(get("/member/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[0].password").doesNotExist());
    }

    @Test
    void returnsEmptyArrayWhenNoMembersExist() throws Exception {
        when(memberService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/member/all"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void updatesAuthenticatedMemberComment() throws Exception {
        member.setComment("새 소개");
        when(memberService.updateComment(1L, "새 소개")).thenReturn(MemberInfo.from(member));
        mockMvc.perform(put("/member/comment").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"새 소개\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comment").value("새 소개"));
        verify(memberService).updateComment(1L, "새 소개");
    }

    @Test
    void rejectsMalformedCommentRequest() throws Exception {
        mockMvc.perform(put("/member/comment").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(memberService);
    }

    @Test
    void delegatesPasswordUpdateAndWrapsResponse() {
        // UpdatePassword has a class-level @NotBlank, so this tests delegation, not MVC validation.
        MemberRequestDto.UpdatePassword request = new MemberRequestDto.UpdatePassword();
        request.setPassword("new-password");
        MemberInfo info = MemberInfo.from(member);
        when(memberService.updatePassword(1L, "new-password")).thenReturn(info);
        assertThat(memberController.updateMemberPassword(request).getData()).isSameAs(info);
        verify(memberService).updatePassword(1L, "new-password");
    }

    @Test
    void uploadsImageForAuthenticatedMember() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", new byte[]{1});
        mockMvc.perform(multipart("/member/img").file(file)).andExpect(status().isOk());
        verify(memberService).setImage(1L, file);
    }

    @Test
    void propagatesImageUploadFailure() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", new byte[]{1});
        doThrow(new IOException("저장 실패")).when(memberService).setImage(1L, file);
        assertThatThrownBy(() -> memberController.updateMemberImage(file))
                .isInstanceOf(IOException.class).hasMessage("저장 실패");
    }

    @Test
    void rejectsCurrentMemberLookupWithoutAuthentication() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> memberController.MemberInfo())
                .isInstanceOf(RuntimeException.class).hasMessage("Security Context 에 인증 정보가 없습니다.");
        verifyNoInteractions(memberService);
    }

    @Test
    void propagatesMissingMemberOnLookup() {
        when(memberService.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> memberController.MemberInfo()).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> memberController.findMemberInfoById(1L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void keepsDefaultProfileImageAndFile() throws Exception {
        Path imagePath = Files.write(tempDir.resolve("DefaultProfile.png"), new byte[]{1});
        Image image = new Image("/default.png", "DefaultProfile.png", imagePath.toString());
        member.setImage(image);
        when(memberService.findById(1L)).thenReturn(Optional.of(member));
        mockMvc.perform(delete("/member/img")).andExpect(status().isOk());
        assertThat(imagePath).exists();
        assertThat(member.getImage()).isSameAs(image);
        verify(memberService, never()).deleteImage(any());
    }

    @Test
    void deletesCustomProfileFileAndDelegatesImageRemoval() throws Exception {
        Path imagePath = Files.write(tempDir.resolve("profile.png"), new byte[]{1});
        member.setImage(new Image("/profile.png", "profile.png", imagePath.toString()));
        when(memberService.findById(1L)).thenReturn(Optional.of(member));
        mockMvc.perform(delete("/member/img")).andExpect(status().isOk());
        assertThat(imagePath).doesNotExist();
        verify(memberService).deleteImage(member);
    }

    @Test
    void removesImageReferenceEvenWhenFileIsMissing() {
        member.setImage(new Image("/missing.png", "missing.png", tempDir.resolve("missing.png").toString()));
        when(memberService.findById(1L)).thenReturn(Optional.of(member));
        memberController.deleteImage();
        verify(memberService).deleteImage(member);
    }
}
