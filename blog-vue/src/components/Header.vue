<template>
    <div name="header" class="header"> 
                <!-- 背景层：背景图 + 遮罩 -->
                <div class="background-layer">
            <div class="overlay"></div>
        </div>
        <div class="top-nav">
            <h1><a href="/">ingker's Blog</a></h1>
            <div class="nav-bar">
                <a href="/" class="nav-item">首页</a>
                <a href="/archive" class="nav-item">归档</a>
                <a href="/about" class="nav-item">关于</a>
                <a href="/index" class="nav-item">导航</a>  
            </div>
        </div>
        
        <div class="top-text">
            <div class="typedbox">
                <span id="subtitle" class="typed-title">清科谷体的博客</span>
            </div>
            <div class="hitokoto" @click="loadHitokoto">
                <p v-html="hitokoto"></p>
            </div>
        </div>
    </div>
</template>

<script setup>
    import { ref, onMounted, onBeforeUnmount } from 'vue';
    import Typed from 'typed.js';

    const hitokoto = ref('加载中...');
    let intervalId = null;

    // 获取随机语录
    async function loadHitokoto() {
        try {
            const response = await fetch('/assets/hitokoto.txt')
            const text = await response.text()
            const lines = text
                .split('\n')
                .map(line => line.trim())
                .filter(Boolean)

            const randomIndex = Math.floor(Math.random() * lines.length)
            hitokoto.value = lines[randomIndex]
        } catch (e) {
            console.error(e)
        }
    }

    onMounted(async() => {
        loadHitokoto();
        intervalId = setInterval(() => {
            loadHitokoto();
        }, 60000) // 每10秒自动切换

        new Typed('#subtitle', {
            strings: [
                'ingker的web试验场',
                '清科谷体的博客',
            ],
            startDelay: 800,
            typeSpeed: 100,
            loop: false,
            backSpeed: 100,
            showCursor: true
        });
    });

    onBeforeUnmount(() => {
        clearInterval(intervalId);
    });

</script>

<style scoped>
.header {
    position: relative;
    height: 280px;
    margin: -8px -8px 0px -8px;
    color: #fff;
    text-shadow: 2px 2px 2px #666;
    overflow: hidden;
}

/* 背景图和遮罩独立层 */
.background-layer {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background-image: url(/src/assets/wallhaven.png);
    background-size: cover;
    background-position: center;
    z-index: 0;
}

.overlay {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(66, 64, 64, 0.25); /* 控制背景暗度 */
}

/* 前景文字层 */
.top-nav,
.top-text {
    position: relative;
    z-index: 1; /* 保证显示在背景图之上 */
}

.top-nav {
    display: flex;
    flex-direction: row;
    color: #fff;
    cursor: default;
}

.top-nav a {
    color: inherit;          /* 继承父元素的颜色 */
    text-decoration: none;   /* 去掉下划线 */
}

.top-nav h1 {
    margin-left: 200px;
    display: flex;
}

.nav-bar {
    text-align: left;
    margin-bottom: 20px;
    margin-top: auto;
    margin-left: auto;
    min-width: 398px;
}

.nav-item {
    margin-right: 15px;
    font-size: 20px;
}

.top-text {
    text-align: center;
    display: block !important;
    color: #fff;
    cursor: default;
    font-family: 'Microsoft YaHei';
}

.top-text .typedbox {
    font-weight: normal;
    font-size: 40px;
    margin: 20px 0px;
}

.top-text .hitokoto {
    width: 500px;
    left: 50%;
    position: relative;
    transform: translateX(-50%);
    margin: 30px 0px;
    font-size: 18px;
}

</style>