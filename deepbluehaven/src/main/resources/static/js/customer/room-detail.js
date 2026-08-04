document.addEventListener("DOMContentLoaded", () => {
    initRoomToast();
    initRoomPlanTabs();
    initRoomGallery();
    initRoomBookingForm();

    console.log("room-detail.js loaded");
});

/* =========================
   TOAST
========================= */
function initRoomToast() {
    const showRoomToast = (type, title, message) => {
        if (typeof showToast === "function") {
            showToast(type, title, message, {
                theme: "dark",
                duration: 3600
            });
            return;
        }

        console.log(`${title}: ${message}`);
    };

    const availabilityBtn = document.querySelector("[data-toast='availability']");
    const shareBtn = document.querySelector("[data-toast='share']");
    const photosBtn = document.querySelector("[data-toast='photos']");

    if (availabilityBtn) {
        availabilityBtn.addEventListener("click", () => {
            showRoomToast(
                "success",
                "Availability checked",
                "This room is ready for your selected stay."
            );
        });
    }

    if (shareBtn) {
        shareBtn.addEventListener("click", () => {
            showRoomToast(
                "info",
                "Share room",
                "Room detail link has been prepared for sharing."
            );
        });
    }

    if (photosBtn) {
        photosBtn.addEventListener("click", () => {
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

    if (!tabs.length || !panels.length) {
        return;
    }

    tabs.forEach((tab) => {
        tab.addEventListener("click", () => {
            const target = tab.dataset.planTab;

            tabs.forEach((item) => item.classList.remove("is-active"));
            tab.classList.add("is-active");

            panels.forEach((panel) => {
                panel.classList.toggle(
                    "is-active",
                    panel.dataset.planPanel === target
                );
            });
        });
    });
}

/* =========================
   ROOM GALLERY SLIDER
========================= */
function initRoomGallery() {
    const gallery = document.querySelector("[data-room-gallery]");

    if (!gallery) {
        return;
    }

    let slides = Array.from(gallery.querySelectorAll(".room-gallery__item"));

    /*
       Nếu fragment image-card chưa có class .room-gallery__item,
       JS vẫn bắt được các card ảnh phổ biến và gắn class vào để slider chạy.
    */
    if (!slides.length) {
        slides = Array.from(
            gallery.querySelectorAll(".image-card, [data-gallery-item], .room-image-card")
        );

        slides.forEach((slide) => slide.classList.add("room-gallery__item"));
    }

    const prevBtn = gallery.querySelector(".room-gallery__nav--prev");
    const nextBtn = gallery.querySelector(".room-gallery__nav--next");
    const dotsWrap = gallery.querySelector(".room-gallery__dots");

    if (!slides.length) {
        return;
    }

    let currentIndex = 0;

    const getLoopIndex = (index) => {
        if (index < 0) return slides.length - 1;
        if (index >= slides.length) return 0;
        return index;
    };

    const renderDots = () => {
        if (!dotsWrap) {
            return;
        }

        dotsWrap.innerHTML = "";

        slides.forEach((_, index) => {
            const dot = document.createElement("button");
            dot.type = "button";
            dot.className = "room-gallery__dot";
            dot.setAttribute("aria-label", `Go to photo ${index + 1}`);

            dot.addEventListener("click", () => {
                setActiveSlide(index);
            });

            dotsWrap.appendChild(dot);
        });
    };

    const setActiveSlide = (index) => {
        currentIndex = getLoopIndex(index);

        const prevIndex = getLoopIndex(currentIndex - 1);
        const nextIndex = getLoopIndex(currentIndex + 1);
        const farPrevIndex = getLoopIndex(currentIndex - 2);
        const farNextIndex = getLoopIndex(currentIndex + 2);

        slides.forEach((slide, slideIndex) => {
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

        const dots = gallery.querySelectorAll(".room-gallery__dot");
        dots.forEach((dot, dotIndex) => {
            dot.classList.toggle("is-active", dotIndex === currentIndex);
        });
    };

    if (prevBtn) {
        prevBtn.addEventListener("click", () => {
            setActiveSlide(currentIndex - 1);
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener("click", () => {
            setActiveSlide(currentIndex + 1);
        });
    }

    slides.forEach((slide, index) => {
        slide.addEventListener("click", () => {
            if (!slide.classList.contains("is-active")) {
                setActiveSlide(index);
            }
        });
    });

    renderDots();
    setActiveSlide(currentIndex);
}

function initRoomBookingForm() {
    const form = document.getElementById("roomBookingForm");
    if (!form) return;

    function getApiUrl(path) {
        let ctx = document.querySelector('meta[name="_context_path"]')?.content;
        if (!ctx || ctx === "/") {
            const match = window.location.pathname.match(/^\/([^\/]+)/);
            ctx = (match && match[1] === "deepbluehaven") ? "/" + match[1] : "";
        }
        return ctx.replace(/\/$/, "") + (path.startsWith("/") ? path : "/" + path);
    }

    form.addEventListener("submit", async function (e) {
        e.preventDefault();
        const formData = new FormData(form);
        const roomId = formData.get("roomId");
        const checkIn = formData.get("checkIn");
        const checkOut = formData.get("checkOut");

        try {
            const res = await fetch(getApiUrl(`/api/booking/create?roomId=${roomId}&checkIn=${checkIn}&checkOut=${checkOut}`), {
                method: "POST"
            });
            const data = await res.json();

            if (res.status === 401 || !data.success) {
                if (data.redirectUrl) {
                    alert("Vui lòng đăng nhập tài khoản để thực hiện đặt phòng.");
                    window.location.href = getApiUrl(data.redirectUrl);
                } else {
                    alert(data.message || "Đặt phòng không thành công.");
                }
                return;
            }

            alert(`Đặt phòng thành công! Mã đơn: ${data.bookingCode}`);
            if (data.redirectUrl) {
                window.location.href = getApiUrl(data.redirectUrl);
            }
        } catch (err) {
            alert("Đã xảy ra lỗi kết nối khi đặt phòng.");
        }
    });
}