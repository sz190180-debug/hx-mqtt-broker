// 搜索用户
function searchUsers() {
    const searchTerm = $('#userSearch').val().trim();
    loadUserList(searchTerm);
}

// 加载用户列表
function loadUserList(searchTerm = '') {
    $.ajax({
        url: '/api/user/select',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({clientId: searchTerm}),
        success: function (response) {
            if (response.code === 10000 && response.data) {
                const userSelect = $('#userSelect');
                userSelect.empty();

                response.data.forEach(user => {
                    userSelect.append(`<option value="${user.hxUserId}">${user.clientId} (${user.username})</option>`);
                });
            } else {
                alert(response.msg || '加载用户列表失败');
            }
        },
        error: function () {
            alert('服务器错误');
        }
    });
}