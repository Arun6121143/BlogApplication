package com.myblogapplication.service;

import com.myblogapplication.entity.Tag;
import com.myblogapplication.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TagService {
    @Autowired
    TagRepository tagRepository;

    public Tag findByTagId(int id) {
        return tagRepository.findById(id).get();
    }

    public List<Tag> saveTag(String tags, LocalDate createdDate) {
        String[] postTags = tags.split(",");
        List<Tag> tagList = new ArrayList<>();
        for (String tagName : postTags) {
            Tag tag = new Tag();
            tag.setName(tagName.trim());
            tag.setCreatedAt(createdDate);
            tagList.add(tag);
            tagRepository.save(tag);
        }
        return tagList;
    }

    public List<Tag> updateTag(String tags, LocalDate updatedAt, List<Tag> existingPostTags) {
        String[] postTags = tags.split(",");
        List<Tag> updatedTags = new ArrayList<>();
        if (tags.length() == 0 && existingPostTags.size() == 1) {
            for (int tagIndex = 0; tagIndex < existingPostTags.size(); tagIndex++) {
                tagRepository.deleteById(existingPostTags.get(tagIndex).getId());
            }
        } else if (existingPostTags.size() == 2 && postTags.length == 1) {
            for (int tagIndex = 0; tagIndex < existingPostTags.size(); tagIndex++) {
                if (existingPostTags.get(tagIndex).getName().trim().equals(tags.trim())) {
                    updatedTags.add(existingPostTags.get(tagIndex));
                } else {
                    tagRepository.deleteById(existingPostTags.get(tagIndex).getId());
                }
            }
        } else {
            if (tags.length() == 0) {
                for (int tagIndex = 0; tagIndex < existingPostTags.size(); tagIndex++) {
                    tagRepository.deleteById(existingPostTags.get(tagIndex).getId());
                }
            }
            if ((tags.length() != 0) && (existingPostTags.size() == postTags.length || existingPostTags.size() < postTags.length)) {
                for (int tagIndex = 0; tagIndex < existingPostTags.size(); tagIndex++) {
                    Tag tag = existingPostTags.get(tagIndex);
                    String newTagName = postTags[tagIndex].trim();
                    tag.setName(newTagName);
                    tag.setUpdatedAt(updatedAt);
                    tagRepository.save(tag);
                    updatedTags.add(tag);
                }
            }
            if (tags.length() != 0 && postTags.length > existingPostTags.size()) {
                for (int tagIndex = existingPostTags.size(); tagIndex < postTags.length; tagIndex++) {
                    String newTagName = postTags[tagIndex].trim();
                    Tag newTag = new Tag();
                    newTag.setName(newTagName);
                    newTag.setCreatedAt(updatedAt);
                    newTag.setUpdatedAt(updatedAt);
                    tagRepository.save(newTag);
                    updatedTags.add(newTag);
                }
            } else if ((tags.length() != 0) && (postTags.length < existingPostTags.size() && postTags.length != 0)) {
                for (int tagIndex = 0; tagIndex < postTags.length; tagIndex++) {
                    Tag tag = existingPostTags.get(tagIndex);
                    String newTagName = postTags[tagIndex].trim();
                    tag.setName(newTagName);
                    tag.setUpdatedAt(updatedAt);
                    tagRepository.save(tag);
                    updatedTags.add(tag);
                }
                for (int tagIndex = existingPostTags.size() - postTags.length; tagIndex < existingPostTags.size(); tagIndex++) {
                    tagRepository.deleteById(existingPostTags.get(tagIndex).getId());
                }
            }
        }
        return updatedTags;
    }

    public List<Tag> saveNewTag(List<Tag> tags) {
        List<Tag> tagList = new ArrayList<>();
        for (Tag tagName : tags) {
            Tag tag = new Tag();
            tag.setName(tagName.getName());
            tag.setCreatedAt(LocalDate.now());
            tagList.add(tag);
            tagRepository.save(tag);
        }
        return tagList;
    }
}