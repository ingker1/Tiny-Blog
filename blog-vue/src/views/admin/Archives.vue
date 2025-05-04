<template>
    <h2>归档管理</h2>

    <div class="filters" style="margin-bottom: 10px;">
        <label for="taxonomy">归档方法：</label>
        <select v-model="taxonomy" id="taxonomy" @change="applyFilters">
            <option value="category">分类</option>
            <option value="post_tag">标签</option>
        </select>
        
        <button @click="ShowDialog()">新增归档</button>
    </div>

    <div class="container">
        <table>
            <thead>
                <tr>
                <th @click="reveseSortByField('name')">归档名<span :class="getArrowClass('name')"></span></th>
                <th @click="reveseSortByField('count')">总数<span :class="getArrowClass('count')"></span></th>
                </tr>
            </thead>
            <tbody>
                <template v-for="(archive, index) in archives" :key="archive.name">
                    <tr v-if="editingArticleIndex !== index"
                        :class="index % 2 === 0 ? 'odd-row':'even-row'">
                    <td>{{ archive.name }}
                        <br/>
                        <button @click="ShowDialog(Object.fromEntries(Object.entries(archive).slice(0, 3)))">编辑</button>
                        <button class="delete" @click="deleteArchive(archive.archiveId)">删除</button>
                    </td>
                    <td>{{ archive.count }}</td>
                    </tr>
                </template>
            </tbody>
        </table>

        <!-- 编辑对话框 -->
        <div v-if="isShowDialog" class="dialog-overlay">
            <div class="dialog">
                <h3 v-if="isAddNew">添加归档</h3>
                <h3 v-else>编辑归档</h3>
                <label>归档名：</label>
                <input v-model="currentArchive.name" type="text" class="input"/>
                <div v-if="isAddNew">
                    <label for="taxonomy">归档方法：</label>
                    <select v-model="taxonomy" id="taxonomy" @change="applyFilters">
                        <option value="category">分类</option>
                        <option value="post_tag">标签</option>
                    </select>
                </div>
                
                <p>{{ errorMessage }}</p>

                <div class="dialog-buttons">
                    <button v-if="isAddNew" @click="addNewArchive">添加</button>
                    <button v-if="!isAddNew" @click="editArchive">保存</button>
                    <button class="delete" @click="isShowDialog = false">取消</button>
                </div>
            </div>
        </div>

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
    import { useRouter } from 'vue-router'; // 导入useRouter来进行路由跳转
    import axios from 'axios';


    const archives = ref([]);           // 存储分类数据
    const currentPage = ref(1);         // 当前页
    const totalPages = ref(1);          // 总页数
    const RecordPerPage = ref(15);    	// 每页记录数
    const pageInput = ref(1);           // 用于输入跳转页码的变量
    const sortField = ref('name');      // 排序字段
	const sortOrder = ref('asc');		// 排序顺序
    const taxonomy = ref('category');   // 分类方法
    const router = useRouter(); 	    // 路由管理器
    const isShowDialog = ref(false);    // 控制对话框显示
    const isAddNew = ref(false);
    const currentArchive = ref({ archiveId: 0, archiveName: '', taxonomy: ''}); // 记录当前编辑的分类
    const errorMessage = ref('');


    const loadArchives = () => {
        axios.get('http://localhost:8080/admin/archives/count', {
            params: {
                page: currentPage.value,
                limit: RecordPerPage.value,
                sort: sortField.value,
                order: sortOrder.value,
                taxonomy: taxonomy.value
            }
        })
        .then(response => {
            archives.value = response.data.content;
            totalPages.value = response.data.totalPages;
        })
        .catch(error => {
            console.error('请求分类存档列表失败:', error);
        });
    }

    // 应用筛选条件，重置为第一页并重新获取数据
    const applyFilters = () => {
		currentPage.value = 1;
		loadArchives();
    };
    
    // 分页跳转
	const changePage = (page) => {
        if (page > 0 && page <= totalPages.value) {
            currentPage.value = page;       
			// 更新路由的 page 参数
			router.push({ path: '/admin/archives', query: { page } });
			loadArchives();
        } else {
			alert('页码无效');
		}
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
        loadArchives();
    }

    const ShowDialog = (archive) => {
        isShowDialog.value = true;
        console.log(archive);
        if (archive != null) {
            // 编辑分类
            isAddNew.value = false;
            currentArchive.value = { ...archive }; // 深拷贝数据，防止原数据变动
            errorMessage.value = ''; // 清除之前的错误信息
        } else {
            // 添加新分类
            isAddNew.value = true;
            currentArchive.value.name = '';
            errorMessage.value = ''; // 清除之前的错误信息
        }
    }

    const addNewArchive = () => {
        axios.post('http://localhost:8080/admin/archives',{
            name: currentArchive.value.name,
            taxonomy: taxonomy.value
        })
        .then(() => {
            alert('添加成功');
            loadArchives(); // 刷新文章列表
            isShowDialog.value = false; // 关闭对话框
        })
        .catch(error => {
            errorMessage.value = '添加失败，请重试'; 
            console.log(currentArchive);
            console.error(error);
        });
    }

    const editArchive = () => {
        axios.put('http://localhost:8080/admin/archives',{
            archiveId: currentArchive.value.archiveId,
            name: currentArchive.value.name,
            taxonomy: currentArchive.value.taxonomy
        })
        .then(() => {
            alert('修改成功');
            loadArchives(); // 刷新文章列表
            isShowDialog.value = false; // 关闭对话框
        })
        .catch(error => {
            errorMessage.value = '修改失败，请重试'; 
            console.error(error);
        });
    }

    const deleteArchive = (archiveId) => {
		const confirmed = window.confirm('确定要删除这个分类吗（不会删除文章，会清除与文章的对应关系）？');
		if (confirmed) {
			axios.delete(`http://localhost:8080/admin/archives/${archiveId}`)
        	.then(() => {
				alert('删除成功');
				loadArchives(); // 刷新文章列表
        	})
			.catch(error => {
				alert('删除失败');
				console.error(error);
			});
		} else {
			console.log('删除操作已取消'); // 使用 alert 提示操作已取消
		}	
    }

    onMounted(() => {
        document.title = '清科谷体的博客 - 分类管理';
        loadArchives();
    });
</script>

<style scoped>
.container {
    /* width: 80%; */
    margin: 20px auto;
    padding: 20px;
    background-color: #fff;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    font-family: 'M';
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

h1 {
    text-align: center;
    margin-bottom: 20px;
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

tbody tr button {
    visibility: hidden;
}

tbody tr:hover button {
    visibility: visible;
} 

button {
    padding: 5px 10px;
	margin-right: 10px;
    background-color: #007bff;
    color: white;
    border: none;
    cursor: pointer;
}

.input {
    margin-bottom: 10px;
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

.arrow-up::before {
    content: '⯅'; /* 向上的箭头 */
}

.arrow-down::before {
    content: '⯆'; /* 向下的箭头 */
}

/* 对话框样式 */
.dialog-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
}

.dialog {
    background: white;
    padding: 20px;
    border-radius: 5px;
    width: 300px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
    text-align: center;
}

.dialog-buttons {
    margin-top: 10px;
}

.dialog input {
    width: 100%;
    padding: 5px;
    margin-top: 5px;
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

span {
    margin-right: 10px;
}
</style>
  