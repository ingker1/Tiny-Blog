<template>
    <div class ="quickEdit">
        <div class="editItem">
            <h3>发布</h3>
            <div class="content">
                <span>状态：</span>
                <select v-model="statusComputed" id="status" class="status">
                    <option value="publish" v-if="formatDate(new Date()) < localTime">定时发布</option>
                    <option value="publish" v-else>已发布</option>
                    <option value="draft">草稿</option>
                    <option value="private">私密</option>
                    <option value="trash">垃圾</option>
                </select><br />
            </div>
        </div>
      
        <div class="editItem">
            <h3>时间</h3>
            <div class="content">
                <input v-model="timeComputed" type="datetime-local"><br />
            </div>
            
        </div>
      

        <div class="editItem">
            <h3>分类</h3>
            <div class="content">
                <select v-model="categoryComputed" id="categories">
                    <option v-for="category in categories" :key="category.archiveId" :value="category">
                    {{ category.name }}
                    </option>
                </select>
            </div>

        </div>

    
        <div class="editItem">
            <h3>标签</h3>
            <div class="content">
                <input v-model="inputTagText" type="text" style="height: 24px;">
                <button @click="addTags" style="height: 30px; margin-left: 10px;">添加</button>
            </div>
            
        </div>

        <div class="editItem">
            <ul class="content">
                <li v-for="(tag, index) in tagsComputed" :key="index">{{ tag.name }} 
                <span @click="removeTag(index)" style="cursor: pointer; margin-left: 10px; color: red;">删除</span>
                </li>      
            </ul>
        </div>
    </div>
</template>
  
  
<script setup>
    import { ref, computed, watch, onMounted, defineProps, defineEmits } from 'vue';

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
        get: () => localCategory.value,
        set: (newValue) => {
            localCategory.value = newValue;
            emit('update:category', newValue);
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
    });

</script>
