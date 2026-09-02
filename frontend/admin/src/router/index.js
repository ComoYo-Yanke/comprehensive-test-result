import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'students', name: 'Students', component: () => import('../views/Students.vue') },
      { path: 'employees', name: 'Employees', component: () => import('../views/Employees.vue') },
      { path: 'schools', name: 'Schools', component: () => import('../views/Schools.vue') },
      { path: 'majors', name: 'Majors', component: () => import('../views/Majors.vue') },
      { path: 'clazzs', name: 'Clazzs', component: () => import('../views/Clazzs.vue') },
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

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
