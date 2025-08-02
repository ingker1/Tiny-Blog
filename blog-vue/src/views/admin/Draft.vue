<template>
    <h2>草稿箱</h2>
    <div class="container">
        <!-- 文章列表 -->
        <table>
            <thead>
                <tr>
                <th style="width: 40%;" @click="reveseSortByField('title')">文章标题<span :class="getArrowClass('title')"></span></th>
                <th style="width: 10%;">分类</th>
                <th style="width: 20%;">标签</th>
                <th style="width: 5%;">状态</th>
                <th style="width: 15%;" @click="reveseSortByField('postDate')">上次修改时间<span :class="getArrowClass('postDate')"></span></th>
                <th style="width: 10%">操作</th>
                </tr>
            </thead>

            <tbody>
                <!-- 使用一个数组来动态插入编辑行 -->
                <template v-for="(article, index) in articles" :key="article.postDate">
                    <tr v-if="editingArticleIndex !== index"
                        :class="index % 2 === 0 ? 'odd-row':'even-row'">
                        <td>{{ article.title }}
                            <p>摘要：{{ article.summary.substring(0, 100) }}</p>
                        </td>
                        <td>{{ article.category ? article.category.name : '' }}</td>
                        <td>{{ article.tags ? article.tags.map(tag => tag.name).join('、') : '' }}</td>
                        <td>{{ formatPostStatus(article.status) }}</td>
                        <td>{{ formatDate(article.updateDate) }}</td>
                        <td>
                            <div style="display: flex;">
                                <button @click="editArticle(article)">编辑</button><br>
                                <button @click="recycleArticle(article)" class="delete">废纸篓</button>
                            </div>
                        </td>
                    </tr>
                </template>
            </tbody>
        </table>

        <!-- 分页组件 -->
        <div class="paging-bar">
            <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1">上一页</button>
            <span> {{ currentPage }} / {{ totalPages }} 页</span>
            <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages">下一页</button>

            <!-- 跳转到指定页码的输入框 -->
            <input type="number" v-model="pageInput" :min="1" :max="totalPages" style="width:40px; font-size: 18px;" @keyup.enter="changePage(pageInput)"/>
            &nbsp;&nbsp;<span>页</span>
            <button @click="changePage(pageInput)">跳转</button>

            <!-- 每页条数选择 -->
            <select v-model="RecordPerPage" @change="changePage(1)">
                <option :value="10">10</option>
                <option :value="15">15</option>
                <option :value="25">25</option>
                <option :value="50">50</option>
                <option :value="100">100</option>
            </select>
            &nbsp;&nbsp;<span>条/页</span>
        </div>
    </div>
</template>

<script setup>
    import { ref, onMounted } from 'vue';
    import { useRoute, useRouter } from 'vue-router'; // 导入useRouter来进行路由跳转
    import axios from 'axios';

    import '@/assets/style.css';

    // 定义响应式变量
    const articles = ref([]);           // 用于存储文章列表
    const currentPage = ref(1);         // 当前页
    const totalPages = ref(1);          // 总页数
    
    const sortField = ref('postDate');  // 排序字段
    const sortOrder = ref('desc');		// 排序顺序
    const RecordPerPage = ref(15);    	// 每页记录数
    const searchKeywords = ref('')      // 搜索关键词
    const router = useRouter(); 	    // 路由管理器
    const route = useRoute();			// 当前的路由信息
    const pageInput = ref(1);           // 用于输入跳转页码的变量
    const queryParams = ref([]);        // 创建一个查询参数对象


    const categories = ref([]);
    const category = ref(null);        	// 文章分类
    const allCategory = ref({ archiveId: 0, taxonomy: "category", name: ""});

    const editingArticleIndex = ref(null); // 当前正在编辑的文章的索引

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

    function formatPostStatus(Field) {
        // 定义前端字段和数据库列名的映射关系
        const fieldMapping = new Map();
        fieldMapping.set('publish', '已发布');
        fieldMapping.set('draft', '草稿');
        fieldMapping.set('trash', '垃圾');
        fieldMapping.set('private', '私密');
        return fieldMapping.get(Field) || "未知";
    }

    // 获取文章数据的函数
    const loadArticles = async () => {
        console.log('重新加载页面');
        articles.value = []; // 切换页面时清空旧数据
        
        // 获取文章列表
        try {
            const response = await axios.get('http://localhost:8080/admin/articles', {
                params: {
                    page: currentPage.value,
                    limit: RecordPerPage.value,
                    sort: sortField.value,
                    order: sortOrder.value,
                    category: category.value.name,
                    status: 'draft',
                    keywords: searchKeywords.value
                }
            });
            articles.value = response.data.content;  // 赋值文章数据
            totalPages.value = response.data.totalPages;
        } catch (error) {
            console.error('请求文章列表失败:', error);
            return; // 如果获取文章失败，后续的请求就不执行
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
            category.value = allCategory.value;
        }).catch(error => {
            console.error('请求分类存档列表失败:', error);
        });
    };

    const recycleArticle = async (article) => {
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
            updateDate: article.updateDate,
            status: 'trash',
            likes: article.likes,
            views: article.views,
            category: article.category,
            tags: article.tags,
            collection: article.collection
        })
        .then (()=> {
            alert('已移至废纸篓');
            loadArticles(); // 刷新文章列表
        })
        .catch (error => {
            console.error("文章回收出错", error);
        });
    };

    // 返回当前排序列的箭头类
    const getArrowClass = (field) => {
        if (sortField.value === field) {
            return sortOrder.value === 'asc' ? 'arrow-up' : 'arrow-down';
        }
        return '';
    };

    // 更换排序字段，并以相反顺序排序
    const reveseSortByField = (field) => {
        sortField.value = field;
        sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc';
        //applyFilters();
        loadArticles();
    }

    // 分页跳转
    const changePage = (page) => {
        if (page > 0 && page <= totalPages.value) {
            currentPage.value = page;       
            // 更新路由的 page 参数
            queryParams.value.page = page;
            router.push({ path: '/admin/drafts', query: queryParams.value });
            loadArticles(); 
        } else {
            alert('页码无效');
        }
    };

    // 点击编辑按钮后，跳转到编辑页面，并传递文章ID作为参数
    const editArticle = (article) => {
        router.push({ name: 'EditWithID', params: { id: article.articleId } }); // 跳转到编辑页面，传递文章ID
    };

    // 页面加载时请求文章数据，并设置页面标题
    onMounted(async () => {
        document.title = '清科谷体的博客 - 草稿箱';
        const pageParam = Number(route.query.page) || 1;
        currentPage.value = pageParam;
        pageInput.value = pageParam;
        await loadCategory();
        await loadArticles();
    });

</script>


<style scoped>
/* 这里是样式代码 */
.container {
    /* width: 80%; */
    margin: 20px auto;
    padding: 20px;
    background-color: #fff;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    font-family: 'M';
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

.arrow-up::before {
    content: '⯅'; /* 向上的箭头 */
}

.arrow-down::before {
    content: '⯆'; /* 向下的箭头 */
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

/* .quickbutton button {
    display: none;

}

tbody tr:hover button {
    display:inline;
} */


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

.even-row {
    background-color: #f9f9f9;
}

.odd-row {
    background-color: #f3f3f3;
}

.paging-bar {
    align-items: center;
    display: flex; 
    justify-content: flex-end;
    font-size: 16px;
    font-family: "M";
}

</style>
