package com.ingker.blogvue.mapper;


import com.ingker.blogvue.entity.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ArticleMapper {
    @Select("SELECT * FROM article WHERE article_id = #{id}")
    Article getOne(Integer id);

    /**
     * 通过筛选条件搜索文章，支持动态多个关键词
     * 可查询包含所有关键词的文章
     * 可获取特定归档分类和发布状态的文章
     * 可自定义排序字段和顺序
     * */
    @Select("""
    <script>
            SELECT
              at.article_id, at.article_title, LEFT(at.article_content, 500) as article_content,
              at.post_time, at.update_time, at.post_status,
              at.likes_count, at.views_count
            FROM article at
            LEFT JOIN archive_relationship arr ON at.article_id = arr.article_id
            LEFT JOIN archive ar ON arr.archive_id = ar.archive_id
            <where>
              <if test="status != null and status != ''">
                  at.post_status = #{status}
              </if>
              <if test="archiveName != null and archiveName != ''">
                  AND ar.archive_name = #{archiveName}
              </if>
              <if test="keywords != null and keywords.size() > 0">
                  AND (
                  <foreach collection='keywords' item='keyword' separator='AND'>
                      (at.article_title LIKE CONCAT('%', #{keyword}, '%')
                      OR at.article_content LIKE CONCAT('%', #{keyword}, '%'))
                  </foreach>
                  )
              </if>
            </where>
            GROUP BY at.article_id
            ORDER BY ${sort} ${order}
            LIMIT #{limit} OFFSET #{offset}
    </script>""")
    List<Article> searchAndFilter(String status, String archiveName, List<String> keywords,
                                  String sort, String order, Integer limit, Integer offset);


    @Select("""
    <script>
            SELECT count(DISTINCT at.article_id)
                 FROM article at
                 LEFT JOIN archive_relationship arr ON arr.article_id = at.article_id
                 LEFT JOIN archive ar ON arr.archive_id = ar.archive_id
                 <where>
                     <if test="status != null and status != ''">
                         at.post_status = #{status}
                     </if>
                     <if test="archiveName != null and archiveName != ''">
                         AND ar.archive_name = #{archiveName}
                     </if>
                     <if test="keywords != null and keywords.size() > 0">
                         AND (
                         <foreach collection='keywords' item='keyword' separator='AND'>
                            (article_title LIKE CONCAT('%', #{keyword}, '%')
                            OR article_content LIKE CONCAT('%', #{keyword}, '%'))
                         </foreach>
                         )
                     </if>
                 </where>
    </script>""")
    Integer countBySearchAndFilter(String status, String archiveName, List<String> keywords);

    /**
     * 使用useGeneratedKeys，MyBatis 可以自动获取 INSERT 后的主键
     * */
    @Insert("INSERT INTO article(article_title, article_content, post_time, update_time, post_status, likes_count, views_count) " +
            "VALUES(#{articleTitle}, #{articleContent}, #{postTime}, #{updateTime}, #{postStatus}, #{likesCount}, #{viewsCount})")
    @Options(useGeneratedKeys = true, keyProperty = "articleId")
    Integer add(Article article);

    @Delete("DELETE FROM article WHERE article_id = #{id}")
    void delete(Integer id);

    @Update("UPDATE article SET article_title = #{articleTitle}, article_content = #{articleContent}, post_time = #{postTime}, update_time = #{updateTime}, " +
            "post_status = #{postStatus}, likes_count = #{likesCount}, views_count = #{viewsCount} WHERE article_id = #{articleId}")
    void update(Article article);

/*    @Update("UPDATE article SET likes_count = #{number} WHERE article_id = #{id}")
    void increaseLikes(Integer number, Integer id);

    @Update("UPDATE article SET views_count = #{number} WHERE article_id = #{id}")
    void increaseViews(Integer number, Integer id);*/

    @Update("UPDATE article SET ${field} = #{number} WHERE article_id = #{id}")
    void increaseField(String field, Integer number, Integer id);
}
