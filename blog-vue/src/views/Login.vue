<template>
    <div class="login-container">
        <h2>欢迎登录</h2>
        <input v-model="username" type="text" placeholder="用户名" v-if="!isLoggedIn" />
        <input v-model="password" type="password" placeholder="密码" v-if="!isLoggedIn" />

        <div class="button-group">
            <button
                class="login-btn"
                @click="isLoggedIn ? logout() : login()"
                :disabled="isLoading"
            >
                {{ isLoggedIn ? '退出登录' : '登录' }}
            </button>

            <button
                class="register-btn"
                v-if="!isLoggedIn"
                @click="goToRegister"
            >
                注册
            </button>
        </div>

        <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
        <p v-if="isLoggedIn" class="user-info">已登录：{{ storedUsername }}</p>
    </div>
</template>

  
<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';



// 定义响应式数据
const username = ref('');
const password = ref('');
const isLoading = ref(false);
const errorMessage = ref('');
const isLoggedIn = ref(false);  // 用于判断是否已经登录
const storedUsername = ref('');

// 页面加载时检查是否已有登录状态
onMounted(() => {
    checkSession();
});

const checkSession = async() => {
    const savedUsername = localStorage.getItem('username');

    if (savedUsername) {
        storedUsername.value = savedUsername;

        try {
            // 向后端发送请求检查 session 是否有效
            const response = await axios.get('http://localhost:8080/check-session');
            if (response.status === 200) {
                isLoggedIn.value = true; // 会话有效，用户已登录
            }
        } catch (error) {
            if (error.response && error.response.status === 401) {
                // 如果会话过期，清除 localStorage 并设置未登录状态
                localStorage.removeItem('username');
                isLoggedIn.value = false;
                storedUsername.value = '';
                errorMessage.value = '会话已过期，请重新登录';
            } else {
                errorMessage.value = '无法检查会话状态，请稍后再试';
            }
        }
    }
}

// 登录请求方法
const login = async () => {
    errorMessage.value = ''; // 清空之前的错误信息
    if (!username.value || !password.value) {
        errorMessage.value = '用户名和密码不能为空';
        return;
    }

    isLoading.value = true;

    try {
        const response = await axios.post('http://localhost:8080/login', {
            login: username.value,
            password: password.value,
        });

        // 登录成功
        alert(response.data);  // 登录成功的提示
        window.location.href = '/admin/posts'; // 假设你的登录页面路径为 /login
        // 保存用户信息到 localStorage
        localStorage.setItem('username', username.value);
        isLoggedIn.value = true;
        storedUsername.value = username.value;
    } catch (error) {
        // 错误处理
        if (error.response) {
            errorMessage.value = error.response.data || '发生了错误';
        } else {
            errorMessage.value = '网络错误，请稍后再试';
        }
    } finally {
        isLoading.value = false;
    }
};


// 退出登录
const logout = async () => {
    // 清除登录信息
    localStorage.removeItem('username');

    try {
        await axios.post('http://localhost:8080/logout');
    } catch (error) {
        // 错误处理
        if (error.response) {
            errorMessage.value = error.response.data || '发生了错误';
        } else {
            errorMessage.value = '网络错误，请稍后再试';
        }
    }

    isLoggedIn.value = false;
    storedUsername.value = '';
};


</script>
  
<style scoped>
.login-container {
    width: 350px;
    padding: 30px 25px;
    background: #f9f9f9;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
    border-radius: 12px;
    position: absolute;
    top: 25%;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    font-family: 'Segoe UI', sans-serif;
}

.login-container h2 {
    margin-bottom: 20px;
    color: #333;
}

.login-container input {
    width: 100%;
    padding: 12px 10px;
    margin-bottom: 15px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font-size: 15px;
}

.button-group {
    width: 100%;
    display: flex;
    gap: 10px;
    margin-top: 10px;
    justify-content: space-between;
}

.login-container button {
    flex: 1;
    padding: 10px 0;
    font-size: 15px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.login-btn {
    background-color: #007BFF;
    color: white;
}

.login-btn:hover {
    background-color: #0056b3;
}

.login-btn:disabled {
    background-color: #ccc;
    cursor: not-allowed;
}

.register-btn {
    background-color: #6c757d;
    color: white;
}

.register-btn:hover {
    background-color: #5a6268;
}

.error {
    color: #d9534f;
    font-size: 14px;
    margin-top: 12px;
    text-align: center;
}

.user-info {
    font-size: 14px;
    color: green;
    margin-top: 12px;
}
</style>
  