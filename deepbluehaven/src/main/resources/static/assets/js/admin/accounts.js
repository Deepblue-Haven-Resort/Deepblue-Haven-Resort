const dropdowns = document.querySelectorAll(".filter-dropdown");

dropdowns.forEach((dropdown) => {
    const btn = dropdown.querySelector(".filter-btn");
    const label = dropdown.querySelector(".filter-label");
    const items = dropdown.querySelectorAll(".filter-menu button");

    btn.addEventListener("click", (e) => {
        e.stopPropagation();

        dropdowns.forEach((item) => {
            if (item !== dropdown) {
                item.classList.remove("active");
            }
        });

        dropdown.classList.toggle("active");
    });

    items.forEach((item) => {
        item.addEventListener("click", (e) => {
            e.stopPropagation();

            if (label) {
                label.textContent = item.textContent.trim();
            }

            dropdown.classList.remove("active");
        });
    });
});

document.addEventListener("click", () => {
    dropdowns.forEach((dropdown) => {
        dropdown.classList.remove("active");
    });
});


const state = {
    search: "",
    role: "",
    status: "",
    page: 0,
};

const accountTableBody = document.getElementById("accountTableBody");
const accountSearchInput = document.getElementById("accountSearchInput");
const roleFilterMenu = document.getElementById("roleFilterMenu");
const statusFilterMenu = document.getElementById("statusFilterMenu");
const accountTableSummary = document.getElementById("accountTableSummary");
const accountPagination = document.getElementById("accountPagination");

function getApiUrl(path) {
    const contextPath = document.querySelector('meta[name="_context_path"]')?.content || "/";
    return contextPath + path;
}

function getHeaders(extra = {}) {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;

    const headers = { ...extra };
    if (token && header) headers[header] = token;

    return headers;
}

function notify(type, title, message) {
    if (typeof showToast === "function") {
        showToast(type, title, message);
    }
}

async function loadStats() {
    try {
        const res = await fetch(getApiUrl("admin/accounts/stats"));
        if (!res.ok) return;

        const stats = await res.json();

        document.getElementById("statTotalAccounts").textContent = stats.totalAccounts;
        document.getElementById("statActive").textContent = stats.active;
        document.getElementById("statEmployees").textContent = stats.employees;
        document.getElementById("statLocked").textContent = stats.locked;
    } catch (err) {
        notify("error", "Load failed", "Could not load account stats.");
    }
}

async function loadAccounts() {
    if (!accountTableBody) return;

    const params = new URLSearchParams();
    if (state.search) params.set("search", state.search);
    if (state.role) params.set("role", state.role);
    if (state.status) params.set("status", state.status);
    params.set("page", state.page);

    try {
        const res = await fetch(getApiUrl(`admin/accounts/list?${params.toString()}`));
        if (!res.ok) {
            notify("error", "Load failed", "Could not load accounts.");
            return;
        }

        const data = await res.json();
        renderTable(data.items);
        renderSummary(data);
        renderPagination(data);
    } catch (err) {
        notify("error", "Network error", "Could not reach the server.");
    }
}

function renderTable(items) {
    accountTableBody.innerHTML = "";

    if (!items || items.length === 0) {
        accountTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No accounts found</td></tr>`;
        return;
    }

    items.forEach((worker) => {
        const isActive = worker.status === "ACTIVE";
        const roleClass = (worker.role || "").toLowerCase();
        const statusClass = isActive ? "status-success" : "status-error";
        const statusLabel = isActive ? "Active" : "Locked";
        const roleLabel = worker.role ? worker.role.charAt(0) + worker.role.slice(1).toLowerCase() : "";

        let createdDateStr = "-";
        if (worker.createdAt) {
            if (Array.isArray(worker.createdAt)) {
                const [year, month, day] = worker.createdAt;
                createdDateStr = `${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/${year}`;
            } else {
                const d = new Date(worker.createdAt);
                createdDateStr = d.toLocaleDateString('vi-VN');
            }
        }

        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>
                <div class="user-info">
                    <div>
                        <a href="${getApiUrl(`admin/profile-employee/${worker.id}`)}" class="user-name-link">
                            <strong>${escapeHtml(worker.fullName || "")}</strong>
                        </a>
                        <span>${roleLabel} Account</span>
                    </div>
                </div>
            </td>
            <td>${escapeHtml(worker.email || "")}</td>
            <td><span class="role ${roleClass}">${roleLabel}</span></td>
            <td><span class="status ${statusClass}">${statusLabel}</span></td>
            <td>${createdDateStr}</td>
            <td>
                <div class="action-group">
                    <a href="${getApiUrl(`admin/profile-employee/${worker.id}`)}" class="action-btn view-btn" title="Xem chi tiết"><i class="fa-solid fa-eye"></i></a>
                    <button class="edit-btn" data-id="${worker.id}" title="Chỉnh sửa"><i class="fa-solid fa-pen"></i></button>
                    ${isActive
                        ? `<button class="lock-btn" data-id="${worker.id}" title="Khóa"><i class="fa-solid fa-lock"></i></button>`
                        : `<button class="unlock-btn" data-id="${worker.id}" title="Mở khóa"><i class="fa-solid fa-unlock"></i></button>`
                    }
                    <button class="delete-btn" data-id="${worker.id}" title="Xóa"><i class="fa-solid fa-trash"></i></button>
                </div>
            </td>
        `;
        accountTableBody.appendChild(tr);
    });
}

