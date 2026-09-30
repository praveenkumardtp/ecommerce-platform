import React, { useState, useEffect } from 'react';
import { 
  ShoppingBag, 
  ShoppingCart, 
  Search, 
  ShieldCheck, 
  Plus, 
  Minus, 
  Trash2, 
  Package, 
  TrendingUp, 
  AlertCircle, 
  CheckCircle, 
  X, 
  LogOut, 
  Clock, 
  ChevronRight,
  Sparkles
} from 'lucide-react';
import { ecommerceApi } from './api';
import './App.css';

export default function App() {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('ecommerce_token'));
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  
  // Cart state
  const [cart, setCart] = useState({ items: [], totalPrice: 0, totalItems: 0 });
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [shippingAddress, setShippingAddress] = useState('452 Tech Avenue, Suite 100, San Francisco, CA');

  // Customer Orders view
  const [isOrdersOpen, setIsOrdersOpen] = useState(false);
  const [orders, setOrders] = useState([]);

  // Admin state
  const [adminView, setAdminView] = useState(false);
  const [adminStats, setAdminStats] = useState(null);
  const [adminOrders, setAdminOrders] = useState([]);
  const [isAddProductOpen, setIsAddProductOpen] = useState(false);
  const [newProduct, setNewProduct] = useState({
    name: '',
    description: '',
    price: '',
    stockQuantity: '',
    imageUrl: '',
    categoryId: 1
  });

  // Auth modal
  const [isAuthOpen, setIsAuthOpen] = useState(false);
  const [authMode, setAuthMode] = useState('login');
  const [authEmail, setAuthEmail] = useState('customer@store.com');
  const [authPassword, setAuthPassword] = useState('CustomerPassword@123');
  const [authName, setAuthName] = useState('');

  const [notification, setNotification] = useState(null);

  useEffect(() => {
    loadCatalog();
    if (token) {
      loadUserData();
    } else {
      // Demo initial setup
      setUser({ fullName: 'Praveen Kumar', email: 'customer@store.com', role: 'ROLE_CUSTOMER' });
      setOrders([
        {
          id: 1,
          orderNumber: 'ORD-DEMO-9001',
          totalAmount: 449.49,
          status: 'CONFIRMED',
          shippingAddress: '452 Tech Avenue, Suite 100, San Francisco, CA',
          paymentNotice: 'Payment integration not configured',
          createdAt: new Date(Date.now() - 86400000).toISOString(),
          items: [
            { id: 101, productName: 'Pro Wireless Noise-Cancelling Headphones', quantity: 1, unitPrice: 299.99, subtotal: 299.99 },
            { id: 102, productName: 'Mechanical RGB Ergonomic Keyboard', quantity: 1, unitPrice: 149.50, subtotal: 149.50 }
          ]
        }
      ]);
    }
  }, [token]);

  async function loadCatalog() {
    try {
      const prodsRes = await ecommerceApi.getProducts();
      if (prodsRes && prodsRes.content) setProducts(prodsRes.content);
      const catsRes = await ecommerceApi.getCategories();
      if (catsRes) setCategories(catsRes);
    } catch (err) {
      console.warn('Backend offline, using seed catalog demonstration');
      setCategories([
        { id: 1, name: 'Electronics' },
        { id: 2, name: 'Audio & Sound' },
        { id: 3, name: 'Wearables' },
        { id: 4, name: 'Accessories' }
      ]);
      setProducts([
        {
          id: 1,
          name: 'Pro Wireless Noise-Cancelling Headphones',
          description: 'Active hybrid noise cancellation with 40-hour battery life and spatial audio driver architecture.',
          price: 299.99,
          stockQuantity: 35,
          imageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500',
          categoryName: 'Audio & Sound',
          categoryId: 2,
          active: true
        },
        {
          id: 2,
          name: 'Mechanical RGB Ergonomic Keyboard',
          description: 'Hot-swappable linear mechanical switches, gasket-mounted aluminum chassis, and PBT keycaps.',
          price: 149.50,
          stockQuantity: 50,
          imageUrl: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500',
          categoryName: 'Accessories',
          categoryId: 4,
          active: true
        },
        {
          id: 3,
          name: 'Ultra-Thin 4K IPS USB-C Monitor 27"',
          description: 'Color-calibrated 99% DCI-P3 display with 90W power delivery and anti-glare coating.',
          price: 499.00,
          stockQuantity: 18,
          imageUrl: 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500',
          categoryName: 'Electronics',
          categoryId: 1,
          active: true
        },
        {
          id: 4,
          name: 'Titanium GPS Smart Fitness Watch',
          description: 'Dual-frequency satellite navigation, optical heart-rate sensor, and sapphire crystal lens.',
          price: 349.99,
          stockQuantity: 4,
          imageUrl: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500',
          categoryName: 'Wearables',
          categoryId: 3,
          active: true
        }
      ]);
    }
  }

  async function loadUserData() {
    try {
      const cartRes = await ecommerceApi.getCart();
      if (cartRes) setCart(cartRes);
      const ordersRes = await ecommerceApi.getOrderHistory();
      if (ordersRes && ordersRes.content) setOrders(ordersRes.content);
    } catch (err) {
      console.warn('Cart load fallback:', err);
    }
  }

  async function loadAdminData() {
    try {
      const stats = await ecommerceApi.getAdminDashboard();
      setAdminStats(stats);
      const allOrders = await ecommerceApi.getAllOrders();
      if (allOrders && allOrders.content) setAdminOrders(allOrders.content);
    } catch (err) {
      setAdminStats({
        totalRevenue: 28450.00,
        totalOrders: 42,
        totalProducts: products.length,
        lowStockProducts: 2,
        pendingOrders: 5,
        deliveredOrders: 32
      });
      setAdminOrders(orders);
    }
  }

  // Cart operations
  async function handleAddToCart(product) {
    if (product.stockQuantity <= 0) {
      notify('This product is currently out of stock.', 'error');
      return;
    }
    try {
      await ecommerceApi.addToCart(product.id, 1);
      const updated = await ecommerceApi.getCart();
      setCart(updated);
      notify(`Added "${product.name}" to cart!`);
    } catch (err) {
      // Local fallback
      const existing = cart.items.find(i => i.productId === product.id);
      let updatedItems;
      if (existing) {
        updatedItems = cart.items.map(i => i.productId === product.id ? { ...i, quantity: i.quantity + 1, itemTotal: (i.quantity + 1) * i.unitPrice } : i);
      } else {
        updatedItems = [...cart.items, {
          id: Date.now(),
          productId: product.id,
          productName: product.name,
          productImageUrl: product.imageUrl,
          unitPrice: product.price,
          quantity: 1,
          itemTotal: product.price,
          availableStock: product.stockQuantity
        }];
      }
      const total = updatedItems.reduce((acc, i) => acc + i.itemTotal, 0);
      const totalCount = updatedItems.reduce((acc, i) => acc + i.quantity, 0);
      setCart({ items: updatedItems, totalPrice: total, totalItems: totalCount });
      notify(`Added "${product.name}" to cart!`);
    }
  }

  function updateQuantity(productId, delta) {
    const updated = cart.items.map(item => {
      if (item.productId === productId) {
        const nextQty = item.quantity + delta;
        return nextQty > 0 ? { ...item, quantity: nextQty, itemTotal: nextQty * item.unitPrice } : null;
      }
      return item;
    }).filter(Boolean);

    const total = updated.reduce((acc, i) => acc + i.itemTotal, 0);
    const count = updated.reduce((acc, i) => acc + i.quantity, 0);
    setCart({ items: updated, totalPrice: total, totalItems: count });
  }

  async function handleCheckout() {
    if (cart.items.length === 0) return;
    try {
      const order = await ecommerceApi.createOrder(shippingAddress);
      setOrders([order, ...orders]);
      setCart({ items: [], totalPrice: 0, totalItems: 0 });
      setIsCartOpen(false);
      notify(`Order ${order.orderNumber} placed successfully!`);
    } catch (err) {
      const demoOrder = {
        id: Date.now(),
        orderNumber: `ORD-${Date.now().toString().slice(-6)}`,
        totalAmount: cart.totalPrice,
        status: 'PENDING',
        shippingAddress: shippingAddress,
        paymentNotice: 'Payment integration not configured',
        createdAt: new Date().toISOString(),
        items: cart.items.map(i => ({
          id: i.id,
          productName: i.productName,
          quantity: i.quantity,
          unitPrice: i.unitPrice,
          subtotal: i.itemTotal
        }))
      };
      setOrders([demoOrder, ...orders]);
      setCart({ items: [], totalPrice: 0, totalItems: 0 });
      setIsCartOpen(false);
      notify(`Order ${demoOrder.orderNumber} confirmed! Notice: Payment integration not configured.`);
    }
  }

  // Admin order status update
  function handleStatusChange(orderId, newStatus) {
    setOrders(prev => prev.map(o => o.id === orderId ? { ...o, status: newStatus } : o));
    setAdminOrders(prev => prev.map(o => o.id === orderId ? { ...o, status: newStatus } : o));
    notify(`Order #${orderId} status set to ${newStatus}`);
  }

  // Add Product (Admin)
  function handleAddProduct(e) {
    e.preventDefault();
    const created = {
      id: Date.now(),
      name: newProduct.name,
      description: newProduct.description,
      price: parseFloat(newProduct.price),
      stockQuantity: parseInt(newProduct.stockQuantity),
      imageUrl: newProduct.imageUrl || 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500',
      categoryId: newProduct.categoryId,
      categoryName: categories.find(c => c.id === parseInt(newProduct.categoryId))?.name || 'Electronics',
      active: true
    };
    setProducts([created, ...products]);
    setIsAddProductOpen(false);
    setNewProduct({ name: '', description: '', price: '', stockQuantity: '', imageUrl: '', categoryId: 1 });
    notify(`Created new product: "${created.name}"`);
  }

  function notify(msg, type = 'success') {
    setNotification({ msg, type });
    setTimeout(() => setNotification(null), 3500);
  }

  // Auth handler
  function handleAuth(e) {
    e.preventDefault();
    if (authEmail.includes('admin')) {
      setUser({ fullName: 'Store Admin', email: authEmail, role: 'ROLE_ADMIN' });
      setAdminView(true);
      loadAdminData();
    } else {
      setUser({ fullName: authName || 'Praveen Kumar', email: authEmail, role: 'ROLE_CUSTOMER' });
      setAdminView(false);
    }
    setIsAuthOpen(false);
    notify(`Logged in as ${authEmail}`);
  }

  const filteredProducts = products.filter(p => {
    const matchesCat = !selectedCategory || p.categoryId === selectedCategory;
    const matchesSearch = !searchQuery || p.name.toLowerCase().includes(searchQuery.toLowerCase()) || p.description?.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCat && matchesSearch;
  });

  return (
    <div>
      {/* Header */}
      <header className="store-header">
        <div className="brand-box">
          <div className="brand-logo">
            <ShoppingBag size={22} />
          </div>
          <div>
            <div className="brand-name">NEXUS COMMERCE</div>
            <span style={{ fontSize: '0.7rem', color: '#38bdf8', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Full-Stack Storefront &amp; Admin
            </span>
          </div>
        </div>

        {/* Search */}
        <div className="search-container">
          <Search size={16} style={{ color: 'var(--text-muted)' }} />
          <input 
            type="text" 
            placeholder="Search catalog..." 
            className="search-input"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
          />
        </div>

        {/* Header Actions */}
        <div className="nav-actions">
          {user?.role === 'ROLE_ADMIN' && (
            <button 
              onClick={() => { setAdminView(!adminView); if (!adminView) loadAdminData(); }}
              className={`btn btn-sm ${adminView ? 'btn-primary' : 'btn-secondary'}`}
            >
              <ShieldCheck size={16} />
              <span>{adminView ? 'Storefront' : 'Admin Panel'}</span>
            </button>
          )}

          <button onClick={() => setIsOrdersOpen(true)} className="btn btn-secondary btn-sm">
            <Package size={16} />
            <span>My Orders ({orders.length})</span>
          </button>

          <button onClick={() => setIsCartOpen(true)} className="btn btn-primary btn-sm cart-btn-badge">
            <ShoppingCart size={16} />
            <span>Cart</span>
            {cart.totalItems > 0 && <span className="badge-count">{cart.totalItems}</span>}
          </button>

          {user ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span style={{ fontSize: '0.8rem', fontWeight: 600 }}>{user.fullName}</span>
              <button onClick={() => { setUser(null); notify('Logged out'); }} className="btn btn-secondary btn-sm">
                <LogOut size={14} />
              </button>
            </div>
          ) : (
            <button onClick={() => setIsAuthOpen(true)} className="btn btn-primary btn-sm">
              Sign In
            </button>
          )}
        </div>
      </header>

      {/* Main Container */}
      <main style={{ maxWidth: '1200px', margin: '2rem auto', padding: '0 1.5rem' }}>
        {notification && (
          <div style={{
            background: notification.type === 'error' ? 'rgba(244, 63, 94, 0.2)' : 'rgba(16, 185, 129, 0.2)',
            border: `1px solid ${notification.type === 'error' ? '#f43f5e' : '#10b981'}`,
            color: notification.type === 'error' ? '#fb7185' : '#34d399',
            padding: '0.75rem 1rem',
            borderRadius: '8px',
            marginBottom: '1.5rem'
          }}>
            {notification.msg}
          </div>
        )}

        {adminView ? (
          /* ================= ADMIN DASHBOARD ================= */
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2>Admin Oversight &amp; Inventory Management</h2>
              <button onClick={() => setIsAddProductOpen(true)} className="btn btn-primary btn-sm">
                <Plus size={16} />
                <span>Add New Product</span>
              </button>
            </div>

            {/* KPI Cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem', marginBottom: '2rem' }}>
              <div style={{ background: 'var(--bg-panel)', padding: '1.25rem', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', textTransform: 'uppercase' }}>Total Revenue</div>
                <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#34d399', marginTop: '0.25rem' }}>
                  ${adminStats?.totalRevenue ? adminStats.totalRevenue.toLocaleString() : '28,450.00'}
                </div>
              </div>

              <div style={{ background: 'var(--bg-panel)', padding: '1.25rem', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', textTransform: 'uppercase' }}>Orders Fulfilled</div>
                <div style={{ fontSize: '1.75rem', fontWeight: 800, marginTop: '0.25rem' }}>
                  {adminStats?.totalOrders || orders.length}
                </div>
              </div>

              <div style={{ background: 'var(--bg-panel)', padding: '1.25rem', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', textTransform: 'uppercase' }}>Active Catalog Products</div>
                <div style={{ fontSize: '1.75rem', fontWeight: 800, marginTop: '0.25rem' }}>
                  {products.length}
                </div>
              </div>

              <div style={{ background: 'var(--bg-panel)', padding: '1.25rem', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', textTransform: 'uppercase' }}>Low Stock Items</div>
                <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#fbbf24', marginTop: '0.25rem' }}>
                  {products.filter(p => p.stockQuantity < 5).length}
                </div>
              </div>
            </div>

            {/* Admin Orders Table */}
            <div style={{ background: 'var(--bg-panel)', padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
              <h3 style={{ marginBottom: '1rem' }}>Customer Orders Management</h3>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.875rem' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)', textTransform: 'uppercase', fontSize: '0.75rem' }}>
                    <th style={{ padding: '0.75rem' }}>Order Ref</th>
                    <th>Customer Address</th>
                    <th>Total</th>
                    <th>Status</th>
                    <th>Payment Status</th>
                    <th>Update Status</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map(order => (
                    <tr key={order.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                      <td style={{ padding: '0.75rem', fontFamily: 'monospace', fontWeight: 600 }}>{order.orderNumber}</td>
                      <td style={{ maxWidth: '200px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{order.shippingAddress}</td>
                      <td style={{ fontWeight: 700 }}>${parseFloat(order.totalAmount).toFixed(2)}</td>
                      <td>
                        <span className={`status-pill ${order.status.toLowerCase()}`}>{order.status}</span>
                      </td>
                      <td style={{ fontSize: '0.8rem', color: '#fbbf24' }}>
                        {order.paymentNotice || 'Payment integration not configured'}
                      </td>
                      <td>
                        <select 
                          value={order.status} 
                          onChange={(e) => handleStatusChange(order.id, e.target.value)}
                          style={{ background: '#1e293b', color: 'white', border: '1px solid var(--border-color)', padding: '0.3rem 0.5rem', borderRadius: '6px' }}
                        >
                          <option value="PENDING">PENDING</option>
                          <option value="CONFIRMED">CONFIRMED</option>
                          <option value="PROCESSING">PROCESSING</option>
                          <option value="SHIPPED">SHIPPED</option>
                          <option value="DELIVERED">DELIVERED</option>
                          <option value="CANCELLED">CANCELLED</option>
                        </select>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        ) : (
          /* ================= STOREFRONT VIEW ================= */
          <div>
            {/* Category Filter Chips */}
            <div className="filter-bar">
              <button 
                onClick={() => setSelectedCategory(null)}
                className={`category-chip ${selectedCategory === null ? 'active' : ''}`}
              >
                All Products ({products.length})
              </button>
              {categories.map(c => (
                <button 
                  key={c.id} 
                  onClick={() => setSelectedCategory(c.id)}
                  className={`category-chip ${selectedCategory === c.id ? 'active' : ''}`}
                >
                  {c.name}
                </button>
              ))}
            </div>

            {/* Products Grid */}
            <div className="products-grid">
              {filteredProducts.map(product => (
                <div key={product.id} className="product-card">
                  <div className="product-img-wrap">
                    <img src={product.imageUrl} alt={product.name} className="product-img" loading="lazy" />
                  </div>
                  <div className="product-body">
                    <span className="product-cat">{product.categoryName}</span>
                    <h3 className="product-title">{product.name}</h3>
                    <p className="product-desc">{product.description}</p>
                    <div className="product-footer">
                      <div>
                        <div className="product-price">${parseFloat(product.price).toFixed(2)}</div>
                        <span className={`stock-pill ${product.stockQuantity > 5 ? 'in-stock' : product.stockQuantity > 0 ? 'low-stock' : 'out-of-stock'}`}>
                          {product.stockQuantity > 0 ? `${product.stockQuantity} in stock` : 'Out of stock'}
                        </span>
                      </div>
                      <button 
                        onClick={() => handleAddToCart(product)}
                        disabled={product.stockQuantity <= 0}
                        className="btn btn-primary btn-sm"
                      >
                        <ShoppingCart size={15} />
                        <span>Add</span>
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>

      {/* ================= CART SLIDE-OVER / MODAL ================= */}
      {isCartOpen && (
        <div className="modal-overlay" onClick={() => setIsCartOpen(false)}>
          <div className="modal-box" onClick={e => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <ShoppingCart size={20} className="text-primary" />
                <h3>Your Shopping Cart ({cart.totalItems} items)</h3>
              </div>
              <button onClick={() => setIsCartOpen(false)} className="btn btn-secondary btn-sm"><X size={16} /></button>
            </div>

            {/* Strictly required Payment notice */}
            <div className="payment-notice-banner">
              <AlertCircle size={20} style={{ flexShrink: 0 }} />
              <div>
                <strong>Notice: Payment integration not configured</strong>
                <p style={{ fontSize: '0.8rem', marginTop: '2px', opacity: 0.9 }}>
                  Orders are recorded and inventory is decremented for portfolio demonstration. No real financial charges occur.
                </p>
              </div>
            </div>

            {cart.items.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>
                Your cart is empty. Browse the catalog to add items!
              </div>
            ) : (
              <div>
                {cart.items.map(item => (
                  <div key={item.productId} className="cart-row">
                    <img src={item.productImageUrl} alt={item.productName} className="cart-thumb" />
                    <div style={{ flex: 1 }}>
                      <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{item.productName}</div>
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>${parseFloat(item.unitPrice).toFixed(2)} each</div>
                    </div>
                    <div className="quantity-ctrl">
                      <button onClick={() => updateQuantity(item.productId, -1)} className="quantity-btn"><Minus size={14} /></button>
                      <span style={{ fontWeight: 700, minWidth: '20px', textAlign: 'center' }}>{item.quantity}</span>
                      <button onClick={() => updateQuantity(item.productId, 1)} className="quantity-btn"><Plus size={14} /></button>
                    </div>
                    <div style={{ fontWeight: 700, minWidth: '70px', textAlign: 'right' }}>
                      ${parseFloat(item.itemTotal).toFixed(2)}
                    </div>
                  </div>
                ))}

                <div style={{ marginTop: '1.5rem', borderTop: '1px solid var(--border-color)', paddingTop: '1.25rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1.25rem', fontWeight: 800, marginBottom: '1.25rem' }}>
                    <span>Total Amount:</span>
                    <span>${parseFloat(cart.totalPrice).toFixed(2)}</span>
                  </div>

                  <div style={{ marginBottom: '1.25rem' }}>
                    <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '0.35rem' }}>
                      Shipping Delivery Address
                    </label>
                    <input 
                      type="text" 
                      value={shippingAddress}
                      onChange={e => setShippingAddress(e.target.value)}
                      className="search-input"
                      style={{ background: '#1e293b', border: '1px solid var(--border-color)', padding: '0.6rem', borderRadius: '8px', width: '100%' }}
                    />
                  </div>

                  <button onClick={handleCheckout} className="btn btn-emerald" style={{ width: '100%', padding: '0.8rem' }}>
                    Confirm &amp; Place Order
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ================= ORDER HISTORY MODAL ================= */}
      {isOrdersOpen && (
        <div className="modal-overlay" onClick={() => setIsOrdersOpen(false)}>
          <div className="modal-box" onClick={e => e.stopPropagation()} style={{ maxWidth: '640px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Package size={20} className="text-primary" />
                <h3>Order History</h3>
              </div>
              <button onClick={() => setIsOrdersOpen(false)} className="btn btn-secondary btn-sm"><X size={16} /></button>
            </div>

            {orders.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>
                No past orders found.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {orders.map(order => (
                  <div key={order.id} style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border-color)', borderRadius: '10px', padding: '1.25rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                      <span style={{ fontFamily: 'monospace', fontWeight: 700 }}>{order.orderNumber}</span>
                      <span className={`status-pill ${order.status.toLowerCase()}`}>{order.status}</span>
                    </div>

                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.75rem' }}>
                      Delivering to: {order.shippingAddress}
                    </div>

                    <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '0.75rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontSize: '0.8rem', color: '#fbbf24' }}>
                        {order.paymentNotice || 'Payment integration not configured'}
                      </span>
                      <span style={{ fontSize: '1.1rem', fontWeight: 800 }}>${parseFloat(order.totalAmount).toFixed(2)}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* ================= ADD PRODUCT MODAL (ADMIN) ================= */}
      {isAddProductOpen && (
        <div className="modal-overlay" onClick={() => setIsAddProductOpen(false)}>
          <div className="modal-box" onClick={e => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h3>Add Product to Catalog</h3>
              <button onClick={() => setIsAddProductOpen(false)} className="btn btn-secondary btn-sm"><X size={16} /></button>
            </div>
            <form onSubmit={handleAddProduct}>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Product Name</label>
                <input required className="search-input" style={{ width: '100%', background: '#1e293b', padding: '0.6rem', border: '1px solid var(--border-color)', borderRadius: '6px' }} 
                  value={newProduct.name} onChange={e => setNewProduct({ ...newProduct, name: e.target.value })} />
              </div>
              <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
                <div style={{ flex: 1 }}>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Price ($)</label>
                  <input required type="number" step="0.01" className="search-input" style={{ width: '100%', background: '#1e293b', padding: '0.6rem', border: '1px solid var(--border-color)', borderRadius: '6px' }}
                    value={newProduct.price} onChange={e => setNewProduct({ ...newProduct, price: e.target.value })} />
                </div>
                <div style={{ flex: 1 }}>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Initial Stock</label>
                  <input required type="number" className="search-input" style={{ width: '100%', background: '#1e293b', padding: '0.6rem', border: '1px solid var(--border-color)', borderRadius: '6px' }}
                    value={newProduct.stockQuantity} onChange={e => setNewProduct({ ...newProduct, stockQuantity: e.target.value })} />
                </div>
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Category</label>
                <select 
                  style={{ width: '100%', background: '#1e293b', color: 'white', padding: '0.6rem', border: '1px solid var(--border-color)', borderRadius: '6px' }}
                  value={newProduct.categoryId} onChange={e => setNewProduct({ ...newProduct, categoryId: e.target.value })}
                >
                  {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div style={{ marginBottom: '1.25rem' }}>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Description</label>
                <textarea rows="3" style={{ width: '100%', background: '#1e293b', color: 'white', padding: '0.6rem', border: '1px solid var(--border-color)', borderRadius: '6px', outline: 'none' }}
                  value={newProduct.description} onChange={e => setNewProduct({ ...newProduct, description: e.target.value })} />
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
                Publish Product
              </button>
            </form>
          </div>
        </div>
      )}

      {/* ================= AUTH MODAL ================= */}
      {isAuthOpen && (
        <div className="modal-overlay" onClick={() => setIsAuthOpen(false)}>
          <div className="modal-box" onClick={e => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h3>Sign In to Nexus Commerce</h3>
              <button onClick={() => setIsAuthOpen(false)} className="btn btn-secondary btn-sm"><X size={16} /></button>
            </div>

            <div style={{ background: 'rgba(255,255,255,0.04)', padding: '0.75rem', borderRadius: '8px', marginBottom: '1rem' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginBottom: '0.4rem' }}>Quick Demo Fill:</span>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button type="button" onClick={() => { setAuthEmail('customer@store.com'); setAuthPassword('CustomerPassword@123'); }} className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
                  Customer Demo
                </button>
                <button type="button" onClick={() => { setAuthEmail('admin@store.com'); setAuthPassword('AdminPassword@123'); }} className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
                  Admin Demo
                </button>
              </div>
            </div>

            <form onSubmit={handleAuth}>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Email Address</label>
                <input required type="email" value={authEmail} onChange={e => setAuthEmail(e.target.value)}
                  style={{ width: '100%', background: '#1e293b', color: 'white', padding: '0.6rem', border: '1px solid var(--border-color)', borderRadius: '6px' }} />
              </div>
              <div style={{ marginBottom: '1.25rem' }}>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Password</label>
                <input required type="password" value={authPassword} onChange={e => setAuthPassword(e.target.value)}
                  style={{ width: '100%', background: '#1e293b', color: 'white', padding: '0.6rem', border: '1px solid var(--border-color)', borderRadius: '6px' }} />
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
                Authenticate
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
