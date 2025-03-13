package com.ingker.blogvue.mapper;


import com.ingker.blogvue.dto.CommentListDTO;
import com.ingker.blogvue.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface CommentMapper {
    @Select("""
            SELECT c.*, a.article_title FROM comment c
            left join article a on c.article_id = a.article_id
            WHERE status != 'trash'
            ORDER BY ${sort} ${order}
            limit #{limit} offset #{offset}
            """)
    List<CommentListDTO> getAll(String sort, String order, Integer limit, Integer offset);

    @Select("SELECT count(*) FROM comment WHERE status != 'trash'")
    Integer count();

    @Select("SELECT * FROM comment WHERE article_id = #{id} AND status != 'trash'")
    List<Comment> getByArticleId(Integer id);

    @Insert("INSERT INTO comment(content, author, publish_date, anonymous_status, parent_id, article_id, status) " +
            "VALUES(#{content}, #{author}, #{publishDate}, #{anonymousStatus}, #{parentId}, #{articleId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "commentId")
    void add(Comment comment);

    @Delete("DELETE FROM comment WHERE comment_id = #{id}")
    void delete(Integer id);

    @Select("SELECT count(*) FROM comment WHERE status = 'unread'")
    Integer countUnread();

    @Update("UPDATE comment SET status = 'read' WHERE comment_id = #{id}")
    void readMark(Integer id);

    @Delete("UPDATE comment SET status = 'trash' WHERE article_id = #{articleId}")
    void trashMark(Integer articleId);

    @Delete("UPDATE comment SET status = 'read' WHERE article_id = #{articleId}")
    void recycle(Integer articleId);
}
