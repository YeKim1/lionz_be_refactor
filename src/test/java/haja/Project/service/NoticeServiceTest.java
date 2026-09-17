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
class NoticeServiceTest {
    @Mock NoticeRepository repository;
    @InjectMocks NoticeService service;

    @Test
    void savesEntityAndReturnsGeneratedId() {
        Notice entity = new Notice();
        doAnswer(call -> { entity.setId(7L); return null; }).when(repository).save(entity);
        assertThat(service.save(entity)).isEqualTo(7L);
        verify(repository).save(entity);
    }

    @Test
    void propagatesSaveFailure() {
        Notice entity = new Notice();
        RuntimeException failure = new IllegalStateException("save failed");
        doThrow(failure).when(repository).save(entity);
        assertThatThrownBy(() -> service.save(entity)).isSameAs(failure);
    }

    @Test
    void findByIdReturnsRepositoryResult() {
        Notice expected = new Notice();
        when(repository.findById(7L)).thenReturn(expected);
        assertThat(service.findById(7L)).isSameAs(expected);
        verify(repository).findById(7L);
    }

    @Test
    void findByIdHandlesMissingResults() {
        when(repository.findById(7L)).thenReturn(null);
        assertThat(service.findById(7L)).isNull();
    }

    @Test
    void findAllReturnsRepositoryResult() {
        List<Notice> expected = List.of(new Notice(), new Notice());
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
    void findByTargetReturnsRepositoryResult() {
        List<Notice> expected = List.of(new Notice(), new Notice());
        when(repository.findByTarget("BE")).thenReturn(expected);
        assertThat(service.findByTarget("BE")).isSameAs(expected);
        verify(repository).findByTarget("BE");
    }

    @Test
    void findByTargetHandlesMissingResults() {
        when(repository.findByTarget("BE")).thenReturn(List.of());
        assertThat(service.findByTarget("BE")).isEmpty();
    }

    @Test
    void findByWordReturnsRepositoryResult() {
        List<Notice> expected = List.of(new Notice(), new Notice());
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
    void forwardsAllUpdateFields() {
        LocalDateTime deadline = LocalDateTime.of(2026, 10, 1, 12, 0);
        service.update(7L, "title", "body", deadline);
        verify(repository).update(7L, "title", "body", deadline);
    }
}
