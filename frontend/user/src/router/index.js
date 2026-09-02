import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/activities',
    children: [
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue') },
      { path: 'activities', name: 'Activities', component: () => import('../views/Activities.vue') },
      { path: 'extra-items', name: 'ExtraItems', component: () => import('../views/ExtraItems.vue') },
      { path: 'scores', name: 'Scores', component: () => import('../views/Scores.vue') },
      { path: 'penalties', name: 'Penalties', component: () => import('../views/Penalties.vue') },
      { path: 'notifications', name: 'Notifications', component: () => import('../views/Notifications.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
