<template>
    <h1>文章列表</h1>

    <!-- 筛选和排序 -->
    <div class="filters">
        <label for="status">发布状态:</label>
        <select v-model="status" id="status" @change="applyFilters()">
            <option value="">全部</option>
            <option value="publish">已发布</option>
            <option value="draft">草稿</option>
            <option value="trash">垃圾</option>
            <option value="private">私密</option>
        </select>

        <label for="category">分类:</label>
        <select v-model="category" id="category" @change="applyFilters()">
            <option value="">全部</option>
            <option v-for="categoryItem in categories" :key="categoryItem.archiveId" :value="categoryItem.name">
            {{ categoryItem.name }}
            </option>
        </select>

        <label for="sort">排序:</label>
        <select v-model="option" id="option" @change="applyFilters()">    
            <option v-for="optionItem in options" :key="optionItem.id" :value="optionItem">
                {{ optionItem.label }}
            </option>
        </select>
        
        <!-- 搜索框和图标并行 -->
        <div style="display: inline-flex; align-items: center; border: 1px solid #ccc; margin-bottom: 10px; padding: 5px 10px; border-radius: 4px;">
            <input v-model="searchKeywords" @keyup.enter="applyFilters()" placeholder="按照关键词搜索（空格分开）" style="padding: 5px, 5px; margin-right: 8px; border: none; outline: none;">
            <div @click="clearSearch" style="width: 24px; height: 24px; display: flex; align-items: center; justify-content: center;">
                <svg viewBox="0 0 24 24" width="1.2em" height="1.2em" style="align-items: center;">
                    <path fill="currentColor" d="M12 22C6.477 22 2 17.523 2 12S6.477 2 12 2s10 4.477 10 10s-4.477 10-10 10Zm0-2a8 8 0 1 0 0-16a8 8 0 0 0 0 16Zm0-9.414l2.828-2.829l1.415 1.415L13.414 12l2.829 2.828l-1.415 1.415L12 13.414l-2.828 2.829l-1.415-1.415L10.586 12L7.757 9.172l1.415-1.415L12 10.586Z"></path>
                </svg>
            </div>
        </div>
    </div>
    
    <div class="container">
        <!-- 文章列表 -->
        <table>
            <thead>
                <tr>
                <th style="width: 40%;">文章标题</th>
                <th style="width: 10%;">分类</th>
                <th style="width: 20%;">标签</th>
                <th style="width: 5%;">状态</th>
                <th style="width: 15%;">时间</th>
                <th style="width: 10%">数据</th>
                </tr>
            </thead>

            <tbody>
                <template v-for="(article, index) in articles" :key="article.updateDate">
                    <tr v-if="editingArticleIndex !== index">
                        <td>{{ article.title }}
                            <p>摘要：{{ article.summary.substring(0, 100) }}</p>
                            <div class="quickbutton">
                                <button @click="editArticle(article)">编辑</button>
                                <button @click="quickEdit(index)">快速编辑</button>
                                <button @click="viewBlog(article.articleId)">查看文章</button>
                                <button v-if="article.status !== 'trash'" @click="recycleArticle(article)" class="delete">移到废纸篓</button>
                            </div>
                        </td>
                        <td>{{ article.category ? article.category.name : '' }}</td>
                        <td>{{ article.tags ? article.tags.map(tag => tag.name).join('、') : '' }}</td>
                        <td>{{ formatPostStatus(article) }}</td>
                        <td>
                            {{ getDisplayTime(article).label }}<br>
                            {{ getDisplayTime(article).date }}<br>
                        </td>
                        <td>
                            <span class="meta-item">{{ article.views }} 浏览</span> <br>
                            <span class="meta-item">{{ article.likes }} 点赞</span> <br>
                            <span class="meta-item">{{ article.comments }} 评论</span> <br>
                        </td>
                    </tr>

                    <!-- 动态插入编辑表单 -->
                    <tr v-if="editingArticleIndex === index">
                        <td>
                            文章标题：<input v-model="article.title" type="text" style="width: 400px;">
                            <QuickEdit
                            :categories="categories"
                            v-model:status="article.status"
                            v-model:time="article.postDate"
                            v-model:category="article.category"
                            v-model:tags="article.tags"
                            />
                            <button @click="updateButton(article)">修改</button>
                            <button @click="editingArticleIndex = null">取消</button>
                        </td>
                    </tr>
                    
                </template>
            </tbody>
        </table>

        <!-- 分页组件 -->
        <div style="margin: 10px; text-align: right;">
            <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1">上一页</button>
            <span> {{ currentPage }} / {{ totalPages }} 页</span>
            <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages">下一页</button>

            <!-- 跳转到指定页码的输入框 -->
            <input type="number" v-model="pageInput" :min="1" :max="totalPages" style="width:40px;" />
            <span>页</span>
            <button @click="changePage(pageInput)">跳转</button>

            <!-- 每页条数选择 -->
            <select v-model="RecordPerPage" @change="changePage(1)">
                <option :value="10">10</option>
                <option :value="15">15</option>
                <option :value="25">25</option>
                <option :value="50">50</option>
                <option :value="100">100</option>
            </select>
            <span>条/页</span>
        </div>
    </div>
