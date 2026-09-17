package haja.Project.api;

import haja.Project.api.dto.AllSearchDto;
import haja.Project.domain.Notice;
import haja.Project.domain.Part;
import haja.Project.domain.Task;
import haja.Project.domain.Tasknotice;
import haja.Project.domain.Tasknotice_Tag;
import haja.Project.service.AllSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Standalone MVC tests exercise request binding and JSON without a database or security filters.
@ExtendWith(MockitoExtension.class)
class AllSearchApiControllerTest {
    @Mock AllSearchService allSearchService;
    @InjectMocks AllSearchApiController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnsSearchResultsInEachJsonCategory() throws Exception {
        Notice notice = new Notice();
        notice.setId(11L);
        notice.setTitle("스프링 공지");
        notice.setExplanation("공지 내용");
        notice.setTarget(Part.ALL);
        notice.setDate(LocalDateTime.of(2026, 9, 17, 12, 0));
        notice.setDeadline(LocalDateTime.of(2026, 9, 24, 12, 0));
        Tasknotice tasknotice = new Tasknotice();
        tasknotice.setId(22L);
        tasknotice.setTitle("스프링 과제 공지");
        Task task = new Task();
        task.setId(33L);
        task.setLink("https://example.com/tasks/33");
        task.setExplanation("제출 내용");
        Tasknotice_Tag tag = new Tasknotice_Tag();
        tag.setId(44L);
        when(allSearchService.search("스프링")).thenReturn(AllSearchDto.Response.of(
                List.of(notice), List.of(tasknotice), List.of(task), List.of(tag)));

        mockMvc.perform(get("/all").param("word", "스프링"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.notice.length()").value(1))
                .andExpect(jsonPath("$.notice[0].id").value(11))
                .andExpect(jsonPath("$.notice[0].title").value("스프링 공지"))
                .andExpect(jsonPath("$.notice[0].explanation").value("공지 내용"))
                .andExpect(jsonPath("$.notice[0].target").value("ALL"))
                .andExpect(jsonPath("$.notice[0].date").value("2026-09-17 12:00:00"))
                .andExpect(jsonPath("$.notice[0].deadline").value("2026-09-24 12:00:00"))
                .andExpect(jsonPath("$.notice[0].tag").isEmpty())
                .andExpect(jsonPath("$.tasknotice.length()").value(1))
                .andExpect(jsonPath("$.tasknotice[0].id").value(22))
                .andExpect(jsonPath("$.tasknotice[0].title").value("스프링 과제 공지"))
                .andExpect(jsonPath("$.task.length()").value(1))
                .andExpect(jsonPath("$.task[0].id").value(33))
                .andExpect(jsonPath("$.task[0].link").value("https://example.com/tasks/33"))
                .andExpect(jsonPath("$.task[0].explanation").value("제출 내용"))
                .andExpect(jsonPath("$.tasknotice_tag.length()").value(1))
                .andExpect(jsonPath("$.tasknotice_tag[0].id").value(44));
        verify(allSearchService).search("스프링");
    }

    @ParameterizedTest
    @ValueSource(strings = {"검색 결과 없음", "", "  스프링  ", "C++ & JPA"})
    void forwardsKeywordUnchangedAndReturnsEmptyArrays(String word) throws Exception {
        when(allSearchService.search(word)).thenReturn(
                AllSearchDto.Response.of(List.of(), List.of(), List.of(), List.of()));

        mockMvc.perform(get("/all").param("word", word))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"notice\":[],\"tasknotice\":[],\"task\":[],\"tasknotice_tag\":[]}", true));
        verify(allSearchService).search(word);
    }

    @Test
    void rejectsRequestWithoutRequiredKeyword() throws Exception {
        mockMvc.perform(get("/all")).andExpect(status().isBadRequest());
        verifyNoInteractions(allSearchService);
    }

    @Test
    void propagatesServiceFailure() {
        RuntimeException failure = new IllegalStateException("검색 실패");
        when(allSearchService.search("스프링")).thenThrow(failure);
        assertThatThrownBy(() -> controller.AllSearch("스프링")).isSameAs(failure);
    }
}
