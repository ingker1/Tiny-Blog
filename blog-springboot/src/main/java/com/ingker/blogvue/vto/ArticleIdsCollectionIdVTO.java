package com.ingker.blogvue.vto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class ArticleIdsCollectionIdVTO {
    private List<Integer> articleIds;

    private Integer collectionId;
    public ArticleIdsCollectionIdVTO(List<Integer> articleIds, Integer collectionId) {
        this.articleIds = articleIds;
        this.collectionId = collectionId;
    }
}
