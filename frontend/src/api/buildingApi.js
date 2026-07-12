import apiClient from './axiosConfig';

const BASE = '/api/v1/buildings';

const buildingApi = {
  create:   (data)       => apiClient.post(BASE, data).then(r => r.data),
  getById:  (id)         => apiClient.get(`${BASE}/${id}`).then(r => r.data),
  getAll:   ()           => apiClient.get(`${BASE}/all`).then(r => r.data),
  search:   (params)     => apiClient.get(BASE, { params }).then(r => r.data),
  update:   (id, data)   => apiClient.put(`${BASE}/${id}`, data).then(r => r.data),
  delete:   (id)         => apiClient.delete(`${BASE}/${id}`).then(r => r.data),
};

export default buildingApi;
