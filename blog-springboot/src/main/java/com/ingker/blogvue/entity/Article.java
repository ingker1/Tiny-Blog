package com.ingker.blogvue.entity;

import java.util.Date;

public class Article {
    private Integer articleId;
    private String articleTitle;
    private String articleContent;
    private Date postTime;
    private Date updateTime;
    private String postStatus;
    private Integer likesCount;
    private Integer viewsCount;
    private Integer commentsCount;

    public void setViewsCount(Integer viewsCount) {
        this.viewsCount = viewsCount;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public void setPostStatus(String postStatus) {
        this.postStatus = postStatus;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setArticleContent(String articleContent) {
        this.articleContent = articleContent;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public Date getPostTime() {
        return postTime;
    }

    public String getPostStatus() {
        return postStatus;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public Integer getViewsCount() {
        return viewsCount;
    }

    public String getArticleContent() {
        return articleContent;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    @Override
    public String toString() {
        return "Article{" +
                "articleId=" + articleId +
                ", articleTitle='" + articleTitle + '\'' +
                ", articleContent='" + articleContent + '\'' +
                ", postTime=" + postTime +
                ", updateTime=" + updateTime +
                ", postStatus='" + postStatus + '\'' +
                ", likesCount=" + likesCount +
                ", viewsCount=" + viewsCount +
                ", commentsCount=" + commentsCount +
                '}';
    }
}
