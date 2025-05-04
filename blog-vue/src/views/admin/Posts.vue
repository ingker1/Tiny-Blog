<template>
    <h2>文章列表</h2>

    <!-- 筛选和排序 -->
    <div class="filters">
        <CustomSelect
        v-model="status"
        :options="statusOptions"
        placeholder="Select an option"
        :searchable="true"
        @change="applyFilters()"
        />

        <CustomSelect
        v-model="category"
        :options="categorySelectItmes"
        placeholder="Select an option"
        :searchable="true"
        @change="applyFilters()"
        />

        <CustomSelect
        v-model="option"
        :options="options"
        placeholder="Select an option"
        :searchable="true"
        @change="applyFilters()"
        />

        <!-- 搜索框和图标并行 -->
        <div class="search-bar">
            <input class="search-input" v-model="searchKeywords" @keyup.enter="applyFilters()" placeholder="按照关键词搜索（空格分开）">
            <div @click="clearSearch" style="width: 24px; height: 24px; display: flex; align-items: center; justify-content: center;">
                <svg viewBox="0 0 24 24" width="1.2em" height="1.2em" style="align-items: center;">
                    <path fill="currentColor" d="M12 22C6.477 22 2 17.523 2 12S6.477 2 12 2s10 4.477 10 10s-4.477 10-10 10Zm0-2a8 8 0 1 0 0-16a8 8 0 0 0 0 16Zm0-9.414l2.828-2.829l1.415 1.415L13.414 12l2.829 2.828l-1.415 1.415L12 13.414l-2.828 2.829l-1.415-1.415L10.586 12L7.757 9.172l1.415-1.415L12 10.586Z"></path>
                </svg>
            </div>
        </div>

        <div style="gap: 10px !important; display: flex; height: 40px;">
            <button  class="delete" v-show="selectedArticles.length > 0" v-on:click="batchDelete">移到废纸篓</button>
            <button v-show="selectedArticles.length > 0" v-on:click="batchEdit">编辑文章</button>
        </div>

        <!-- 分页组件 -->
        <div class="paging-bar" style="margin-left: auto;">
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
    
    <div class="container">
        <!-- 文章列表 -->
        <table>
            <thead>
                <tr>
                <th><input type="checkbox" class="1" v-model="selectAll" @change="toggleSelectAll"></th>
                <th style="width: 40%;">文章标题</th>
                <th style="width: 10%;">分类</th>
                <th style="width: 20%;">标签</th>
                <th style="width: 7%;">状态</th>
                <th style="width: 15%;">时间</th>
                <th style="width: 10%">数据</th>
                </tr>
            </thead>

            <tbody>
                <template v-for="(article, index) in articles" :key="article.articleId">
                    <tr v-if="index === 0 && batchShow" class="even-row">
                        <td></td>
                        <td>
                            批量编辑栏
                            <div style="margin-bottom: 10px;" v-for="a in selectedArticles" :key="a.articleId">
                                <span class="delete-icon" @click="removeArticle(a.articleId)"></span>
                                {{ a.title }}
                            </div>
                            <button @click="batchSave(selectedArticles)">更新文章</button>
                            <button @click="batchShow = false">取消编辑</button>
                        </td>
                        <td>
                            <select v-model="categoryMultipleBar">
                            <option 
                                v-for="item in categorySelectItmes.slice(1)" 
                                :key="item.value" 
                                :value="item"
                            >
                                {{ item.label }}
                            </option>
                        </select>
                        </td>
                        <td style="vertical-align: top;">
                            <div style=" display: flex;flex-direction: column;justify-content: flex-start;">
                                <div class="content" style="flex-wrap: nowrap; display: flex;">
                                    <input v-model="tagText" type="text">
                                    <button @click="addTags" style="width: 50px;height: 37px; margin-left: 10px;">添加</button>
                                </div>  
                            </div>

                            <div style=" display: flex;flex-direction: column;justify-content: flex-start;">
                                <ul class="content" style="padding: 0;">
                                    <li class="text-with-icon" v-for="(tag, index) in tagsMultipleBar" :key="index">{{ tag.name }} 
                                        <span class="delete-icon" @click="removeTag(index)"></span>
                                    </li>      
                                </ul>
                            </div>
                        </td>
                        <td colspan="3" style="vertical-align: top;">
                            设置文章合集
                        </td>
                        <td></td>
                    </tr>
                    
                    <tr v-if="editingArticleIndex !== index"
                        :class="index % 2 === 0 ? 'odd-row':'even-row'">
                        <td>
                            <input type="checkbox"
                            :value="article"
                            v-model="selectedArticles"
                            @change="checkSelectAll"
                            >
                        </td>
                        <td>{{ article.title }}
                            <p>摘要：{{ article.summary.substring(0, 100) }}</p>
                            <div class="quickbutton">
                                <button @click="editArticle(article)">编辑</button>
                                <button @click="quickEdit(index)">快速编辑</button>
                                <button @click="viewBlog(article.articleId)">查看文章</button>
                                <button v-if="article.status !== 'trash'" @click="recycleArticle([article])" class="delete">移到废纸篓</button>
                            </div>
                        </td>
                        <td>{{ article.category ? article.category.name : '' }}</td>
                        <td>{{ article.tags ? article.tags.map(tag => tag.name).join('、') : '' }}</td>
                        <td>{{ formatPostStatus(article) }}</td>
                        <td>
                            {{ getDisplayTime(article).name }}<br>
                            {{ getDisplayTime(article).date }}<br>
                        </td>
                        <td>
                            <span class="meta-item">{{ article.views }} 浏览</span> <br>
                            <span class="meta-item">{{ article.likes }} 点赞</span> <br>
                            <span class="meta-item">{{ article.comments }} 评论</span> <br>
                        </td>
                    </tr>

                    <!-- 动态插入编辑表单 -->
                    <tr v-if="editingArticleIndex === index"
                        :class="index % 2 === 0 ? 'odd-row':'even-row'">
                        <td></td>
                        <td :colspan="6">
                            文章标题：<input v-model="article.title" type="text" style="width: 400px; height: 32px; font-size: 18px;">
                            <QuickEdit
                            :categories="categories"
                            v-model:status="article.status"
                            v-model:time="article.postDate"
                            v-model:category="article.category"
                            v-model:tags="article.tags"
                            />
                            <button @click="updateButton(article)">修改</button>
                            <button @click="cancelQuickEdit()">取消</button>
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
    import { ref, onMounted, watch } from 'vue';
    import { useRoute, useRouter } from 'vue-router'; // 导入useRouter来进行路由跳转
    import axios from 'axios';
    import QuickEdit from '@/components/QuickEdit.vue';
    import CustomSelect from '@/components/CustomSelect.vue';
    import '@/assets/style.css';


    // 定义响应式变量
    const articles = ref([]);           // 用于存储文章列表
    const currentPage = ref(1);         // 当前页
    const totalPages = ref(1);          // 总页数
    const status = ref('');             // 发布状态
    const RecordPerPage = ref(15);    	// 每页记录数
    const searchKeywords = ref('')      // 搜索关键词
    const router = useRouter(); 	    // 路由管理器
    const route = useRoute();			// 当前的路由信息
    const pageInput = ref(1);           // 用于输入跳转页码的变量
    const queryParams = ref([]);        // 创建一个查询参数对象
    const categories = ref([]);         // 文章分类下拉框列表
    const category = ref(null);        	// 文章分类

    const editingArticleIndex = ref(null); // 当前正在编辑的文章的索引

    const categorySelectItmes = ref([]);         // 文章分类下拉框列表

    // 定义排序选项
    const options  = [
        { value: 'postDate-desc', label: '最近发布' , sortField: 'postDate', sortOrder: 'desc'},
        { value: 'postDate-asc', label: '最早发布', sortField: 'postDate', sortOrder: 'asc'},
        { value: 'updateDate-desc', label: '最近修改', sortField: 'updateDate', sortOrder: 'desc'},
        { value: 'updateDate-asc', label: '最早修改', sortField: 'updateDate', sortOrder: 'asc'},
        { value: 'views-desc', label: '浏览量', sortField: 'views', sortOrder: 'desc'},
        { value: 'likes-desc', label: '点赞数', sortField: 'likes', sortOrder: 'desc'},
        { value: 'comments-desc', label: '评论数', sortField: 'comments', sortOrder: 'desc'}
    ];

    const option = ref('postDate-desc');

    const targetOption = ref(options[0]);

    const statusOptions = ref([
      { value: '', label: '全部状态' },
      { value: 'publish', label: '已发布' },
      { value: 'draft', label: '草稿' },
      { value: 'trash', label: '垃圾' },
      { value: 'private', label: '私密' }
    ]);

    // 计算每个 article 的显示内容
    const getDisplayTime = (article) => {
        const isPublish = article.status === 'publish';
        const isScheduled = formatDate(new Date()) < formatDate(article.postDate);

        if (targetOption.value && targetOption.value.sortField === 'updateDate') {
            return { name: '最后修改', date: formatDate(article.updateDate) };
        }

        if (isPublish) {
            return isScheduled
                ? { name: '定时发布', date: formatDate(article.postDate) }
                : { name: '已发布', date: formatDate(article.postDate) };
        } else {
            return { name: '最后修改', date: formatDate(article.updateDate) };
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
        if (formatDate(new Date()) < formatDate(article.postDate)) {
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
            targetOption.value = options.find(item => item.value === orderString);
        }

        // 获取文章列表
        try {
            const response = await axios.get('http://localhost:8080/admin/articles', {
                params: {
                    page: currentPage.value,
                    limit: RecordPerPage.value,
                    sort: targetOption.value.sortField,
                    order: targetOption.value.sortOrder,
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
            categorySelectItmes.value.push({'value':'', label:'所有分类'});
            const newItems = response.data.content.map(item => ({
                value: item.name,
                label: item.name,
                id: item.archiveId
            }));
            categorySelectItmes.value.push(...newItems);

            category.value = '';
            categories.value = response.data.content;
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
            targetOption.value = options.find(item => item.value === option.value);
            queryParams.value.sort = targetOption.value.sortField;
            queryParams.value.order = targetOption.value.sortOrder;
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

        // 保存文章标签
        let temptags = article.tags;
        article.tags = [];
        for (const tag of temptags) {
            try {
                const response = await axios.post('http://localhost:8080/admin/archives', {
                    name: tag.name,
                    taxonomy: 'post_tag'
                });
                article.tags.push(response.data);
                console.log(`Tag "${tag}" saved successfully.`);
            } catch (error) {
                console.error(`保存标签 "${tag}" 失败:`, error);
            }
        }

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

    const recycleArticle = async (articles) => {
        try {
            for (const article of articles) {
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
                    comments: article.comments,
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
            }

            alert('已移至废纸篓');
            loadArticles(); // 刷新文章列表
        } catch (error) {
            console.error("文章回收出错", error);
        } 
    };

    function cancelQuickEdit() {
        editingArticleIndex.value = null;
        loadArticles();
    }

    // 页面加载时请求文章数据，并设置页面标题
    onMounted(async () => {
        document.title = '清科谷体的博客 - 文章列表';
        const pageParam = Number(route.query.page) || 1;
        currentPage.value = pageParam;
        pageInput.value = pageParam;
        await loadCategory();
        await loadArticles();
    }); 


    // 勾选的文章数组
    const selectedArticles = ref([]);

    // 全选状态
    const selectAll = ref(false);

    const batchShow = ref(false);

    // 全选/取消全选方法
    function toggleSelectAll() {
        if (selectAll.value) {
            selectedArticles.value = [...articles.value];
        } else {
            selectedArticles.value = [];
        }
        console.log(selectedArticles.value);
    }

    // 单选时，判断是否该勾上全选框
    function checkSelectAll() {
        selectAll.value = selectedArticles.value.length === articles.value.length;
        console.log(selectedArticles.value);
    }

    // 批量操作示例方法
    function batchDelete() {
        recycleArticle(selectedArticles.value);
    }

    function removeArticle(articleId) {
        const idx = selectedArticles.value.findIndex(item => item.articleId === articleId);
        if (idx !== -1) {
            selectedArticles.value.splice(idx, 1);
        }
    }

    function batchEdit() {
        batchShow.value = true;
    }

    // 如果 articles 列表变动，重置全选状态
    watch(articles, () => {
        selectedArticles.value = [];
        selectAll.value = false;
    });

    const categoryMultipleBar = ref(null)  // 多选编辑的分类
    const tagText = ref('');             // 多选编辑的标签输入框的文本
    const tagsMultipleBar = ref([]);     // 多选编辑的标签项

    function addTags() {
        if (tagText.value) {
            const newTag = {
                archiveId: null,
                taxonomy: 'post_tag',
                name: tagText.value
            };

            tagsMultipleBar.value.push(newTag);
            tagText.value = '';
        }
        console.log(tagsMultipleBar.value);
    }

    function removeTag(index) {
        tagsMultipleBar.value.splice(index, 1); // 删除指定位置的标签
        console.log(tagsMultipleBar.value);
    }

    async function batchSave(articles) {
        try {
            for (const article of articles) {
                let articleContent = '';
                await axios.get(`http://localhost:8080/articles/${article.articleId}`)
                .then(response => {
                    articleContent = response.data.content;
                });

                let categoryItem = {
                    archiveId: categoryMultipleBar.value.id,
                    name: categoryMultipleBar.value.value,
                    taxonomy: 'category'
                };

                article.tags = [];
                for (const tag of tagsMultipleBar.value) {
                    try {
                        const response = await axios.post('http://localhost:8080/admin/archives', {
                            name: tag.name,
                            taxonomy: 'post_tag'
                        });
                        article.tags.push(response.data);
                        console.log(`Tag "${tag}" saved successfully.`);
                    } catch (error) {
                        console.error(`保存标签 "${tag}" 失败:`, error);
                    }
                }

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
                    category: categoryItem,
                    tags: article.tags
                })
                .catch (error => {
                    console.error("修改文章错误", error);
                });
            }

            batchShow.value = false;  // 关闭快捷编辑框
            await loadArticles();  // 刷新文章数据
            tagsMultipleBar.value = [];
            categoryMultipleBar.value = null;
        } catch (error) {
            console.error("文章批量编辑出错", error);
        }
    }

</script>


<style scoped>
.container {
    /* width: 80%; */
    margin: 20px auto;
    padding: 20px;
    background-color: #f9f9f9;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    font-family: 'M';
}

h1 {
    text-align: center;
    margin-bottom: 20px;
}

.filters {
    margin-bottom: 20px;
    display: flex; 
    flex-direction: row; 
    align-items: center; 
    gap: 30px;
}

.filters .search-bar {
    display: inline-flex;
    border: 1px solid #ccc; 
    padding: 9px 10px; 
    border-radius: 4px;
    background-color: #fff;
}

button {
    padding: 5px 10px;
    margin-right: 10px;
    background-color: #007bff;
    color: white;
    border: none;
    cursor: pointer;
    border-radius: 4px;
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

.search-input {
    padding: 5px, 5px; 
    margin-right: 8px; 
    border: none; 
    outline: none;
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

.even-row {
    background-color: #f9f9f9;
}

.odd-row {
    background-color: #f3f3f3;
}

select {
    padding: 10px 16px;
    border: 1px solid #e0e0e0;
    border-radius: 10px; /* 超圆润，像 pill 一样 */
    background-color: #f9f9f9;
    font-size: 14px;
    color: #333;
    appearance: none;
    background-image: url("data:image/svg+xml,%3Csvg width='14' height='8' viewBox='0 0 14 8' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1l6 6 6-6' stroke='%23999' stroke-width='2' fill='none' fill-rule='evenodd'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 6px center;
    background-size: 14px 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    outline: none;
}

select:hover {
    background-color: #f0f0f0;
}

select:focus {
    border-color: #007bff;
    background-color: #fff;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='14' height='8' viewBox='0 0 14 8'%3E%3Cpath d='M1 7l6-6 6 6' stroke='%23999' stroke-width='2' fill='none'/%3E%3C/svg%3E");
}

.paging-bar {
    align-items: center;
    display: flex; 
    justify-content: flex-end;
    font-size: 16px;
    font-family: "M";
}

::v-deep .quickEdit .editItem .content input {
    height: 32px;
    font-size: 16px;
}

.delete-icon {
    width: 18px;
    height: 18px;
    background-color: #007BFF;
    border-radius: 50%;
    position: relative;
    cursor: pointer;
    transition: background-color 0.3s ease;
    display: inline-block;
    vertical-align: text-bottom;
    margin-left: 3px;
}

.delete-icon::before,
.delete-icon::after {
    content: '';
    position: absolute;
    background-color: white;
    width: 10px;
    height: 2px;
    top: 50%;
    left: 50%;
    transform-origin: center;
}

.delete-icon::before {
    transform: translate(-50%, -50%) rotate(45deg);
}

.delete-icon::after {
    transform: translate(-50%, -50%) rotate(-45deg);
}

.delete-icon:hover {
    background-color: #FF4136;
}
</style>
