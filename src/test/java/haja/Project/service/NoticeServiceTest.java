package haja.Project.service;

import haja.Project.api.dto.NoticeRequestDto;
import haja.Project.domain.Member;
import haja.Project.domain.Notice;
import haja.Project.domain.Part;
import haja.Project.repository.NoticeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {
    @Mock NoticeRepository repository;
    @Mock MemberService memberService;
    @Mock Notice_TagService associationService;
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

    private NoticeRequestDto.Create createRequest(List<String> tags) {
        return new NoticeRequestDto.Create("title", "body", Part.BE,
                LocalDateTime.of(2026, 10, 1, 12, 0), tags);
    }

    @Test
    void createsNoticeThenAttachesTagsAndReturnsSavedEntity() {
        Member member = Member.builder().id(7L).build();
        NoticeRequestDto.Create request = createRequest(List.of("existing", "new"));
        when(memberService.findById(7L)).thenReturn(Optional.of(member));
        doAnswer(call -> {
            Notice notice = call.getArgument(0);
            notice.setId(9L);
            return null;
        }).when(repository).save(any(Notice.class));
        LocalDateTime before = LocalDateTime.now();

        Notice result = service.create(7L, request);

        ArgumentCaptor<Notice> saved = ArgumentCaptor.forClass(Notice.class);
        InOrder order = inOrder(repository, associationService);
        order.verify(repository).save(saved.capture());
        order.verify(associationService).attachTags(same(saved.getValue()), eq(request.getTags()));
        assertThat(result).isSameAs(saved.getValue());
        assertThat(result.getMember()).isSameAs(member);
        assertThat(result).extracting("id", "title", "explanation", "target", "deadline")
                .containsExactly(9L, "title", "body", Part.BE, request.getDeadline());
        assertThat(result.getDate()).isBetween(before, LocalDateTime.now());
        verify(memberService).findById(7L);
    }

    @Test
    void createsNoticeWithEmptyTagList() {
        when(memberService.findById(7L)).thenReturn(Optional.of(Member.builder().id(7L).build()));

        Notice result = service.create(7L, createRequest(List.of()));

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
        when(memberService.findById(7L)).thenReturn(Optional.of(Member.builder().id(7L).build()));
        RuntimeException failure = new IllegalStateException("save failed");
        doThrow(failure).when(repository).save(any(Notice.class));

        assertThatThrownBy(() -> service.create(7L, createRequest(List.of("tag")))).isSameAs(failure);

        verifyNoInteractions(associationService);
    }

    @Test
    void propagatesTagAttachmentFailure() {
        when(memberService.findById(7L)).thenReturn(Optional.of(Member.builder().id(7L).build()));
        RuntimeException failure = new IllegalStateException("tag attachment failed");
        doThrow(failure).when(associationService).attachTags(any(Notice.class), eq(List.of("tag")));

        assertThatThrownBy(() -> service.create(7L, createRequest(List.of("tag")))).isSameAs(failure);

        ArgumentCaptor<Notice> saved = ArgumentCaptor.forClass(Notice.class);
        InOrder order = inOrder(repository, associationService);
        order.verify(repository).save(saved.capture());
        order.verify(associationService).attachTags(same(saved.getValue()), eq(List.of("tag")));
        // Mockito verifies propagation and call order, not database transaction rollback.
    }
}
