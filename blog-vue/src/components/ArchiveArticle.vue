<template>
    <div>
        <Header/>
        <div name="articles" class="container">
            <div v-if="articles.length > 0" class="postlist">
                <div v-for="article in articles" :key="article.articleId" >
                    <div class="article-item">
                        <h3>
                            <a :href="`http://localhost:8081/blog/${article.articleId}`" target="_blank">
                                {{ article.title }}
                            </a>
                        </h3>
                        <p>{{ article.summary.substring(0, 200) }}...</p>
                    </div>
                    <div class="meta">
                        <span v-if="article.category">{{ article.category.name }} : </span>
                        <span class="meta-item">{{ formatDate(article.postDate) }}</span>
                        <span class="meta-item">{{ article.views }}浏览量</span>
                        <span class="meta-item">{{ article.likes }}人点赞</span>
                        <span class="meta-item">{{ article.comments }}条评论</span>
                    </div>
                </div>
            </div>
            <!-- 如果没有文章 -->
            <p v-else>暂无文章</p>

            <!-- 分页导航 -->
            <div class="pagination">
                <button :disabled="currentPage === 1" @click="changePage(currentPage - 1)">上一页</button>
                <span v-for="page in totalPages" :key="page">
                    <button :class="{ active: page === currentPage }" @click="changePage(page)">
                        {{ page }}
                    </button>
                </span>
                <button :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">下一页</button>
            </div>
        </div>
        <Footer/>
    </div>
</template>

<script setup>
    import { ref, onMounted, watch } from 'vue';
    import { useRoute, useRouter } from 'vue-router'; // 导入useRouter来进行路由跳转
    import axios from 'axios';
    import Header from '@/components/Header.vue';
    import Footer from '@/components/Footer.vue';


    const articles = ref([]);       // 当前页文章
    const currentPage = ref(1);     // 当前页码
    const totalPages = ref(1);      // 总页数
    const pageSize = ref(10);       // 每页文章数量
    const category = ref('');       // 文章分类
    const tag = ref('');            // 文章标签
    const router = useRouter(); 	// 路由管理器
    const route = useRoute();       // url路由
    const archiveMethod = ref('');

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
        if (route.params.page) {
            currentPage.value =  route.params.page;
        }
        
        
        if (archiveMethod.value === 'category') {
            category.value = route.params.name;
        }
        if (archiveMethod.value === 'tag') {
            tag.value = route.params.name;
        }
        try {
            const response1 = await axios.get('http://localhost:8080/articles', {
                params: {
                    page: currentPage.value,
                    limit: pageSize.value,
                    category: category.value,
                    tag: tag.value
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
            router.push(`/archive/${archiveMethod.value}/${route.params.name}`);
        } else if (page > 0 && page <= totalPages.value) {
            currentPage.value = page;
            // 更新路由的 page 参数
            router.push(`/archive/${archiveMethod.value}/${route.params.name}/page/${currentPage.value}`);
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
    onMounted(() => {
        archiveMethod.value = route.path.split('/')[2];
        loadArticles();
        updateTitle();
    });

    // 监听 currentPage 的变化，动态更新标题
    watch(currentPage, updateTitle);

</script>

<style scoped>
.container {
    background:#f5f5f5; 
    padding: 20px;
    margin: -8px;
}

.postlist {
    position: relative;
    transform: translateX(-50%);
    left: 50%;
    width: 60vw;
}

.article-item {
    border: 1px solid #ccc;
    margin: 10px 0;
    padding: 10px;
/*     border-radius: 5px; */
}

.pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    align-items: center;
}

button {
    margin: 0 5px;
    padding: 5px 10px;
    border: 1px solid #ccc;
    background-color: #fff;
    cursor: pointer;
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

.post {
    position: relative;
    transform: translateX(-50%);
    left: 50%;
    width: 70vw;
}

h2 {
    text-align: center;
}

.meta {
    font-size: 16px;
    border: 1px solid #ccc;
    margin-top: -11px;
    padding: 10px;
}

.meta-item {
    margin-right: 10px;
}
</style>
