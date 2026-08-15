/**
 * Deep Blue Haven Resort - Contact Page Interactivity
 * FAQ Accordion & Form submission enhancements
 */
document.addEventListener('DOMContentLoaded', () => {
    // --- FAQ Accordion ---
    const faqItems = document.querySelectorAll('.faq-item');

    faqItems.forEach(item => {
        const questionBtn = item.querySelector('.faq-question');
        if (questionBtn) {
            questionBtn.addEventListener('click', () => {
                const isOpen = item.classList.contains('is-open');

                // Close all other items
                faqItems.forEach(other => {
                    if (other !== item) {
                        other.classList.remove('is-open');
                    }
                });

                // Toggle current
                if (isOpen) {
                    item.classList.remove('is-open');
                } else {
                    item.classList.add('is-open');
                }
            });
        }
    });

    // --- Contact Form Submission Handler ---
    const contactForm = document.getElementById('contactInquiryForm');
    if (contactForm) {
        contactForm.addEventListener('submit', (e) => {
            const submitBtn = contactForm.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin"></i> Sending Message...';
            }
        });
    }
});
