package com.ingker.blogvue.mapper;


import org.apache.ibatis.annotations.*;

public interface ArchiveRelationshipMapper {
    /**
     * archive_id和article_id的唯一性约束
     * 添加文章和归档关系的记录，使用INSERT IGNORE INTO确保满足多条相关记录只保存一条*/
    @Insert("INSERT IGNORE INTO archive_relationship(archive_id, article_id) VALUES (#{archiveId}, #{articleId})")
    void add(Integer archiveId, Integer articleId);

    @Delete("DELETE FROM archive_relationship arr WHERE arr.archive_id = #{id}")
    void deleteByArchiveId(Integer id);

    @Delete("DELETE FROM archive_relationship arr WHERE arr.article_id = #{id}")
    void deleteByArticleId(Integer id);

    @Delete("DELETE FROM archive_relationship arr WHERE arr.article_id = #{articleId} AND arr.archive_id = #{collectionId}")
    void deleteOne(Integer articleId, Integer collectionId);
}
