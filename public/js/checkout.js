/**
 * ==============================================================================
 * Brew Haven — Checkout & Order Processing Script
 * Handles order confirmation, discount calculation, backend submission,
 * and seamless redirect to printable receipt.
 * ==============================================================================
 */

// Known discount codes and percentages for client-side preview
const DISCOUNT_PROMOS = {
    'WELCOME10': 10,
    'STUDENT15': 15,
    'BREW20': 20
};

let appliedDiscountCode = '';
let appliedDiscountPercent = 0;

document.addEventListener('DOMContentLoaded', () => {
    initCheckout();
});

/**
 * Initializes the checkout view and verifies that the cart is not empty.
 */
function initCheckout() {
    const cart = getCart();

    if (cart.length === 0) {
        alert('Your cart is empty! Please select some items from the menu first.');
        window.location.href = 'menu.html';
        return;
    }

    renderCheckoutItems(cart);
    calculateTotals();
    setupCouponHandler();
    setupFormSubmission();
}

/**
 * Renders the preview of ordered items in the checkout summary.
 */
function renderCheckoutItems(cart) {
    const list = document.getElementById('checkout-items-list');
    if (!list) return;

    list.innerHTML = cart.map(item => `
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-size: 0.95rem;">
            <div>
                <strong>${item.quantity}x</strong> ${escapeHtml(item.name)}
                <div style="font-size: 0.8rem; color: var(--color-muted-text);">${formatPKR(item.price)} each</div>
            </div>
            <div style="font-weight: 600; color: var(--color-coffee-brown);">
                ${formatPKR(item.price * item.quantity)}
            </div>
        </div>
    `).join('');
}

/**
 * Sets up promo code application buttons and input listener.
 */
function setupCouponHandler() {
    const applyBtn = document.getElementById('btn-apply-coupon');
    const couponInput = document.getElementById('coupon-code-input');
    const couponMsg = document.getElementById('coupon-message');

    if (!applyBtn || !couponInput) return;

    applyBtn.addEventListener('click', () => {
        applyCouponCode(couponInput.value.trim().toUpperCase(), couponMsg);
    });

    couponInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            applyCouponCode(couponInput.value.trim().toUpperCase(), couponMsg);
        }
    });
}

function applyCouponCode(code, msgElement) {
    if (!code) {
        appliedDiscountCode = '';
        appliedDiscountPercent = 0;
        if (msgElement) msgElement.innerHTML = '';
        calculateTotals();
        return;
    }

    if (DISCOUNT_PROMOS[code]) {
        appliedDiscountCode = code;
        appliedDiscountPercent = DISCOUNT_PROMOS[code];
        if (msgElement) {
            msgElement.innerHTML = `<span style="color: var(--color-success); font-weight: 600;">✔ Coupon applied: ${appliedDiscountPercent}% discount!</span>`;
        }
        showToast(`Promo ${code} applied (-${appliedDiscountPercent}%)`, 'success');
    } else {
        appliedDiscountCode = '';
        appliedDiscountPercent = 0;
        if (msgElement) {
            msgElement.innerHTML = `<span style="color: var(--color-danger); font-weight: 600;">✖ Invalid promo code</span>`;
        }
        showToast('Invalid promo code', 'error');
    }

    calculateTotals();
}

/**
 * Helper to click coupon chips on the checkout page.
 */
function usePromo(code) {
    const couponInput = document.getElementById('coupon-code-input');
    const couponMsg = document.getElementById('coupon-message');
    if (couponInput) {
        couponInput.value = code;
        applyCouponCode(code, couponMsg);
    }
}

/**
 * Calculates and updates subtotal, discount, and grand total in the UI.
 */
function calculateTotals() {
    const subtotal = getCartTotal();
    const discountAmount = subtotal * (appliedDiscountPercent / 100);
    const grandTotal = Math.max(0, subtotal - discountAmount);

    const subtotalEl = document.getElementById('checkout-subtotal');
    const discountRow = document.getElementById('checkout-discount-row');
    const discountEl = document.getElementById('checkout-discount-amt');
    const grandTotalEl = document.getElementById('checkout-grand-total');

    if (subtotalEl) subtotalEl.textContent = formatPKR(subtotal);

    if (discountRow) {
        if (appliedDiscountPercent > 0) {
            discountRow.style.display = 'flex';
            if (discountEl) discountEl.textContent = `- ${formatPKR(discountAmount)} (${appliedDiscountPercent}%)`;
        } else {
            discountRow.style.display = 'none';
        }
    }

    if (grandTotalEl) grandTotalEl.textContent = formatPKR(grandTotal);
}

/**
 * Handles order submission to Java backend: POST /api/sales
 */
function setupFormSubmission() {
    const checkoutForm = document.getElementById('checkout-form');
    const submitBtn = document.getElementById('btn-complete-sale');

    if (!checkoutForm || !submitBtn) return;

    checkoutForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const cart = getCart();
        if (cart.length === 0) {
            alert('Your cart is empty!');
            return;
        }

        const customerName = document.getElementById('customer-name')?.value.trim() || 'Valued Guest';
        const orderType = document.getElementById('order-type')?.value || 'Dine-in';

        // Prepare backend payload with minimal structure
        const payload = {
            customerName: customerName,
            orderType: orderType,
            discountCode: appliedDiscountCode,
            discountPercent: appliedDiscountPercent,
            items: cart.map(item => ({
                productId: item.id,
                quantity: item.quantity
            }))
        };

        // Disable button to prevent double-submit
        submitBtn.disabled = true;
        const originalText = submitBtn.innerHTML;
        submitBtn.innerHTML = '<span>Processing Sale on Java Server...</span>';

        try {
            const response = await fetch('/api/sales', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const result = await response.json();

            if (response.ok && result.success && result.sale) {
                // CLEAR CART ONLY AFTER SUCCESSFUL BACKEND PERSISTENCE
                clearCart();

                // Redirect to receipt page with unique receipt ID
                window.location.href = `receipt.html?id=${encodeURIComponent(result.sale.receiptId)}`;
            } else {
                throw new Error(result.error || 'Server rejected the order.');
            }
        } catch (error) {
            console.error('Checkout error:', error);
            alert(`Sale could not be completed: ${error.message}`);
            showToast('Sale failed to record on server', 'error');
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        }
    });
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
