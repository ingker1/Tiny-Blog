<template>
    <div class="editor-container">
        <div id="1" class="title-box">
            <div style="display: flex; flex-direction: row; align-items: center;">
                <h2>文章标题：</h2>
            </div>      
            <input v-model="articletitle" placeholder="添加标题" class="articletitle" />
        </div>

        <div class="content-row">
            <div id="2" class="quickedit-box">
                <QuickEdit
                    :categories="categories"
                    v-model:status="status"
                    v-model:time="time"
                    v-model:category="category"
                    v-model:tags="tags"
                />
            </div>

            <div id="3" class="editor-box">
                <p v-if="message" :class="{ success: isSuccess, error: !isSuccess }"> {{ message }}</p>
                <div class="editor-header">
                    <button class="article-button" style="background-color: #389ee7;">存为Markdown</button>
                    <button class="article-button" style="background-color: #e54c21;">存为HTML</button>
                    <button class="article-button" v-if="route.params.id" id="updateButton" @click="updateButton">修改文章</button>
                </div>
                <Editor ref="editorRef" @editor-ready="onEditorReady" />
            </div>
        </div>
    </div>

  </template>

<script setup>
    import { ref, onMounted, onUnmounted } from 'vue';
    import { useRoute } from 'vue-router';
    import Editor from '@/components/Editor.vue';
    import QuickEdit from '@/components/QuickEdit.vue';
    import axios from 'axios';

    const route = useRoute();
    const editorRef = ref(null);        // 用于获取 Editor 的实例
    const message = ref('');            // 提示信息
    const articletitle = ref('');       //文章标题
    const isSuccess = ref(true);        // 提示信息类型（成功/失败）
    const isSubmitting = ref(false);    // 提交状态
    const time = ref('');
    const status = ref('publish');

    const categories = ref([]);
    const category = ref(null);
    const tags = ref([]);

    const likes = ref(0);
    const views = ref(0);
    const comments = ref(0);

    const saveTags = async () => {
        let temptags = tags.value;
        tags.value = [];
        for (const tag of temptags) {
            try {
                const response = await axios.post('http://localhost:8080/admin/archives', {
                    name: tag.name,
                    taxonomy: 'post_tag'
                });
                tags.value.push(response.data);
                console.log(`Tag "${tag}" saved successfully.`);
            } catch (error) {
                console.error(`保存标签 "${tag}" 失败:`, error);
            }
        }
    }

    const updateButton = async () => {
        if (!articletitle.value.trim()) {
            message.value = "标题不能为空！";
            isSuccess.value = false;
            return;
        }
        isSubmitting.value = true;

        await saveTags();

        const id = route.params.id;
        console.log('tags:'+tags.value);
        try {
            const response = await axios.put('http://localhost:8080/admin/articles', {
                articleId: id,
                title: articletitle.value,
                content: editorRef.value.getValue(),
                postDate: new Date(time.value),
                updateDate: new Date(),
                status: status.value,
                likes: likes.value,
                views: views.value,
                comments: comments.value,
                category: category.value,
                tags: tags.value
            });

            if (response.status === 200 && response.data) {
                isSuccess.value = true;
                message.value = "修改成功";
            } else {
                throw new Error("修改失败，请稍后重试。");
            }

        } catch (error) {
            console.error("修改文章错误", error);
            isSuccess.value = false;
            message.value = "修改失败";
        } finally {
            isSubmitting.value = false;
        }
    };

    onMounted(() => {
        // 监听编辑器准备完成的事件
        document.addEventListener('editor-ready', onEditorReady);

        // 在页面销毁时移除监听器，防止内存泄漏
        onUnmounted(() => {
            document.removeEventListener('editor-ready', onEditorReady);
        });
    });

    // 当编辑器准备好时的处理逻辑
    const onEditorReady = () => {
        console.log("Editor is ready. Loading article...");
        loadCategory();
        loadArticle(); // 加载文章内容
    };

    const loadArticle = async () => {
        if (route.path === '/admin/edit') {
            document.title = '清科谷体的博客 - 新建文章';
            // time.value = formatDate(new Date());
            time.value = new Date();
            return;
        }

        const id = route.params.id;
        try {
            const response = await axios.get(`http://localhost:8080/articles/${id}`);
            articletitle.value = response.data.title;
            editorRef.value.setValue(response.data.content);
            status.value = response.data.status;
            //time.value = formatDate(response.data.postDate);
            time.value = response.data.postDate;
            category.value = response.data.category;
            tags.value = response.data.tags;
            
            likes.value = response.data.likes;
            views.value = response.data.views;
            comments.value = response.data.comments;

            document.title = '编辑文章 - ' + articletitle.value;
        } catch {
            console.error(`不能读取文章`);
        }
    };

    const loadCategory = () => {
        axios.get('http://localhost:8080/admin/archives', {
            params: {
                page: 1,
                limit: 1000,
                sort: 'id',
                order: 'asc',
                taxonomy: 'category'
            }
        }).then(response => {
            categories.value = response.data.content;
        }).catch(error => {
            console.error('请求分类存档列表失败:', error);
        });
    }

</script>


<style scoped>
textarea {
    width: 100%;
    margin-bottom: 10px;
    padding: 10px;
}

.articletitle {
    width: calc(100% - 20px);
    margin-bottom: 10px;
    padding: 5px 10px;
}

.success {
    color: green;
}

.error {
    color: red;
}

button {
    padding: 5px 10px;
	margin-right: 10px;
    background-color: #007bff;
    color: white;
    border: none;
    cursor: pointer;
}

.editor-container {
    margin: 0 50px;
    /* padding: 20px; */
    width: 88%;
}

.title-box {
    width: 100%;
    /* margin-bottom: 20px; */
}

.content-row {
    display: flex;
    gap: 20px;
}

.quickedit-box {
    flex: 3;
}

.editor-box {
    flex: 7;
}

.articletitle {
    width: calc(100% - 25px);
    height: 24px;
    font-size: 16px;
    margin-bottom: 10px;
    padding: 5px 10px;
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

button {
    padding: 5px 10px;
    margin-right: 10px;
    background-color: #007bff;
    color: white;
    border: none;
    cursor: pointer;
}

.article-button {
    padding: 8px 10px;
    margin: 5px 15px 5px 0px;
    background-color: #007bff;
    color: white;
    border: none;
    cursor: pointer;
    border-radius: 4px;
}

.editor-header {
    display: flex;
    flex-direction: row;
    align-items: center;
    margin-bottom: 10px;
}
</style>