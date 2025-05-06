<template>
    <Header/>

    <div class="container">
        <h2>分类</h2>
        <div class="archive-list">
            <ul v-for="category in categories" :key="category.archiveId">
                <a :href="`archive/category/${category.name}`" class="archive-item">{{ category.name }}({{ category.count }})</a> 
            </ul>
        </div>

        <h2>标签</h2>
        <div class="archive-list">
            <ul v-for="tag in tags" :key="tag.archiveId">
                <a :href="`archive/tag/${tag.name}`" class="archive-item">{{ tag.name }}({{ tag.count }})</a>
            </ul>
        </div>
        
        <h2>时间轴</h2>
        <div>
            <button @click="toggleAllYears" class="toggle-all-btn">
                {{ isAllYearsExpanded ? '收起所有年份' : '展开所有年份' }}
            </button>
            <button @click="toggleAll" class="toggle-all-btn">
                {{ isAllExpanded ? '收起所有文章' : '展开所有文章' }}
            </button>

            <div v-for="year in sortedYears" :key="year" class="year-section">
                <h2 @click="toggleYear(year)">
                    {{ year }}年
                    <span class="arrow" :class="{ expanded: isYearExpanded(year) }">▶</span>
                </h2>
                <ul v-if="isYearExpanded(year)">
                    <li 
                        v-for="(articles, month) in sortedMonths(Timeline[year])" 
                        :key="month" 
                        @click="toggleMonth(year, Number(month))"
                    >
                        {{ month }}月 ({{ articles.length }}篇)
                        <span class="arrow" :class="{ expanded: isMonthExpanded(year, Number(month)) }">▶</span>
                        <ul v-if="isMonthExpanded(year, Number(month))">
                            <li 
                                v-for="article in articles" 
                                :key="article.id" 
                                @click.stop="goToArticle(article.id)"
                            >
                                <a :href="`/blog/${article.id}`">{{ article.title }}</a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>
    </div>

    <Footer/>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';

import Header from '@/components/Header.vue';
import Footer from '@/components/Footer.vue';

const Timeline = ref({});
const categories = ref([]);
const tags = ref([]);
const expandedMonths = ref({});
const expandedYears = ref({}); // 用于标识年份是否展开

onMounted(async () => {
    try {
        const timelineRes = await axios.get('http://localhost:8080/archives/date');
        const categoryRes = await axios.get('http://localhost:8080/archives/categories');
        const tagRes = await axios.get('http://localhost:8080/archives/tags');
        Timeline.value = timelineRes.data;
        categories.value = categoryRes.data;
        tags.value = tagRes.data;
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

// === 展开/折叠年份 ===
const toggleYear = (year) => {
    expandedYears.value[year] = !expandedYears.value[year];
};

// 判断年份是否展开
const isYearExpanded = (year) => !!expandedYears.value[year];

// === 展开/折叠月份 ===
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

// 判断月份是否展开
const isMonthExpanded = (year, month) => {
    return expandedMonths.value[year]?.includes(Number(month));
};

// === 全部展开/折叠年份 ===
const isAllYearsExpanded = computed(() => {
    return sortedYears.value.every(year => expandedYears.value[year]);
});

const toggleAllYears = () => {
    if (isAllYearsExpanded.value) {
        expandedYears.value = {};
    } else {
        expandedYears.value = sortedYears.value.reduce((acc, year) => {
            acc[year] = true;
            return acc;
        }, {});
    }
};

// === 全部展开/折叠月份和文章 ===
const isAllExpanded = computed(() => {
    return sortedYears.value.every(year => {
        // 如果年份未展开或未定义，直接判定为“未完全展开”
        if (!expandedMonths.value[year]) return false;

        // 判断该年份的所有月份是否都已展开
        return Object.keys(Timeline.value[year])
            .map(Number)
            .every(month => expandedMonths.value[year]?.includes(month));
    });
});


const toggleAll = () => {
    if (isAllExpanded.value) {
        expandedMonths.value = {};
    } else {
        expandedMonths.value = sortedYears.value.reduce((acc, year) => {
            acc[year] = Object.keys(Timeline.value[year]).map(Number);
            return acc;
        }, {});
        // 直接展开年份，保证年份也会显示
        expandedYears.value = sortedYears.value.reduce((acc, year) => {
            acc[year] = true;
            return acc;
        }, {});
    }
};
</script>

<style scoped>
.container {
    position: relative;
    transform: translateX(-50%);
    left: 50%;
    width: 60vw;
    font-family: 'Microsoft Yahei';
}

.archive-list {
    display: flex;
    flex-wrap:wrap;
}

.archive-item {
    border: 1px solid;
    padding: 5px;
}

.archive-item:hover {
    color: #fff;
    background-color: #00a2ff;
    text-decoration: none;
}

/* 全部展开/收起按钮 */
.toggle-all-btn {
    margin-bottom: 15px;
    margin-right: 20px;
    padding: 8px 10px;
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

/* 箭头图标 */
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

/* 子文章列表样式 */
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

a {
    color: inherit;          /* 继承父元素的颜色 */
    text-decoration: none;   /* 去掉下划线 */
}

a:visited {
    color: inherit;          /* 访问过的链接颜色与普通文字一致 */
}

a:hover {
    text-decoration: underline; /* 鼠标悬停时显示下划线（可选） */
}
</style>
