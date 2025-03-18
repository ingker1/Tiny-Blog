package com.ingker.blogvue.mapper;


import com.ingker.blogvue.dto.ArchiveListDTO;
import com.ingker.blogvue.dto.ArticleArchive;
import com.ingker.blogvue.dto.ArticleCollection;
import com.ingker.blogvue.dto.ArticleRecord;
import com.ingker.blogvue.entity.Archive;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ArchiveMapper {

    @Select("SELECT * FROM archive WHERE archive_id = #{id}")
    Archive getByArchiveId(Integer id);
    /**
     *
     * 根据文章id查询存档分类*/
    @Select("""
            SELECT ar.*
            FROM archive ar
            INNER JOIN archive_relationship arr
            ON ar.archive_id = arr.archive_id
            INNER JOIN article at
            ON at.article_id = arr.article_id
            WHERE at.article_id = #{articleId}
            AND ar.taxonomy = #{taxonomy}""")
    List<Archive> getByArticleId(Integer articleId, String taxonomy);

    @Select("""
            <script>
            SELECT at.article_id, ar.archive_id, ar.archive_name, ar.taxonomy
            FROM archive ar
            INNER JOIN archive_relationship arr
            ON ar.archive_id = arr.archive_id
            INNER JOIN article at
            ON at.article_id = arr.article_id
            WHERE at.article_id IN
            <foreach collection='articleIds' item='articleId' open='(' separator=',' close=')'>
                #{articleId}
            </foreach>
            AND ar.taxonomy = #{taxonomy}
            </script>""")
    @Results({
            @Result(column = "article_id", property = "articleId"),  // 映射 articleId
            @Result(column = "archive_id", property = "archive.archiveId"),  // 映射 Archive 的 archiveId
            @Result(column = "archive_name", property = "archive.archiveName"),  // 映射 Archive 的 name
            @Result(column = "taxonomy", property = "archive.taxonomy")  // 映射 Archive 的 taxonomy
    })
    List<ArticleArchive> getByArticleIds(List<Integer> articleIds, String taxonomy);

    /**
     * 根据分类方法查询存档分类
     * 返回分类名称和其总数
     * */
    @Select("""
            SELECT ar.archive_id, ar.archive_name, ar.taxonomy, COUNT(arr.archive_id)
            FROM archive ar
            LEFT JOIN archive_relationship arr ON ar.archive_id = arr.archive_id
            WHERE ar.taxonomy = #{taxonomy}
            GROUP BY ar.archive_id, ar.archive_name, ar.taxonomy
            ORDER BY ${sort} ${order}
            limit #{limit} offset #{offset}
            """)
    List<ArchiveListDTO> getByTaxonomyWithCount(String taxonomy, String sort, String order, Integer limit, Integer offset);

    @Select("""
            SELECT ar.archive_id, ar.archive_name, ar.taxonomy, COUNT(arr.archive_id)
            FROM archive ar
            LEFT JOIN archive_relationship arr ON ar.archive_id = arr.archive_id
            WHERE ar.taxonomy = #{taxonomy}
            GROUP BY ar.archive_id, ar.archive_name, ar.taxonomy
            ORDER BY ${sort} ${order}
            limit #{limit} offset #{offset}
            """)
    List<Archive> getByTaxonomy(String taxonomy, String sort, String order, Integer limit, Integer offset);
    @Select("""
            SELECT COUNT(*) AS total_count
            FROM (
                SELECT ar.archive_name
                FROM archive ar
                LEFT JOIN archive_relationship arr ON ar.archive_id = arr.archive_id
                WHERE ar.taxonomy = #{taxonomy}
                GROUP BY ar.archive_id, ar.archive_name, ar.taxonomy
            ) AS grouped
            """)
    Integer countByTaxonomy(String taxonomy);

    /**
     * archive_name和taxonomy有唯一性约束
     * DUPLICATE KEY UPDATE，即使数据已经存在，也不会插入新记录，而是更新已有记录。
     * 使用 LAST_INSERT_ID(archiveId)，这样不管是新插入的还是已存在的，都能返回 archiveId
     * */
    @Insert("""
            INSERT INTO archive(archive_name, taxonomy)
            VALUES(#{archiveName}, #{taxonomy})
            ON DUPLICATE KEY UPDATE
            archive_name = VALUES(archive_name), taxonomy = VALUES(taxonomy),
            archive_id = LAST_INSERT_ID(archive_id)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "archiveId")
    void add(Archive archive);

    @Delete("DELETE FROM archive WHERE archive_id = #{id}")
    void delete(Integer id);

    @Update("UPDATE archive SET archive_name = #{archiveName}, taxonomy = #{taxonomy} WHERE archive_id = #{archiveId}")
    void update(Archive archive);

    @Select("""
            SELECT
                YEAR(post_time) AS year,
                MONTH(post_time) AS month,
                article_id AS id,
                article_title as title
            FROM
                article
            WHERE
                post_status = 'publish'
            ORDER BY
                YEAR(post_time) DESC, MONTH(post_time) ASC, post_time ASC
            """)
    List<ArticleRecord> getArchiveDates();

    @Select("""
            SELECT ar.archive_id, ar.archive_name, ar.taxonomy, COUNT(arr.archive_id) as count
            FROM archive ar
            LEFT JOIN archive_relationship arr ON ar.archive_id = arr.archive_id
            WHERE ar.taxonomy = #{taxonomy}
            GROUP BY ar.archive_id, ar.archive_name, ar.taxonomy
            HAVING count > 1
            ORDER BY ${sort} ${order}
            """)
    List<ArchiveListDTO> getAllByTaxonomyWithCount(String taxonomy, String sort, String order);


    @Select("""
            select at.article_id, at.article_title, at.post_time, ar.archive_name
            from article at
            inner join archive_relationship arr on at.article_id = arr.article_id
            inner join archive ar on ar.archive_id = arr.archive_id
            where ar.taxonomy = 'collection'
            and ar.archive_name in (
                select ar.archive_name
                from archive_relationship arr
                inner join archive ar on arr.archive_id = ar.archive_id
                where arr.article_id = #{articleId} and ar.taxonomy = 'collection'
            )
            ORDER BY at.post_time
            """)
    List<ArticleCollection> getCollection(Integer articleId);
}
