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
class Tasknotice_TagServiceTest {
    @Mock Tasknotice_TagRepository repository;
    @InjectMocks Tasknotice_TagService service;

    @Test
    void savesEntityAndReturnsGeneratedId() {
        Tasknotice_Tag entity = new Tasknotice_Tag();
        doAnswer(call -> { entity.setId(7L); return null; }).when(repository).save(entity);
        assertThat(service.save(entity)).isEqualTo(7L);
        verify(repository).save(entity);
    }

    @Test
    void propagatesSaveFailure() {
        Tasknotice_Tag entity = new Tasknotice_Tag();
        RuntimeException failure = new IllegalStateException("save failed");
        doThrow(failure).when(repository).save(entity);
        assertThatThrownBy(() -> service.save(entity)).isSameAs(failure);
    }

    @Test
    void findOneReturnsRepositoryResult() {
        Tasknotice_Tag expected = new Tasknotice_Tag();
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
    void findByTasknoticeIdReturnsRepositoryResult() {
        List<Tasknotice_Tag> expected = List.of(new Tasknotice_Tag(), new Tasknotice_Tag());
        when(repository.findByTasknoticeId(7L)).thenReturn(expected);
        assertThat(service.findByTasknoticeId(7L)).isSameAs(expected);
        verify(repository).findByTasknoticeId(7L);
    }

    @Test
    void findByTasknoticeIdHandlesMissingResults() {
        when(repository.findByTasknoticeId(7L)).thenReturn(List.of());
        assertThat(service.findByTasknoticeId(7L)).isEmpty();
    }

    @Test
    void findByTagNameReturnsRepositoryResult() {
        List<Tasknotice_Tag> expected = List.of(new Tasknotice_Tag(), new Tasknotice_Tag());
        when(repository.findByTagName("spring")).thenReturn(expected);
        assertThat(service.findByTagName("spring")).isSameAs(expected);
        verify(repository).findByTagName("spring");
    }

    @Test
    void findByTagNameHandlesMissingResults() {
        when(repository.findByTagName("spring")).thenReturn(List.of());
        assertThat(service.findByTagName("spring")).isEmpty();
    }

    @Test
    void deletesAssociation() {
        Tasknotice_Tag tag = new Tasknotice_Tag();
        service.delete(tag); verify(repository).delete(tag);
    }
    @Test
    void deletesAssociationsByNotice() {
        service.deleteByTasknoticeId(7L); verify(repository).deleteByTasknoticeId(7L);
    }
}
