/**
 * ==============================================================================
 * Brew Haven — Core Application Utilities
 * Beginner-friendly, thoroughly commented JavaScript module.
 * ==============================================================================
 */

// Storage key for active shopping cart state in the browser
const CART_STORAGE_KEY = 'brew_haven_cart';

/**
 * Retrieves the current cart array from localStorage.
 * Returns an array of items: [{ id, name, price, quantity, image, category }, ...]
 */
function getCart() {
    try {
        const stored = localStorage.getItem(CART_STORAGE_KEY);
        return stored ? JSON.parse(stored) : [];
    } catch (e) {
        console.error('Error reading cart from localStorage', e);
        return [];
    }
}

/**
 * Saves the updated cart array to localStorage and updates the navigation badge.
 */
function saveCart(cart) {
    try {
        localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart));
        updateNavBadge();
    } catch (e) {
        console.error('Error saving cart to localStorage', e);
    }
}

/**
 * Adds a product to the cart or increments quantity if it already exists.
 */
function addToCart(product, quantity = 1) {
    if (!product || !product.id) return;
    const qty = parseInt(quantity, 10);
    if (isNaN(qty) || qty <= 0) return;

    let cart = getCart();
    const existingIndex = cart.findIndex(item => item.id === product.id);

    if (existingIndex > -1) {
        cart[existingIndex].quantity += qty;
    } else {
        cart.push({
            id: product.id,
            name: product.name,
            price: parseFloat(product.price),
            quantity: qty,
            image: product.image,
            category: product.category
        });
    }

    saveCart(cart);
    showToast(`Added ${qty}x ${product.name} to cart!`, 'success');
}

/**
 * Updates the quantity for a specific product in the cart.
 * Prevents quantities from becoming zero or negative.
 */
function updateItemQty(productId, newQty) {
    let cart = getCart();
    const item = cart.find(i => i.id === productId);
    if (!item) return;

    const parsedQty = parseInt(newQty, 10);
    if (parsedQty > 0) {
        item.quantity = parsedQty;
        saveCart(cart);
    }
}

/**
 * Removes a product completely from the cart.
 */
function removeFromCart(productId) {
    let cart = getCart();
    const filtered = cart.filter(i => i.id !== productId);
    saveCart(filtered);
    showToast('Item removed from cart', 'info');
}

/**
 * Clears the shopping cart after a successful checkout.
 */
function clearCart() {
    localStorage.removeItem(CART_STORAGE_KEY);
    updateNavBadge();
}

/**
 * Calculates total number of items in the cart.
 */
function getCartCount() {
    const cart = getCart();
    return cart.reduce((sum, item) => sum + (item.quantity || 0), 0);
}

/**
 * Calculates the total monetary bill of the cart in the browser.
 */
function getCartTotal() {
    const cart = getCart();
    return cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
}

/**
 * Formats a numeric value into Pakistani Rupees (PKR) format: "Rs. 450"
 */
function formatPKR(amount) {
    const num = Number(amount) || 0;
    return `Rs. ${num.toLocaleString('en-PK', { minimumFractionDigits: 0, maximumFractionDigits: 2 })}`;
}

/**
 * Updates the cart counter badge in the navigation bar across all pages.
 */
function updateNavBadge() {
    const count = getCartCount();
    const badgeElements = document.querySelectorAll('.cart-badge');
    badgeElements.forEach(badge => {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'flex' : 'none';
    });
}

/**
 * Displays a friendly toast notification in the bottom right corner of the screen.
 */
function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    // Icon selection
    const icon = type === 'success' ? '✔' : (type === 'error' ? '✖' : 'ℹ');
    toast.innerHTML = `<span style="font-weight: bold;">${icon}</span> <span>${message}</span>`;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3200);
}

// Mobile navigation menu toggle
document.addEventListener('DOMContentLoaded', () => {
    updateNavBadge();

    const mobileToggle = document.querySelector('.mobile-nav-toggle');
    const navMenu = document.querySelector('.nav-menu');
    if (mobileToggle && navMenu) {
        mobileToggle.addEventListener('click', () => {
            navMenu.classList.toggle('open');
        });
    }
});
