package com.ingker.blogvue.service;

import com.ingker.blogvue.dto.ArchiveArticle;
import com.ingker.blogvue.dto.ArchiveListDTO;
import com.ingker.blogvue.dto.ArticleRecord;
import com.ingker.blogvue.entity.Archive;
import com.ingker.blogvue.mapper.ArchiveMapper;
import com.ingker.blogvue.mapper.ArchiveRelationshipMapper;
import com.ingker.blogvue.util.Page;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ArchiveService {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveService.class);

    @Autowired
    ArchiveMapper archiveMapper;

    @Autowired
    ArchiveRelationshipMapper archiveRelationshipMapper;

    private int getValidPage(Integer page) {
        return (page == null || page < 1) ? 1 : page;
    }

    private int getValidSize(Integer size) {
        return (size == null || size < 1) ? 10 : size;
    }

    /**
     * 排序字段值映射到具体的数据库列名，防止错误输入和SQL注入
     * */
    private String getValidSortField(String sortField) {
        // 定义前端字段和数据库列名的映射关系
        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("id", "archive_id");
        fieldMapping.put("name", "archive_name");
        fieldMapping.put("count", "count(arr.archive_id)");
        // 返回数据库列名，默认为 id
        return fieldMapping.getOrDefault(sortField, "archive_name");
    }

    private String getValidOrder(String order) {
        if (!Arrays.asList("asc", "desc", "ASC", "DESC").contains(order)) {
            order = "asc"; // 默认排序顺序
        }
        return order;
    }

    @Transactional
    public Page<ArchiveListDTO> listWithCount(Integer size, Integer page, String sort, String order, String taxonomy) {
        validateNonEmptyString(taxonomy, "分类方法不能为空");
        size = getValidSize(size);
        page = getValidPage(page);
        String dbSortField = getValidSortField(sort);
        order = getValidOrder(order);
        Integer offset = size * (page - 1); // page 从 1 开始

        List<ArchiveListDTO> archiveListDTOSs = archiveMapper.getByTaxonomyWithCount(taxonomy, dbSortField, order, size, offset);
        int total = archiveMapper.countByTaxonomy(taxonomy);

        logger.info("查询完成，分类方法: {}, 记录总数: {}", taxonomy, total);
        return new Page<>(archiveListDTOSs, total, page, size);
    }

    @Transactional
    public Page<Archive> list(Integer size, Integer page, String sort, String order,String taxonomy) {
        validateNonEmptyString(taxonomy, "分类方法不能为空");
        size = getValidSize(size);
        page = getValidPage(page);
        String dbSortField = getValidSortField(sort);
        order = getValidOrder(order);
        Integer offset = size * (page - 1); // page 从 1 开始

        List<Archive> archives = archiveMapper.getByTaxonomy(taxonomy, dbSortField, order, size, offset);
        int total = archiveMapper.countByTaxonomy(taxonomy);

        logger.info("查询完成，分类方法: {}, 记录总数: {}", taxonomy, total);
        return new Page<>(archives, total, page, size);
    }

    @Transactional
    public void add(Archive archive) {
        validateArchive(archive);
        archiveMapper.add(archive);
        logger.info("新增归档成功: {}", archive);
    }

    @Transactional
    public void delete(Integer id) {
        validateId(id, "归档 ID ");
        archiveMapper.delete(id);
        archiveRelationshipMapper.deleteByArchiveId(id);
        logger.info("删除归档成功，ID: {}", id);
    }

    @Transactional
    public void update(Archive archive) {
        validateArchive(archive);
        if (archiveMapper.getByArchiveId(archive.getArchiveId()) == null) {
            throw new IllegalArgumentException("归档 ID 不存在，无法更新");
        }
        archiveMapper.update(archive);
        logger.info("归档更新成功: {}", archive);
    }

    public Map<Integer, Map<Integer, List<ArchiveArticle>>> getArchiveByDate() {
        logger.info("查询时间线归档");

        List<ArticleRecord> records = archiveMapper.getArchiveDates();

        // 使用 TreeMap 保证年份按降序排序
        Map<Integer, Map<Integer, List<ArchiveArticle>>> archiveMap = new TreeMap<>(Collections.reverseOrder());

        for (ArticleRecord record : records) {
            int year = record.getYear();
            int month = record.getMonth();

            // 获取年份节点，如果不存在则创建
            archiveMap
                    .computeIfAbsent(year, y -> new TreeMap<>()) // TreeMap 按月份升序
                    .computeIfAbsent(month, m -> new ArrayList<>()) // 创建文章列表
                    .add(new ArchiveArticle(record.getId(), record.getTitle()));
        }

        return archiveMap;
    }

    @Transactional
    public List<ArchiveListDTO> getArchiveByCategory(String sort, String order) {
        logger.info("查询分类归档");
        String sortFiled = getValidSortField(sort);
        String orderFiled = getValidOrder(order);
        return archiveMapper.getAllByTaxonomyWithCount("category", sortFiled, orderFiled);
    }

    @Transactional
    public List<ArchiveListDTO> getArchiveByTag(String sort, String order) {
        logger.info("查询标签归档");
        String sortFiled = getValidSortField(sort);
        String orderFiled = getValidOrder(order);
        return archiveMapper.getAllByTaxonomyWithCount("post_tag", sortFiled, orderFiled);
    }

    private void validateArchive(Archive archive) {
        if (archive == null) {
            throw new IllegalArgumentException("归档信息不能为空");
        }

        validateNonEmptyString(archive.getArchiveName(), "归档名不能为空");
        validateNonEmptyString(archive.getTaxonomy(), "分类方法不能为空");
    }

    /**
     * 校验 ID 是否有效
     */
    private void validateId(Integer id, String fieldName) {
        if (id == null || id < 1) {
            throw new IllegalArgumentException(fieldName + " 无效");
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
}