function renderSummary(data) {
    if (!accountTableSummary) return;

    const from = data.totalItems === 0 ? 0 : data.currentPage * 5 + 1;
    const to = Math.min((data.currentPage + 1) * 5, data.totalItems);

    accountTableSummary.innerHTML = `Showing <strong>${from}-${to}</strong> of <strong>${data.totalItems}</strong> accounts`;
}

function renderPagination(data) {
    if (!accountPagination) return;

    accountPagination.innerHTML = "";
    const totalPages = data.totalPages;
    const currentPage = data.currentPage; // 0-indexed

    if (totalPages <= 1) return;

    const prevBtn = document.createElement("button");
    prevBtn.className = `page-btn${currentPage === 0 ? " disabled" : ""}`;
    prevBtn.innerHTML = `<i class="fa-solid fa-chevron-left"></i>`;
    prevBtn.addEventListener("click", () => {
        if (state.page > 0) {
            state.page -= 1;
            loadAccounts();
        }
    });
    accountPagination.appendChild(prevBtn);

    function getPageItems(total, current) {
        const cur1 = current + 1;
        if (total <= 7) {
            const arr = [];
            for (let i = 1; i <= total; i++) arr.push(i);
            return arr;
        }
        const pages = new Set();
        pages.add(1);
        pages.add(2);
        pages.add(3);
        for (let i = cur1 - 1; i <= cur1 + 1; i++) {
            if (i >= 1 && i <= total) {
                pages.add(i);
            }
        }
        pages.add(total - 2);
        pages.add(total - 1);
        pages.add(total);

        const sorted = Array.from(pages).sort((a, b) => a - b);
        const result = [];
        let prev = 0;
        for (const p of sorted) {
            if (prev > 0) {
                if (p - prev === 2) {
                    result.push(prev + 1);
                } else if (p - prev > 2) {
                    result.push('...');
                }
            }
            result.push(p);
            prev = p;
        }
        return result;
    }

    const items = getPageItems(totalPages, currentPage);
    items.forEach(item => {
        if (item === '...') {
            const dots = document.createElement("span");
            dots.className = "page-dots";
            dots.style.cssText = "display: inline-flex; align-items: center; justify-content: center; min-width: 32px; height: 36px; color: var(--neutral-400, #94a3b8); font-weight: bold;";
            dots.textContent = "...";
            accountPagination.appendChild(dots);
        } else {
            const pageIndex = item - 1;
            const pageBtn = document.createElement("button");
            pageBtn.className = `page-btn${pageIndex === currentPage ? " active" : ""}`;
            pageBtn.textContent = item;
            pageBtn.addEventListener("click", () => {
                state.page = pageIndex;
                loadAccounts();
            });
            accountPagination.appendChild(pageBtn);
        }
    });

    const nextBtn = document.createElement("button");
    nextBtn.className = `page-btn${currentPage >= totalPages - 1 ? " disabled" : ""}`;
    nextBtn.innerHTML = `<i class="fa-solid fa-chevron-right"></i>`;
    nextBtn.addEventListener("click", () => {
        if (state.page < totalPages - 1) {
            state.page += 1;
            loadAccounts();
        }
    });
    accountPagination.appendChild(nextBtn);

    if (totalPages > 7) {
        const jumpWrap = document.createElement("div");
        jumpWrap.className = "pagination-jump";
        jumpWrap.style.cssText = "display: inline-flex; align-items: center; gap: 6px; margin-left: 12px;";
        jumpWrap.innerHTML = `
            <span style="font-size: 0.8125rem; color: #64748b; white-space: nowrap;">Go to:</span>
            <input type="number" min="1" max="${totalPages}" class="page-jump-input" style="width: 52px; height: 34px; text-align: center; font-size: 0.8125rem; border-radius: 6px; border: 1px solid #cbd5e1; outline: none;" placeholder="${currentPage + 1}" />
            <button type="button" class="page-btn page-jump-btn" style="height: 34px; padding: 0 10px; font-size: 0.8125rem; min-width: auto;">Go</button>
        `;

        const jumpInput = jumpWrap.querySelector(".page-jump-input");
        const jumpBtn = jumpWrap.querySelector(".page-jump-btn");

        const doJump = () => {
            const val = parseInt(jumpInput.value, 10);
            if (!isNaN(val) && val >= 1 && val <= totalPages) {
                state.page = val - 1;
                loadAccounts();
            } else {
                jumpInput.value = "";
            }
        };

        jumpBtn.addEventListener("click", doJump);
        jumpInput.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                doJump();
            }
        });

        accountPagination.appendChild(jumpWrap);
    }
}

