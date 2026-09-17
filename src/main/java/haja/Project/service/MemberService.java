package haja.Project.service;

import haja.Project.api.dto.MemberResponseDto;
import haja.Project.domain.Image;
import haja.Project.domain.Member;
import haja.Project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberResponseDto findMemberInfoById(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberResponseDto::of)
                .orElseThrow(() -> new RuntimeException("로그인 유저 정보가 없습니다."));
    }

    public MemberResponseDto findMemberInfoByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(MemberResponseDto::of)
                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public Optional<Member> findByEmail(String email){ return memberRepository.findByEmail(email); }

    @Transactional
    public void deleteImage(Member member) {
        member.setImage(null);
        memberRepository.save(member);
    }

    @Transactional
    public MemberResponseDto.MemberInfo updateComment(Long id, String comment) {
        Member member = memberRepository.findByIdOrElseThrow(id);
        member.setComment(comment);
        return MemberResponseDto.MemberInfo.from(member);
    }

    @Transactional
    public MemberResponseDto.MemberInfo updatePassword(Long id, String password) {
        Member member = memberRepository.findByIdOrElseThrow(id);
        member.setPassword(password);
        return MemberResponseDto.MemberInfo.from(member);
    }
    @Transactional
    public void setImage(Long id, MultipartFile file) throws IOException {
        Member member = memberRepository.findByIdOrElseThrow(id);
        member.setImage(uploadImage(file));
    }

    private Image uploadImage(MultipartFile file) throws IOException {
        Date date = new Date();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("비어있는 파일입니다.");
        }

        String file_name = date.getTime() + file.getOriginalFilename();
        //String img_path = "C:\\Users\\kjk87\\Desktop\\img\\" + file_name;
        String img_path = "/home/img/" + file_name;
        String img_link = "http://localhost:8080/member/img/" + file_name;
        //String img_link = "https://lionz.kro.kr/member/img/" + file_name;
        File dest = new File(img_path);

        // 이미지 용량 제한
        String format = file_name.substring(file_name.lastIndexOf(".") + 1);
        BufferedImage bufferedImage = Scalr.resize(ImageIO.read(file.getInputStream()), 1000, 1000, Scalr.OP_ANTIALIAS);
        ImageIO.write(bufferedImage, format, dest);

        return new Image(img_link, file_name, img_path);
    }

    @Transactional
    public void setTokenCount(Member member, LocalDateTime date) {
        member.setAccessTokenExpiresIn(date);
        memberRepository.save(member);
    }

    @Transactional
    public void updateCount(Member member,Integer c){
        member.setCount(c + 1);
        memberRepository.save(member);
    }
}