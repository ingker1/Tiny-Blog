<template>
    <Header/>
    <div class="container">
        <h3>请选择你要访问的站点：</h3>
        <h3><a href="https://blog.ingke.net/">访问清科谷体的博客</a></h3>
        <h3><a href="https://rss.ingke.net/">反向代理动画下载站点rss解析</a></h3>
        <h3><a href="https://rsshub.ingke.net/">RSSHub服务生成网站的RSS订阅</a></h3>
    </div>
</template>

<script setup>
    import Header from '@/components/Header.vue';
</script>

<style scoped>
.container {
    width: 60%;
    margin: 10% auto 0;
    background-color: #f0f0f0;
    padding: 2% 5%;
    border-radius: 10px
}

ul {
    padding-left: 20px;
}

ul li {
    line-height: 2.3
}

a {
    color: #20a53a
}

</style>
