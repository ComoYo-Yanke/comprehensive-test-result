import request from './request'

// 学生端 API 集合
export const authApi = {
  login: data => request.post('/auth/student/login', data),
  info: () => request.get('/auth/info'),
  captcha: () => request.get('/captcha')
}

export const studentApi = {
  // 个人信息
  profile: () => request.get('/student/profile'),
  updateProfile: data => request.put('/student/profile', data),
  // 活动
  activities: params => request.get('/student/activities', { params }),
  activityDetail: id => request.get(`/student/activities/${id}`),
  joinActivity: id => request.post(`/student/activities/${id}/join`),
  createActivity: data => request.post('/student/activities', data),
  activityOptions: () => request.get('/student/activity/options'),
  // 加分项
  extraItems: params => request.get('/student/extra-items', { params }),
  addExtraItem: data => request.post('/student/extra-items', data),
  // 综测成绩
  scores: params => request.get('/student/scores', { params }),
  computeScore: params => request.post('/student/scores', null, { params }),
  deleteScore: id => request.delete(`/student/scores/${id}`),
  // 违规记录 / 通知
  penalties: params => request.get('/student/penalties', { params }),
  notifications: params => request.get('/student/notifications', { params }),
  readNotification: id => request.put(`/student/notifications/${id}/read`)
}

export const fileApi = {
  upload: formData => request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
