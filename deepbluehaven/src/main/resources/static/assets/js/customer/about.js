/**
 * Deep Blue Haven Resort - About Us Page Interactive Scripts
 * Scroll reveal animations & counter tickers
 */
document.addEventListener('DOMContentLoaded', () => {
    // --- Counter animation on scroll ---
    const counters = document.querySelectorAll('.about-stat-number');
    let animated = false;

    function animateCounters() {
        if (animated) return;

        const triggerPoint = window.innerHeight * 0.85;
        const statsSection = document.querySelector('.about-stats-grid');
        if (!statsSection) return;

        const rect = statsSection.getBoundingClientRect();
        if (rect.top <= triggerPoint) {
            animated = true;
            counters.forEach(counter => {
                const targetText = counter.textContent.trim();
                const isRating = targetText.includes('.');
                const hasPlus = targetText.includes('+');
                const hasPercent = targetText.includes('%');
                const numericValue = parseFloat(targetText.replace(/[^0-9.]/g, ''));

                if (isNaN(numericValue)) return;

                let start = 0;
                const duration = 1500;
                const stepTime = 25;
                const totalSteps = duration / stepTime;
                const stepValue = numericValue / totalSteps;

                const timer = setInterval(() => {
                    start += stepValue;
                    if (start >= numericValue) {
                        start = numericValue;
                        clearInterval(timer);
                    }

                    if (isRating) {
                        counter.textContent = start.toFixed(1) + (hasPlus ? '+' : '');
                    } else {
                        counter.textContent = Math.floor(start) + (hasPlus ? '+' : '') + (hasPercent ? '%' : '');
                    }
                }, stepTime);
            });
        }
    }

    window.addEventListener('scroll', animateCounters);
    animateCounters();
});
