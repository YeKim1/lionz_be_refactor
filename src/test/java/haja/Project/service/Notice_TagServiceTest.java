package haja.Project.service;

import haja.Project.domain.Notice;
import haja.Project.domain.Notice_Tag;
import haja.Project.domain.Tag;
import haja.Project.repository.Notice_TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Notice_TagServiceTest {
    @Mock Notice_TagRepository repository;
    @Mock TagService tagService;
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

    @Test
    @DisplayName("이름 리스트가 비어있으면 바로 반환한다.")
    void 공지사항태그생성_빈리스트() {
        Notice notice = new Notice();
        List<String> names =  new ArrayList<>();

        service.attachTags(notice, names);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("이름 리스트를 받아서 태그를 저장한다.")
    void 공지사항태그생성_생성완료() {
        Notice notice = new Notice();
        List<String> names =  List.of("1", "2");
        Tag firstTag = Tag.builder().name("1").build();
        Tag secondTag = Tag.builder().name("2").build();
        when(tagService.findOrCreate("1")).thenReturn(firstTag);
        when(tagService.findOrCreate("2")).thenReturn(secondTag);

        service.attachTags(notice, names);

        verify(tagService).findOrCreate("1");
        verify(tagService).findOrCreate("2");

        ArgumentCaptor<Notice_Tag> captor = ArgumentCaptor.forClass(Notice_Tag.class);
        verify(repository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Notice_Tag::getTag)
                .containsExactly(firstTag, secondTag);
    }
}
