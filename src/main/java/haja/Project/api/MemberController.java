package haja.Project.api;

import haja.Project.api.dto.ApiResponse;
import haja.Project.api.dto.MemberRequestDto;
import haja.Project.api.dto.MemberResponseDto.MemberInfo;
import haja.Project.domain.Image;
import haja.Project.domain.Member;
import haja.Project.service.MemberService;
import haja.Project.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

// 사진을 100kb로 줄이자

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
@Tag(name = "Member")
public class MemberController {
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "멤버 코멘트 수정")
    @PutMapping("comment")
    public ApiResponse<MemberInfo> updateMemberComment(@RequestBody @Valid MemberRequestDto.UpdateComment request) {
        MemberInfo response = memberService.updateComment(SecurityUtil.getCurrentMemberId(), request.getComment());
        return ApiResponse.from(response);
    }
    @Operation(summary = "멤버 비밀번호 수정")
    @PutMapping("password")
    public ApiResponse<MemberInfo> updateMemberPassword(@RequestBody @Valid MemberRequestDto.UpdatePassword request) {
        MemberInfo response = memberService.updatePassword(SecurityUtil.getCurrentMemberId(), request.getPassword());
        return ApiResponse.from(response);
    }


    @Operation(summary = "멤버 프로필 업로드")
    @PostMapping(value = "/img", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateMemberImage(@RequestBody @Valid MultipartFile file) throws IOException {
        memberService.setImage(SecurityUtil.getCurrentMemberId(), file);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Operation(summary = "로그인 중인 멤버 조회")
    @GetMapping
    public ApiResponse<MemberInfo> MemberInfo() {
        Member member = memberService.findById(SecurityUtil.getCurrentMemberId()).get();
        return ApiResponse.from(MemberInfo.from(member));
    }



    @Operation(summary = "id로 멤버 조회")
    @GetMapping("/{id}")
    public ApiResponse<MemberInfo> findMemberInfoById(@PathVariable("id") Long id) {
            return ApiResponse.from(MemberInfo.from(memberService.findById(id).get()));
    }

    @Operation(summary = "전체 멤버 조회")
    @GetMapping("/all")
    public ApiResponse<List<MemberInfo>> findAllMember() {
        List<Member> members = memberService.findAll();
        List<MemberInfo> memberResult = members.stream()
                .map(MemberInfo::from)
                .collect(Collectors.toList());
        return ApiResponse.from(memberResult);
    }

        @Operation(summary = "멤버 id로 프로필 조회")
        @GetMapping(value = "/img/{name}", produces = MediaType.IMAGE_JPEG_VALUE)
        public ResponseEntity<byte[]> getImage(@PathVariable("name") String name) throws IOException {
        //String path = "C:\\Users\\kjk87\\Desktop\\img\\";
            String path = "/home/img/";
            InputStream inputStream = new FileInputStream(path + name);
            byte[] bytes = inputStream.readAllBytes();
        inputStream.close();
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", Files.probeContentType(Paths.get(path + name)));
        return new ResponseEntity<byte[]>(bytes, header, HttpStatus.OK);
    }

    @Operation(summary = "현재 로그인한 멤버의 프로필 삭제")
    @DeleteMapping(value = "/img")
    public void deleteImage() {
        Member member = memberService.findById(SecurityUtil.getCurrentMemberId()).get();

        // 기본 이미지 삭제 불가
        if (member.getImage().img_name.equals("DefaultProfile.png")) {
            return;
        }

        Image image = member.getImage();
        File file = new File(image.img_path);
        if(file.exists()) file.delete();

        memberService.deleteImage(member);



    }
}
