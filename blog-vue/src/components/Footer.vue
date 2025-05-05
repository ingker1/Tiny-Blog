<template>
    <div name="footer" class="footer">
        COPYRIGHT © {{ new Date().getFullYear()}} ingker's blog. ALL RIGHTS RESERVED.<br>
        MADE BY <a href="https://github.com/ingker1" style="color: white;">INGKER</a> | This <a href="https://github.com/ingker1/Tiny-Blog" style="color: white;">Project</a> Powered<br>
        <span id="jinrishici-sentence">今日诗词加载中...</span><br>
        本站已运行 <span style="color: green;">{{ runTime }}</span>
    </div>
</template>

<script setup>
    import { ref, onMounted, onUnmounted } from 'vue'

    const runTime = ref('')
    const startDate = new Date('2021-06-12')

    let timer = null

    function updateRunTime() {
        const now = new Date()
        const diff = now.getTime() - startDate.getTime()

        const msPerDay = 24 * 60 * 60 * 1000
        const msPerYear = 365 * msPerDay

        const years = Math.floor(diff / msPerYear)
        const days = Math.floor(diff / msPerDay)

        // 处理闰年额外的一天（简化逻辑，仅对2000~2100适用）
        let leapDays = 0
        for (let y = startDate.getFullYear(); y <= now.getFullYear(); y++) {
            if ((y % 4 === 0 && y % 100 !== 0) || y % 400 === 0) {
                leapDays++
            }
        }

        if (startDate.getFullYear() % 4 === 0 && startDate.getMonth() >= 2) leapDays-- // 起始年不满2月不算
        if (now.getFullYear() % 4 === 0 && now.getMonth() < 2) leapDays-- // 当前年未过2月不算

        const daysInCurrentYear = days - years * 365 - leapDays

        const hours = now.getHours().toString().padStart(2, '0')
        const minutes = now.getMinutes().toString().padStart(2, '0')
        const seconds = now.getSeconds().toString().padStart(2, '0')

        runTime.value = `${years}年${daysInCurrentYear}天${hours}时${minutes}分${seconds}秒`
    }

    onMounted(() => {
        updateRunTime()
        timer = setInterval(updateRunTime, 1000)

        const script = document.createElement('script');
        script.src = "https://sdk.jinrishici.com/v2/browser/jinrishici.js";
        script.async = true;
        document.body.appendChild(script);
    })

    onUnmounted(() => {
        clearInterval(timer)
    })

</script>

<style scoped>
.footer {
    background-color: #23282d;
    padding: 20px;
    margin: -8px;
    color: gray;
    text-align: center;
    font-size: 15px;
    line-height: 26px;
    letter-spacing: 1.5px;
    font-weight: lighter;
    font-family: 'Microsoft YaHei';
}

.footer a {
    color: inherit;          /* 继承父元素的颜色 */
    text-decoration: none;   /* 去掉下划线 */
}

h1 {
    text-align: center;
    color: #42b983;
}
</style>