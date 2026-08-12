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
                        <a href="${getApiUrl(`admin/profile-employee`)}" class="user-name-link">
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
                    <button class="edit-btn" 
                            data-id="${worker.id}"
                            data-username="${escapeHtml(worker.username || "")}"
                            data-phone="${escapeHtml(worker.phoneNumber || "0901234567")}"><i class="fa-solid fa-pen"></i></button>
                    ${isActive
                        ? `<button class="lock-btn" data-id="${worker.id}"><i class="fa-solid fa-lock"></i></button>`
                        : `<button class="unlock-btn" data-id="${worker.id}"><i class="fa-solid fa-unlock"></i></button>`
                    }
                    <button class="delete-btn" data-id="${worker.id}"><i class="fa-solid fa-trash"></i></button>
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

    accountTableSummary.innerHTML = `Hiển thị <strong>${from}-${to}</strong> trong <strong>${data.totalItems}</strong> tài khoản`;
}

function renderPagination(data) {
    if (!accountPagination) return;

    accountPagination.innerHTML = "";

    const prevBtn = document.createElement("button");
    prevBtn.className = `page-btn${data.currentPage === 0 ? " disabled" : ""}`;
    prevBtn.innerHTML = `<i class="fa-solid fa-chevron-left"></i>`;
    prevBtn.addEventListener("click", () => {
        if (state.page > 0) {
            state.page -= 1;
            loadAccounts();
        }
    });
    accountPagination.appendChild(prevBtn);

    for (let i = 0; i < data.totalPages; i++) {
        const pageBtn = document.createElement("button");
        pageBtn.className = `page-btn${i === data.currentPage ? " active" : ""}`;
        pageBtn.textContent = i + 1;
        pageBtn.addEventListener("click", () => {
            state.page = i;
            loadAccounts();
        });
        accountPagination.appendChild(pageBtn);
    }

    const nextBtn = document.createElement("button");
    nextBtn.className = `page-btn${data.currentPage >= data.totalPages - 1 ? " disabled" : ""}`;
    nextBtn.innerHTML = `<i class="fa-solid fa-chevron-right"></i>`;
    nextBtn.addEventListener("click", () => {
        if (state.page < data.totalPages - 1) {
            state.page += 1;
            loadAccounts();
        }
    });
    accountPagination.appendChild(nextBtn);
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

        // Cấu hình mở Modal chỉnh sửa nhân viên thay vì chuyển hướng trang
        if (editBtn) {
            const tr = editBtn.closest("tr");
            if (!tr) return;

            const fullName = tr.querySelector(".user-name-link strong").textContent.trim();
            const email = tr.querySelector("td:nth-child(2)").textContent.trim();
            const role = tr.querySelector("td:nth-child(3) span").textContent.trim().toUpperCase();
            const username = editBtn.getAttribute("data-username") || "staff.user";
            const phone = editBtn.getAttribute("data-phone") || "0901234567";

            document.getElementById("modalStaffUsername").value = username;
            document.getElementById("modalStaffFullName").value = fullName;
            document.getElementById("modalStaffEmail").value = email;
            document.getElementById("modalStaffPhone").value = phone;
            document.getElementById("modalStaffRole").value = role;

            const editStaffModal = document.getElementById("editStaffModal");
            if (editStaffModal) {
                editStaffModal.classList.add("active");
            }
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