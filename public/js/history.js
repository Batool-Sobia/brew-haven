/**
 * ==============================================================================
 * Brew Haven — Sales History & Daily Summary Interaction Script
 * Reads persistent sales records and real-time daily metrics from Java backend.
 * ==============================================================================
 */

let allSalesList = [];

document.addEventListener('DOMContentLoaded', () => {
    loadDailySummary();
    loadSalesHistory();
    setupSearch();
});

/**
 * Fetches today's sales metrics from Java backend: GET /api/summary
 */
async function loadDailySummary() {
    try {
        const response = await fetch('/api/summary');
        if (!response.ok) throw new Error('Could not fetch daily summary');

        const data = await response.json();
        if (data.success && data.summary) {
            const sum = data.summary;
            const totalEl = document.getElementById('metric-today-sales');
            const ordersEl = document.getElementById('metric-today-orders');
            const itemsEl = document.getElementById('metric-today-items');

            if (totalEl) totalEl.textContent = formatPKR(sum.totalSales);
            if (ordersEl) ordersEl.textContent = sum.transactionCount;
            if (itemsEl) itemsEl.textContent = sum.totalItemsSold;
        }
    } catch (e) {
        console.error('Error loading daily summary', e);
    }
}

/**
 * Fetches all completed sales from Java backend: GET /api/sales
 */
async function loadSalesHistory() {
    const tableBody = document.getElementById('history-table-body');
    if (!tableBody) return;

    tableBody.innerHTML = `
        <tr>
            <td colspan="6" style="text-align: center; padding: 30px;">
                Loading permanent sales records from server files...
            </td>
        </tr>
    `;

    try {
        const response = await fetch('/api/sales');
        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const data = await response.json();
        if (data.success && Array.isArray(data.sales)) {
            allSalesList = data.sales;
            renderSalesTable(allSalesList);
        } else {
            throw new Error(data.error || 'Unable to read sales records');
        }
    } catch (error) {
        console.error('Sales history error:', error);
        tableBody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align: center; padding: 30px; color: var(--color-danger);">
                    Could not retrieve sales history from the Java server.
                    <div style="margin-top: 10px;">
                        <button class="btn btn-outline btn-sm" onclick="loadSalesHistory()">Retry</button>
                    </div>
                </td>
            </tr>
        `;
    }
}

/**
 * Renders the table of sales records.
 */
function renderSalesTable(sales) {
    const tableBody = document.getElementById('history-table-body');
    const countDisplay = document.getElementById('history-record-count');
    if (!tableBody) return;

    if (countDisplay) {
        countDisplay.textContent = `${sales.length} record${sales.length === 1 ? '' : 's'} found`;
    }

    if (sales.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align: center; padding: 40px; color: var(--color-muted-text);">
                    No sales records found yet. Complete a checkout to create your first sale!
                </td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = sales.map(sale => `
        <tr>
            <td>
                <a href="receipt.html?id=${encodeURIComponent(sale.receiptId)}" class="receipt-id-link">
                    ${escapeHtml(sale.receiptId)}
                </a>
            </td>
            <td>${escapeHtml(sale.timestamp)}</td>
            <td>
                <strong>${escapeHtml(sale.customerName || 'Valued Guest')}</strong>
                <div style="font-size: 0.8rem; color: var(--color-muted-text);">${escapeHtml(sale.orderType || 'Dine-in')}</div>
            </td>
            <td>${sale.totalItemsCount || (sale.items ? sale.items.length : 0)} items</td>
            <td>
                <strong>${formatPKR(sale.finalTotal)}</strong>
                ${sale.discountPercent > 0 ? `<div style="font-size: 0.75rem; color: var(--color-success);">${sale.discountPercent}% off applied</div>` : ''}
            </td>
            <td>
                <a href="receipt.html?id=${encodeURIComponent(sale.receiptId)}" class="btn btn-outline btn-sm">
                    View Receipt
                </a>
            </td>
        </tr>
    `).join('');
}

/**
 * Sets up live search for receipt ID, customer name, etc.
 */
function setupSearch() {
    const searchInput = document.getElementById('history-search-input');
    if (!searchInput) return;

    searchInput.addEventListener('input', () => {
        const query = searchInput.value.trim().toLowerCase();
        if (!query) {
            renderSalesTable(allSalesList);
            return;
        }

        const filtered = allSalesList.filter(s => {
            const idMatch = s.receiptId && s.receiptId.toLowerCase().includes(query);
            const nameMatch = s.customerName && s.customerName.toLowerCase().includes(query);
            const dateMatch = s.timestamp && s.timestamp.toLowerCase().includes(query);
            return idMatch || nameMatch || dateMatch;
        });

        renderSalesTable(filtered);
    });
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
