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
class TagServiceTest {
    @Mock TagRepository repository;
    @InjectMocks TagService service;

    @Test
    void savesEntityAndReturnsGeneratedId() {
        Tag entity = new Tag();
        doAnswer(call -> { entity.setId(7L); return null; }).when(repository).save(entity);
        assertThat(service.save(entity)).isEqualTo(7L);
        verify(repository).save(entity);
    }

    @Test
    void propagatesSaveFailure() {
        Tag entity = new Tag();
        RuntimeException failure = new IllegalStateException("save failed");
        doThrow(failure).when(repository).save(entity);
        assertThatThrownBy(() -> service.save(entity)).isSameAs(failure);
    }

    @Test
    void findOneReturnsRepositoryResult() {
        Tag expected = new Tag();
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
    void findByWordReturnsRepositoryResult() {
        List<Tag> expected = List.of(new Tag(), new Tag());
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
    void findsTagByExactNameOrReturnsNull() {
        Tag tag = new Tag(); tag.setName("spring");
        when(repository.findByName("spring")).thenReturn(Optional.of(tag), Optional.empty());
        assertThat(service.findByName("spring")).isSameAs(tag);
        assertThat(service.findByName("spring")).isNull();
    }
}
