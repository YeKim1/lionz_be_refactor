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
class Notice_TagServiceTest {
    @Mock Notice_TagRepository repository;
    @InjectMocks Notice_TagService service;

    @Test
    void savesEntityAndReturnsGeneratedId() {
        Notice_Tag entity = new Notice_Tag();
        doAnswer(call -> { entity.setId(7L); return null; }).when(repository).save(entity);
        assertThat(service.save(entity)).isEqualTo(7L);
        verify(repository).save(entity);
    }

    @Test
    void propagatesSaveFailure() {
        Notice_Tag entity = new Notice_Tag();
        RuntimeException failure = new IllegalStateException("save failed");
        doThrow(failure).when(repository).save(entity);
        assertThatThrownBy(() -> service.save(entity)).isSameAs(failure);
    }

    @Test
    void findByNoticeReturnsRepositoryResult() {
        List<Notice_Tag> expected = List.of(new Notice_Tag(), new Notice_Tag());
        when(repository.findByNotice(7L)).thenReturn(expected);
        assertThat(service.findByNotice(7L)).isSameAs(expected);
        verify(repository).findByNotice(7L);
    }

    @Test
    void findByNoticeHandlesMissingResults() {
        when(repository.findByNotice(7L)).thenReturn(List.of());
        assertThat(service.findByNotice(7L)).isEmpty();
    }

    @Test
    void returnsDeletedRowCountIncludingZero() {
        when(repository.deleteByNoticeId(7L)).thenReturn(3, 0);
        assertThat(service.deleteByNoticeId(7L)).isEqualTo(3);
        assertThat(service.deleteByNoticeId(7L)).isZero();
    }
}
