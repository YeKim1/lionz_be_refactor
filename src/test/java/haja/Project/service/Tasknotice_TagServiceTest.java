package haja.Project.service;

import haja.Project.domain.Tag;
import haja.Project.domain.Tasknotice;
import haja.Project.domain.Tasknotice_Tag;
import haja.Project.repository.Tasknotice_TagRepository;
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
class Tasknotice_TagServiceTest {
    @Mock Tasknotice_TagRepository repository;
    @Mock TagService tagService;
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

    @Test
    @DisplayName("이름 리스트가 비어있으면 바로 반환한다.")
    void 공지사항태그생성_빈리스트() {
        Tasknotice tasknotice = new Tasknotice();
        List<String> names =  new ArrayList<>();

        service.attachTags(tasknotice, names);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("이름 리스트를 받아서 태그를 저장한다.")
    void 공지사항태그생성_생성완료() {
        Tasknotice tasknotice = new Tasknotice();
        List<String> names =  List.of("1", "2");
        Tag firstTag = Tag.builder().name("1").build();
        Tag secondTag = Tag.builder().name("2").build();
        when(tagService.findOrCreate("1")).thenReturn(firstTag);
        when(tagService.findOrCreate("2")).thenReturn(secondTag);

        service.attachTags(tasknotice, names);

        verify(tagService).findOrCreate("1");
        verify(tagService).findOrCreate("2");

        ArgumentCaptor<Tasknotice_Tag> captor = ArgumentCaptor.forClass(Tasknotice_Tag.class);
        verify(repository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Tasknotice_Tag::getTag)
                .containsExactly(firstTag, secondTag);
    }
}
