package com.ingker.blogvue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ingker.blogvue.entity.Archive;
import com.ingker.blogvue.entity.Article;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@JsonPropertyOrder({"articleId", "title", "summary", "postDate", "updateDate", "status", "likes", "views", "comments", "category", "tags", "collection"})
public class ArticleDTO {
    private Integer articleId;

    @JsonProperty("title")
    private String articleTitle;

    @JsonProperty("content")
    private String articleContent;

    @JsonProperty("postDate")
    private Date postTime;

    @JsonProperty("updateDate")
    private Date updateTime;

    @JsonProperty("status")
    private String postStatus;

    @JsonProperty("likes")
    private Integer likesCount;

    @JsonProperty("views")
    private Integer viewsCount;

    @JsonProperty("comments")
    private Integer commentsCount;

    private Archive category;
    private List<Archive> tags;
    private Archive collection;

    public void setArticle(Article article) {
        this.articleId = article.getArticleId();
        this.articleTitle = article.getArticleTitle();
        this.articleContent = article.getArticleContent();
        this.postTime = article.getPostTime();
        this.updateTime = article.getUpdateTime();
        this.postStatus = article.getPostStatus();
        this.likesCount = article.getLikesCount();
        this.viewsCount = article.getViewsCount();
        this.commentsCount = article.getCommentsCount();
    }

    @JsonIgnore
    public Article getArticle() {
        Article article = new Article();
        article.setArticleId(this.articleId);
        article.setArticleTitle(this.articleTitle);
        article.setArticleContent(this.articleContent);
        article.setPostTime(this.postTime);
        article.setUpdateTime(this.updateTime);
        article.setPostStatus(this.postStatus);
        article.setLikesCount(this.likesCount);
        article.setViewsCount(this.viewsCount);
        article.setCommentsCount(this.commentsCount);
        return article;
    }
}
