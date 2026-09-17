package haja.Project.service;

import haja.Project.domain.Tag;
import haja.Project.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Long save(Tag tag){
        tagRepository.save(tag);
        return tag.getId();
    }

    public Tag findOne(Long id){
        return tagRepository.findOne(id);
    }

    public Tag findByName(String name) {
        Optional<Tag> tag = tagRepository.findByName(name);
        if (tag.isEmpty()) return null;
        else return tag.get();
    }

    public List<Tag> findByWord(String name){
        return tagRepository.findByWord(name);
    }

    @Transactional
    public Tag findOrCreate(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> create(name));
    }

    private Tag create(String name) {
        Tag tag = Tag.builder().name(name).build();
        tagRepository.save(tag);
        return tag;
    }
}
