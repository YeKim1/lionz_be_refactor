package haja.Project.service;

import haja.Project.api.dto.MemberResponseDto.MemberInfo;
import haja.Project.domain.Image;
import haja.Project.domain.Member;
import haja.Project.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock MemberRepository memberRepository;
    @InjectMocks MemberService memberService;
    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder().id(1L).email("member@example.com")
                .password("old-password").comment("기존 소개").build();
    }

    @Test
    void findsMemberInfoById() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        assertThat(memberService.findMemberInfoById(1L).getEmail()).isEqualTo(member.getEmail());
    }

    @Test
    void rejectsMissingMemberInfoById() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> memberService.findMemberInfoById(1L))
                .isInstanceOf(RuntimeException.class).hasMessage("로그인 유저 정보가 없습니다.");
    }

    @Test
    void findsMemberInfoByEmail() {
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        assertThat(memberService.findMemberInfoByEmail(member.getEmail()).getEmail())
                .isEqualTo(member.getEmail());
    }

    @Test
    void rejectsMissingMemberInfoByEmail() {
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> memberService.findMemberInfoByEmail(member.getEmail()))
                .isInstanceOf(RuntimeException.class).hasMessage("유저 정보가 없습니다.");
    }

    @Test
    void returnsAllMembers() {
        List<Member> members = List.of(member, Member.builder().id(2L).build());
        when(memberRepository.findAll()).thenReturn(members);
        assertThat(memberService.findAll()).containsExactlyElementsOf(members);
    }

    @Test
    void returnsEmptyMemberList() {
        when(memberRepository.findAll()).thenReturn(List.of());
        assertThat(memberService.findAll()).isEmpty();
    }

    @Test
    void returnsMemberOptionals() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        assertThat(memberService.findById(1L)).containsSame(member);
        assertThat(memberService.findByEmail(member.getEmail())).containsSame(member);
    }

    @Test
    void preservesEmptyMemberOptionals() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.empty());
        assertThat(memberService.findById(1L)).isEmpty();
        assertThat(memberService.findByEmail(member.getEmail())).isEmpty();
    }

    @Test
    void updatesCommentAndReturnsUpdatedProfile() {
        when(memberRepository.findByIdOrElseThrow(1L)).thenReturn(member);
        MemberInfo result = memberService.updateComment(1L, "새 소개");
        assertThat(member.getComment()).isEqualTo("새 소개");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getComment()).isEqualTo("새 소개");
    }

    @Test
    void updatesPasswordAndReturnsMemberProfile() {
        when(memberRepository.findByIdOrElseThrow(1L)).thenReturn(member);
        MemberInfo result = memberService.updatePassword(1L, "new-password");
        assertThat(member.getPassword()).isEqualTo("new-password");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo(member.getEmail());
    }

    @Test
    void propagatesMissingMemberForUpdates() {
        when(memberRepository.findByIdOrElseThrow(1L))
                .thenThrow(new IllegalArgumentException("존재하지 않는 유저 정보입니다."));
        assertThatThrownBy(() -> memberService.updateComment(1L, "소개"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("존재하지 않는 유저 정보입니다.");
        assertThatThrownBy(() -> memberService.updatePassword(1L, "password"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("존재하지 않는 유저 정보입니다.");
        assertThatThrownBy(() -> memberService.setImage(1L, new MockMultipartFile("file", new byte[0])))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("존재하지 않는 유저 정보입니다.");
        verify(memberRepository, never()).save(any());
    }

    @Test
    void deletesImageAndSavesMember() {
        member.setImage(new Image("/profile.png", "profile.png", "/tmp/profile.png"));
        memberService.deleteImage(member);
        assertThat(member.getImage()).isNull();
        verify(memberRepository).save(member);
    }

    @Test
    void rejectsEmptyImageWithoutReplacingExistingImage() {
        Image original = new Image("/profile.png", "profile.png", "/tmp/profile.png");
        member.setImage(original);
        when(memberRepository.findByIdOrElseThrow(1L)).thenReturn(member);
        assertThatThrownBy(() -> memberService.setImage(1L, new MockMultipartFile("file", new byte[0])))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("비어있는 파일입니다.");
        assertThat(member.getImage()).isSameAs(original);
    }

    @Test
    void propagatesImageReadFailureWithoutReplacingExistingImage() throws IOException {
        Image original = new Image("/profile.png", "profile.png", "/tmp/profile.png");
        member.setImage(original);
        MultipartFile file = mock(MultipartFile.class);
        when(memberRepository.findByIdOrElseThrow(1L)).thenReturn(member);
        when(file.getOriginalFilename()).thenReturn("profile.png");
        when(file.getInputStream()).thenThrow(new IOException("읽기 실패"));
        assertThatThrownBy(() -> memberService.setImage(1L, file))
                .isInstanceOf(IOException.class).hasMessage("읽기 실패");
        assertThat(member.getImage()).isSameAs(original);
    }

    @Test
    void setsTokenExpirationAndSavesMember() {
        LocalDateTime expiration = LocalDateTime.of(2026, 9, 17, 12, 0);
        memberService.setTokenCount(member, expiration);
        assertThat(member.getAccessTokenExpiresIn()).isEqualTo(expiration);
        verify(memberRepository).save(member);
    }

    @Test
    void incrementsProvidedCountAndSavesMember() {
        member.setCount(10);
        memberService.updateCount(member, 2);
        assertThat(member.getCount()).isEqualTo(3);
        verify(memberRepository).save(member);
    }
}
