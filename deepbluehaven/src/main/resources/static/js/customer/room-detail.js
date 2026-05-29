document.addEventListener("DOMContentLoaded", function () {
    initRoomToast();
    initRoomPlanTabs();
    initRoomGallery();
});

/* =========================
   TOAST
========================= */

function initRoomToast() {
    const showRoomToast = function (type, title, message) {
        if (typeof showToast === "function") {
            showToast(type, title, message, {
                theme: "dark",
                duration: 3600
            });
        }
    };

    const availabilityBtn = document.querySelector("[data-toast='availability']");
    const shareBtn = document.querySelector("[data-toast='share']");
    const photosBtn = document.querySelector("[data-toast='photos']");

    if (availabilityBtn) {
        availabilityBtn.addEventListener("click", function () {
            showRoomToast(
                "success",
                "Availability checked",
                "This room is ready for your selected stay."
            );
        });
    }

    if (shareBtn) {
        shareBtn.addEventListener("click", function () {
            showRoomToast(
                "info",
                "Share room",
                "Room detail link has been prepared for sharing."
            );
        });
    }

    if (photosBtn) {
        photosBtn.addEventListener("click", function () {
            showRoomToast(
                "info",
                "Photo gallery",
                "Full photo gallery will be opened from this action."
            );
        });
    }
}

/* =========================
   ROOM PLAN TABS
========================= */

function initRoomPlanTabs() {
    const tabs = document.querySelectorAll("[data-plan-tab]");
    const panels = document.querySelectorAll("[data-plan-panel]");

    if (!tabs.length || !panels.length) return;

    tabs.forEach(function (tab) {
        tab.addEventListener("click", function () {
            const target = tab.dataset.planTab;

            tabs.forEach(function (item) {
                item.classList.remove("is-active");
            });

            panels.forEach(function (panel) {
                panel.classList.toggle("is-active", panel.dataset.planPanel === target);
            });

            tab.classList.add("is-active");
        });
    });
}


function initRoomGallery() {
    const gallery = document.querySelector("[data-room-gallery]");

    if (!gallery) return;

    const slides = Array.from(gallery.querySelectorAll(".room-gallery__item"));
    const prevBtn = gallery.querySelector(".room-gallery__nav--prev");
    const nextBtn = gallery.querySelector(".room-gallery__nav--next");
    const dotsWrap = gallery.querySelector(".room-gallery__dots");

    if (!slides.length) return;

    let currentIndex = 0;

    function getLoopIndex(index) {
        if (index < 0) {
            return slides.length - 1;
        }

        if (index >= slides.length) {
            return 0;
        }

        return index;
    }

    function getPrevIndex(index) {
        return getLoopIndex(index - 1);
    }

    function getNextIndex(index) {
        return getLoopIndex(index + 1);
    }

    function getFarPrevIndex(index) {
        return getLoopIndex(index - 2);
    }

    function getFarNextIndex(index) {
        return getLoopIndex(index + 2);
    }

    function renderDots() {
        if (!dotsWrap) return;

        dotsWrap.innerHTML = "";

        slides.forEach(function (_, index) {
            const dot = document.createElement("button");

            dot.type = "button";
            dot.className = "room-gallery__dot";
            dot.setAttribute("aria-label", "Go to photo " + (index + 1));

            dot.addEventListener("click", function () {
                setActiveSlide(index);
            });

            dotsWrap.appendChild(dot);
        });
    }

    function setActiveSlide(index) {
        currentIndex = getLoopIndex(index);

        const prevIndex = getPrevIndex(currentIndex);
        const nextIndex = getNextIndex(currentIndex);
        const farPrevIndex = getFarPrevIndex(currentIndex);
        const farNextIndex = getFarNextIndex(currentIndex);

        slides.forEach(function (slide, slideIndex) {
            slide.classList.remove(
                "is-active",
                "is-prev",
                "is-next",
                "is-far-prev",
                "is-far-next"
            );

            if (slideIndex === currentIndex) {
                slide.classList.add("is-active");
            } else if (slideIndex === prevIndex) {
                slide.classList.add("is-prev");
            } else if (slideIndex === nextIndex) {
                slide.classList.add("is-next");
            } else if (slideIndex === farPrevIndex) {
                slide.classList.add("is-far-prev");
            } else if (slideIndex === farNextIndex) {
                slide.classList.add("is-far-next");
            }
        });

        const dots = Array.from(gallery.querySelectorAll(".room-gallery__dot"));

        dots.forEach(function (dot, dotIndex) {
            dot.classList.toggle("is-active", dotIndex === currentIndex);
        });
    }

    if (prevBtn) {
        prevBtn.addEventListener("click", function () {
            setActiveSlide(currentIndex - 1);
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener("click", function () {
            setActiveSlide(currentIndex + 1);
        });
    }

    slides.forEach(function (slide, index) {
        slide.addEventListener("click", function () {
            if (!slide.classList.contains("is-active")) {
                setActiveSlide(index);
            }
        });
    });

    renderDots();
    setActiveSlide(currentIndex);
}