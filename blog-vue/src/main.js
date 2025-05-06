import { createApp } from 'vue'
import App from './App.vue'
import router from './router';
import axios from 'axios';
import './assets/style.css';

const app = createApp(App);

app.use(router).mount('#app');

axios.defaults.withCredentials = true;// 允许跨域携带cookie