</template>

<script setup>
    import { ref, onMounted } from 'vue';
    import { useRoute, useRouter } from 'vue-router'; // 导入useRouter来进行路由跳转
    import QuickEdit from '@/components/QuickEdit.vue';
    import axios from 'axios';

    import '@/assets/style.css';


    // 定义响应式变量
    const articles = ref([]);           // 用于存储文章列表
    const currentPage = ref(1);         // 当前页
    const totalPages = ref(1);          // 总页数
    const status = ref('publish');      // 发布状态
    const RecordPerPage = ref(15);    	// 每页记录数
    const searchKeywords = ref('')      // 搜索关键词
    const router = useRouter(); 	    // 路由管理器
    const route = useRoute();			// 当前的路由信息
    const pageInput = ref(1);           // 用于输入跳转页码的变量
    const queryParams = ref([]);        // 创建一个查询参数对象
    const categories = ref([]);         // 文章分类下拉框列表
    const category = ref(null);        	// 文章分类

    const editingArticleIndex = ref(null); // 当前正在编辑的文章的索引

    // 定义排序选项
    const options  = {
        'postDate-desc': { sortField: 'postDate', label: '最近发布', sortOrder: 'desc' },
        'postDate-asc': { sortField: 'postDate', label: '最早发布', sortOrder: 'asc' },
        'updateDate-desc': { sortField: 'updateDate', label: '最近修改', sortOrder: 'desc' },
        'updateDate-asc': { sortField: 'updateDate', label: '最早修改', sortOrder: 'asc' },
        'views-desc': { sortField: 'views', label: '浏览量', sortOrder: 'desc' },
        'likes-desc': { sortField: 'likes', label: '点赞数', sortOrder: 'desc' },
        'comments-desc': { sortField: 'comments', label: '评论数', sortOrder: 'desc' }
    };
    const option = ref(options['postDate-desc']);

    // 计算每个 article 的显示内容
    const getDisplayTime = (article) => {
        const isPublish = article.status === 'publish';
        const isScheduled = formatDate(new Date()) < article.postDate;

        if (option.value.sortField === 'updateDate') {
            return { label: '最后修改', date: formatDate(article.updateDate) };
        }
        
        if (isPublish) {
            return isScheduled
                ? { label: '定时发布', date: formatDate(article.postDate) }
                : { label: '已发布', date: formatDate(article.postDate) };
        } else {
            return { label: '最后修改', date: formatDate(article.updateDate) };
        } 
    };

    // 格式化日期
    const formatDate = (dateStr) => {
        const date = new Date(dateStr);
        const year = date.getFullYear().toString().padStart(4, '0');
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        // 获取小时并转换为12小时制
        let hour = date.getHours();
        const period = hour < 12 ? '上午' : '下午';
        hour = (hour % 12 || 12).toString().padStart(2, '0'); // 0点和12点特殊处理为12
        const minute = date.getMinutes().toString().padStart(2, '0');
        const result = `${year}-${month}-${day} ${period}${hour}:${minute} `;
        return result;
    };

    function formatPostStatus(article) {
        // 定义前端字段和数据库列名的映射关系
        const fieldMapping = new Map();
        if (formatDate(new Date()) < article.postDate) {
            fieldMapping.set('publish', '定时发布');
        } else {
            fieldMapping.set('publish', '已发布');
        }
        fieldMapping.set('draft', '草稿');
        fieldMapping.set('trash', '垃圾');
        fieldMapping.set('private', '私密');
        return fieldMapping.get(article.status) || "未知";
    }

    const viewBlog = (articleId) => {
        router.push(`/blog/${ articleId }`);
    }

    const quickEdit = (index) => {
        editingArticleIndex.value = index;
    }

    // 获取文章数据的函数
    const loadArticles = async () => {
        console.log('重新加载页面');
        articles.value = []; // 切换页面时清空旧数据
        
        if (route.query.search) {
            searchKeywords.value = route.query.search;
        }
        if (route.query.status) {
            status.value = route.query.status;
        }
        if (route.query.category) {
            category.value = route.query.category;
        }
        if (route.query.sort) {
            let orderString = `${route.query.sort}` + '-' + `${route.query.order}`;
            option.value = options[orderString];
        }
        // 获取文章列表
        try {
            const response = await axios.get('http://localhost:8080/admin/articles', {
                params: {
                    page: currentPage.value,
                    limit: RecordPerPage.value,
                    sort: option.value.sortField,
                    order: option.value.sortOrder,
                    category: category.value,
                    status: status.value,
                    keywords: searchKeywords.value
                },
            });
            articles.value = response.data.content;  // 赋值文章数据
            totalPages.value = response.data.totalPages;
        } catch (error) {
            console.error('请求文章列表失败:', error);
            return;
        }
    };

    const loadCategory = async() => {
        await axios.get('http://localhost:8080/admin/archives', {
            params: {
                page: 1,
                limit: 1000,
                sort: 'id',
                order: 'asc',
                taxonomy: 'category'
            }
        }).then(response => {
            categories.value = response.data.content;
            category.value = '';
        }).catch(error => {
            console.error('请求分类存档列表失败:', error);
        });
    };

    const applyFilters = () => {
        currentPage.value = 1;   
        queryParams.value = {};  // 清空查询参数，防止残存数据

        // 根据条件在路由中添加相应的查询字段
        if (status.value && status.value !== '') {
            queryParams.value.status = status.value;
        }
        if (category.value && category.value !== '') {
            queryParams.value.category = category.value;
        }
        if (option.value) {
            queryParams.value.sort = option.value.sortField;
            queryParams.value.order = option.value.sortOrder;
        }
        if (searchKeywords.value) {
            let keywords = searchKeywords.value.split(/\s+/).filter(keyword => keyword);
            queryParams.value.search = keywords.join(' ');  // 将关键词数组拼接成字符串，以空格分隔
        }

        // 只有当查询参数真正变化时才更新路由
        if (JSON.stringify(queryParams) !== JSON.stringify(router.currentRoute.value.query)) {
            router.replace({
                path: '/admin/posts',
                query: queryParams.value
            });
        }

        setTimeout(() => {
            loadArticles(); // 确保路由更新后再加载文章
        }, 0);
    };

    // 分页跳转
    const changePage = (page) => {
        if (page > 0 && page <= totalPages.value) {
            currentPage.value = page;       
            // 更新路由的 page 参数
            queryParams.value.page = page;
            router.push({ path: '/admin/posts', query: queryParams.value });
            loadArticles(); 
        } else {
            alert('页码无效');
        }
    };

    // 点击编辑按钮后，跳转到编辑页面，并传递文章ID作为参数
    const editArticle = (article) => {
        router.push({ name: 'EditWithID', params: { id: article.articleId } }); // 跳转到编辑页面，传递文章ID
    };

    const clearSearch = () => {
        searchKeywords.value = ''; // 清空搜索框内容
        applyFilters();

        setTimeout(() => {
            loadArticles(); // 确保路由更新后再加载文章
        }, 0);
    };

    const updateButton = async (article) => {
        let articleContent = '';
        await axios.get(`http://localhost:8080/articles/${article.articleId}`)
        .then(response => {
            articleContent = response.data.content;
        });

        await axios.put('http://localhost:8080/admin/articles', {
            articleId: article.articleId,
            title: article.title,
            content: articleContent,
            postDate: article.postDate,
            updateDate: new Date(),
            status: article.status,
            likes: article.likes,
            views: article.views,
            comments: article.comments,
            category: article.category,
            tags: article.tags
        })
        .catch (error => {
            console.error("修改文章错误", error);
        });

        editingArticleIndex.value = null;  // 关闭快捷编辑框
        await loadArticles();  // 刷新文章数据
    };

    const recycleArticle = async (article) => {
        try {
            let articleContent = '';
            await axios.get(`http://localhost:8080/articles/${article.articleId}`)
            .then(response => {
                articleContent = response.data.content;
            })
            .catch(error => {
                console.error(error);
            });

            await axios.put('http://localhost:8080/admin/articles', {
                articleId: article.articleId,
                title: article.title,
                content: articleContent,
                postDate: article.postDate,
                updateDate: article.updateDate,
                status: 'trash',
                likes: article.likes,
                views: article.views,
                category: article.category,
                tags: article.tags
            })
            .catch(error => {
                console.error(error);
            });

            await axios.put(`http://localhost:8080/admin/comments/trash/${article.articleId}`)
            .catch(error => {
                console.error(error);
            });

            alert('已移至废纸篓');
            loadArticles(); // 刷新文章列表
        } catch (error) {
            console.error("文章回收出错", error);
        }
        
    };

    // 页面加载时请求文章数据，并设置页面标题
    onMounted(async () => {
        document.title = '清科谷体的博客 - 文章列表';
        const pageParam = Number(route.query.page) || 1;
        currentPage.value = pageParam;
        pageInput.value = pageParam;
        await loadCategory();
        await loadArticles();
    }); 

