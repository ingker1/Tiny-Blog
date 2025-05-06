<template>
    <Header/>
    <div class="container">
        <div v-if="articles.length > 0" name="articles" class="postlist">
            <div v-for="article in articles" :key="article.articleId" >
                <div v-if="article.postDate < new Date().toISOString()" class="article-panel">
                    <div class="article-item">
                        <h3 class="article-title">
                            <a :href="`http://localhost:8081/blog/${article.articleId}`" target="_blank">
                                {{ article.title }}
                            </a>
                        </h3>
                        <p class="article-sum">{{ article.summary.substring(0, 250) }}...</p>
                    </div>
                    <div class="meta">
                        <span v-if="article.category" class="category-tag">
                            <a :href="`/archive/category/${article.category.name}`">{{ article.category.name }}</a>
                        </span>
                        <span v-else class="category-tag"><a href="/archive/category/未分类">未分类</a></span>
                        <span class="meta-item">{{ formatDate(article.postDate) }}</span>
                        <span class="meta-item">{{ article.views }}浏览量</span>
                        <span class="meta-item">{{ article.likes }}人点赞</span>
                        <span class="meta-item">{{ article.comments }}条评论</span>
                    </div>
                </div>
            </div>
        </div>
        <!-- 如果没有文章 -->
        <p v-else>暂无文章</p>

        <!-- 分页导航 -->
        <div class="pagination">
            <button :disabled="currentPage === 1" @click="changePage(currentPage - 1)">←</button>

            <span v-for="page in visiblePages" :key="page">
                <button v-if="page === '...'">...</button>
                <button v-else
                        :class="{ active: page === currentPage }"
                        @click="changePage(page)">
                    {{ page }}
                </button>
            </span>

            <button :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">→</button>
        </div>
    </div>
    
    <Footer/>
</template>

<script setup>
    import { ref, onMounted, watch, computed } from 'vue';
    import { useRoute, useRouter } from 'vue-router'; // 导入useRouter来进行路由跳转
    import axios from 'axios';
    import Header from '@/components/Header.vue';
    import Footer from '@/components/Footer.vue';


    const articles = ref([]);       // 当前页文章
    const currentPage = ref(1);     // 当前页码
    const totalPages = ref(1);      // 总页数
    const pageSize = ref(5);       // 每页文章数量
    const router = useRouter(); 	// 路由管理器
    const route = useRoute();       // url路由

    // 格式化日期
    const formatDate = (dateStr) => {
        const date = new Date(dateStr);
        const year = date.getFullYear().toString().padStart(4, '0');
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        const result = `${year}年${month}月${day}日`;
        return result;
    };

    const loadArticles = async () => {
        if (route.params.id)
            currentPage.value =  route.params.id;
        try {
            const response1 = await axios.get('http://localhost:8080/articles', {
                params: {
                    page: currentPage.value,
                    limit: pageSize.value
                },
            });
            const data = response1.data;
            articles.value = data.content || [];
            totalPages.value = data.totalPages || 1;
            currentPage.value = data.currentPage;
        } catch (error) {
            console.error('加载文章错误:', error);
        }
    };

    const changePage = (page) => {
        if (page === 1) {
            currentPage.value = page;
            router.push('/');
        } else if (page > 0 && page <= totalPages.value) {
            currentPage.value = page;
            // 更新路由的 page 参数
            router.push(`/page/${currentPage.value}`);
        } else {
            alert('页码无效');
        }

        setTimeout(() => {
            loadArticles(); // 确保路由更新后再加载文章
        }, 0);
    };
    
    // 设置页面标题
    const updateTitle = () => {
        document.title = `清科谷体的博客 - 第 ${currentPage.value} 页`;
    };

    // 页面加载时设置标题
    onMounted(async() => {
        await loadArticles();
        await updateTitle();
    });

    // 监听 currentPage 的变化，动态更新标题
    watch(currentPage, updateTitle);

    // 计算显示的页码数组
    const visiblePages = computed(() => {
        const total = totalPages.value
        const current = currentPage.value
        const pages = []

        // 保证最多显示5个数字页码
        let start = current - 2
        let end = current + 2

        // 调整 start 和 end，确保页码在合法范围且数量为5
        if (start < 1) {
            end += (1 - start)
            start = 1
        }
        if (end > total) {
            start -= (end - total)
            end = total
        }

        start = Math.max(1, start)
        end = Math.min(total, end)

        // 最终只取5个页码
        const range = []
        for (let i = start; i <= end; i++) {
            range.push(i)
        }
        while (range.length > 5) {
            if (current - start > end - current) {
                range.pop() // 删除末尾
            } else {
                range.shift() // 删除开头
            }
        }

        // 判断是否加省略号
        if (range[0] > 1) {
            pages.push(1)
            if (range[0] > 2) {
                pages.push('...')
            }
        }

        pages.push(...range)

        if (range[range.length - 1] < total) {
            if (range[range.length - 1] < total - 1) {
                pages.push('...')
            }
            pages.push(total)
        }

        return pages
    })
</script>

<style scoped>
.article-item {
    border: 1px solid #ccc;
    margin: 10px 0;
    padding: 10px;
/*     border-radius: 5px; */
}

.pagination {
    display: flex;
    justify-content: center;
    align-items: center;
}

button {
    margin: 0 0px;
    padding: 10px 16px;
    border: 0px solid #ccc;
    background-color: #fff;
    cursor: pointer;
    font-family: 'Microsoft Yahei';
}

button.active {
    background-color: #007bff;
    color: white;
    border-color: #007bff;
}

button:disabled {
    background-color: #eee;
    cursor: not-allowed;
}

.postlist {
    position: relative;
    transform: translateX(-50%);
    left: 50%;
    width: 60vw;
}

.container {
    background:#f5f5f5; 
    padding: 20px;
    margin: -8px;
}

h2 {
    text-align: center;
}

.meta {
    font-size: 16px;
    border: 1px solid #ccc;
    margin-top: -11px;
    padding: 10px;
    font-family: 'Microsoft Yahei';
}

.meta-item {
    margin-right: 10px;
}

.article-panel a {
    color: inherit;          /* 继承父元素的颜色 */
    text-decoration: none;   /* 去掉下划线 */
}

.article-panel .article-item a:hover {
    color: #0097ee;
}

.article-panel {
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    margin: 20px;
}

.category-tag {
    background-color: #00a2ff;
    padding: 5px;
    margin-right: 5px;
    color: #fff;
}

.article-panel .article-item .article-title {
    font-family: 'Microsoft Yahei';
    font-weight: 400;
    font-size: 21px;
    margin: 10px 0px;
}

.article-panel .article-item .article-sum {
    font-family: 'Microsoft Yahei';
    font-weight: 350;
}

</style>
