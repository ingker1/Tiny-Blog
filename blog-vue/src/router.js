import { createRouter, createWebHistory } from 'vue-router';

const routes = [
    // 自定义路由路径名称，使用懒加载方式加载组件
    { path: '/page/:id', name: 'RootPage', component: () => import('@/views/Blog') },
    { path: '/', name: 'Root', component: () => import('@/views/Blog') },

    { path: '/blog/:id', name: 'Post', component: () => import('@/views/Post') },
    { path: '/404', name: '404', component: () => import('@/views/404')},
    { path: '/login', name: 'Login', component: () => import('@/views/Login')},
    { path: '/archive', name: 'Archive', component: () => import('@/components/Archive.vue')},
    { path: '/about', name: 'About', component: () => import('@/views/About.vue')},
    { path: '/index', name: 'Index', component: () => import('@/views/Index.vue')},
    { 
        path: '/admin', 
        name: 'admin', 
        component: () => import('@/views/admin/Admin'),
        children: [
            { path: '', redirect: '/admin/posts' }, // 默认跳转到文章管理
            { path: '/', redirect: '/admin/posts' }, // 默认跳转到文章管理
            { path: 'posts', name: 'Posts', component: () => import('@/views/admin/Posts') },
            { path: 'archives', name: 'Archives', component: () => import('@/views/admin/Archives')},
            { path: 'comments', name: 'Comments', component: () => import('@/views/admin/Comments')},
            { path: 'post-new', name: 'Add', component: () => import('@/views/admin/Post-New') },
            { path: 'edit=:id', name: 'EditWithID', component: () => import('@/views/admin/Edit') },
            { path: 'drafts', name: 'Draft', component: () => import('@/views/admin/Draft') },
            { path: 'trash', name: 'Trash', component: () => import('@/views/admin/Trash') },
        ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/404' }, // 404 重定向
];

const router = createRouter({
    history: createWebHistory(),  // 使用 HTML5 History 模式，不会在 URL 中显示 '#'
    routes
});

export default router;
