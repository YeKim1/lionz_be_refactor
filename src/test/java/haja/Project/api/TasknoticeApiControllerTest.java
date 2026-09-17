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
class TasknoticeApiControllerTest {
    @Mock TasknoticeService tasknoticeService;
    @Mock MemberService memberService;
    @Mock TagService tagService;
    @Mock Tasknotice_TagService associationService;
    @Mock TaskService taskService;
    @InjectMocks TasknoticeApiController controller;
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
    private static final String BODY = "{\"title\": \"title\", \"explanation\": \"body\", \"target\": \"BE\", \"deadline\": \"2026-10-01 12:00:00\", \"tags\": [\"existing\", \"new\"], \"link\": \"https://example.com\"}";
    private Tasknotice notice() {
        Tasknotice n = new Tasknotice(); n.setId(9L); n.setTitle("title"); n.setExplanation("body"); n.setTarget(Part.BE);
        n.setDate(LocalDateTime.of(2026, 9, 17, 12, 0)); n.setDeadline(LocalDateTime.of(2026, 10, 1, 12, 0)); return n;
    }
    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void adminCreatesOrUpdatesWithExistingAndNewTags(String method) throws Exception {
        authorize(Authority.ROLE_ADMIN);
        Tasknotice existing = notice();
        when(tasknoticeService.findOne(9L)).thenReturn(existing);
        Tag tag = new Tag(); tag.setName("existing");
        when(tagService.findByName("existing")).thenReturn(tag);
        when(tasknoticeService.save(any())).thenReturn(9L);
        Tasknotice_Tag old = new Tasknotice_Tag(); old.setId(4L);
        if (method.equals("PUT")) {
            when(associationService.findByTasknoticeId(9L)).thenReturn(List.of(old));
            when(associationService.findOne(4L)).thenReturn(old);
        }
        LocalDateTime before = LocalDateTime.now();
        mvc.perform((method.equals("POST") ? post("/tasknotice") : put("/tasknotice/9"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));
        ArgumentCaptor<Tasknotice_Tag> links = ArgumentCaptor.forClass(Tasknotice_Tag.class);
        verify(associationService, times(2)).save(links.capture());
        assertThat(links.getAllValues()).extracting(a -> a.getTag().getName()).containsExactly("existing", "new");
        assertThat(links.getAllValues()).allSatisfy(a -> assertThat(a.getTasknotice()).isSameAs(existing));
        assertThat(links.getAllValues().get(0).getTag()).isSameAs(tag);
        verify(tagService).save(argThat(t -> t.getName().equals("new")));
        ArgumentCaptor<Tasknotice> saved = ArgumentCaptor.forClass(Tasknotice.class);
        verify(tasknoticeService).save(saved.capture());
        assertThat(saved.getValue()).extracting("title", "explanation", "target", "deadline", "link")
                .containsExactly("title", "body", Part.BE, existing.getDeadline(), "https://example.com");
        if (method.equals("PUT")) verify(associationService).delete(old);
        else {
            assertThat(saved.getValue().getDate()).isBetween(before, LocalDateTime.now());
            assertThat(saved.getValue().getMember().getId()).isEqualTo(7L);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    void ordinaryMemberCannotMutateNotices(String method) throws Exception {
        authorize(Authority.ROLE_USER);
        var request = method.equals("POST") ? post("/tasknotice") : method.equals("PUT") ? put("/tasknotice/9") : delete("/tasknotice/9");
        mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isOk());
        verifyNoInteractions(tasknoticeService, tagService, associationService, taskService);
    }
    @Test
    void adminDeletesDependenciesBeforeNotice() throws Exception {
        authorize(Authority.ROLE_ADMIN);
        mvc.perform(delete("/tasknotice/9")).andExpect(status().isOk());
        InOrder order = inOrder(associationService, tasknoticeService, taskService);
        order.verify(associationService).deleteByTasknoticeId(9L);
        order.verify(taskService).deleteByTasknotice(9L);
        order.verify(tasknoticeService).delete(9L);
    }
    @Test
    void returnsSingleNoticeWithTags() throws Exception {
        when(tasknoticeService.findOne(9L)).thenReturn(notice());
        Tag tag = new Tag(); tag.setName("spring");
        Tasknotice_Tag link = new Tasknotice_Tag(); link.setTag(tag);
        when(associationService.findByTasknoticeId(9L)).thenReturn(List.of(link));
        when(taskService.isSubmit(9L)).thenReturn(true);
        mvc.perform(get("/tasknotice/9")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9)).andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.target").value("BE"))
                .andExpect(jsonPath("$.deadline").value("2026-10-01 12:00:00"))
                .andExpect(jsonPath("$.tag[0]").value("spring")).andExpect(jsonPath("$.isSubmit").value(true));
    }
    @Test
    void returnsAllNoticesAndEmptyResults() throws Exception {
        authorize(Authority.ROLE_USER);
        when(tasknoticeService.findAll()).thenReturn(List.of(notice()), List.of());
        mvc.perform(get("/tasknotice")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(9))
                .andExpect(jsonPath("$.data[0].tag").isEmpty()).andExpect(jsonPath("$.data[0].isSubmit").value(false));
        mvc.perform(get("/tasknotice")).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }
    @Test
    void readsPartSpecificNotices() throws Exception {
        when(tasknoticeService.findFe()).thenReturn(List.of(notice()));
        mvc.perform(get("/tasknotice/FE")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(9));
        verify(tasknoticeService).findFe();
        when(tasknoticeService.findBe()).thenReturn(List.of(notice()));
        mvc.perform(get("/tasknotice/BE")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(9));
        verify(tasknoticeService).findBe();
        when(tasknoticeService.findPartAll()).thenReturn(List.of(notice()));
        mvc.perform(get("/tasknotice/ALL")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(9));
        verify(tasknoticeService).findPartAll();
    }

    @Test
    void createsWithoutTags() throws Exception {
        authorize(Authority.ROLE_ADMIN);
        when(tasknoticeService.save(any())).thenReturn(9L);
        mvc.perform(post("/tasknotice").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));
        verifyNoInteractions(tagService, associationService);
    }
    @Test
    void rejectsInvalidTargetBeforeServiceCalls() throws Exception {
        mvc.perform(post("/tasknotice").contentType(MediaType.APPLICATION_JSON).content(BODY.replace("BE", "invalid")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(tasknoticeService, memberService, tagService, associationService);
    }

}
