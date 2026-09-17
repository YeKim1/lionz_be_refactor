package haja.Project.service;

import haja.Project.api.dto.AllSearchDto;
import haja.Project.domain.Notice;
import haja.Project.domain.Part;
import haja.Project.domain.Task;
import haja.Project.domain.Tasknotice;
import haja.Project.domain.Tasknotice_Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllSearchServiceTest {
    @Mock NoticeService noticeService;
    @Mock TasknoticeService tasknoticeService;
    @Mock TaskService taskService;
    @Mock Tasknotice_TagService tasknoticeTagService;
    @InjectMocks AllSearchService allSearchService;

    @Test
    void combinesAllSearchCategoriesAndMapsTheirFields() {
        String word = "스프링";
        LocalDateTime date = LocalDateTime.of(2026, 9, 17, 12, 0);
        LocalDateTime deadline = date.plusDays(7);
        Notice notice = new Notice();
        notice.setId(11L);
        notice.setTitle("스프링 공지");
        notice.setExplanation("공지 내용");
        notice.setDate(date);
        notice.setDeadline(deadline);
        notice.setTarget(Part.ALL);
        Tasknotice tasknotice = new Tasknotice();
        tasknotice.setId(22L);
        tasknotice.setTitle("스프링 과제 공지");
        tasknotice.setExplanation("과제 공지 내용");
        tasknotice.setDate(date);
        tasknotice.setDeadline(deadline);
        tasknotice.setTarget(Part.BE);
        Task task = new Task();
        task.setId(33L);
        task.setLink("https://example.com/tasks/33");
        task.setExplanation("스프링 과제 제출");
        Tasknotice_Tag tag = new Tasknotice_Tag();
        tag.setId(44L);
        when(noticeService.findByWord(word)).thenReturn(List.of(notice));
        when(tasknoticeService.findByWord(word)).thenReturn(List.of(tasknotice));
        when(taskService.findByWord(word)).thenReturn(List.of(task));
        when(tasknoticeTagService.findByTagName(word)).thenReturn(List.of(tag));

        AllSearchDto.Response result = allSearchService.search(word);

        assertThat(result.getNotice()).singleElement().satisfies(dto -> {
            assertThat(dto).extracting("id", "title", "explanation", "date", "deadline", "target")
                    .containsExactly(11L, "스프링 공지", "공지 내용", date, deadline, Part.ALL);
            assertThat(dto.getTag()).isEmpty();
        });
        assertThat(result.getTasknotice()).singleElement()
                .extracting("id", "title", "explanation", "date", "deadline", "target")
                .containsExactly(22L, "스프링 과제 공지", "과제 공지 내용", date, deadline, Part.BE);
        assertThat(result.getTask()).singleElement().extracting("id", "link", "explanation")
                .containsExactly(33L, "https://example.com/tasks/33", "스프링 과제 제출");
        assertThat(result.getTasknotice_tag()).extracting(AllSearchDto.Tasknotice_TagDto::getId)
                .containsExactly(44L);
        verifySearches(word);
    }

    @ParameterizedTest
    @ValueSource(strings = {"검색 결과 없음", "", "  스프링  ", "C++ & JPA"})
    void passesKeywordUnchangedToEveryServiceAndReturnsEmptyLists(String word) {
        when(noticeService.findByWord(word)).thenReturn(List.of());
        when(tasknoticeService.findByWord(word)).thenReturn(List.of());
        when(taskService.findByWord(word)).thenReturn(List.of());
        when(tasknoticeTagService.findByTagName(word)).thenReturn(List.of());

        AllSearchDto.Response result = allSearchService.search(word);

        assertThat(result.getNotice()).isEmpty();
        assertThat(result.getTasknotice()).isEmpty();
        assertThat(result.getTask()).isEmpty();
        assertThat(result.getTasknotice_tag()).isEmpty();
        verifySearches(word);
    }

    @Test
    void preservesResultOrderWhenOnlyOneCategoryHasMatches() {
        Task first = new Task();
        first.setId(30L);
        Task second = new Task();
        second.setId(10L);
        when(noticeService.findByWord("과제")).thenReturn(List.of());
        when(tasknoticeService.findByWord("과제")).thenReturn(List.of());
        when(taskService.findByWord("과제")).thenReturn(List.of(first, second));
        when(tasknoticeTagService.findByTagName("과제")).thenReturn(List.of());

        AllSearchDto.Response result = allSearchService.search("과제");

        assertThat(result.getTask()).extracting(AllSearchDto.TaskDto::getId).containsExactly(30L, 10L);
        assertThat(result.getNotice()).isEmpty();
        assertThat(result.getTasknotice()).isEmpty();
        assertThat(result.getTasknotice_tag()).isEmpty();
        verifySearches("과제");
    }

    @Test
    void propagatesSearchFailureInsteadOfReturningPartialResults() {
        RuntimeException failure = new IllegalStateException("과제 검색 실패");
        when(noticeService.findByWord("스프링")).thenReturn(List.of(new Notice()));
        when(tasknoticeService.findByWord("스프링")).thenReturn(List.of(new Tasknotice()));
        when(taskService.findByWord("스프링")).thenThrow(failure);

        assertThatThrownBy(() -> allSearchService.search("스프링")).isSameAs(failure);
    }

    private void verifySearches(String word) {
        verify(noticeService).findByWord(word);
        verify(tasknoticeService).findByWord(word);
        verify(taskService).findByWord(word);
        verify(tasknoticeTagService).findByTagName(word);
        verifyNoMoreInteractions(noticeService, tasknoticeService, taskService, tasknoticeTagService);
    }
}
