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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;


@Service
public class ArticleService {
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
        // 参数校验
        if (page == null || page < 1) {
            throw new IllegalArgumentException("页码必须是正整数");
        }
        if (size == null || size < 1) {
            throw new IllegalArgumentException("每页数量必须是正整数");
        }
        if (sort == null || sort.isEmpty()) {
            sort = "id"; // 默认排序字段
        }
        if (order == null || (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc"))) {
            order = "asc"; // 默认升序
        }

        // 防止SQL注入，映射排序字段
        String dbSortField = mapSortField(sort);
        order = order.toLowerCase();

        // 处理搜索关键字
        List<String> keywords = processSearchKeyword(searchKeyword);

        // 计算分页偏移量
        int offset = size * (page - 1);

        // 获取总记录数
        int total = articleMapper.countBySearchAndFilter(status, category, keywords);

        // 查询文章
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

        System.out.printf("分页查询文章，分类：%s，发布状态：%s，搜索关键词：%s，页数：%d，每页个数：%d，总记录数：%d，排序：%s，顺序：%s%n",
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

    // 处理搜索关键词
    private List<String> processSearchKeyword(String searchKeyword) {
        return Arrays.stream(searchKeyword.trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .toList();
    }

    // 获取文章评论数映射
    Map<Integer, Integer> getArticleCommentCountMap(List<Integer> articleIds){
        return commentMapper.countByArticle(articleIds).stream()
                .collect(Collectors.toMap(ArticleCommentCount::getArticleId, ArticleCommentCount::getCount,
                        Integer::sum   // 若有重复 articleId，则合并评论数
                ));
    }

    // 获取文章分类映射
    private Map<Integer, Archive> getArticleCategoryMap(List<Integer> articleIds) {
        return archiveMapper.getByArticleIds(articleIds, "category").stream()
                .collect(Collectors.toMap(ArticleArchive::getArticleId, ArticleArchive::getArchive));
    }

    // 获取文章标签映射
    private Map<Integer, List<Archive>> getArticleTagMap(List<Integer> articleIds) {
        return archiveMapper.getByArticleIds(articleIds, "post_tag").stream()
                .collect(Collectors.groupingBy(ArticleArchive::getArticleId,
                        Collectors.mapping(ArticleArchive::getArchive, Collectors.toList())));
    }

    // 组装 ArticleListDTO
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
        ArticleDTO articleDTO = new ArticleDTO();

        Article article = articleMapper.getOne(id);
        articleDTO.setArticle(article);
        List<Archive> category = archiveMapper.getByArticleId(id, "category");
        articleDTO.setCategory(category.isEmpty() ? null : category.get(0));
        articleDTO.setTags(new ArrayList<>());
        articleDTO.getTags().addAll(archiveMapper.getByArticleId(id, "post_tag"));

        System.out.println("查询文章，ID: " + id);
        return articleDTO;
    }
    
    @Transactional
    public void add(ArticleDTO articleDTO) {
        try {
            Article article = articleDTO.getArticle();
            articleMapper.add(article);

            archiveRelationshipMapper.add(articleDTO.getCategory().getArchiveId(), article.getArticleId());
            System.out.println("添加归档记录: " + articleDTO.getCategory());

            for (Archive tag:articleDTO.getTags()) {
                archiveRelationshipMapper.add(tag.getArchiveId(), article.getArticleId());
                System.out.println("添加归档记录: " + tag);
            }

            System.out.println("添加文章成功: " + article);
            System.out.println("分类: " + articleDTO.getCategory());
            System.out.println("标签: " + articleDTO.getTags());
        } catch (Exception e) {
            System.out.println("添加文章失败: " + e.getMessage());
            throw new RuntimeException(e); // 重新抛出
        }
    }
    
    @Transactional
    public void delete(Integer id) {
        try {
            articleMapper.delete(id);
            archiveRelationshipMapper.deleteByArticleId(id);

            System.out.println("删除文章成功, ID: " + id);
        } catch (Exception e) {
            System.out.println("删除文章失败: " + e.getMessage());
            throw new RuntimeException(e); // 重新抛出
        }
    }
    
    @Transactional
    public void update(ArticleDTO articleDTO) {
        try {
            articleMapper.update(articleDTO.getArticle());

            archiveRelationshipMapper.deleteByArticleId(articleDTO.getArticleId());
            archiveRelationshipMapper.add(articleDTO.getCategory().getArchiveId(), articleDTO.getArticleId());
            System.out.println("添加归档记录: " + articleDTO.getCategory());

            for (Archive tag:articleDTO.getTags()) {
                archiveRelationshipMapper.add(tag.getArchiveId(), articleDTO.getArticleId());
                System.out.println("添加归档记录: " + tag);
            }

            System.out.println("更新文章成功: " + articleDTO);
        } catch (Exception e) {
            System.out.println("更新文章失败: " + e.getMessage());
            throw new RuntimeException(e); // 重新抛出
        }
    }

    @Transactional
    public void increaseLikes(Integer number, Integer id) {
        try {
            articleMapper.increaseLikes(number, id);
        } catch (Exception e) {
            System.out.println("更新文章点赞量失败: " + e.getMessage());
            throw new RuntimeException(e); // 重新抛出
        }
    }

    @Transactional
    public void increaseViews(Integer number, Integer id) {
        try {
            articleMapper.increaseViews(number, id);
        } catch (Exception e) {
            System.out.println("更新文章浏览量失败: " + e.getMessage());
            throw new RuntimeException(e); // 重新抛出
        }
    }
}