package com.ingker.blogvue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
@JsonPropertyOrder({"articleId", "title", "postDate"})
public class ArticleCollection {
    private Integer articleId;

    @JsonProperty("title")
    private String articleTitle;

    @JsonProperty("postDate")
    private Date postTime;
}
