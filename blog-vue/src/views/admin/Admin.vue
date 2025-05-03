<template>
    <div class="admin-container">
        <!-- 侧边栏 -->
        <div class="sidebar">
            <ul>
                <!-- 可折叠的“文章”栏 -->
                <li @click="toggleArticles" class="out-list">
                    <a>📚 文章管理 &nbsp;
                        <span class="arrow">{{ isExpanded ? '▼' : '▶' }}</span>
                    </a>
                </li>
                <ul v-show="isExpanded" class="nested-list">
                    <li :class="{ active: $route.path === '/admin/posts' }">
                        <router-link to="/admin/posts"> 文章列表</router-link>
                    </li>
                    <li :class="{ active: $route.path === '/admin/post-new' }">
                        <router-link to="/admin/post-new"> 写博文</router-link>
                    </li>
                    <li :class="{ active: $route.path === '/admin/drafts' }">
                        <router-link to="/admin/drafts"> 草稿</router-link>
                    </li>
                    <li :class="{ active: $route.path === '/admin/trash' }">
                        <router-link to="/admin/trash"> 废纸篓</router-link>
                    </li>
                </ul>

                <!-- 其他单独的菜单项 -->
                <li :class="{ active: $route.path === '/admin/comments' }" class="out-list">
                    <router-link to="/admin/comments" active-class="active">
                        💬 评论管理
                        <span class="red-dot">{{ unreadCommentCount }}</span>
                    </router-link>
                </li>
                <li :class="{ active: $route.path === '/admin/archives' }" class="out-list">
                    <router-link to="/admin/archives" active-class="active">📂 归档管理</router-link>
                </li>
            </ul>
        </div>

        <!-- 右侧内容区域 -->
        <div class="content">
            <router-view />
        </div>
    </div>
</template>

<script setup>
    import axios from 'axios';
    import { onMounted, ref } from 'vue';

    const unreadCommentCount = ref(0);

    const isExpanded = ref(true);

    const toggleArticles = () => {
        isExpanded.value = !isExpanded.value;
    }

    const loadComments = async() => {
        await axios.get('http://localhost:8080/admin/comments/unread')
        .then(response => {
            unreadCommentCount.value = response.data;
        })
        .catch(error => {
            console.error('请求评论列表失败:', error);
        });
    }

    const checkSession = async() => {
        try {
            // 向后端发送请求检查 session 是否有效
            await axios.get('http://localhost:8080/check-session');
        } catch (error) {
            if (error.response && error.response.status === 401) {
                window.location.href = '/login'; // 假设你的登录页面路径为 /login
            } else {
                alert('无法检查会话状态，请稍后再试');
            }
        }
    }

    onMounted(() => {
        document.title = '后台仪表盘';
        loadComments();
        checkSession();
    });
</script>

<style scoped>
.admin-container {
    display: flex;
    height: 100vh; /* 使父容器充满整个视口高度 */
    margin: -8px 0px; /* 去掉父容器的外边距 */
    padding: 0; /* 去掉父容器的内边距 */
}

.sidebar {
    width: 200px;
    height: 100vh; /* 让侧边栏高度充满整个视口 */
    position: fixed; /* 固定侧边栏 */
    top: 0; /* 确保从顶部开始 */
    left: 0; /* 确保从左边开始 */
    background: #f5f5f5;
    padding: 0; /* 去掉侧边栏的内边距 */
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    overflow-y: auto; /* 允许侧边栏滚动 */
    background-color: #323339;
    color: #abb1b7;
    font-family: 'Microsoft Yahei';
}

.sidebar ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.sidebar li {
    padding: 10px;
    cursor: pointer;
    transition: background 0.3s;
    user-select: none; /* 禁止文本选择 */
    border-bottom: 1px solid #ddd;
}

.sidebar a {
    display: block;
    text-decoration: none;
    color: #abb1b7;
    padding: 10px 0px;
    transition: background 0.3s;
}

.sidebar li:hover {
    background: #1f1f23;
}

.sidebar li.active {
    color: #007bff;
    background: #1f1f23;
}
  
.content {
    margin-left: 192px; /* 让内容区域避开固定的侧边栏 */
    /* margin-top: -8px; */
    /* margin-bottom: -28px; */
    flex-grow: 1;
    overflow-y: auto;
    padding: 0px 30px;
    height: 100vh; /* 使内容区域高度充满整个视口 */
    box-sizing: border-box; /* 计算内边距时包含在宽高内 */
    background-color: #f3f3f3;
}

/* 控制展开部分的样式 */
.sidebar .nested-list a {
    padding-left: 30px; /* 给子菜单一个缩进 */
}

.sidebar .nested-list li.active {
    border-left: 6px solid #007bff;
    padding-left: 4px;
}

.sidebar .out-list.active {
    border-left: 6px solid #fff;
    padding-left: 4px;
}

.sidebar li.active a {
    color: #f0f0f1;
}

.red-dot {
    display: inline-block;
  vertical-align: top;
  box-sizing: border-box;
  margin:
1px 0 -1px 2px;
  padding:
0 5px;
  min-width: 18px;
  height: 18px;
  border-radius:
9px;
  background-color: #d63638;
  color: #fff;
  font-size: 11px;
  line-height: 1.6;
  text-align: center;
  z-index: 26;
}

html, body, #app {
    margin: 0;
    padding: 0;
    height: 100%;
}

</style>
