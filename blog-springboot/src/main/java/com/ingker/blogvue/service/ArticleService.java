package com.ingker.blogvue.service;


import com.ingker.blogvue.dto.ArticleArchive;
import com.ingker.blogvue.dto.ArticleCommentCount;
import com.ingker.blogvue.dto.ArticleDTO;
import com.ingker.blogvue.dto.ArticleListDTO;
import com.ingker.blogvue.entity.Archive;
import com.ingker.blogvue.entity.Article;
import com.ingker.blogvue.mapper.ArchiveMapper;
import com.ingker.blogvue.mapper.ArchiveRelationshipMapper;
import com.ingker.blogvue.mapper.ArticleMapper;
import com.ingker.blogvue.mapper.CommentMapper;
import com.ingker.blogvue.util.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;


@Service
public class ArticleService {
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    ArticleMapper articleMapper;

    @Autowired
    ArchiveMapper archiveMapper;

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    ArchiveRelationshipMapper archiveRelationshipMapper;

    @Transactional
    public Page<ArticleListDTO> getAll(Integer page, Integer size, String sort, String order,
                                       String category, String status, String searchKeyword) {
        validatePositiveInteger(page, "页码");
        validatePositiveInteger(size, "每页数量");

        if (sort == null || sort.isEmpty()) {
            sort = "id"; // 默认排序字段
        }
        if (order == null || (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc"))) {
            order = "asc"; // 默认升序
        }

        String dbSortField = mapSortField(sort);
        order = order.toLowerCase();

        List<String> keywords = processSearchKeyword(searchKeyword);

        int offset = size * (page - 1);
        int total = articleMapper.countBySearchAndFilter(status, category, keywords);
        List<Article> articles = articleMapper.searchAndFilter(status, category, keywords, dbSortField, order, size, offset);

        if (articles.isEmpty()) {
            return new Page<>(Collections.emptyList(), total, page, size);
        }

        // 获取文章的分类和标签
        List<Integer> articleIds = articles.stream().map(Article::getArticleId).toList();
        Map<Integer, Integer> commentCountMap = getArticleCommentCountMap(articleIds);
        Map<Integer, Archive> categoryMap = getArticleCategoryMap(articleIds);
        Map<Integer, List<Archive>> tagMap = getArticleTagMap(articleIds);

        // 组装 DTO
        List<ArticleListDTO> articleListDTOs = buildArticleListDTOs(articles, categoryMap, tagMap, commentCountMap);

        logger.info("分页查询文章，分类：{}，发布状态：{}，搜索关键词：{}，页数：{}，每页个数：{}，总记录数：{}，排序：{}，顺序：{}",
                category, status, searchKeyword, page, size, total, sort, order);
        return new Page<>(articleListDTOs, total, page, size);
    }

    /**
     * 排序字段值映射到具体的数据库列名，防止错误输入和SQL注入
     * */
    private String mapSortField(String sortField) {
        // 定义前端字段和数据库列名的映射关系
        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("id", "at.article_id");
        fieldMapping.put("title", "article_title");
        fieldMapping.put("postDate", "post_time");
        fieldMapping.put("updateDate", "update_time");
        // 返回数据库列名，默认为 id
        return fieldMapping.getOrDefault(sortField, "article_id");
    }

    /**
     * 处理搜索关键词
     */
    private List<String> processSearchKeyword(String searchKeyword) {
        return Arrays.stream(searchKeyword.trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * 获取文章评论数映射
     */
    Map<Integer, Integer> getArticleCommentCountMap(List<Integer> articleIds){
        return commentMapper.countByArticle(articleIds).stream()
                .collect(Collectors.toMap(ArticleCommentCount::getArticleId, ArticleCommentCount::getCount,
                        Integer::sum   // 若有重复 articleId，则合并评论数
                ));
    }

    /**
     * 处理搜索关键词
     */
    // 获取文章分类映射
    private Map<Integer, Archive> getArticleCategoryMap(List<Integer> articleIds) {
        return archiveMapper.getByArticleIds(articleIds, "category").stream()
                .collect(Collectors.toMap(ArticleArchive::getArticleId, ArticleArchive::getArchive));
    }

    /**
     * 获取文章标签映射
     */
    private Map<Integer, List<Archive>> getArticleTagMap(List<Integer> articleIds) {
        return archiveMapper.getByArticleIds(articleIds, "post_tag").stream()
                .collect(Collectors.groupingBy(ArticleArchive::getArticleId,
                        Collectors.mapping(ArticleArchive::getArchive, Collectors.toList())));
    }

    /**
     * 组装 ArticleListDTO
     */
    private List<ArticleListDTO> buildArticleListDTOs(List<Article> articles, Map<Integer, Archive> categoryMap,
                                                      Map<Integer, List<Archive>> tagMap, Map<Integer, Integer> commentCountMap) {
        return articles.stream()
                .map(article -> {
                    ArticleListDTO dto = new ArticleListDTO();
                    dto.setArticle(article);
                    dto.setCommentsCount(commentCountMap.getOrDefault(article.getArticleId(), 0));
                    dto.setCategory(categoryMap.getOrDefault(article.getArticleId(), null));
                    dto.setTags(tagMap.getOrDefault(article.getArticleId(), Collections.emptyList()));
                    return dto;
                })
                .toList();
    }


    @Transactional
    public ArticleDTO get(Integer id) {
        Article article = getExistingArticle(id);

        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setArticle(article);
        List<Archive> category = archiveMapper.getByArticleId(id, "category");
        articleDTO.setCategory(category.isEmpty() ? null : category.get(0));
        articleDTO.setTags(new ArrayList<>());
        articleDTO.getTags().addAll(archiveMapper.getByArticleId(id, "post_tag"));

        logger.info("查询文章，ID: {}", id);
        return articleDTO;
    }

    @Transactional
    public void add(ArticleDTO articleDTO) {
        validateNonEmptyArticleDTO(articleDTO);
        validateNonEmptyString(articleDTO.getArticle().getArticleTitle(), "文章标题");
        validateNonEmptyString(articleDTO.getArticle().getArticleContent(), "文章内容");

        try {
            articleMapper.add(articleDTO.getArticle());
            updateArticleCategoriesAndTags(articleDTO);
            logger.info("文章添加成功: ID={}", articleDTO.getArticle().getArticleId());
        } catch (Exception e) {
            logger.error("文章添加失败: 标题={}, 错误信息={}", articleDTO.getArticle().getArticleTitle(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void delete(Integer id) {
        getExistingArticle(id);

        try {
            articleMapper.delete(id);
            archiveRelationshipMapper.deleteByArticleId(id);
            logger.info("文章删除成功: ID={}", id);
        } catch (Exception e) {
            logger.error("文章删除失败: ID={}, 错误信息={}", id, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void update(ArticleDTO articleDTO) {
        validateNonEmptyArticleDTO(articleDTO);
        validatePositiveInteger(articleDTO.getArticle().getArticleId(), "文章ID");
        Article article = getExistingArticle(articleDTO.getArticle().getArticleId());

        validateNonEmptyString(articleDTO.getArticle().getArticleTitle(), "文章标题");
        validateNonEmptyString(articleDTO.getArticle().getArticleContent(), "文章内容");

        try {
            articleMapper.update(article);
            archiveRelationshipMapper.deleteByArticleId(articleDTO.getArticleId());
            updateArticleCategoriesAndTags(articleDTO);
            logger.info("文章更新成功: ID={}", articleDTO.getArticle().getArticleId());
        } catch (Exception e) {
            logger.error("文章更新失败: ID={}, 错误信息={}", articleDTO.getArticle().getArticleId(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void increaseField(String field, Integer number, Integer id) {
        validatePositiveInteger(id, "文章ID");
        validatePositiveInteger(number, field + " 增加值");
        try {
            articleMapper.increaseField(field, number, id);
            logger.info("文章字段更新成功: field={}, id={}, 新增值={}", field, id, number);
        } catch (Exception e) {
            logger.error("文章字段更新失败: field={}, id={}, 错误信息={}", field, id, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 校验是否为正整数
     */
    private void validatePositiveInteger(Integer value, String fieldName) {
        if (value == null || value < 1) {
            throw new IllegalArgumentException(fieldName + " 必须是正整数");
        }
    }

    /**
     * 校验非空字符串
     */
    private void validateNonEmptyString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
    }

    /**
     * 校验ArticleDTO
     */
    private void validateNonEmptyArticleDTO(ArticleDTO value) {
        if (value == null || value.getArticle() == null) {
            throw new IllegalArgumentException("文章数据不能为空");
        }
    }

    /**
     * 查询文章是否存在
     */
    private Article getExistingArticle(Integer id) {
        validatePositiveInteger(id, "文章ID");
        Article article = articleMapper.getOne(id);
        if (article == null) {
            throw new NoSuchElementException("文章不存在");
        }
        return article;
    }

    /**
     * 更新文章的分类和标签
     */
    private void updateArticleCategoriesAndTags(ArticleDTO articleDTO) {
        Integer articleId = articleDTO.getArticle().getArticleId();
        archiveRelationshipMapper.deleteByArticleId(articleId);

        if (articleDTO.getCategory() != null) {
            validatePositiveInteger(articleDTO.getCategory().getArchiveId(), "分类ID");
            archiveRelationshipMapper.add(articleDTO.getCategory().getArchiveId(), articleId);
        }

        for (Archive tag : articleDTO.getTags()) {
            validatePositiveInteger(tag.getArchiveId(), "标签ID");
            archiveRelationshipMapper.add(tag.getArchiveId(), articleId);
        }
    }
}