package com.ingker.blogvue.controller;

import com.ingker.blogvue.dto.ArticleDTO;
import com.ingker.blogvue.dto.ArticleListDTO;
import com.ingker.blogvue.entity.Article;
import com.ingker.blogvue.mapper.ArticleMapper;
import com.ingker.blogvue.service.ArticleService;
import com.ingker.blogvue.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class ArticleController {
    @Autowired
    ArticleService articleService;

    @GetMapping("/articles")
    public ResponseEntity<Page<ArticleListDTO>> getPublish(
            @RequestParam(value="page", defaultValue = "0") Integer page,
            @RequestParam(value="limit", defaultValue = "10") Integer limit,
            @RequestParam(value="category") String category) {
        page = page<0?0:page;
        try {
            Page<ArticleListDTO> articlePage = articleService.getAll(page, limit, "postDate", "desc", category, "publish","");
            return ResponseEntity.ok(articlePage); // 返回 200 和查询结果
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // 返回 500 错误
        }
    }

    /**
     * 搜索功能接口
     * 以分页的形式返回包含所有关键词的文章
     * */
/*    @PostMapping("/articles/search")
    public ResponseEntity<Page<Article>> search(
            @RequestParam(value="page", defaultValue = "0") Integer page,
            @RequestParam(value="limit", defaultValue = "10") Integer limit,
            @RequestBody List<String> fields) {
        page = page<0?0:page;
        try {
            //Page<Article> articlePage = articleService.(page, limit, fields);
            //return ResponseEntity.ok(articlePage); // 返回 200 和查询结果
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // 返回 500 错误
        }
    }*/

    @GetMapping("/admin/articles")
    public ResponseEntity<Page<ArticleListDTO>> getAll(
            @RequestParam(value="page", defaultValue = "0") Integer page,
            @RequestParam(value="limit", defaultValue = "10") Integer limit,
            @RequestParam(value="sort") String sort,
            @RequestParam(value="order") String order,
            @RequestParam(value="category") String category,
            @RequestParam(value="status") String status,
            @RequestParam(value="keywords") String searchKeyword) {
        page = page<0?0:page;
        try {
            Page<ArticleListDTO> articlePage = articleService.getAll(page, limit, sort, order, category, status, searchKeyword);
            return ResponseEntity.ok(articlePage); // 返回 200 和查询结果
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // 返回 500 错误
        }
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleDTO> get(@PathVariable("id") Integer id) {
        try {
            ArticleDTO bean = articleService.get(id);
            return ResponseEntity.ok(bean); // 返回 200 和查询结果
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // 返回 500 错误
        }
    }

    @PostMapping("/admin/articles")
    public ResponseEntity<Object> add(@RequestBody ArticleDTO bean) {
        try {
            articleService.add(bean);
            return ResponseEntity.status(HttpStatus.CREATED).body(bean); // 返回 201 和创建的评论对象
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 返回 500 错误
                    .body("添加文章失败" + e.getMessage());
        }
    }

    @DeleteMapping("/admin/articles/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") Integer id) {
        try {
            articleService.delete(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 返回 204 无内容
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 返回 500 错误
                    .body("删除文章失败" + e.getMessage());
        }
    }

    @PutMapping("/admin/articles")
    public ResponseEntity<Object> update(@RequestBody ArticleDTO bean) {
        try {
            articleService.update(bean);
            return ResponseEntity.ok(bean); // 返回 200 和更新的评论对象
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 返回 500 错误
                    .body("更新文章失败" + e.getMessage());
        }
    }

    @PutMapping("/articles/likes/{id}")
    public ResponseEntity<Object> addLikes(@PathVariable("id") Integer id, @RequestBody Integer likes) {
        try {
            articleService.increaseField("likes_count", likes + 1, id);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("更新文章点赞量失败" + e.getMessage());
        }
    }

    @PutMapping("/articles/views/{id}")
    public ResponseEntity<Object> addViews(@PathVariable("id") Integer id, @RequestBody Integer views) {
        try {
            articleService.increaseField("views_count", views + 1, id);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("更新文章浏览量失败" + e.getMessage());
        }
    }
}
