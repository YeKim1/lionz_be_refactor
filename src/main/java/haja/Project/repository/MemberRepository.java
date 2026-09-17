package haja.Project.repository;

import haja.Project.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);

    default Member findByIdOrElseThrow(Long memberId) {
        return findById(memberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 정보입니다."));
    }
}
