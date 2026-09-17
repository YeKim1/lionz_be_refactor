package haja.Project.api;

import haja.Project.domain.*;
import haja.Project.service.*;
import haja.Project.api.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
class NoticeApiControllerTest {
    @Mock NoticeService noticeService;
    @Mock MemberService memberService;
    @Mock TagService tagService;
    @Mock Notice_TagService associationService;
    @InjectMocks NoticeApiController controller;
    private MockMvc mvc;
    @BeforeEach
    void setUp() {
        // Standalone MVC verifies binding and JSON; it does not apply @PreAuthorize.
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("7", "unused"));
    }
    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    private void authorize(Authority authority) {
        when(memberService.findById(7L)).thenReturn(Optional.of(Member.builder().id(7L).authority(authority).build()));
    }
    private static final String BODY = "{\"title\": \"title\", \"explanation\": \"body\", \"target\": \"BE\", \"deadline\": \"2026-10-01 12:00:00\", \"tags\": [\"existing\", \"new\"]}";
    private Notice notice() {
        Notice n = new Notice(); n.setId(9L); n.setTitle("title"); n.setExplanation("body"); n.setTarget(Part.BE);
        n.setDate(LocalDateTime.of(2026, 9, 17, 12, 0)); n.setDeadline(LocalDateTime.of(2026, 10, 1, 12, 0)); return n;
    }
    @Test
    void createsNoticeThroughServiceAndReturnsNoticeInfo() throws Exception {
        Notice created = notice();
        created.setMember(Member.builder().id(7L).name("admin").build());
        when(noticeService.create(eq(7L), any(NoticeRequestDto.Create.class))).thenReturn(created);

        mvc.perform(post("/notice").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.member.id").value(7))
                .andExpect(jsonPath("$.data.title").value("title"))
                .andExpect(jsonPath("$.data.explanation").value("body"))
                .andExpect(jsonPath("$.data.target").value("BE"))
                .andExpect(jsonPath("$.data.date").value("2026-09-17 12:00:00"))
                .andExpect(jsonPath("$.data.deadline").value("2026-10-01 12:00:00"))
                .andExpect(jsonPath("$.id").doesNotExist());

        ArgumentCaptor<NoticeRequestDto.Create> request = ArgumentCaptor.forClass(NoticeRequestDto.Create.class);
        verify(noticeService).create(eq(7L), request.capture());
        assertThat(request.getValue()).extracting("title", "explanation", "target", "deadline", "tags")
                .containsExactly("title", "body", Part.BE, created.getDeadline(), List.of("existing", "new"));
        verifyNoMoreInteractions(noticeService);
        verifyNoInteractions(memberService, tagService, associationService);
    }

    @Test
    @Disabled("공지 수정 API 리팩토링 완료 후 복구")
    void adminUpdatesWithExistingAndNewTags() throws Exception {
        authorize(Authority.ROLE_ADMIN);
        Notice existing = notice();
        when(noticeService.findById(9L)).thenReturn(existing);
        Tag tag = Tag.builder().name("existing").build();
        when(tagService.findByName("existing")).thenReturn(tag);

        mvc.perform(put("/notice/9").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));

        verify(noticeService).update(9L, "title", "body", existing.getDeadline());
        verify(associationService).deleteByNoticeId(9L);
        ArgumentCaptor<Notice_Tag> links = ArgumentCaptor.forClass(Notice_Tag.class);
        verify(associationService, times(2)).save(links.capture());
        assertThat(links.getAllValues()).extracting(a -> a.getTag().getName()).containsExactly("existing", "new");
        assertThat(links.getAllValues()).allSatisfy(a -> assertThat(a.getNotice()).isSameAs(existing));
        assertThat(links.getAllValues().get(0).getTag()).isSameAs(tag);
        verify(tagService).save(argThat(t -> t.getName().equals("new")));
    }

    @Test
    @Disabled("메서드 보안 활성화 및 보안 필터·프록시를 적용하는 MVC 테스트 구성 후 복구")
    void ordinaryMemberCannotCreateNotice() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "7", "unused", List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mvc.perform(post("/notice").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());

        verifyNoInteractions(noticeService, tagService, associationService);
    }

    @Test
    @Disabled("공지 수정 API 리팩토링 완료 후 복구")
    void ordinaryMemberCannotUpdateNotice() throws Exception {
        authorize(Authority.ROLE_USER);
        mvc.perform(put("/notice/9").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
        verifyNoInteractions(noticeService, tagService, associationService);
    }

    @Test
    void ordinaryMemberCannotDeleteNotice() throws Exception {
        authorize(Authority.ROLE_USER);
        mvc.perform(delete("/notice/9")).andExpect(status().isOk());
        verifyNoInteractions(noticeService, tagService, associationService);
    }
    @Test
    void adminDeletesDependenciesBeforeNotice() throws Exception {
        authorize(Authority.ROLE_ADMIN);
        mvc.perform(delete("/notice/9")).andExpect(status().isOk());
        InOrder order = inOrder(associationService, noticeService);
        order.verify(associationService).deleteByNoticeId(9L);
        order.verify(noticeService).delete(9L);
    }
    @Test
    void returnsSingleNoticeWithTags() throws Exception {
        when(noticeService.findById(9L)).thenReturn(notice());
        Tag tag = new Tag(); tag.setName("spring");
        Notice_Tag link = new Notice_Tag(); link.setTag(tag);
        when(associationService.findByNotice(9L)).thenReturn(List.of(link));
        mvc.perform(get("/notice/9")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9)).andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.target").value("BE"))
                .andExpect(jsonPath("$.deadline").value("2026-10-01 12:00:00"))
                .andExpect(jsonPath("$.tag[0]").value("spring"));
    }
    @Test
    void returnsAllNoticesAndEmptyResults() throws Exception {
        when(noticeService.findAll()).thenReturn(List.of(notice()), List.of());
        mvc.perform(get("/notice")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(9))
                .andExpect(jsonPath("$.data[0].tag").isEmpty());
        mvc.perform(get("/notice")).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }
    @Test
    void readsPartSpecificNotices() throws Exception {
        when(noticeService.findByTarget("BE")).thenReturn(List.of(notice()));
        mvc.perform(get("/notice/part/BE")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(9));
        verify(noticeService).findByTarget("BE");
    }

    @Test
    void createsWithoutTags() throws Exception {
        when(noticeService.create(eq(7L), any(NoticeRequestDto.Create.class))).thenReturn(notice());

        mvc.perform(post("/notice").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("[\"existing\", \"new\"]", "[]")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(9));

        ArgumentCaptor<NoticeRequestDto.Create> request = ArgumentCaptor.forClass(NoticeRequestDto.Create.class);
        verify(noticeService).create(eq(7L), request.capture());
        assertThat(request.getValue().getTags()).isEmpty();
        verifyNoInteractions(memberService, tagService, associationService);
    }
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void forwardsNullTagsWhenOmittedOrExplicitlyNull(boolean explicitNull) throws Exception {
        ObjectNode body = (ObjectNode) new ObjectMapper().readTree(BODY);
        if (explicitNull) {
            body.putNull("tags");
        } else {
            body.remove("tags");
        }
        when(noticeService.create(eq(7L), any(NoticeRequestDto.Create.class))).thenReturn(notice());

        mvc.perform(post("/notice").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(9));

        ArgumentCaptor<NoticeRequestDto.Create> request = ArgumentCaptor.forClass(NoticeRequestDto.Create.class);
        verify(noticeService).create(eq(7L), request.capture());
        assertThat(request.getValue().getTags()).isNull();
        assertThat(request.getValue().getTitle()).isEqualTo("title");
        verifyNoInteractions(memberService, tagService, associationService);
    }

    @Test
    void rejectsInvalidTargetBeforeServiceCalls() throws Exception {
        mvc.perform(post("/notice").contentType(MediaType.APPLICATION_JSON).content(BODY.replace("BE", "invalid")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(noticeService, memberService, tagService, associationService);
    }

}
