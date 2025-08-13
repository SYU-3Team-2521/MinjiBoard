package syu.likealion3.hackathon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syu.likealion3.hackathon.dto.PageResponse;
import syu.likealion3.hackathon.dto.PostDetailDto;
import syu.likealion3.hackathon.dto.PostListItemDto;
import syu.likealion3.hackathon.entity.Category;
import syu.likealion3.hackathon.entity.Post;
import syu.likealion3.hackathon.repository.PostRepository;

import java.util.NoSuchElementException;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostRepository postRepository;

    public PageResponse<PostListItemDto> getPosts(Category category, Pageable pageable) {
        Page<Post> page = (category == null)
                ? postRepository.findAll(pageable)
                : postRepository.findByCategory(category, pageable);

        Page<PostListItemDto> mapped = page.map(PostListItemDto::from);
        return PageResponse.of(mapped);
    }

    public PostDetailDto getPostDetail(Long id, Set<Long> likedIds) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        boolean liked = likedIds != null && likedIds.contains(p.getId());
        return PostDetailDto.from(p, liked);
    }
}
