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
class TaskApiControllerTest {
    @Mock TaskService taskService;
    @Mock TasknoticeService tasknoticeService;
    @Mock MemberRepository memberRepository;
    @InjectMocks TaskApiController controller;
    private MockMvc mvc;
    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("7", "unused"));
    }
    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    private Task task() {
        Task t = new Task(); t.setId(9L); t.setLink("https://example.com"); t.setExplanation("submission");
        t.setDate(LocalDateTime.of(2026, 9, 17, 12, 0)); return t;
    }
    @Test
    void createsSubmissionForCurrentMemberAndNotice() throws Exception {
        Member member = Member.builder().id(7L).build(); Tasknotice notice = new Tasknotice();
        when(memberRepository.findById(7L)).thenReturn(Optional.of(member));
        when(tasknoticeService.findOne(3L)).thenReturn(notice);
        when(taskService.save(any())).thenReturn(9L);
        LocalDateTime before = LocalDateTime.now();
        mvc.perform(post("/task").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://example.com\",\"explanation\":\"submission\",\"tasknotice_id\":3}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));
        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class); verify(taskService).save(saved.capture());
        assertThat(saved.getValue()).extracting("member", "tasknotice", "link", "explanation")
                .containsExactly(member, notice, "https://example.com", "submission");
        assertThat(saved.getValue().getDate()).isBetween(before, LocalDateTime.now());
    }
    @Test
    void readsSingleTask() throws Exception {
        when(taskService.findOne(9L)).thenReturn(task());
        mvc.perform(get("/task/9")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));
    }
    @Test
    void readsAllOwnAndNoticeSubmissions() throws Exception {
        when(taskService.findAll()).thenReturn(List.of(task()));
        when(taskService.findByMember(7L)).thenReturn(List.of(task()));
        when(taskService.findByTasknotice(3L)).thenReturn(List.of(task()));
        for (String path : List.of("/task", "/task/me", "/task/tasknotice/3")) {
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(9))
                    .andExpect(jsonPath("$.data[0].link").value("https://example.com"))
                    .andExpect(jsonPath("$.data[0].explanation").value("submission"))
                    .andExpect(jsonPath("$.data[0].date").value("2026-09-17 12:00:00"));
        }
        verify(taskService).findByMember(7L);
    }
    @Test
    void updatesOnlySubmissionContent() throws Exception {
        Task existing = task(); Member member = new Member(); Tasknotice notice = new Tasknotice();
        existing.setMember(member); existing.setTasknotice(notice);
        when(taskService.findOne(9L)).thenReturn(existing); when(taskService.save(existing)).thenReturn(9L);
        mvc.perform(put("/task/9").contentType(MediaType.APPLICATION_JSON).content("{\"link\":\"new-link\",\"explanation\":\"new-body\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));
        assertThat(existing).extracting("link", "explanation", "member", "tasknotice", "date")
                .containsExactly("new-link", "new-body", member, notice, task().getDate());
        verify(taskService).save(existing);
    }
    @Test
    void deletesTask() throws Exception {
        mvc.perform(delete("/task/9")).andExpect(status().isOk()); verify(taskService).delete(9L);
    }
    @Test
    void returnsEmptyList() throws Exception {
        mvc.perform(get("/task")).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }
    @Test
    void rejectsInvalidId() throws Exception {
        mvc.perform(get("/task/not-a-number")).andExpect(status().isBadRequest()); verifyNoInteractions(taskService);
    }
    @Test
    void rejectsUnauthenticatedCreationBeforeSaving() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> controller.createTaskResponse(new TaskApiController.CreateTaskRequest()))
                .hasMessage("Security Context 에 인증 정보가 없습니다.");
        verifyNoInteractions(taskService, memberRepository, tasknoticeService);
    }

}
