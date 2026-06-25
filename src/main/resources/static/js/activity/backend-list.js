// backend-list.js

document.addEventListener('DOMContentLoaded', function() {

    // 處理狀態切換的 Checkbox
    const statusSwitches = document.querySelectorAll('.toggle-status-btn');
    
    statusSwitches.forEach(function(switchInput) {
        switchInput.addEventListener('change', function(e) {
            
            const activityId = this.getAttribute('data-id');
            const isChecked = this.checked;
            const newStatus = isChecked ? 1 : 0;
            
            // 防止連續點擊，暫時禁用
            this.disabled = true;

            // 準備打 API
            fetch(`/backend/activity/${activityId}/status`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `status=${newStatus}`
            })
            .then(response => {
                if (response.ok) {
                    // 更新成功
                    return response.text();
                } else {
                    throw new Error('狀態切換失敗');
                }
            })
            .then(data => {
                console.log(data); // "狀態更新成功"
                // 恢復禁用
                this.disabled = false;
                
                // 動態更新純文字狀態
                const badgeLabel = document.getElementById(`status-badge-${activityId}`);
                if (isChecked) {
                    badgeLabel.textContent = '已上架';
                    badgeLabel.classList.remove('text-muted');
                    badgeLabel.classList.add('text-dark');
                    
                    // 開啟預覽連結
                    const nameLink = document.querySelector(`.activity-name-link[data-id="${activityId}"]`);
                    if (nameLink) {
                        nameLink.classList.remove('text-muted', 'preview-disabled');
                        nameLink.classList.add('text-dark', 'preview-active');
                        nameLink.title = nameLink.textContent + ' (點擊預覽前台畫面)';
                        nameLink.setAttribute('onclick', `openPreviewModal(${activityId}); return false;`);
                    }
                } else {
                    badgeLabel.textContent = '已下架';
                    badgeLabel.classList.remove('text-dark');
                    badgeLabel.classList.add('text-muted');
                    
                    // 關閉預覽連結
                    const nameLink = document.querySelector(`.activity-name-link[data-id="${activityId}"]`);
                    if (nameLink) {
                        nameLink.classList.remove('text-dark', 'preview-active');
                        nameLink.classList.add('text-muted', 'preview-disabled');
                        nameLink.title = nameLink.textContent + ' (已下架無法預覽)';
                        nameLink.setAttribute('onclick', 'return false;');
                    }
                }
            })
            .catch(error => {
                console.error(error);
                alert('系統錯誤，切換失敗！');
                // 失敗的話，把 UI 切換回原本的狀態
                this.checked = !isChecked;
                this.disabled = false;
            });

        });
    });

});
