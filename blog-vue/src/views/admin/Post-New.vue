<template>
    <div style="display: flex; flex-direction: column; min-height: 100vh;">

        <div style="flex: 1; display: flex; margin-left: 50px;">
            <!-- 使用 QuickEdit 组件，并传递数据 -->
            <QuickEdit
            :categories="categories"
            v-model:status="status"
            v-model:time="time"
            v-model:category="category"
            v-model:tags="tags"
            />

            <div style="width: min-content;">
                <h3>文章标题：</h3>
                <input v-model="articletitle" placeholder="添加标题" class="articletitle" />
                <div style="margin: 10px 0px; display: flex; gap: 10px;">
                    <button @click="showHTML">显示HTML</button>
                    <button @click="showMarkdown">显示Markdown</button>
                    <button v-if="!route.params.id" id="addButton" @click="addButton">发布文章</button>
                </div>
                <Editor ref="editorRef" @editor-ready="onEditorReady" />
                <p v-if="message" :class="{ success: isSuccess, error: !isSuccess }"> {{ message }}</p>
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

    const clear = () => {
        category.value = categories.value[0];
        tags.value = [];
        articletitle.value = '';
        editorRef.value.setValue('');
    }

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

    const addButton = async () => {
        if (!articletitle.value.trim()) {
            message.value = "标题不能为空！";
            isSuccess.value = false;
            return;
        }
        isSubmitting.value = true;

        try {

            // 等待标签保存完成
            await saveTags();

            // 文章提交请求
            const response = await axios.post('http://localhost:8080/admin/articles', {
                title: articletitle.value,
                content: editorRef.value.getValue(),
                postDate: new Date(),
                updateDate: new Date(),
                status: status.value,
                likes: 0,
                views: 0,
                comments: 0,
                category: category.value,
                tags: tags.value
            });

            if (response.status === 201 && response.data) {
                isSuccess.value = true;
                message.value = "提交成功";
                clear(); // 清空结果
            } else {
                throw new Error("提交失败，请稍后重试。");
            }

        } catch (error) {
            console.error("提交文章错误", error);
            isSuccess.value = false;
            message.value = "提交失败";
        } finally {
            isSubmitting.value = false;
        }
    };

    const showHTML = () => {
        message.value = editorRef.value.getHTML();
    }

    const showMarkdown = () => {
        message.value = editorRef.value.getValue();
    }

    onMounted(() => {
        // 监听编辑器准备完成的事件
        document.addEventListener('editor-ready', onEditorReady);

        loadArchives();
        // time.value = formatDate(Date());
        time.value = Date();

        // 在页面销毁时移除监听器，防止内存泄漏
        onUnmounted(() => {
            document.removeEventListener('editor-ready', onEditorReady);
        });
    });

    // 当编辑器准备好时的处理逻辑
    const onEditorReady = () => {
        console.log("Editor is ready. Loading article...");
    };

    const loadArchives = () => {
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
            category.value = categories.value[0];
        }).catch(error => {
            console.error('请求分类存档列表失败:', error);
        });
    }

</script>


<style>
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
</style>