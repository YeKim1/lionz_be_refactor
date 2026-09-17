package haja.Project.service;

import haja.Project.domain.Tag;
import haja.Project.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    @DisplayName(value = "태그가 존재하지 않으면 생성해서 반환한다.")
    void 태그찾거나생성_생성() {
        String name = "이름";
        when(repository.findByName(name)).thenReturn(Optional.empty());

        Tag tag = service.findOrCreate(name);

        assertThat(tag.getName()).isEqualTo(name);
        verify(repository).findByName(name);
        verify(repository).save(same(tag));
    }

    @Test
    @DisplayName(value = "태그가 존재하면 찾은 태그를 반환한다.")
    void 태그찾거나생성_기존태그반환() {
        String name = "이름";
        Tag tag = Tag.builder().name(name).build();
        when(repository.findByName(name)).thenReturn(Optional.of(tag));

        Tag result = service.findOrCreate(name);

        assertThat(result).isEqualTo(tag);
        verify(repository, never()).save(any(Tag.class));
    }
}