function escapeHtml(str) {
    const div = document.createElement("div");
    div.textContent = str;
    return div.innerHTML;
}

let searchDebounceTimer;
if (accountSearchInput) {
    accountSearchInput.addEventListener("input", () => {
        clearTimeout(searchDebounceTimer);
        searchDebounceTimer = setTimeout(() => {
            state.search = accountSearchInput.value.trim();
            state.page = 0;
            loadAccounts();
        }, 400);
    });
}

if (roleFilterMenu) {
    roleFilterMenu.querySelectorAll("button").forEach((btn) => {
        btn.addEventListener("click", () => {
            state.role = btn.dataset.role || "";
            state.page = 0;
            loadAccounts();
        });
    });
}

if (statusFilterMenu) {
    statusFilterMenu.querySelectorAll("button").forEach((btn) => {
        btn.addEventListener("click", () => {
            state.status = btn.dataset.status || "";
            state.page = 0;
            loadAccounts();
        });
    });
}

if (accountTableBody) {
    accountTableBody.addEventListener("click", async (e) => {
        const editBtn = e.target.closest(".edit-btn");
        const lockBtn = e.target.closest(".lock-btn");
        const unlockBtn = e.target.closest(".unlock-btn");
        const deleteBtn = e.target.closest(".delete-btn");

        if (editBtn) {
            window.location.href = getApiUrl(`admin/accounts/${editBtn.dataset.id}/edit`);
            return;
        }

        if (lockBtn) {
            await changeStatus(lockBtn.dataset.id, "LOCKED");
            return;
        }

        if (unlockBtn) {
            await changeStatus(unlockBtn.dataset.id, "ACTIVE");
            return;
        }

        if (deleteBtn) {
            if (!confirm("This will hide the account from the list (soft-delete). The data stays in the database. Continue?")) {
                return;
            }
            await deleteAccount(deleteBtn.dataset.id);
            return;
        }
    });
}

async function changeStatus(id, newStatus) {
    try {
        const res = await fetch(getApiUrl(`admin/accounts/${id}/status?status=${newStatus}`), {
            method: "PATCH",
            headers: getHeaders(),
        });

        if (!res.ok) {
            notify("error", "Update failed", "Could not update account status.");
            return;
        }

        notify("success", "Status updated", newStatus === "ACTIVE" ? "Account unlocked." : "Account locked.");
        loadAccounts();
        loadStats();
    } catch (err) {
        notify("error", "Network error", "Could not reach the server.");
    }
}

async function deleteAccount(id) {
    try {
        const res = await fetch(getApiUrl(`admin/accounts/${id}`), {
            method: "DELETE",
            headers: getHeaders(),
        });

        if (!res.ok) {
            let message = "Could not delete this account.";
            try {
                const data = await res.json();
                if (data.message) message = data.message;
            } catch (_) { /* no JSON body */ }

            notify("error", "Delete failed", message);
            return;
        }

        notify("success", "Account deleted", "The account has been removed.");
        loadAccounts();
        loadStats();
    } catch (err) {
        notify("error", "Network error", "Could not reach the server.");
    }
}

loadStats();
loadAccounts();