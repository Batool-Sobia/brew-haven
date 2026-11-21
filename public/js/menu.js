/**
 * ==============================================================================
 * Brew Haven — Menu Page Interaction Script
 * Handles fetching menu from the Java backend and category filtering.
 * ==============================================================================
 */

// Cache of loaded products
let productsList = [];
let currentCategory = 'all';

document.addEventListener('DOMContentLoaded', () => {
    loadMenu();
    setupCategoryFilters();
});

/**
 * Fetches products from the Java HttpServer backend endpoint: GET /api/products
 */
async function loadMenu() {
    const grid = document.getElementById('products-grid');
    if (!grid) return;

    grid.innerHTML = `
        <div style="grid-column: 1/-1; text-align: center; padding: 40px;">
            <p>Brewing fresh menu items from server...</p>
        </div>
    `;

    try {
        const response = await fetch('/api/products');
        if (!response.ok) {
            throw new Error(`Server returned HTTP ${response.status}`);
        }
        const data = await response.json();
        if (data.success && Array.isArray(data.products)) {
            productsList = data.products;
            renderProducts();
        } else {
            throw new Error(data.error || 'Invalid menu data');
        }
    } catch (error) {
        console.error('Failed to load menu:', error);
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--color-danger);">
                <p>Could not connect to the Java backend server.</p>
                <p style="font-size: 0.85rem; margin-top: 8px;">Make sure the Java server is running in your terminal.</p>
                <button class="btn btn-outline btn-sm" onclick="loadMenu()" style="margin-top: 14px;">Try Again</button>
            </div>
        `;
    }
}

/**
 * Filters and renders products based on the selected category tab.
 */
function renderProducts() {
    const grid = document.getElementById('products-grid');
    if (!grid) return;

    const filtered = currentCategory === 'all' 
        ? productsList 
        : productsList.filter(p => p.category.toLowerCase() === currentCategory.toLowerCase());

    if (filtered.length === 0) {
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 50px;">
                <p>No products found in this category.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = filtered.map(product => `
        <div class="product-card" data-id="${product.id}">
            <div class="product-img-wrapper">
                <span class="badge badge-caramel product-category-badge">${escapeHtml(product.category)}</span>
                <img src="${product.image}" alt="${escapeHtml(product.name)}" class="product-img" onerror="this.src='/images/espresso.svg'">
            </div>
            <div class="product-info">
                <h3 class="product-name">${escapeHtml(product.name)}</h3>
                <p class="product-desc">${escapeHtml(product.description)}</p>
                <div class="product-footer">
                    <div class="product-price">
                        <span>Rs.</span>${Number(product.price).toLocaleString()}
                    </div>
                    <button class="btn-add-cart" onclick="handleAddToCart(${product.id})">
                        <span>Add to Cart</span>
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

/**
 * Handles adding an item to the shopping cart.
 */
function handleAddToCart(productId) {
    const product = productsList.find(p => p.id === productId);
    if (!product) return;

    addToCart(product, 1);
}

/**
 * Sets up category filter buttons (All, Hot Coffees, Cold Brews & Iced, Bakery & Treats)
 */
function setupCategoryFilters() {
    const filterButtons = document.querySelectorAll('.filter-btn');
    filterButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            filterButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentCategory = btn.getAttribute('data-category') || 'all';
            renderProducts();
        });
    });
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
