<template>
  <!-- 指定一个容器 -->
  <div id="vditor"></div>
</template>

<script setup>
import Vditor from 'vditor'
import 'vditor/dist/index.css'; 
import { ref, onMounted } from 'vue';

// 2. 获取DOM引用
const editor = ref()
const isReady = ref(false); // 标志位，表示 Vditor 是否已初始化


onMounted(() => {
  editor.value = new Vditor('vditor',{
        height: '77vh',
        width: '60vw',
        mode: 'ir', //编辑模式设置为即时渲染
        cdn: 'http://localhost:8081/vditor', // 设置为本地CDN
        icon: 'material',
        after() {
            isReady.value = true; 
            console.log('Vditor 初始化完成');
            const event = new CustomEvent('editor-ready'); // 通知父组件编辑器已准备好
            document.dispatchEvent(event);
        },
        counter: {
            enable: true,  // 是否启用计数器
        },
        cache: {
            enable: false,    // 不保存原来内容
        },
        preview: {
            maxWidth: 1000,
            hljs:{ // 启用代码高亮
                enable: true,
                style: "monokai",
                lineNumber: true, // 显示行号
            } 
        },
        outline: {
          enable: true,
          position: 'right'
        }
    });
});

// 向父组件暴露获取编辑器内容的方法
const getValue = () => {
  return editor.value?.getValue() || '';
};

const setValue = (value) => {
  editor.value?.setValue(value);
};

const getHTML = () => {
  return editor.value?.getHTML() || '';
};

defineExpose({
  getValue, // 父组件可以调用这个方法获取内容
  setValue,
  getHTML,
  isReady, // 暴露 isReady 标志
});

</script>
