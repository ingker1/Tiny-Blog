<template>
    <Header/>
    <div class="post">
        <div name="article">
            <h1>{{article.title}}</h1>
            <div class="post-meta">{{ article.postDate }}</div>
            <div v-if="collections.length > 0" class="collection-container">
                <div  class="collection-title">
                    合集：{{ collections[0]?.collection }}
                    <!-- 切换展开/收缩按钮 -->
                    <button @click="toggleExpand" class="toggle-btn">
                        {{ isExpanded ? '收起 ▲' : '展开 ▼' }}
                    </button>
                </div>
                    
                <!-- 文章列表（使用 v-show 控制显示） -->
                <div class="collection-list" v-show="isExpanded">
                    <div v-for="(article, index) in collections" :key="article.articleId" class="collection-bar" @click="getCollectionArticle(article.articleId)">
                        <div>{{ index + 1 }}. {{ article.title }}</div>
                        <div class="collection-date">{{ formatDate(article.postDate) }}</div>
                    </div>
                </div>
            </div>
            <div v-html="article.content" class="post-content"></div>       
        </div>
        <div class="post-foot">
            <div>最后修改：{{ article.updateDate }}</div>
            <button @click="like" class="like-buttion">👍 点赞</button>
        </div>
        <Comement style="margin-top: 20px;"/>
    </div>
    <Footer/>
</template>


<script setup>
    import { ref, onMounted } from 'vue';
    import { useRoute, useRouter } from 'vue-router'; // 确保导入 useRoute
    import Header from '@/components/Header.vue'
    import Footer from '@/components/Footer.vue'
    import Comement from '@/components/Comment.vue'
    import axios from 'axios'; // 你可以用 fetch 或其他库

    const article = ref({});     // 文章内容
    const route = useRoute();    // url路由
    const id = route.params.id;  // 获取URL中的id
    const collections = ref({});
    const router = useRouter(); 	// 路由管理器
    const isExpanded = ref(true);

    const toggleExpand = () => {
        isExpanded.value = !isExpanded.value;
    };
    // 格式化日期
    const formatDate = (dateStr) => {
        const date = new Date(dateStr);
        const year = date.getFullYear().toString().padStart(4, '0');
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        const result = `${year}年${month}月${day}日`;
        return result;
    };

    const like = () => {
        axios.put(`http://localhost:8080/articles/likes/${id}`, JSON.stringify(article.value.likes), {
            headers: {
                'Content-Type': 'application/json'
            }
        })
    };

    const loadArticle = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/articles/${id}`); // 发起请求
            const data = response.data;

            // 处理返回的数据并赋值
            article.value = {
                title: data.title, // 显示文章标题
                content: data.content, // 显示文章内容（包含 HTML）
                postDate: formatDate(data.postDate),
                updateDate: formatDate(data.updateDate),
                views: data.views,
                likes: data.likes
            };
        } catch (error) {
            console.error('文章加载失败：', error); // 如果出错，打印错误
        }

        await axios.put(`http://localhost:8080/articles/views/${id}`, JSON.stringify(article.value.views), {
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .catch((error) => {
            console.error('文章浏览量增加失败：', error);
        });
    }

    const getCollections = async() => {
        axios.get('http://localhost:8080/collections', {
            params: {
                articleId: route.params.id
            }
        }).then(response => {
            collections.value = response.data;
        })
        .catch(error => {
            console.error('获取博客合集失败:', error);
        });
    }

    const getCollectionArticle = (id) => {
        router.push(`/blog/${id}`);
        loadArticle();
    }

    onMounted(async () => {
        await loadArticle(); 
        await getCollections();
        updateTitle();
    });

    // 设置页面标题
    const updateTitle = () => {
        document.title = `${article.value.title}`;
    };

</script>


<style scoped>
.post {
    position: relative;
    transform: translateX(-50%);
    left: 50%;
    width: 70vw;
}

.like-buttion {
    width: 70px;
    height: 35px;
    background-color: color;
    border: 1px solid red;
    margin-top: 10px;
}

.like-buttion:hover {
    background-color: #fb5235;
    color: white;
}

h1 {
    text-align: center;
}

.post-meta {
    text-align: center;
}

.post-content {
    margin-bottom: 20px;
}

.post-foot {
    text-align: right;
}

.collection-container {
    width: 100%;
    max-width: 800px;
    border: 1px solid #ccc;
    border-radius: 8px;
    padding: 8px;
    margin: 15px;
    background-color: #f9f9f9;
    transform: translateX(25%);
}

.collection-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 8px;
    text-align: center;
}

.toggle-btn {
    display: inline;
    color: #007bff;
    cursor: pointer;
    text-align: center;
    margin-bottom: 8px;
    font-size: 16px;
    user-select: none;
    transition: color 0.2s;
}

.toggle-btn:hover {
    color: #0056b3;
}

.collection-list {
    max-height: 200px;
    overflow-y: auto;
    border-top: 1px solid #eee;
}

.collection-bar {
    padding: 8px;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: background-color 0.2s;
}

.collection-bar:hover {
    background-color: #f0f0f0;
}

.collection-date {
    color: #999;
    font-size: 14px;
}
</style>