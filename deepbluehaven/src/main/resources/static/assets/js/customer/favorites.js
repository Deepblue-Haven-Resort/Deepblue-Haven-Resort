/**
 * Deep Blue Haven Resort - Customer Favorites Page Interactivity
 * Matches booking/history vibe:
 * - Tab switching with badge counts
 * - Live real-time search across saved rooms and services
 * - Reactive favorite heart toggling with smooth card removal
 * - Empty states & redirect actions
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

    const tabs = document.querySelectorAll('.favorites-tab');
    const roomsSection = document.getElementById('roomsSection');
    const servicesSection = document.getElementById('servicesSection');
    const grandEmptyState = document.getElementById('grandEmptyState');
    const searchEmptyState = document.getElementById('searchEmptyState');
    const roomsEmptyPanel = document.getElementById('roomsEmptyPanel');
    const servicesEmptyPanel = document.getElementById('servicesEmptyPanel');

    const searchInput = document.getElementById('favoritesSearch');
    const clearSearchBtn = document.getElementById('clearSearchBtn');

    const badgeAll = document.getElementById('badgeAll');
    const badgeRooms = document.getElementById('badgeRooms');
    const badgeServices = document.getElementById('badgeServices');

    let currentTab = 'all';
    let currentSearchQuery = '';

    // --- Transform Service Card Action Buttons in Favorites Page ---
    function setupServiceCardActions() {
        const serviceActions = document.querySelectorAll('.favorites-page .service-card__action');
        serviceActions.forEach(btn => {
            const span = btn.querySelector('span');
            const icon = btn.querySelector('i');
            if (span && span.textContent.trim() !== 'Unavailable') {
                span.textContent = 'Book Service';
            }
            if (icon && !icon.classList.contains('fa-ban')) {
                icon.className = 'fa-solid fa-arrow-right';
            }

            btn.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                window.location.href = `${baseUrl}/services`;
            });
        });
    }

    setupServiceCardActions();

    // --- Tab Switching ---
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => {
                t.classList.remove('is-active');
                t.setAttribute('aria-selected', 'false');
            });
            tab.classList.add('is-active');
            tab.setAttribute('aria-selected', 'true');

            currentTab = tab.getAttribute('data-tab') || 'all';
            filterCards();
        });
    });

    // --- Live Search ---
    if (searchInput) {
        searchInput.addEventListener('input', () => {
            currentSearchQuery = searchInput.value.trim().toLowerCase();
            filterCards();
        });
    }

    if (clearSearchBtn && searchInput) {
        clearSearchBtn.addEventListener('click', () => {
            searchInput.value = '';
            currentSearchQuery = '';
            filterCards();
            searchInput.focus();
        });
    }

    // --- Core Card Filtering (Tabs + Search) ---
    function filterCards() {
        const roomCards = Array.from(document.querySelectorAll('#favoriteRoomsGrid .room-card'));
        const serviceCards = Array.from(document.querySelectorAll('#favoriteServicesGrid .service-card'));

        let visibleRooms = 0;
        let visibleServices = 0;

        // Filter Rooms
        roomCards.forEach(card => {
            const matchesTab = (currentTab === 'all' || currentTab === 'rooms');
            const text = card.textContent.toLowerCase();
            const matchesSearch = !currentSearchQuery || text.includes(currentSearchQuery);

            if (matchesTab && matchesSearch) {
                card.style.display = '';
                visibleRooms++;
            } else {
                card.style.display = 'none';
            }
        });

        // Filter Services
        serviceCards.forEach(card => {
            const matchesTab = (currentTab === 'all' || currentTab === 'services');
            const text = card.textContent.toLowerCase();
            const matchesSearch = !currentSearchQuery || text.includes(currentSearchQuery);

            if (matchesTab && matchesSearch) {
                card.style.display = '';
                visibleServices++;
            } else {
                card.style.display = 'none';
            }
        });

        // Update Section Visibility
        if (roomsSection) {
            if (currentTab === 'services' || (roomCards.length === 0 && !currentSearchQuery)) {
                roomsSection.classList.add('is-hidden');
            } else {
                roomsSection.classList.remove('is-hidden');
            }
        }

        if (servicesSection) {
            if (currentTab === 'rooms' || (serviceCards.length === 0 && !currentSearchQuery)) {
                servicesSection.classList.add('is-hidden');
            } else {
                servicesSection.classList.remove('is-hidden');
            }
        }

        // Search empty state check
        const totalMatching = visibleRooms + visibleServices;
        const totalCardsInDOM = roomCards.length + serviceCards.length;

        if (totalCardsInDOM > 0 && currentSearchQuery && totalMatching === 0) {
            if (searchEmptyState) searchEmptyState.style.display = 'block';
            if (roomsSection) roomsSection.classList.add('is-hidden');
            if (servicesSection) servicesSection.classList.add('is-hidden');
        } else {
            if (searchEmptyState) searchEmptyState.style.display = 'none';
        }

        // Grand empty state check
        if (totalCardsInDOM === 0) {
            if (grandEmptyState) grandEmptyState.classList.add('is-visible');
            if (roomsSection) roomsSection.classList.add('is-hidden');
            if (servicesSection) servicesSection.classList.add('is-hidden');
            if (searchEmptyState) searchEmptyState.style.display = 'none';
        }
    }

    // --- Reactive Favorite Toggled Event Listener ---
    document.addEventListener('favoriteToggled', (e) => {
        const { id, type, isFavorite } = e.detail;

        if (!isFavorite) {
            let card = null;
            if (type === 'room') {
                card = document.querySelector(`.room-card[data-id="${id}"]`);
            } else if (type === 'service') {
                card = document.querySelector(`.service-card[data-id="${id}"]`);
            }

            if (card) {
                card.classList.add('favorite-card-removing');
                setTimeout(() => {
                    card.remove();
                    updateCounts(type, -1);
                }, 300);
            }
        }
    });

    function updateCounts(type, change) {
        let roomsCount = parseInt(badgeRooms?.textContent || '0', 10);
        let servicesCount = parseInt(badgeServices?.textContent || '0', 10);

        if (type === 'room') {
            roomsCount = Math.max(0, roomsCount + change);
            if (badgeRooms) badgeRooms.textContent = roomsCount;
        } else if (type === 'service') {
            servicesCount = Math.max(0, servicesCount + change);
            if (badgeServices) badgeServices.textContent = servicesCount;
        }

        const totalCount = roomsCount + servicesCount;
        if (badgeAll) badgeAll.textContent = totalCount;

        // Individual panels
        if (roomsCount === 0 && roomsEmptyPanel) {
            roomsEmptyPanel.classList.remove('is-hidden');
        }
        if (servicesCount === 0 && servicesEmptyPanel) {
            servicesEmptyPanel.classList.remove('is-hidden');
        }

        filterCards();
    }
});
