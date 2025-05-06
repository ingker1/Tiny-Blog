<template>
    <Header/>
    <div class="container">
        <div class="post">
            <h1>关于</h1>
            <p style="text-align: center; height: 40vh; font-size: 18px;"> 
            我是清科谷体，简称清科，英文网名是ingker。<br><br>

            一个使用SpringBoot和Vue.js驱动的博客网站。<br><br>

            这个网站会发布自己的想法、思考和学习心得；也出一些简明的教程给需要的人看。<br><br>
            </p>
        </div>
    </div>
    <Footer/>
</template>
  
<script setup>
    import Header from '@/components/Header.vue';
    import Footer from '@/components/Footer.vue';
</script>

<style scoped>
.container {
    position: relative;
    transform: translateX(-50%);
    left: 50%;
    width: 70vw;
    padding: 20px;
    margin: -8px;
}

h1 {
    text-align: center;
}

</style>
  