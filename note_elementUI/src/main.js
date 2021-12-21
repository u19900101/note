import Vue from 'vue'
import App from './App.vue'

//关闭Vue的生产提示
Vue.config.productionTip = false
// 完整引入 element-ui
import ElementUI from 'element-ui'
Vue.use(ElementUI)
// 引入element-ui样式
import 'element-ui/lib/theme-chalk/index.css'
//引入VueRouter
import VueRouter from 'vue-router'
//引入路由器
import router from './router'
//应用插件
Vue.use(VueRouter)
// 解决ElementUI导航栏中的vue-router在3.0版本以上重复点菜单报错问题
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}
//引入网络请求
import https from './server/index'
Vue.use(https);

//引入store
import store from './store'

//引入工具类
import tool from './assets/js/tool'
Vue.use(tool);
new Vue({
  render: h => h(App),
  router:router,
  beforeCreate() {
    Vue.prototype.$bus = this //安装全局事件总线
  },
  store
}).$mount('#app')
