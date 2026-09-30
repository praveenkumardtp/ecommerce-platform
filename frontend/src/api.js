const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081/api/v1';

function getAuthHeaders() {
  const token = localStorage.getItem('ecommerce_token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };
}

async function handleResponse(response) {
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const errorMsg = data.message || data.error || `HTTP error! status: ${response.status}`;
    throw new Error(errorMsg);
  }
  return data;
}

export const ecommerceApi = {
  // Auth
  async login(email, password) {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    return handleResponse(res);
  },

  async register(data) {
    const res = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return handleResponse(res);
  },

  // Products
  async getProducts({ search = '', categoryId = '', minPrice = '', maxPrice = '', page = 0, size = 12 } = {}) {
    const params = new URLSearchParams();
    if (search) params.append('search', search);
    if (categoryId) params.append('categoryId', categoryId);
    if (minPrice) params.append('minPrice', minPrice);
    if (maxPrice) params.append('maxPrice', maxPrice);
    params.append('page', page);
    params.append('size', size);

    const res = await fetch(`${API_BASE_URL}/products?${params.toString()}`);
    return handleResponse(res);
  },

  async getProductById(id) {
    const res = await fetch(`${API_BASE_URL}/products/${id}`);
    return handleResponse(res);
  },

  async getCategories() {
    const res = await fetch(`${API_BASE_URL}/categories`);
    return handleResponse(res);
  },

  // Cart
  async getCart() {
    const res = await fetch(`${API_BASE_URL}/cart`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  async addToCart(productId, quantity = 1) {
    const res = await fetch(`${API_BASE_URL}/cart/items`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ productId, quantity })
    });
    return handleResponse(res);
  },

  async updateCartItem(itemId, quantity) {
    const res = await fetch(`${API_BASE_URL}/cart/items/${itemId}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify({ quantity })
    });
    return handleResponse(res);
  },

  async removeCartItem(itemId) {
    const res = await fetch(`${API_BASE_URL}/cart/items/${itemId}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  // Orders
  async createOrder(shippingAddress) {
    const res = await fetch(`${API_BASE_URL}/orders`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ shippingAddress })
    });
    return handleResponse(res);
  },

  async getOrderHistory(page = 0, size = 10) {
    const res = await fetch(`${API_BASE_URL}/orders?page=${page}&size=${size}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  // Admin
  async getAdminDashboard() {
    const res = await fetch(`${API_BASE_URL}/admin/dashboard`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  async addProduct(productData) {
    const res = await fetch(`${API_BASE_URL}/admin/products`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(productData)
    });
    return handleResponse(res);
  },

  async updateProduct(id, productData) {
    const res = await fetch(`${API_BASE_URL}/admin/products/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(productData)
    });
    return handleResponse(res);
  },

  async deleteProduct(id) {
    const res = await fetch(`${API_BASE_URL}/admin/products/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    return res.status === 204 ? true : handleResponse(res);
  },

  async updateInventory(productId, quantity) {
    const res = await fetch(`${API_BASE_URL}/admin/products/${productId}/inventory?quantity=${quantity}`, {
      method: 'PUT',
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  async getAllOrders(page = 0, size = 15) {
    const res = await fetch(`${API_BASE_URL}/admin/orders?page=${page}&size=${size}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  async updateOrderStatus(orderId, status) {
    const res = await fetch(`${API_BASE_URL}/admin/orders/${orderId}/status`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify({ status })
    });
    return handleResponse(res);
  }
};
