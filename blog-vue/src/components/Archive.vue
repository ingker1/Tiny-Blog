<template>
    <div>
        <!-- ✅ 新增：全部展开/收起按钮 -->
        <button @click="toggleAll" class="toggle-all-btn">
            {{ isAllExpanded ? '全部收起' : '全部展开' }}
        </button>

        <div v-for="year in sortedYears" :key="year" class="year-section">
            <h2>{{ year }}年</h2>
            <ul>
                <li 
                    v-for="(articles, month) in sortedMonths(Timeline[year])" 
                    :key="month" 
                    @click="toggleMonth(year, Number(month))"
                >
                    {{ formatMonth(month) }}月 ({{ articles.length }})
                    <span class="arrow" :class="{ expanded: isMonthExpanded(year, Number(month)) }">▶</span>
                    <ul v-if="isMonthExpanded(year, Number(month))">
                        <li 
                            v-for="article in articles" 
                            :key="article.id" 
                            @click.stop="goToArticle(article.id)"
                        >
                            {{ article.title }}
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';

const Timeline = ref({});
const expandedMonths = ref({});

// ✅ 加载数据
onMounted(async () => {
    try {
        const res = await axios.get('http://localhost:8080/archives/date');
        Timeline.value = res.data;
    } catch (err) {
        console.error('加载归档数据失败：', err);
    }
});

// **年份倒序排列（从新到旧）**
const sortedYears = computed(() => {
    return Object.keys(Timeline.value)
        .map(Number)
        .sort((a, b) => b - a);
});

// **月份正序排列（从1月到12月）**
const sortedMonths = (months) => {
    return Object.fromEntries(
        Object.entries(months).sort((a, b) => a[0] - b[0])
    );
};

// ✅ 格式化成两位数月份
const formatMonth = (month) => {
    return String(month).padStart(2, '0');
};

// ✅ 展开/折叠月份
const toggleMonth = (year, month) => {
    if (!expandedMonths.value[year]) {
        expandedMonths.value[year] = [];
    }
    if (expandedMonths.value[year].includes(month)) {
        expandedMonths.value[year] = expandedMonths.value[year].filter(m => m !== month);
    } else {
        expandedMonths.value[year].push(month);
    }
};

// ✅ 判断月份是否展开
const isMonthExpanded = (year, month) => {
    return expandedMonths.value[year]?.includes(Number(month));
};

// ✅ 判断是否全部展开
const isAllExpanded = computed(() => {
    return sortedYears.value.every(year =>
        Timeline.value[year] &&
        Object.keys(Timeline.value[year]).map(Number).every(month =>
            expandedMonths.value[year]?.includes(month)
        )
    );
});

// ✅ 全部展开/收起（修复逻辑）
const toggleAll = () => {
    if (isAllExpanded.value) {
        // 全部收起
        expandedMonths.value = {};
    } else {
        // 全部展开
        expandedMonths.value = sortedYears.value.reduce((acc, year) => {
            acc[year] = Object.keys(Timeline.value[year]).map(Number);
            return acc;
        }, {});
    }
};

// ✅ 点击文章跳转
const goToArticle = (id) => {
    console.log(`跳转到文章 ID: ${id}`);
    // router.push(`/article/${id}`);
};
</script>

<style scoped>
/* ✅ 全部展开/收起按钮 */
.toggle-all-btn {
    margin-bottom: 15px;
    padding: 8px 16px;
    font-size: 14px;
    background-color: #42b983;
    color: #fff;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.2s ease;
}

.toggle-all-btn:hover {
    background-color: #369f6c;
}

.year-section {
    margin-bottom: 20px;
}

h2 {
    font-size: 20px;
    margin-bottom: 10px;
    border-bottom: 2px solid #eee;
    padding-bottom: 5px;
}

ul {
    padding-left: 20px;
    list-style-type: none;
}

li {
    margin-bottom: 5px;
    font-size: 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
}

li:hover {
    color: #42b983;
}

/* ✅ 箭头图标 */
.arrow {
    display: inline-block;
    transition: transform 0.2s ease;
    margin-left: 5px;
    font-size: 14px;
    transform: rotate(90deg);
}

.expanded {
    transform: rotate(0);
}

/* ✅ 子文章列表样式 */
li ul {
    margin-top: 5px;
    padding-left: 20px;
}

li ul li {
    font-size: 14px;
    color: #666;
    cursor: pointer;
}

li ul li:hover {
    color: #42b983;
}
</style>
