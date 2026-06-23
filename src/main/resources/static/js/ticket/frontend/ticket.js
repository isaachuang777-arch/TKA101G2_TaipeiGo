document.addEventListener('DOMContentLoaded', function () {
    const container = document.getElementById('categoryListContainer');
    const leftBtn = document.getElementById('scrollLeftBtn');
    const rightBtn = document.getElementById('scrollRightBtn');
    
    if (container && leftBtn && rightBtn) {
        leftBtn.classList.remove('bound-hidden');
        rightBtn.classList.remove('bound-hidden');

        leftBtn.onclick = () => {
            container.scrollBy({ left: -(container.clientWidth + 16), behavior: 'smooth' });
        };

        rightBtn.onclick = () => {
            container.scrollBy({ left: (container.clientWidth + 16), behavior: 'smooth' });
        };
    }
});
