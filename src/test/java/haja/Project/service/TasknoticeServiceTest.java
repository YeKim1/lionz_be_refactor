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
class TasknoticeServiceTest {
    @Mock TasknoticeRepository repository;
    @InjectMocks TasknoticeService service;

    @Test
    void savesEntityAndReturnsGeneratedId() {
        Tasknotice entity = new Tasknotice();
        doAnswer(call -> { entity.setId(7L); return null; }).when(repository).save(entity);
        assertThat(service.save(entity)).isEqualTo(7L);
        verify(repository).save(entity);
    }

    @Test
    void propagatesSaveFailure() {
        Tasknotice entity = new Tasknotice();
        RuntimeException failure = new IllegalStateException("save failed");
        doThrow(failure).when(repository).save(entity);
        assertThatThrownBy(() -> service.save(entity)).isSameAs(failure);
    }

    @Test
    void findOneReturnsRepositoryResult() {
        Tasknotice expected = new Tasknotice();
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
        List<Tasknotice> expected = List.of(new Tasknotice(), new Tasknotice());
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
    void findPartAllReturnsRepositoryResult() {
        List<Tasknotice> expected = List.of(new Tasknotice(), new Tasknotice());
        when(repository.findPartAll()).thenReturn(expected);
        assertThat(service.findPartAll()).isSameAs(expected);
        verify(repository).findPartAll();
    }

    @Test
    void findPartAllHandlesMissingResults() {
        when(repository.findPartAll()).thenReturn(List.of());
        assertThat(service.findPartAll()).isEmpty();
    }

    @Test
    void findFeReturnsRepositoryResult() {
        List<Tasknotice> expected = List.of(new Tasknotice(), new Tasknotice());
        when(repository.findFe()).thenReturn(expected);
        assertThat(service.findFe()).isSameAs(expected);
        verify(repository).findFe();
    }

    @Test
    void findFeHandlesMissingResults() {
        when(repository.findFe()).thenReturn(List.of());
        assertThat(service.findFe()).isEmpty();
    }

    @Test
    void findBeReturnsRepositoryResult() {
        List<Tasknotice> expected = List.of(new Tasknotice(), new Tasknotice());
        when(repository.findBe()).thenReturn(expected);
        assertThat(service.findBe()).isSameAs(expected);
        verify(repository).findBe();
    }

    @Test
    void findBeHandlesMissingResults() {
        when(repository.findBe()).thenReturn(List.of());
        assertThat(service.findBe()).isEmpty();
    }

    @Test
    void findByWordReturnsRepositoryResult() {
        List<Tasknotice> expected = List.of(new Tasknotice(), new Tasknotice());
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
    void deletesById() { service.delete(7L); verify(repository).deleteTasknotice(7L); }

    @Test
    void updatesExistingNotice() {
        Tasknotice notice = new Tasknotice(); notice.setId(7L);
        assertThat(service.update(notice)).isEqualTo(7L);
        verify(repository).save(notice);
    }
}
