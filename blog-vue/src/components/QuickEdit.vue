<template>
    <div class ="quickEdit">
        <div class="editItem">
            <h3>发布状态</h3>
            <div class="content">
                <CustomSelect
                    v-model="statusComputed"
                    :options="statusOptions"
                    placeholder="选择状态"
                />
            </div>
        </div>
      
        <div class="editItem">
            <h3>发布时间</h3>
            <div class="content">
                <input v-model="timeComputed" type="datetime-local"><br />
            </div>
            
        </div>
            <div class="editItem">
            <h3>分类</h3>
            <div class="content">
                <CustomSelect
                    v-model="categoryComputed"
                    :options="categoryOptions"
                    placeholder="选择分类"
                />
            </div>
        </div>

        <div class="editItem">
            <h3>标签</h3>
            <div class="content" style="flex-wrap: nowrap;">
                <input v-model="inputTagText" type="text">
                <button @click="addTags" style="width: 50px;height: 37px; margin-left: 10px;">添加</button>
            </div>  
        </div>

        <div class="editItem">
            <ul class="content" style="padding: 0;">
                <li class="text-with-icon" v-for="(tag, index) in tagsComputed" :key="index">{{ tag.name }} 
                <span class="delete-icon" @click="removeTag(index)"></span>
                </li>      
            </ul>
        </div>
    </div>
</template>
  
  
<script setup>
    import { ref, computed, watch, onMounted, defineProps, defineEmits } from 'vue';
    import CustomSelect from '@/components/CustomSelect.vue';

    // 替换为你的状态列表（动态设置 label）
    const statusOptions = computed(() => [
        {
            value: 'publish',
            label: formatDate(new Date()) < localTime.value ? '定时发布' : '已发布'
        },
        { value: 'draft', label: '草稿' },
        { value: 'private', label: '私密' },
        { value: 'trash', label: '垃圾' }
    ]);

    // 分类 options，把每个分类对象映射成 { value, label }
    const categoryOptions = computed(() => {
        return (props.categories || []).map(cat => ({
            value: cat.archiveId,
            label: cat.name
        }));
    });

    // 接收父组件的props
    const props = defineProps({
        categories: Array,
        status: String,
        time: String,
        category: Object,
        tags: Array,
    });

    // 需要对父组件传递的 `status`, `time`, `category` 和 `tags` 进行响应式绑定
    const localStatus = ref(props.status);
    const localTime = ref(props.time);
    const localCategory = ref(props.category);
    const localTags = ref([...props.tags]);
    const inputTagText = ref('');

    const emit = defineEmits(['update:status', 'update:time', 'update:category', 'update:tags']);

    // 调试信息
    console.log('父组件传入的 props:', props);

    // 格式化日期，适用于 <input type="datetime-local">
    const formatDate = (dateStr) => {
        const localDate = new Date(dateStr);

        const year = localDate.getFullYear().toString().padStart(4, '0');
        const month = (localDate.getMonth() + 1).toString().padStart(2, '0');
        const day = localDate.getDate().toString().padStart(2, '0');
        const hour = localDate.getHours().toString().padStart(2, '0');
        const minute = localDate.getMinutes().toString().padStart(2, '0');

        return `${year}-${month}-${day}T${hour}:${minute}`;
    };

    // 使用 watchEffect 来确保父组件传入的数据发生变化时，子组件能够同步更新
    watch(() => props.status, (newStatus) => {
        console.log('状态更新为:', newStatus);
        localStatus.value = newStatus;
    });

    watch(() => props.time, (newTime) => {
        console.log('时间更新为:', newTime);
        localTime.value = newTime;
    });

    watch(() => props.category, (newCategory) => {
        console.log('分类更新为:', newCategory);
        localCategory.value = newCategory;
    });

    watch(() => props.tags, (newTags) => {
        console.log('标签更新为:', newTags);
        localTags.value = [...newTags];
    });

    // 根据本地的值来更新父组件的值
    const statusComputed = computed({
        get: () => localStatus.value,
        set: (newValue) => {
            localStatus.value = newValue;
            emit('update:status', newValue);
        }
    });

    const timeComputed = computed({
        get: () => formatDate(localTime.value),
        set: (newValue) => {
            localTime.value = newValue;
            emit('update:time', newValue);
        }
    });

    const categoryComputed = computed({
        get: () => localCategory.value ? localCategory.value.archiveId : null,
        set: (newValue) => {
            const selectedCategory = props.categories.find(cat => cat.archiveId === newValue);
            localCategory.value = selectedCategory;
            emit('update:category', selectedCategory);
        }
    });


    const tagsComputed = computed({
        get: () => localTags.value,
        set: (newValue) => {
            localTags.value = newValue;
            emit('update:tags', newValue);
        }
    });

    const addTags = () => {
        if (inputTagText.value) {
            const newTag = {
                archiveId: null,
                taxonomy: 'post_tag',
                name: inputTagText.value
            };

            localTags.value.push(newTag);
            emit('update:tags', [...localTags.value]); // 确保同步到父组件
            inputTagText.value = '';
        }
        console.log(tagsComputed.value);
    }

    const removeTag = (index) => {
        tagsComputed.value.splice(index, 1); // 删除指定位置的标签
        console.log(tagsComputed.value);
        emit('update:tags', tagsComputed.value); // 确保同步到父组件
    };

    // 确保异步加载数据后再进行其他操作
    onMounted(() => {
        console.log('子组件已挂载，等待父组件传递的数据...');
        console.log('categories:', props.categories);
    });

</script>

<style scoped>
.delete-icon {
    width: 18px;
    height: 18px;
    background-color: #007BFF;
    border-radius: 50%;
    position: relative;
    cursor: pointer;
    transition: background-color 0.3s ease;
    display: inline-block;
    vertical-align: text-bottom;
    margin-left: 3px;
}

.delete-icon::before,
.delete-icon::after {
    content: '';
    position: absolute;
    background-color: white;
    width: 10px;
    height: 2px;
    top: 50%;
    left: 50%;
    transform-origin: center;
}

.delete-icon::before {
    transform: translate(-50%, -50%) rotate(45deg);
}

.delete-icon::after {
    transform: translate(-50%, -50%) rotate(-45deg);
}

.delete-icon:hover {
    background-color: #FF4136;
}

.text-with-icon {
    display: inline-flex;
    align-items: center;
    font-size: 16px;
    font-family: sans-serif;
    margin-bottom: 10px;
    margin-right: 10px;
    /* float: left; */
}

.quickEdit {
    margin-bottom: 20px;
}

.content {
    display: flex;
    flex-wrap: wrap;      /* 允许内容换行 */
    gap: 10px;            /* 标签之间的间距可自定义 */
    max-width: 100%;      /* 限制不能超出表格单元格 */
    box-sizing: border-box;
}
</style>