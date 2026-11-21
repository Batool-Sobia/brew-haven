/**
 * ==============================================================================
 * Brew Haven — Shopping Cart Interaction Script
 * Manages item list, quantity updates, subtotals, and checkout transition.
 * ==============================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    renderCart();
});

/**
 * Renders the shopping cart items and calculated totals on cart.html.
 */
function renderCart() {
    const cart = getCart();
    const cartItemsContainer = document.getElementById('cart-items-container');
    const emptyCartContainer = document.getElementById('empty-cart-view');
    const cartLayout = document.getElementById('cart-layout');

    if (!cartItemsContainer) return;

    if (cart.length === 0) {
        if (cartLayout) cartLayout.style.display = 'none';
        if (emptyCartContainer) emptyCartContainer.style.display = 'block';
        return;
    }

    if (cartLayout) cartLayout.style.display = 'grid';
    if (emptyCartContainer) emptyCartContainer.style.display = 'none';

    cartItemsContainer.innerHTML = cart.map(item => {
        const itemSubtotal = item.price * item.quantity;
        return `
            <tr>
                <td>
                    <div class="cart-item-info">
                        <img src="${item.image || '/images/espresso.svg'}" alt="${escapeHtml(item.name)}" class="cart-item-img" onerror="this.src='/images/espresso.svg'">
                        <div>
                            <div class="cart-item-title">${escapeHtml(item.name)}</div>
                            <div class="cart-item-price">${formatPKR(item.price)} each</div>
                        </div>
                    </div>
                </td>
                <td>
                    <div class="qty-control">
                        <button class="qty-btn" onclick="changeQty(${item.id}, ${item.quantity - 1})" aria-label="Decrease quantity" ${item.quantity <= 1 ? 'disabled' : ''}>-</button>
                        <span class="qty-display">${item.quantity}</span>
                        <button class="qty-btn" onclick="changeQty(${item.id}, ${item.quantity + 1})" aria-label="Increase quantity">+</button>
                    </div>
                </td>
                <td>
                    <span class="cart-subtotal">${formatPKR(itemSubtotal)}</span>
                </td>
                <td>
                    <button class="btn-remove-item" onclick="removeItem(${item.id})" title="Remove item">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="3 6 5 6 21 6"></polyline>
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                            <line x1="10" y1="11" x2="10" y2="17"></line>
                            <line x1="14" y1="11" x2="14" y2="17"></line>
                        </svg>
                    </button>
                </td>
            </tr>
        `;
    }).join('');

    // Update Summary Box
    updateCartSummary(cart);
}

/**
 * Handles modifying quantity with safety checks.
 */
function changeQty(productId, newQty) {
    if (newQty < 1) return; // Prevent zero or negative quantities
    updateItemQty(productId, newQty);
    renderCart();
}

/**
 * Removes an item and re-renders cart.
 */
function removeItem(productId) {
    removeFromCart(productId);
    renderCart();
}

/**
 * Prompts user and clears the cart.
 */
function handleClearCart() {
    if (confirm('Are you sure you want to clear your cart?')) {
        clearCart();
        renderCart();
        showToast('Cart cleared', 'info');
    }
}

/**
 * Updates summary numbers in the side card.
 */
function updateCartSummary(cart) {
    const totalItems = getCartCount();
    const subtotal = getCartTotal();

    const totalItemsEl = document.getElementById('summary-total-items');
    const subtotalEl = document.getElementById('summary-subtotal');
    const grandTotalEl = document.getElementById('summary-grand-total');

    if (totalItemsEl) totalItemsEl.textContent = totalItems;
    if (subtotalEl) subtotalEl.textContent = formatPKR(subtotal);
    if (grandTotalEl) grandTotalEl.textContent = formatPKR(subtotal);
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
