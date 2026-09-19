package haja.Project.service;

import haja.Project.api.dto.TasknoticeRequestDto;
import haja.Project.domain.*;
import haja.Project.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.springframework.security.access.AccessDeniedException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasknoticeServiceTest {
    @Mock TasknoticeRepository repository;
    @Mock MemberService memberService;
    @Mock Tasknotice_TagService associationService;
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
    private TasknoticeRequestDto.Create createRequest(List<String> tags) {
        return new TasknoticeRequestDto.Create("title", "body", Part.BE,
                LocalDateTime.of(2026, 10, 1, 12, 0), tags, "https://example.com");
    }

    @Test
    void createsTasknoticeThenAttachesTagsAndReturnsSavedEntity() {
        Member member = Member.builder().id(7L).authority(Authority.ROLE_ADMIN).build();
        TasknoticeRequestDto.Create request = createRequest(List.of("existing", "new"));
        when(memberService.findById(7L)).thenReturn(Optional.of(member));
        doAnswer(call -> {
            Tasknotice notice = call.getArgument(0);
            notice.setId(9L);
            return null;
        }).when(repository).save(any(Tasknotice.class));
        LocalDateTime before = LocalDateTime.now();

        Tasknotice result = service.create(7L, request);

        ArgumentCaptor<Tasknotice> saved = ArgumentCaptor.forClass(Tasknotice.class);
        InOrder order = inOrder(repository, associationService);
        order.verify(repository).save(saved.capture());
        order.verify(associationService).attachTags(same(saved.getValue()), eq(request.getTags()));
        assertThat(result).isSameAs(saved.getValue());
        assertThat(result.getMember()).isSameAs(member);
        assertThat(result).extracting("id", "title", "explanation", "target", "deadline", "link")
                .containsExactly(9L, "title", "body", Part.BE, request.getDeadline(), "https://example.com");
        assertThat(result.getDate()).isBetween(before, LocalDateTime.now());
        verify(memberService).findById(7L);
    }

    @Test
    void createsTasknoticeWithEmptyTagList() {
        when(memberService.findById(7L)).thenReturn(Optional.of(Member.builder().id(7L).authority(Authority.ROLE_ADMIN).build()));

        Tasknotice result = service.create(7L, createRequest(List.of()));

        verify(repository).save(same(result));
        verify(associationService).attachTags(same(result), eq(List.of()));
    }

    @Test
    void missingAuthorPreventsSavingAndAttachingTags() {
        when(memberService.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(7L, createRequest(List.of("tag"))))
                .isInstanceOf(NoSuchElementException.class);

        verifyNoInteractions(repository, associationService);
    }

    @Test
    void authorLookupFailurePreventsSavingAndAttachingTags() {
        RuntimeException failure = new IllegalStateException("member lookup failed");
        when(memberService.findById(7L)).thenThrow(failure);

        assertThatThrownBy(() -> service.create(7L, createRequest(List.of("tag")))).isSameAs(failure);

        verifyNoInteractions(repository, associationService);
    }

    @Test
    void createSaveFailurePreventsAttachingTags() {
        when(memberService.findById(7L)).thenReturn(Optional.of(Member.builder().id(7L).authority(Authority.ROLE_ADMIN).build()));
        RuntimeException failure = new IllegalStateException("save failed");
        doThrow(failure).when(repository).save(any(Tasknotice.class));

        assertThatThrownBy(() -> service.create(7L, createRequest(List.of("tag")))).isSameAs(failure);

        verifyNoInteractions(associationService);
    }

    @Test
    void propagatesTagAttachmentFailure() {
        when(memberService.findById(7L)).thenReturn(Optional.of(Member.builder().id(7L).authority(Authority.ROLE_ADMIN).build()));
        RuntimeException failure = new IllegalStateException("tag attachment failed");
        doThrow(failure).when(associationService).attachTags(any(Tasknotice.class), eq(List.of("tag")));

        assertThatThrownBy(() -> service.create(7L, createRequest(List.of("tag")))).isSameAs(failure);

        ArgumentCaptor<Tasknotice> saved = ArgumentCaptor.forClass(Tasknotice.class);
        InOrder order = inOrder(repository, associationService);
        order.verify(repository).save(saved.capture());
        order.verify(associationService).attachTags(same(saved.getValue()), eq(List.of("tag")));
        // Mockito verifies propagation and call order, not database transaction rollback.
    }

    @Test
    void createsTasknoticeWithNullTagList() {
        when(memberService.findById(7L)).thenReturn(Optional.of(
                Member.builder().id(7L).authority(Authority.ROLE_ADMIN).build()));

        Tasknotice result = service.create(7L, createRequest(null));

        verify(repository).save(same(result));
        verify(associationService).attachTags(same(result), isNull());
    }

    @Test
    void ordinaryMemberCannotCreateTasknotice() {
        when(memberService.findById(7L)).thenReturn(Optional.of(
                Member.builder().id(7L).authority(Authority.ROLE_USER).build()));

        assertThatThrownBy(() -> service.create(7L, createRequest(List.of("tag"))))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(repository, associationService);
    }
}
