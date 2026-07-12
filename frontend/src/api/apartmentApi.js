import apiClient from './axiosConfig';

const BASE = '/api/v1/apartments';

const apartmentApi = {
  create:      (data)           => apiClient.post(BASE, data).then(r => r.data),
  getById:     (id)             => apiClient.get(`${BASE}/${id}`).then(r => r.data),
  getMyApartment: ()            => apiClient.get(`${BASE}/me`).then(r => r.data),
  getAll:      (params)         => apiClient.get(BASE, { params }).then(r => r.data),
  update:      (id, data)       => apiClient.put(`${BASE}/${id}`, data).then(r => r.data),
  delete:      (id)             => apiClient.delete(`${BASE}/${id}`).then(r => r.data),
  activate:    (id)             => apiClient.patch(`${BASE}/${id}/activate`).then(r => r.data),
  deactivate:  (id)             => apiClient.patch(`${BASE}/${id}/deactivate`).then(r => r.data),
};

export default apartmentApi;
