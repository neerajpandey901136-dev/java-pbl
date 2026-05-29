let userId = localStorage.getItem('userId');
let username = localStorage.getItem('username');
let chartInstance = null;
let currentTransactions = [];

const API_BASE_URL = 'http://localhost:8080/api';

if (!userId) {
    window.location.href = 'index.html';
}

document.getElementById('userNameDisplay').innerText = `Welcome, ${username}`;

function logout() {
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    window.location.href = 'index.html';
}

async function loadTransactions() {
    try {
        const response = await fetch(`${API_BASE_URL}/transactions/user/${userId}`);
        if (response.ok) {
            currentTransactions = await response.json();
            // Sort by datetime descending
            currentTransactions.sort((a, b) => new Date(b.dateTime) - new Date(a.dateTime));
            updateDashboard(currentTransactions);
        } else {
            console.error('Failed to fetch transactions from server');
        }
    } catch (error) {
        console.error('Error fetching transactions:', error);
    }
}

function updateDashboard(transactions) {
    renderTable(transactions);
    calculateTotals(transactions);
    updateChart(transactions);
}

function calculateTotals(transactions) {
    let income = 0;
    let expense = 0;

    transactions.forEach(t => {
        if (t.type === 'CREDIT') income += t.amount;
        if (t.type === 'DEBIT') expense += t.amount;
    });

    const balance = income - expense;

    document.getElementById('totalIncome').innerText = `₹${income.toFixed(2)}`;
    document.getElementById('totalExpense').innerText = `₹${expense.toFixed(2)}`;
    document.getElementById('totalBalance').innerText = `₹${balance.toFixed(2)}`;
}

function renderTable(transactions) {
    const tbody = document.getElementById('transactionsTableBody');
    tbody.innerHTML = '';

    if (transactions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">No transactions yet. Add one or simulate an SMS!</td></tr>';
        return;
    }

    transactions.forEach(t => {
        const tr = document.createElement('tr');
        const date = new Date(t.dateTime).toLocaleString();
        const amountClass = t.type === 'CREDIT' ? 'text-success' : 'text-danger';
        const sign = t.type === 'CREDIT' ? '+' : '-';
        const typeBadgeClass = t.type === 'CREDIT' ? 'badge-credit' : 'badge-debit';
        const isSmsIcon = t.fromSms ? '📱' : '';

        tr.innerHTML = `
            <td style="font-size: 0.875rem; color: var(--text-muted);">${date}</td>
            <td style="font-weight: 500;">${t.merchant || 'Unknown'} ${isSmsIcon}</td>
            <td><span class="badge badge-category">${t.category || 'Others'}</span></td>
            <td><span class="badge ${typeBadgeClass}">${t.type}</span></td>
            <td class="${amountClass}" style="font-weight: 600;">${sign}₹${t.amount.toFixed(2)}</td>
            <td>
                <button class="btn btn-secondary" style="padding: 0.25rem 0.5rem; font-size: 0.75rem;" onclick="deleteTransaction(${t.id})">Delete</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function updateChart(transactions) {
    const ctx = document.getElementById('categoryChart').getContext('2d');
    
    const expensesByCategory = {};
    let hasData = false;
    transactions.forEach(t => {
        if (t.type === 'DEBIT') {
            const category = t.category || 'Others';
            expensesByCategory[category] = (expensesByCategory[category] || 0) + t.amount;
            hasData = true;
        }
    });

    const data = {
        labels: hasData ? Object.keys(expensesByCategory) : ['No Expenses'],
        datasets: [{
            data: hasData ? Object.values(expensesByCategory) : [1],
            backgroundColor: hasData ? [
                '#4F46E5', '#10B981', '#EF4444', '#F59E0B', '#8B5CF6', '#EC4899'
            ] : ['#334155'],
            borderWidth: 0
        }]
    };

    if (chartInstance) {
        chartInstance.destroy();
    }

    chartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: data,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'right', labels: { color: '#F8FAFC' } }
            }
        }
    });
}


function openAddModal() { document.getElementById('addModal').classList.add('active'); }
function closeAddModal() { document.getElementById('addModal').classList.remove('active'); }

document.getElementById('addTxForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const tx = {
        amount: parseFloat(document.getElementById('txAmount').value),
        type: document.getElementById('txType').value,
        category: document.getElementById('txCategory').value,
        merchant: document.getElementById('txMerchant').value,
        description: 'Manual entry',
        dateTime: new Date().toISOString(),
        fromSms: false
    };

    try {
        const response = await fetch(`${API_BASE_URL}/transactions/user/${userId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(tx)
        });
        if (response.ok) {
            closeAddModal();
            document.getElementById('addTxForm').reset();
            await loadTransactions();
        } else {
            const err = await response.json();
            alert(err.message || 'Failed to add transaction.');
        }
    } catch (error) {
        console.error('Error adding transaction:', error);
        alert('Connection error. Could not connect to backend server.');
    }
});

async function deleteTransaction(id) {
    if (!confirm('Are you sure you want to delete this transaction?')) return;
    try {
        const response = await fetch(`${API_BASE_URL}/transactions/${id}`, {
            method: 'DELETE'
        });
        if (response.ok) {
            await loadTransactions();
        } else {
            alert('Failed to delete transaction.');
        }
    } catch (error) {
        console.error('Error deleting transaction:', error);
        alert('Connection error. Could not connect to backend server.');
    }
}

async function simulateSMS() {
    const sms = document.getElementById('smsInput').value;
    const statusDiv = document.getElementById('smsStatus');
    
    if (!sms) {
        statusDiv.innerText = 'Please paste an SMS';
        statusDiv.style.color = 'var(--danger)';
        return;
    }

    statusDiv.innerText = 'Processing via Spring Boot SMS Parser...';
    statusDiv.style.color = 'var(--text-muted)';

    try {
        const response = await fetch(`${API_BASE_URL}/sms/parse/${userId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sms })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            statusDiv.innerText = `Success! Parsed as ₹${data.amount} ${data.type} at ${data.merchant}`;
            statusDiv.style.color = 'var(--secondary)';
            document.getElementById('smsInput').value = '';
            await loadTransactions();
        } else {
            statusDiv.innerText = `Failed: ${data.message || 'Could not extract transaction details.'}`;
            statusDiv.style.color = 'var(--danger)';
        }
    } catch (error) {
        console.error('Error simulating SMS:', error);
        statusDiv.innerText = 'Connection error. Check if backend is running.';
        statusDiv.style.color = 'var(--danger)';
    }
}

loadTransactions();
