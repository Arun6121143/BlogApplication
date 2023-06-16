package com.myblogapplication.service;

import com.myblogapplication.entity.User;
import com.myblogapplication.repository.PostRepository;
import com.myblogapplication.entity.Post;
import com.myblogapplication.entity.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {
    @Autowired
    PostRepository postRepository;
    @Autowired
    TagService tagService;
    @Autowired
    UserService userService;

    public Boolean addPost(int userId, Post post, String author) {
        User user = userService.findUserById(userId);
        Post newPost = new Post();
        if (post.getTitle() == null || post.getTitle().isEmpty()) {
            return false;
        } else if (post.getContent() == null || post.getContent().isEmpty()) {
            return false;
        } else if (post.getTags() == null || post.getTags().isEmpty()) {
            return false;
        }
        if (post.getContent().length() > 250) {
            String excerpt = post.getContent().substring(0, 151);
            newPost.setExcerpt(excerpt);
        } else {
            newPost.setExcerpt(post.getContent());
        }
        if (author == null || author.isEmpty()) {
            newPost.setAuthor(user.getName());
        } else {
            newPost.setAuthor(author);
        }
        newPost.setContent(post.getContent());
        newPost.setCreatedAt(LocalDate.now());
        newPost.setPublished(true);
        newPost.setPublishedAt(LocalDate.now());
        newPost.setTitle(post.getTitle());
        newPost.setUser(user);
        List<Tag> newTagList = tagService.saveNewTag(post.getTags());
        newPost.setTags(newTagList);
        postRepository.save(newPost);
        return true;
    }

    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    public void savePost(String title, String content, String tags, String author, Principal principal) {
        Post post = new Post();
        User user = userService.findUserByEmail(principal.getName());
        post.setTitle(title);
        if (author == null || author.isEmpty()) {
            post.setAuthor(user.getName());
        } else {
            post.setAuthor(author);
        }
        post.setContent(content);
        post.setPublished(true);
        LocalDate createdDate = LocalDate.now();
        post.setCreatedAt(createdDate);
        post.setPublishedAt(createdDate);
        if (content.length() > 250) {
            String excerpt = content.substring(0, 151);
            post.setExcerpt(excerpt);
        } else {
            post.setExcerpt(post.getContent());
        }
        post.setUser(user);
        post.setTags(tagService.saveTag(tags, createdDate));
        postRepository.save(post);
    }

    public Post getPost(int id) {
        return postRepository.findById(id);
    }

    public List<Post> searchPosts(String searchbar) {
        return postRepository.searchPosts(searchbar);
    }

    public void saveUpdatedPost(Post existingPost, String tags, String title, String content, List<Tag> existingPostTags) {
        existingPost.setTitle(title);
        LocalDate updatedAt = LocalDate.now();
        existingPost.setUpdatedAt(updatedAt);
        existingPost.setContent(content);
        if (content.length() > 150) {
            existingPost.setExcerpt(content.substring(0, 151));
        } else {
            existingPost.setExcerpt(content);
        }
        List<Tag> tagList = tagService.updateTag(tags, updatedAt, existingPostTags);
        existingPost.setTags(tagList);
        postRepository.save(existingPost);
    }

    public Page<Post> paginateAndSort(int pageNumber, int pageSize, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
        return this.postRepository.findAll(pageable);
    }

    public List<Post> findFilteredPosts(Integer authorId, List<Integer> tagIds, String date) {
        List<Post> allPosts = postRepository.findAll();
        List<Post> filteredPosts = new ArrayList<>();
        for (Post post : allPosts) {
            if (findFilteredPostByAuthor(post, authorId) &&
                    findFilteredPostByTag(post, tagIds) &&
                    findFilteredPostByDate(post, date)) {
                filteredPosts.add(post);
            }
        }
        return filteredPosts;
    }

    public boolean findFilteredPostByDate(Post post, String date) {
        if (date == null || date.isEmpty()) {
            return true;
        }
        LocalDate filterDate = LocalDate.parse(date);
        return post.getPublishedAt().equals(filterDate);
    }

    public boolean findFilteredPostByTag(Post post, List<Integer> tagIds) {
        if (tagIds==null || tagIds.isEmpty() || tagIds.size() == 0) {
            return true;
        }
        for (int id : tagIds) {
            Tag tag = tagService.findByTagId(id);
            if (tag == null) {
                return false;
            }
            for (Tag tagName : post.getTags()) {
                if (tagName.getName().trim().equals(tag.getName().trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean findFilteredPostByAuthor(Post post, Integer authorId) {
        if (authorId == null || authorId==0) {
            return true;
        }
        return post.getUser()!=null && post.getUser().getId() != null && post.getUser().getId()==authorId;
    }

    public List<Post> findPostByUserId(int userId) {
        return postRepository.findByUserId(userId);
    }

    public void updatePost(int postId, Post updatedPost) {
        Post post = postRepository.findById(postId);
        List<Tag> existingTags = post.getTags();
        List<String> tagNames = new ArrayList<>();
        LocalDate updatedAt = LocalDate.now();
        post.setUpdatedAt(LocalDate.now());
        if (updatedPost.getContent() != null) {
            post.setContent(updatedPost.getContent());
            if (updatedPost.getContent().length() > 250) {
                post.setExcerpt(updatedPost.getContent().substring(0, 151));
            } else {
                post.setExcerpt(updatedPost.getContent());
            }
        }
        if (updatedPost.getTitle() != null) {
            post.setTitle(updatedPost.getTitle());
        }
        for (Tag tag : updatedPost.getTags()) {
            tagNames.add(tag.getName());
        }
        String tags = String.join(",", tagNames);
        List<Tag> updatedTags = tagService.updateTag(tags, updatedAt, existingTags);
        post.setTags(updatedTags);
        postRepository.save(post);
    }

    public void deleteByPostId(int postId) {
        postRepository.deleteById(postId);
    }

    public Page<Post> getAllPosts(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        return postRepository.findAll(pageable);
    }

    public List<Post> sortPosts(String sortOrder, String sortField) {
        Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortingCriteria = Sort.by(direction, sortField);
        return postRepository.findAll(sortingCriteria);
    }

    public void saveAuthor(Post post) {
        postRepository.save(post);
    }
}