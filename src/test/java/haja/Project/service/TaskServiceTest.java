package haja.Project.service;

import haja.Project.domain.*;
import haja.Project.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock TaskRepository repository;
    @InjectMocks TaskService service;

    @Test
    void savesEntityAndReturnsGeneratedId() {
        Task entity = new Task();
        doAnswer(call -> { entity.setId(7L); return null; }).when(repository).save(entity);
        assertThat(service.save(entity)).isEqualTo(7L);
        verify(repository).save(entity);
    }

    @Test
    void propagatesSaveFailure() {
        Task entity = new Task();
        RuntimeException failure = new IllegalStateException("save failed");
        doThrow(failure).when(repository).save(entity);
        assertThatThrownBy(() -> service.save(entity)).isSameAs(failure);
    }

    @Test
    void findOneReturnsRepositoryResult() {
        Task expected = new Task();
        when(repository.findOne(7L)).thenReturn(expected);
        assertThat(service.findOne(7L)).isSameAs(expected);
        verify(repository).findOne(7L);
    }

    @Test
    void findOneHandlesMissingResults() {
        when(repository.findOne(7L)).thenReturn(null);
        assertThat(service.findOne(7L)).isNull();
    }

    @Test
    void findAllReturnsRepositoryResult() {
        List<Task> expected = List.of(new Task(), new Task());
        when(repository.findAll()).thenReturn(expected);
        assertThat(service.findAll()).isSameAs(expected);
        verify(repository).findAll();
    }

    @Test
    void findAllHandlesMissingResults() {
        when(repository.findAll()).thenReturn(List.of());
        assertThat(service.findAll()).isEmpty();
    }

    @Test
    void findByMemberReturnsRepositoryResult() {
        List<Task> expected = List.of(new Task(), new Task());
        when(repository.findByMember(7L)).thenReturn(expected);
        assertThat(service.findByMember(7L)).isSameAs(expected);
        verify(repository).findByMember(7L);
    }

    @Test
    void findByMemberHandlesMissingResults() {
        when(repository.findByMember(7L)).thenReturn(List.of());
        assertThat(service.findByMember(7L)).isEmpty();
    }

    @Test
    void findByTasknoticeReturnsRepositoryResult() {
        List<Task> expected = List.of(new Task(), new Task());
        when(repository.findByTasknotice(7L)).thenReturn(expected);
        assertThat(service.findByTasknotice(7L)).isSameAs(expected);
        verify(repository).findByTasknotice(7L);
    }

    @Test
    void findByTasknoticeHandlesMissingResults() {
        when(repository.findByTasknotice(7L)).thenReturn(List.of());
        assertThat(service.findByTasknotice(7L)).isEmpty();
    }

    @Test
    void findByWordReturnsRepositoryResult() {
        List<Task> expected = List.of(new Task(), new Task());
        when(repository.findByWord("spring")).thenReturn(expected);
        assertThat(service.findByWord("spring")).isSameAs(expected);
        verify(repository).findByWord("spring");
    }

    @Test
    void findByWordHandlesMissingResults() {
        when(repository.findByWord("spring")).thenReturn(List.of());
        assertThat(service.findByWord("spring")).isEmpty();
    }

    @Test
    void deletesById() { service.delete(7L); verify(repository).delete(7L); }

    @Test
    void distinguishesSubmittedAndMissingTasks() {
        when(repository.findSubmit(7L)).thenReturn(Optional.of(new Task()), Optional.empty());
        assertThat(service.isSubmit(7L)).isTrue();
        assertThat(service.isSubmit(7L)).isFalse();
    }
    @Test
    void deletesTasksByNotice() {
        service.deleteByTasknotice(7L); verify(repository).deleteByTasknotice(7L);
    }
}
