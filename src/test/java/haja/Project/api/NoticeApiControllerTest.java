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
class NoticeApiControllerTest {
    @Mock NoticeService noticeService;
    @Mock MemberService memberService;
    @Mock TagService tagService;
    @Mock Notice_TagService associationService;
    @InjectMocks NoticeApiController controller;
    private MockMvc mvc;
    @BeforeEach
    void setUp() {
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
    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void adminCreatesOrUpdatesWithExistingAndNewTags(String method) throws Exception {
        authorize(Authority.ROLE_ADMIN);
        Notice existing = notice();
        when(noticeService.findById(9L)).thenReturn(existing);
        Tag tag = new Tag(); tag.setName("existing");
        when(tagService.findByName("existing")).thenReturn(tag);
        if (method.equals("POST")) when(noticeService.save(any())).thenReturn(9L);
        LocalDateTime before = LocalDateTime.now();
        mvc.perform((method.equals("POST") ? post("/notice") : put("/notice/9"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));
        ArgumentCaptor<Notice_Tag> links = ArgumentCaptor.forClass(Notice_Tag.class);
        verify(associationService, times(2)).save(links.capture());
        assertThat(links.getAllValues()).extracting(a -> a.getTag().getName()).containsExactly("existing", "new");
        assertThat(links.getAllValues()).allSatisfy(a -> assertThat(a.getNotice()).isSameAs(existing));
        assertThat(links.getAllValues().get(0).getTag()).isSameAs(tag);
        verify(tagService).save(argThat(t -> t.getName().equals("new")));
        if (method.equals("PUT")) {
            verify(noticeService).update(9L, "title", "body", existing.getDeadline());
            verify(associationService).deleteByNoticeId(9L);
        } else {
            ArgumentCaptor<Notice> saved = ArgumentCaptor.forClass(Notice.class);
            verify(noticeService).save(saved.capture());
            assertThat(saved.getValue()).extracting("title", "explanation", "target", "deadline")
                    .containsExactly("title", "body", Part.BE, existing.getDeadline());
            assertThat(saved.getValue().getDate()).isBetween(before, LocalDateTime.now());
            assertThat(saved.getValue().getMember().getId()).isEqualTo(7L);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    void ordinaryMemberCannotMutateNotices(String method) throws Exception {
        authorize(Authority.ROLE_USER);
        var request = method.equals("POST") ? post("/notice") : method.equals("PUT") ? put("/notice/9") : delete("/notice/9");
        mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isOk());
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
        authorize(Authority.ROLE_ADMIN);
        when(noticeService.save(any())).thenReturn(9L);
        mvc.perform(post("/notice").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));
        verifyNoInteractions(tagService, associationService);
    }
    @Test
    void rejectsInvalidTargetBeforeServiceCalls() throws Exception {
        mvc.perform(post("/notice").contentType(MediaType.APPLICATION_JSON).content(BODY.replace("BE", "invalid")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(noticeService, memberService, tagService, associationService);
    }

}
