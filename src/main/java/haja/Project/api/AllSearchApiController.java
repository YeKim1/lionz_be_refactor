package haja.Project.api;

import haja.Project.api.dto.AllSearchDto;
import haja.Project.service.AllSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AllSearchApiController {

    private final AllSearchService allSearchService;

    @Tag(name = "통합검색")
    @Operation(summary = "검색모달에서 키워드 검색 부분", description = "Tag, ")
    @GetMapping("all")
    public AllSearchDto.Response AllSearch(@RequestParam String word) {
        return allSearchService.search(word);
    }
}