</script>


<style scoped>
.container {
    /* width: 80%; */
    margin: 20px auto;
    padding: 20px;
    background-color: #fff;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h1 {
    text-align: center;
    margin-bottom: 20px;
}

.filters {
    margin-bottom: 20px;
}

.filters label {
    margin-right: 10px;
}

.filters select {
    padding: 5px;
    margin-right: 20px;
}

button {
    padding: 5px 10px;
    margin-right: 10px;
    background-color: #007bff;
    color: white;
    border: none;
    cursor: pointer;
}

span {
    margin-right: 10px;
}

button:hover {
    background-color: #0056b3;
}

.delete {
    background-color: red;
}

.delete:hover {
    background-color: #b30027;
}

table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 20px;
}

table th, table td {
    padding: 10px;
    text-align: left;
    border-bottom: 1px solid #ddd;
}

th {
    cursor: pointer; /* 鼠标悬停时显示手型 */
}

th span {
    margin-left: 5px;
    font-size: 18px;
}

input {
width: calc(200px);
}

.pagination {
    text-align: center;
}

.pagination button {
    padding: 5px 15px;
    background-color: #007bff;
    color: white;
    border: none;
    cursor: pointer;
    margin: 0 10px;
}

.pagination button:hover {
    background-color: #0056b3;
}

.quickbutton button {
    visibility: hidden;
}

tbody tr:hover button {
    visibility: visible;
} 

.quickEdit {
    display: flex;
    flex-direction: row;
    gap: 20px;
}

::v-deep .quickEdit .editItem {
    display: flex;
    flex-direction: column; /* 让 h3 独占一行 */
    align-items: flex-start; /* 左对齐 */
    gap: 5px;
}

::v-deep .quickEdit .editItem .content {
    display: flex;
    align-items: center;
    gap: 10px;
    white-space: nowrap; /* 避免换行 */
}

</style>
