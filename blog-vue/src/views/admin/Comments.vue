<template>
    <h1>评论管理</h1>
    <div class="container">
        <table>
            <thead>
                <tr>
                <th>用户</th>
                <th>评论内容</th>
                <th>关联文章</th>
                <th>提交日期</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="comment in comments" :key="comment.publishDate">
                <td>{{ comment.author }}</td>
                <td>
                    <div class="in-td">
                        {{ comment.content }}<br/>
                        <button v-if="comment.status != 'read'" @click="readMark(comment.commentId)">标为已读</button>
                        <button class="delete" @click="deleteComments(comment.commentId)">删除</button>
                    </div>     
                </td>
                <td>
                    <div class="in-td">
                        <a :href="`/admin/edit=${ comment.articleId }`">{{ comment.articleTitle }}</a><br/>
                        <a :href="`/blog/${ comment.articleId }`">查看文章</a>
                    </div>    
                </td>
                <td>{{ formatDate(comment.publishDate) }}</td>
                </tr>
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
    import { useRouter } from 'vue-router'; 
    import axios from 'axios';

    const comments = ref([]);
    const sortOrder = ref('desc');
    const currentPage = ref(1);
    const totalPages = ref(0);
    const RecordPerPage = ref(15);

    const router = useRouter(); 	    // 路由管理器
    const pageInput = ref(1);           // 用于输入跳转页码的变量

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

    // 分页跳转
    const changePage = (page) => {
        if (page > 0 && page <= totalPages.value) {
            currentPage.value = page;       
            // 更新路由的 page 参数
            router.push({ path: '/admin/comments', query: { page } });
            loadComments();
        } else {
            alert('页码无效');
        }
    };

    const loadComments = async() => {
        try {
            const response = await axios.get('http://localhost:8080/admin/comments', {
                params: {
                    page: currentPage.value,
                    limit: RecordPerPage.value,
                    sort: '',
                    order: sortOrder.value,
                }
            });
            comments.value = response.data.content;  
            totalPages.value = response.data.totalPages;          
        } catch(error) {
            console.error('请求评论列表失败:', error);
        }
    }

    const deleteComments = (commentId) => {
        const confirmed = window.confirm('确定要删除这条评论吗？');
        if (confirmed) {
            axios.delete(`http://localhost:8080/admin/comments/${commentId}`)
            .then(() => {
                alert('删除成功');
                loadComments(); 
            })
            .catch(error => {
                alert('删除失败');
                console.error(error);
            });
        } else {
            console.log('删除操作已取消'); // 使用 alert 提示操作已取消
        }	
    }

    const readMark = async(commentId) => {
        await axios.put(`http://localhost:8080/admin/comments/read/${commentId}`)
        .catch(error => {
            console.error(error);
        });

        await loadComments();
    }

    onMounted(() => {
        document.title = '清科谷体的博客 - 评论管理';
        loadComments();
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

table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 20px;
}

table th, table td{
    padding: 10px;
    text-align: left;
    border-bottom: 1px solid #ddd;
    margin-top: 10px;
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

.delete {
    background-color: red;
}

.delete:hover {
    background-color: #b30027;
}

.in-td {
    line-height: 1.5em;
}

</style>
  