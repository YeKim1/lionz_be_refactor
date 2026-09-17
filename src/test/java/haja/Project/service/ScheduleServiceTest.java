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
class ScheduleServiceTest {
    @Mock ScheduleRepository repository;
    @InjectMocks ScheduleService service;

    @Test
    void savesEntityAndReturnsGeneratedId() {
        Schedule entity = new Schedule();
        doAnswer(call -> { entity.setId(7L); return null; }).when(repository).save(entity);
        assertThat(service.save(entity)).isEqualTo(7L);
        verify(repository).save(entity);
    }

    @Test
    void propagatesSaveFailure() {
        Schedule entity = new Schedule();
        RuntimeException failure = new IllegalStateException("save failed");
        doThrow(failure).when(repository).save(entity);
        assertThatThrownBy(() -> service.save(entity)).isSameAs(failure);
    }

    @Test
    void findOneReturnsRepositoryResult() {
        Schedule expected = new Schedule();
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
        List<Schedule> expected = List.of(new Schedule(), new Schedule());
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
    void deletesById() { service.delete(7L); verify(repository).delete(7L); }
}
