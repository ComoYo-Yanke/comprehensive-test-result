import request from './request'

// 管理端 API 集合
export const authApi = {
  login: data => request.post('/auth/employee/login', data),
  register: data => request.post('/auth/employee/register', data),
  info: () => request.get('/auth/info'),
  captcha: () => request.get('/captcha')
}

// 下拉选项（新增/编辑表单回显用）
export const optionApi = {
  schools: () => request.get('/employee/options/schools'),
  majors: params => request.get('/employee/options/majors', { params }),
  clazzs: params => request.get('/employee/options/clazzs', { params })
}

export const employeeApi = {
  // 学生
  students: params => request.get('/employee/students', { params }),
  student: id => request.get(`/employee/students/${id}`),
  addStudent: (data, params) => request.post('/employee/students', data, { params }),
  updateStudent: (data, params) => request.put('/employee/students', data, { params }),
  deleteStudent: id => request.delete(`/employee/students/${id}`),
  resetStudentPwd: id => request.put(`/employee/students/${id}/reset-password`),
  // 员工
  employees: params => request.get('/employee/employees', { params }),
  employee: id => request.get(`/employee/employees/${id}`),
  addEmployee: (data, params) => request.post('/employee/employees', data, { params }),
  updateEmployee: (data, params) => request.put('/employee/employees', data, { params }),
  deleteEmployee: id => request.delete(`/employee/employees/${id}`),
  // 学院
  schools: params => request.get('/employee/schools', { params }),
  addSchool: data => request.post('/employee/schools', data),
  updateSchool: data => request.put('/employee/schools', data),
  deleteSchool: id => request.delete(`/employee/schools/${id}`),
  // 专业
  majors: params => request.get('/employee/majors', { params }),
  addMajor: data => request.post('/employee/majors', data),
  updateMajor: data => request.put('/employee/majors', data),
  deleteMajor: id => request.delete(`/employee/majors/${id}`),
  // 班级
  clazzs: params => request.get('/employee/clazzs', { params }),
  addClazz: data => request.post('/employee/clazzs', data),
  updateClazz: data => request.put('/employee/clazzs', data),
  deleteClazz: id => request.delete(`/employee/clazzs/${id}`),
  // 活动
  activities: params => request.get('/employee/activities', { params }),
  activity: id => request.get(`/employee/activities/${id}`),
  updateActivity: data => request.put('/employee/activities', data),
  reviewActivity: (id, data) => request.put(`/employee/activities/${id}/review`, data),
  // 加分项
  extraItems: params => request.get('/employee/extra-items', { params }),
  reviewExtraItem: (id, data) => request.put(`/employee/extra-items/${id}/review`, data),
  myClassExtraItems: params => request.get('/employee/my-class/extra-items', { params }),
  reviewMyClassExtraItem: (id, data) => request.put(`/employee/my-class/extra-items/${id}/review`, data),
  // 违规
  addPenalty: data => request.post('/employee/penalties', data),
  penalties: params => request.get('/employee/penalties', { params }),
  // 综测
  approvedScores: params => request.get('/employee/scores/approved', { params }),
  pendingScores: params => request.get('/employee/scores/pending', { params }),
  reviewScore: (id, data) => request.put(`/employee/scores/${id}/review`, data),
  // 统计
  statistics: () => request.get('/employee/statistics'),
  // 通知
  notifications: params => request.get('/employee/notifications', { params }),
  readNotification: id => request.put(`/employee/notifications/${id}/read`)
}
