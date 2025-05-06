const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
    devServer: {
        host: '0.0.0.0',  // 或者你可以设置成具体的IP地址，例如 '192.168.0.100'
        port: 8081,       // 设置开发服务器的端口号
        open: false,        // 自动打开浏览器
        https: false,      // 如果需要使用 https，则设置为 true
        headers: {
            'Access-Control-Allow-Origin': '*' // 允许所有域访问
        },
        client: {
            overlay: false // 编译错误时，取消全屏覆盖（建议关掉）
        }
    },
    // 配置生产环境中的 Source Maps
    configureWebpack: {
        // 开启 Source Maps 以便别人能查看源代码
        devtool: 'source-map'
    },
})
