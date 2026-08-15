/**
 * Deep Blue Haven Resort - Customer Favorite Toggle Handler
 * Handles live toggling of favorite rooms and services across all customer pages.
 */
document.addEventListener('DOMContentLoaded', () => {
    function getBaseUrl() {
        const meta = document.querySelector('meta[name="_context_path"]');
        if (meta) {
            const val = meta.getAttribute('content');
            if (val && val !== '/') {
                return val.endsWith('/') ? val.slice(0, -1) : val;
            }
        }
        if (window.location.pathname.startsWith('/deepbluehaven')) {
            return '/deepbluehaven';
        }
        return '';
    }

    const baseUrl = getBaseUrl();

    function triggerToast(type, title, message) {
        if (typeof showToast === 'function') {
            showToast(type, title, message);
        } else {
            console.log(`[Toast ${type}] ${title}: ${message}`);
        }
    }

    document.addEventListener('click', async (e) => {
        const toggleBtn = e.target.closest('.favorite-toggle-btn');
        if (!toggleBtn) return;

        e.preventDefault();
        e.stopPropagation();

        const id = toggleBtn.getAttribute('data-id');
        const type = toggleBtn.getAttribute('data-type'); // 'room' or 'service'

        if (!id || !type) return;

        const endpoint = type === 'room' 
            ? `${baseUrl}/api/favorites/room/toggle?roomId=${id}`
            : `${baseUrl}/api/favorites/service/toggle?serviceId=${id}`;

        try {
            toggleBtn.style.pointerEvents = 'none';
            toggleBtn.style.opacity = '0.7';

            const response = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json'
                }
            });

            let data = null;
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                throw new Error('Server returned invalid response');
            }

            if (response.status === 401 || data.authenticated === false) {
                triggerToast('warning', 'Login Required', data.message || 'Please log in to save your favorites.');
                setTimeout(() => {
                    window.location.href = `${baseUrl}/login`;
                }, 1200);
                return;
            }

            if (!response.ok || !data.success) {
                triggerToast('error', 'Action Failed', data.message || 'Could not update favorite status.');
                return;
            }

            const isFavorite = data.isFavorite;

            // Update all matching toggle buttons on the page (if same item appears in multiple places)
            const matchingBtns = document.querySelectorAll(`.favorite-toggle-btn[data-type="${type}"][data-id="${id}"]`);
            matchingBtns.forEach(btn => {
                const icon = btn.querySelector('i');
                const label = btn.querySelector('.favorite-label-text');

                if (isFavorite) {
                    btn.classList.add('is-active');
                    if (icon) {
                        icon.className = 'fa-solid fa-heart';
                    }
                    if (label) {
                        label.textContent = 'Saved to Favorites';
                    }
                } else {
                    btn.classList.remove('is-active');
                    if (icon) {
                        icon.className = 'fa-regular fa-heart';
                    }
                    if (label) {
                        label.textContent = 'Save to Favorites';
                    }
                }
            });

            triggerToast('success', isFavorite ? 'Added to Favorites' : 'Removed from Favorites', data.message);

            // Dispatch custom event for listeners (like favorites page)
            const event = new CustomEvent('favoriteToggled', {
                detail: {
                    id: id,
                    type: type,
                    isFavorite: isFavorite
                }
            });
            document.dispatchEvent(event);

        } catch (error) {
            console.error('Error toggling favorite:', error);
            triggerToast('error', 'Network Error', 'Could not connect to server.');
        } finally {
            toggleBtn.style.pointerEvents = '';
            toggleBtn.style.opacity = '';
        }
    });
});
