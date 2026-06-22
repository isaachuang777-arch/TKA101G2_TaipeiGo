document.addEventListener("DOMContentLoaded", () => {

    const slides = document.querySelectorAll(".slide");

    if(slides.length === 0){
        return;
    }

    let current = 0;

    setInterval(() => {

        slides[current].classList.remove("active");

        current = (current + 1) % slides.length;

        slides[current].classList.add("active");

    }, 3000);

});