$(function () {
    loadData();

    // 下拉菜单选择事件
    $('#user-filed a').click(function () {
        var field = $(this).data('field') || '';
        var text = $(this).text();
        $('#search-field').val(field);
        $('#search-btn').html(text + ' <span class="caret"></span>');
    });

    $('#template-filed a').click(function () {
        var field = $(this).data('field') || '';
        var text = $(this).text();
        $('#template-search-field').val(field);
        $('#template-search-btn').html(text + ' <span class="caret"></span>');
    });

    // 回车键触发搜索
    $('#search-keyword').keypress(function (e) {
        if (e.which == 13) {
            loadData();
        }
    });

    // 在模板搜索框也添加回车键支持
    $('#template-search-keyword').keypress(function (e) {
        if (e.which == 13) {
            loadTaskChainData();
        }
    });

    // 全选/取消全选
    $('#templateSelectAll').click(function () {
        $('.row-checkbox').prop('checked', this.checked);
    });

    // 添加用户对话框的密码显示切换
    $('#toggleAddPassword').click(function () {
        const passwordInput = $('#addPassword');
        const icon = $(this).find('i');

        if (passwordInput.attr('type') === 'password') {
            passwordInput.attr('type', 'text');
            icon.removeClass('mdi-eye').addClass('mdi-eye-off');
        } else {
            passwordInput.attr('type', 'password');
            icon.removeClass('mdi-eye-off').addClass('mdi-eye');
        }
    });

    // 编辑用户对话框的密码显示切换
    $('#toggleEditPassword').click(function () {
        const passwordInput = $('#editPassword');
        const icon = $(this).find('i');

        if (passwordInput.attr('type') === 'password') {
            passwordInput.attr('type', 'text');
            icon.removeClass('mdi-eye').addClass('mdi-eye-off');
        } else {
            passwordInput.attr('type', 'password');
            icon.removeClass('mdi-eye-off').addClass('mdi-eye');
        }
    });
});

// 显示添加客户端对话框
function showAddUserModal() {
    $('#addUserForm')[0].reset();
    $('#addUserModal').modal('show');
}

// 显示编辑客户端对话框
function showEditUserModal(user) {
    $('#editUserForm')[0].reset();
    $('#editHxUserId').val(user.hxUserId);
    $('#editClientId').val(user.clientId);
    $('#editUsername').val(user.username);
    $('#editPassword').val(user.password);
    $('#editRemark').val(user.remark);
    // 设置用户类型单选按钮
    if (user.userType === 1) {
        $('#editUserTypeDisplay').prop('checked', true);
    } else if (user.userType === 2) {
        $('#editUserTypeApp').prop('checked', true);
    }
    $('#editUserModal').modal('show');
}

// 添加客户端
function addUser() {
    const form = $('#addUserForm');
    if (!form[0].checkValidity()) {
        form.addClass('was-validated');
        return;
    }

    const userData = {
        clientId: $('#addClientId').val(),
        userType: $('input[name="userType"]:checked').val(),
        username: $('#addUsername').val(),
        password: $('#addPassword').val(),
        remark: $('#addRemark').val()
    };

    $.ajax({
        url: '/api/user/add',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(userData),
        success: function (response) {
            if (response.code === 10000) {
                $('#addUserModal').modal('hide');
                alert(window.userI18n.addClientSuccess);
                loadData(); // 刷新列表
            } else {
                alert(window.userI18n.addFailed + ': ' + response.msg);
            }
        },
        error: function (xhr) {
            alert(window.userI18n.addFailed + ': ' + (xhr.responseJSON?.msg || window.userI18n.serverError));
        }
    });
}

// 更新客户端
function updateUser() {
    const form = $('#editUserForm');
    if (!form[0].checkValidity()) {
        form.addClass('was-validated');
        return;
    }

    const userData = {
        hxUserId: $('#editHxUserId').val(),
        userType: $('input[name="userType"]:checked').val(), // 获取选中的用户类型
        clientId: $('#editClientId').val(),
        username: $('#editUsername').val(),
        password: $('#editPassword').val(),
        remark: $('#editRemark').val()
    };

    $.ajax({
        url: '/api/user/edit',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(userData),
        success: function (response) {
            if (response.code === 10000) {
                $('#editUserModal').modal('hide');
                alert(window.userI18n.editClientSuccess);
                loadData(); // 刷新列表
            } else {
                alert(window.userI18n.editFailed + ': ' + response.msg);
            }
        },
        error: function (xhr) {
            alert(window.userI18n.editFailed + ': ' + (xhr.responseJSON?.msg || window.userI18n.serverError));
        }
    });
}

function loadData() {
    const searchField = $('#search-field').val();
    const keyword = $('#search-keyword').val();

    // 构建请求参数
    const req = {};
    if (searchField === 'clientId') {
        req.clientId = keyword;
    } else if (searchField === 'remark') {
        req.remark = keyword;
    }

    // 显示加载状态
    $('#userTableBody').html('<tr><td colspan="6" class="text-center"><i class="mdi mdi-loading mdi-spin"></i> ' + window.userI18n.loading + '</td></tr>');

    $.ajax({
        url: '/api/user/select',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(req),
        success: function (res) {
            if (res.code === 10000 && res.data) {
                renderTable(res.data);
            } else {
                $('#userTableBody').html('<tr><td colspan="6" class="text-center">' + window.userI18n.noData + '</td></tr>');
            }
        },
        error: function () {
            $('#userTableBody').html('<tr><td colspan="6" class="text-center">' + window.userI18n.dataLoadFailed + '</td></tr>');
        }
    });
}

function renderTable(data) {
    const tbody = $('#userTableBody');
    tbody.empty();

    if (data.length === 0) {
        tbody.append('<tr><td colspan="6" class="text-center">' + window.userI18n.noData + '</td></tr>');
        return;
    }

    data.forEach((user, index) => {
        const tr = $('<tr>');
        tr.append(`<td>${index + 1}</td>`);
        tr.append(`<td>${user.clientId || '-'}</td>`);
        // 优化userType显示逻辑
        let userTypeText = '-';
        if (user.userType === 1) {
            userTypeText = window.userI18n.userTypeDisplay;
        } else if (user.userType === 2) {
            userTypeText = window.userI18n.userTypeApp;
        }
        tr.append(`<td>${userTypeText}</td>`);
        tr.append(`<td>${user.username || '-'}</td>`);
        tr.append(`<td>${user.password ? '******' : '-'}</td>`);
        const isOnline = user.isOnline || false; // 假设后端返回isOnline字段
        const onlineStatusText = isOnline ?
            '<span class="badge bg-success">' + window.userI18n.online + '</span>' :
            '<span class="badge bg-secondary">' + window.userI18n.offline + '</span>';
        tr.append(`<td>${onlineStatusText}</td>`);
        tr.append(`<td>${user.remark || '-'}</td>`);
        tr.append(`
                <td class="action-buttons">
                    <button class="btn btn-sm btn-info" onclick="showEditUserModal(${JSON.stringify(user).replace(/"/g, '&quot;')})">
                        <i class="mdi mdi-pencil"></i> ${window.userI18n.edit}
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deleteUser(${user.hxUserId})">
                        <i class="mdi mdi-delete"></i> ${window.userI18n.delete}
                    </button>
                    <button class="btn btn-sm btn-primary" onclick="getTaskChainRoles(${user.hxUserId})">
                        <i class="mdi mdi-view-list"></i> ${window.userI18n.taskChainManagement}
                    </button>
                    <button class="btn btn-sm btn-primary" onclick="getAmrRoles(${user.hxUserId})">
                        <i class="mdi mdi-view-list"></i> ${window.userI18n.vehicleManagement}
                    </button>
                    <button class="btn btn-sm btn-primary" onclick="getWarehouseRoles(${user.hxUserId})">
                        <i class="mdi mdi-view-list"></i> ${window.userI18n.warehouseManagement}
                    </button>
                </td>
            `);
        tbody.append(tr);
    });
}

function deleteUser(id) {
    if (confirm(window.userI18n.deleteConfirm)) {
        $.ajax({
            url: '/api/user/delete',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({hxUserId: id}),
            success: function (response) {
                if (response.code === 10000) {
                    alert(window.userI18n.deleteSuccess);
                    loadData();
                } else {
                    alert(response.msg || window.userI18n.deleteFailed);
                }
            },
            error: function () {
                alert(window.userI18n.serverError);
            }
        })
    }
}

let currentTaskChainUserId = null;

// 显示任务链管理对话框
function getTaskChainRoles(userId) {
    currentTaskChainUserId = userId;
    $('#taskChainModal').modal('show');
    loadTaskChainData();
}

// 加载任务链数据
function loadTaskChainData(pageNum = 1) {
    const keyword = $('#template-search-keyword').val();
    const searchField = $('#template-search-field').val();
    const sortOrder = $('#template-sort-order').val() || 'asc';
    const req = {
        hxUserId: currentTaskChainUserId,
        pageNum: pageNum,
        pageSize: 10,
        sortOrder: sortOrder
    };

    // 根据选择的搜索字段构建请求参数
    if (searchField === 'groupName') {
        req.groupName = keyword;
    } else if (searchField === 'name') {
        req.name = keyword;
    } else if (searchField === 'alias') {
        req.alias = keyword;
    }

    $('#taskChainTableBody').html('<tr><td colspan="9" class="text-center"><i class="mdi mdi-loading mdi-spin"></i> ' + window.userI18n.loading + '</td></tr>');

    $.ajax({
        url: '/api/user/select/taskChain',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(req),
        success: function (res) {
            console.log('加载任务链数据响应:', res); // 调试信息
            if (res.code === 10000 && res.data && res.data.records.length > 0) {
                console.log('任务链数据:', res.data.records); // 调试信息
                renderTaskChainTable(res.data);
                renderTaskChainPagination(res.data);
            } else {
                $('#taskChainTableBody').html('<tr><td colspan="8" class="text-center">' + window.userI18n.noTaskChainData + '</td></tr>');
                $('#taskChainPagination').empty();
            }
        },
        error: function () {
            $('#taskChainTableBody').html('<tr><td colspan="8" class="text-center">' + window.userI18n.dataLoadFailed + '</td></tr>');
        }
    });
}

// 渲染任务链表格
function renderTaskChainTable(pageData) {
    const tbody = $('#taskChainTableBody');
    tbody.empty();

    if (!pageData.records || pageData.records.length === 0) {
        tbody.append('<tr><td colspan="9" class="text-center">' + window.userI18n.noTaskChainData + '</td></tr>');
        return;
    }

    pageData.records.forEach((taskChain, index) => {
        const tr = $('<tr>');
        tr.append(`<td><input type="checkbox" class="row-checkbox" value="${taskChain.id}" data-template-id="${taskChain.taskChainTemplateId}"></td>`)
        tr.append(`<td>${(pageData.current - 1) * pageData.size + index + 1}</td>`);
        tr.append(`<td>${taskChain.name || '-'}</td>`);
        tr.append(`<td>${taskChain.groupName || '-'}</td>`);
        tr.append(`<td>${taskChain.alias || '-'}</td>`);
        tr.append(`<td>${taskChain.warehouseName || '-'}</td>`);
        tr.append(`<td>${taskChain.columnName || '-'}</td>`);

        // 排序值（可编辑）
        const sortValue = taskChain.sortOrder || 0;
        console.log('渲染任务链:', taskChain.name, '排序值:', sortValue); // 调试信息
        tr.append(`<td style="text-align: center;">
                <div class="input-group input-group-sm" style="width: 110px; margin: 0 auto;">
                    <input type="number" class="form-control sort-input" value="${sortValue}"
                           data-id="${taskChain.id}" style="text-align: center;">
                    <span class="input-group-btn">
                        <button class="btn btn-primary btn-xs" onclick="updateSortOrder(${taskChain.id}, this)"
                                title="保存排序">
                            <i class="mdi mdi-check"></i>
                        </button>
                    </span>
                </div>
            </td>`);

        // 操作按钮
        const actions = `
            <div class="btn-group">
                <button class="btn btn-sm btn-danger" onclick="removeTaskChainRole(${taskChain.id})">
                    <i class="mdi mdi-delete"></i> ${window.userI18n.remove}
                </button>
            </div>`;
        tr.append(`<td>${actions}</td>`);

        tbody.append(tr);
    });
}

function setGroupName() {
    const groupName = prompt(window.userI18n.enterGroupName);
    const selectedIds = [];
    $('.row-checkbox:checked').each(function () {
        selectedIds.push($(this).val());
    });
    if (groupName === null) return; // 用户取消

    if (!groupName.trim()) {
        alert(window.userI18n.groupNameEmpty);
        return;
    }

    $.ajax({
        url: '/api/task/chain/updateGroupName',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            idList: selectedIds,
            groupName: groupName.trim()
        }),
        success: function (res) {
            if (res.code === 10000) {
                // 局部更新对应行的显示
                loadTaskChainData()
            } else {
                alert(res.msg || window.userI18n.updateFailed);
            }
        },
        error: function () {
            alert(window.userI18n.serverError);
        }
    });
}

function setGlobalGroupName() {
    const groupName = prompt(window.userI18n.enterGlobalGroupName);
    const selectedTemplateIds = [];
    $('.row-checkbox:checked').each(function () {
        const templateId = $(this).data('template-id');
        if (templateId && selectedTemplateIds.indexOf(templateId) === -1) {
            selectedTemplateIds.push(templateId);
        }
    });

    if (groupName === null) return; // 用户取消

    if (!groupName.trim()) {
        alert(window.userI18n.groupNameEmpty);
        return;
    }

    if (selectedTemplateIds.length === 0) {
        alert(window.userI18n.selectTemplateFirst);
        return;
    }

    if (!confirm(window.userI18n.globalGroupConfirm.replace('{0}', groupName.trim()))) {
        return;
    }

    $.ajax({
        url: '/api/task/chain/updateGlobalGroupName',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            templateIds: selectedTemplateIds,
            groupName: groupName.trim()
        }),
        success: function (res) {
            if (res.code === 10000) {
                alert(window.userI18n.globalGroupSuccess);
                loadTaskChainData();
            } else {
                alert(res.msg || window.userI18n.globalGroupFailed);
            }
        },
        error: function () {
            alert(window.userI18n.serverError);
        }
    });
}

function removeTaskChainRole(hxTaskChainTemplateId) {
    if (confirm(window.userI18n.deleteTaskChainConfirm)) {
        $.ajax({
            url: '/api/task/chain/deleteMapping',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({id: hxTaskChainTemplateId}),
            success: function (res) {
                if (res.code === 10000) {
                    alert(window.userI18n.deleteSuccess);
                    loadTaskChainData();
                } else {
                    alert(res.msg || window.userI18n.deleteFailed);
                }
            },
            error: function () {
                alert(window.userI18n.deleteFailed);
            }
        });
    }
}

// 批量删除任务链
function batchDeleteTaskChain() {
    const selectedIds = [];
    $('.row-checkbox:checked').each(function () {
        selectedIds.push(parseInt($(this).val()));
    });

    if (selectedIds.length === 0) {
        alert(window.userI18n.selectAtLeastOneRecord);
        return;
    }

    if (confirm(window.userI18n.batchDeleteConfirm.replace('{0}', selectedIds.length))) {
        $.ajax({
            url: '/api/task/chain/batchDelete',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ids: selectedIds}),
            success: function (res) {
                if (res.code === 10000) {
                    alert(window.userI18n.batchDeleteSuccess);
                    // 取消全选状态
                    $('#templateSelectAll').prop('checked', false);
                    loadTaskChainData();
                } else {
                    alert(res.msg || window.userI18n.batchDeleteFailed);
                }
            },
            error: function () {
                alert(window.userI18n.serverError);
            }
        });
    }
}

function renderTaskChainPagination(pageData) {
    let html = '';
    const totalPages = pageData.pages;
    const currentPage = pageData.current;

    if (totalPages > 1) {
        // 上一页
        html += `<li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0)" onclick="${currentPage === 1 ? '' : `loadTaskChainData(${currentPage - 1})`}">«</a>
            </li>`;

        // 显示页码（最多显示7个）
        const startPage = Math.max(1, currentPage - 3);
        const endPage = Math.min(totalPages, currentPage + 3);

        if (startPage > 1) {
            html += `<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="loadTaskChainData(1)">1</a></li>`;
            if (startPage > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = startPage; i <= endPage; i++) {
            html += `<li class="page-item ${currentPage === i ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0)" onclick="loadTaskChainData(${i})">${i}</a>
                </li>`;
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="loadTaskChainData(${totalPages})">${totalPages}</a></li>`;
        }

        // 下一页
        html += `<li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0)" onclick="${currentPage === totalPages ? '' : `loadTaskChainData(${currentPage + 1})`}">»</a>
            </li>`;
    }
    $('#taskChainPagination').html(html);
}

function getAmrRoles(userId) {
    currentTaskChainUserId = userId;
    $('#amrModal').modal('show');
    loadAmrData();
}

// 加载任务链数据
function loadAmrData(pageNum = 1) {
    const req = {
        hxUserId: currentTaskChainUserId,
        pageNum: pageNum,
        pageSize: 10
    };

    $('#amrTableBody').html('<tr><td colspan="6" class="text-center"><i class="mdi mdi-loading mdi-spin"></i> ' + window.userI18n.loading + '</td></tr>');

    $.ajax({
        url: '/api/user/select/amr',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(req),
        success: function (res) {
            if (res.code === 10000 && res.data && res.data.records.length > 0) {
                renderAmrTable(res.data);
                renderAmrPagination(res.data);
            } else {
                $('#amrTableBody').html('<tr><td colspan="5" class="text-center">' + window.userI18n.noData + '</td></tr>');
                $('#amrPagination').empty();
            }
        },
        error: function () {
            $('#amrTableBody').html('<tr><td colspan="5" class="text-center">' + window.userI18n.dataLoadFailed + '</td></tr>');
        }
    });
}

// 渲染任务链表格
function renderAmrTable(pageData) {
    const tbody = $('#amrTableBody');
    tbody.empty();

    if (!pageData.records || pageData.records.length === 0) {
        tbody.append('<tr><td colspan="5" class="text-center">' + window.userI18n.noData + '</td></tr>');
        return;
    }

    pageData.records.forEach((amr, index) => {
        const tr = $('<tr>');
        tr.append(`<td>${(pageData.current - 1) * pageData.size + index + 1}</td>`);
        tr.append(`<td>${amr.amrId || '-'}</td>`);
        tr.append(`<td>${amr.alias || '-'}</td>`);
        // 操作按钮
        const actions = `
            <div class="btn-group">
                <button class="btn btn-sm btn-danger" onclick="removeAmrRole(${amr.id})">
                    <i class="mdi mdi-delete"></i> ${window.userI18n.remove}
                </button>
            </div>`;
        tr.append(`<td>${actions}</td>`);
        tbody.append(tr);
    });
}

function renderAmrPagination(pageData) {
    let html = '';
    const totalPages = pageData.pages;
    const currentPage = pageData.current;

    if (totalPages > 1) {
        // 上一页
        html += `<li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0)" onclick="${currentPage === 1 ? '' : `loadAmrData(${currentPage - 1})`}">«</a>
            </li>`;

        // 显示页码（最多显示7个）
        const startPage = Math.max(1, currentPage - 3);
        const endPage = Math.min(totalPages, currentPage + 3);

        if (startPage > 1) {
            html += `<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="loadAmrData(1)">1</a></li>`;
            if (startPage > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = startPage; i <= endPage; i++) {
            html += `<li class="page-item ${currentPage === i ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0)" onclick="loadAmrData(${i})">${i}</a>
                </li>`;
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="loadAmrData(${totalPages})">${totalPages}</a></li>`;
        }

        // 下一页
        html += `<li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0)" onclick="${currentPage === totalPages ? '' : `loadAmrData(${currentPage + 1})`}">»</a>
            </li>`;
    }
    $('#amrPagination').html(html);
}

function removeAmrRole(id) {
    $.ajax({
        url: '/api/amr/table/deleteMapping',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({id: id}),
        success: function (res) {
            if (res.code === 10000) {
                alert(window.userI18n.deleteSuccess);
                loadAmrData();
            } else {
                alert(res.msg || window.userI18n.deleteFailed);
            }
        },
        error: function () {
            alert(window.userI18n.deleteFailed);
        }
    });
}

// 更新单个任务链排序
function updateSortOrder(id, button) {
    const input = $(button).closest('.input-group').find('.sort-input');
    const sortOrder = parseInt(input.val());

    console.log('更新排序 - ID:', id, '排序值:', sortOrder); // 调试信息

    if (isNaN(sortOrder)) {
        alert(window.userI18n.enterValidNumber);
        return;
    }

    // 禁用按钮防止重复点击
    $(button).prop('disabled', true);
    $(button).html('<i class="mdi mdi-loading mdi-spin"></i>');

    $.ajax({
        url: '/api/user/taskChain/setSortOrder', // 临时使用测试接口
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            id: id,
            sortOrder: sortOrder
        }),
        success: function (res) {
            console.log('排序更新响应:', res); // 调试信息

            if (res.code === 10000) {
                // 显示成功提示
                $(button).html('<i class="mdi mdi-check text-success"></i>');
                $(button).prop('disabled', false);

                setTimeout(() => {
                    $(button).html('<i class="mdi mdi-check"></i>');
                    // 重新加载数据以更新排序
                    loadTaskChainData();
                }, 100);
            } else {
                $(button).html('<i class="mdi mdi-check"></i>');
                $(button).prop('disabled', false);
                alert(res.msg || window.userI18n.updateFailed);
            }
        },
        error: function (xhr, status, error) {
            console.error('排序更新错误:', xhr, status, error); // 调试信息
            $(button).html('<i class="mdi mdi-check"></i>');
            $(button).prop('disabled', false);
            alert(window.userI18n.serverError + ': ' + error);
        }
    });
}

// 显示批量排序模态框
function showBatchSortModal() {
    const selectedIds = [];
    const selectedData = [];

    $('.row-checkbox:checked').each(function () {
        const id = $(this).val();
        const row = $(this).closest('tr');
        const name = row.find('td:eq(2)').text();
        const groupName = row.find('td:eq(3)').text();
        const currentSort = row.find('.sort-input').val();

        selectedIds.push(id);
        selectedData.push({
            id: id,
            name: name,
            groupName: groupName,
            currentSort: currentSort
        });
    });

    if (selectedIds.length === 0) {
        alert(window.userI18n.selectTaskChainsFirst);
        return;
    }

    // 渲染批量排序表格
    const tbody = $('#batchSortTableBody');
    tbody.empty();

    selectedData.forEach(item => {
        const tr = $(`
                <tr>
                    <td>${item.name}</td>
                    <td>${item.groupName}</td>
                    <td>${item.currentSort}</td>
                    <td>
                        <input type="number" class="form-control batch-sort-input"
                               value="${item.currentSort}" data-id="${item.id}">
                    </td>
                </tr>
            `);
        tbody.append(tr);
    });

    $('#batchSortModal').modal('show');
}

// 保存批量排序
function saveBatchSort() {
    const sortItems = [];

    $('.batch-sort-input').each(function () {
        const id = $(this).data('id');
        const sortOrder = parseInt($(this).val());

        if (!isNaN(sortOrder)) {
            sortItems.push({
                id: id,
                sortOrder: sortOrder
            });
        }
    });

    if (sortItems.length === 0) {
        alert(window.userI18n.enterValidSortValue);
        return;
    }

    $.ajax({
        url: '/api/user/taskChain/batchSetSortOrder',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            sortItems: sortItems
        }),
        success: function (res) {
            if (res.code === 10000) {
                $('#batchSortModal').modal('hide');
                alert(window.userI18n.batchSortSuccess);
                loadTaskChainData();
            } else {
                alert(res.msg || window.userI18n.batchSortFailed);
            }
        },
        error: function () {
            alert(window.userI18n.serverError);
        }
    });
}

// 排序方式改变时重新加载数据
$('#template-sort-order').change(function () {
    loadTaskChainData();
});

// ==================== 仓库权限管理功能 ====================
let currentWarehouseUserId = null;
let allWarehouses = [];

// 显示仓库权限管理对话框
function getWarehouseRoles(userId) {
    currentWarehouseUserId = userId;
    $('#warehouseModal').modal('show');
    loadWarehousePermissionData();
    loadAllWarehouses();
}

// 加载用户的仓库权限数据
function loadWarehousePermissionData() {
    $.ajax({
        url: `/api/user-warehouse/user/${currentWarehouseUserId}/warehouses/details`,
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                renderWarehousePermissionTable(response.data);
            } else {
                alert(window.userI18n.loadWarehousePermissionFailed + ': ' + response.msg);
            }
        },
        error: function () {
            alert(window.userI18n.loadWarehousePermissionFailed + '，' + window.userI18n.checkNetworkConnection);
        }
    });
}

// 加载所有仓库列表
function loadAllWarehouses() {
    $.ajax({
        url: '/api/warehouse/page',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            pageNum: 1,
            pageSize: 1000
        }),
        success: function (response) {
            if (response.code === 10000) {
                allWarehouses = response.data.records;
                renderWarehouseCheckboxes();
            } else {
                alert(window.userI18n.loadWarehouseListFailed + ': ' + response.msg);
            }
        },
        error: function () {
            alert(window.userI18n.loadWarehouseListFailed + '，' + window.userI18n.checkNetworkConnection);
        }
    });
}

// 渲染仓库权限表格
function renderWarehousePermissionTable(warehouses) {
    const tbody = $('#warehousePermissionTableBody');
    tbody.empty();

    if (warehouses.length === 0) {
        tbody.append(`
                <tr>
                    <td colspan="4" class="text-center text-muted">${window.userI18n.noWarehousePermission}</td>
                </tr>
            `);
        return;
    }

    warehouses.forEach(warehouse => {
        const tr = $(`
                <tr>
                    <td>
                        <label class="hx-checkbox checkbox-primary">
                            <input type="checkbox" class="warehouse-permission-checkbox" value="${warehouse.warehouseId}">
                            <span></span>
                        </label>
                    </td>
                    <td>${warehouse.warehouseId}</td>
                    <td>${warehouse.warehouseName}</td>
                    <td>
                        <button class="btn btn-sm btn-danger" onclick="removeWarehousePermission(${warehouse.warehouseId})">
                            <i class="mdi mdi-delete"></i> ${window.userI18n.removeWarehousePermission}
                        </button>
                    </td>
                </tr>
            `);
        tbody.append(tr);
    });
}

// 渲染仓库复选框
function renderWarehouseCheckboxes() {
    const container = $('#warehouseCheckboxList');
    container.empty();

    allWarehouses.forEach(warehouse => {
        const checkbox = $(`
                <div class="checkbox">
                    <label class="hx-checkbox checkbox-primary">
                        <input type="checkbox" value="${warehouse.warehouseId}" name="selectedWarehouses">
                        <span></span>
                        ${warehouse.warehouseName}
                    </label>
                </div>
            `);
        container.append(checkbox);
    });
}

// 显示分配仓库权限对话框
function assignWarehousePermission() {
    // 清空之前的选择
    $('input[name="selectedWarehouses"]').prop('checked', false);
    $('input[name="warehouseAddType"][value="1"]').prop('checked', true);

    $('#assignWarehouseModal').modal('show');
}

// 保存仓库权限设置
function saveWarehouseRoles() {
    const selectedWarehouseIds = [];
    $('input[name="selectedWarehouses"]:checked').each(function () {
        selectedWarehouseIds.push(parseInt($(this).val()));
    });

    const addType = parseInt($('input[name="warehouseAddType"]:checked').val());

    if (selectedWarehouseIds.length === 0) {
        alert(window.userI18n.selectAtLeastOneWarehouse);
        return;
    }

    const requestData = {
        userIds: [currentWarehouseUserId],
        warehouseIds: selectedWarehouseIds,
        assignType: addType
    };

    $.ajax({
        url: '/api/user-warehouse/assign',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        success: function (response) {
            if (response.code === 10000) {
                alert(window.userI18n.warehousePermissionAssignSuccess);
                $('#assignWarehouseModal').modal('hide');
                loadWarehousePermissionData();
            } else {
                alert(window.userI18n.warehousePermissionAssignFailed + ': ' + response.msg);
            }
        },
        error: function () {
            alert(window.userI18n.warehousePermissionAssignFailed + '，' + window.userI18n.checkNetworkConnection);
        }
    });
}

// 移除单个仓库权限
function removeWarehousePermission(warehouseId) {
    if (!confirm(window.userI18n.removeWarehousePermissionConfirm)) {
        return;
    }

    // 获取当前用户的所有仓库权限，然后移除指定的
    $.ajax({
        url: `/api/user-warehouse/user/${currentWarehouseUserId}/warehouses`,
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                const currentWarehouseIds = response.data.filter(id => id !== warehouseId);

                // 如果移除后没有剩余权限，使用特殊处理
                if (currentWarehouseIds.length === 0) {
                    // 创建一个临时仓库权限，然后立即移除所有权限
                    // 这里我们需要调用一个专门的移除接口，或者使用其他方式
                    removeAllWarehousePermissions([warehouseId]);
                } else {
                    const requestData = {
                        userIds: [currentWarehouseUserId],
                        warehouseIds: currentWarehouseIds,
                        assignType: 2 // 覆盖模式
                    };

                    assignWarehousePermissions(requestData, window.userI18n.warehousePermissionRemoveSuccess);
                }
            }
        },
        error: function () {
            alert(window.userI18n.getUserPermissionFailed + '，' + window.userI18n.checkNetworkConnection);
        }
    });
}

// 批量移除仓库权限
function batchRemoveWarehousePermission() {
    const selectedIds = [];
    $('.warehouse-permission-checkbox:checked').each(function () {
        selectedIds.push(parseInt($(this).val()));
    });

    if (selectedIds.length === 0) {
        alert(window.userI18n.selectAtLeastOneWarehousePermission);
        return;
    }

    if (!confirm(window.userI18n.batchRemoveWarehouseConfirm.replace('{0}', selectedIds.length))) {
        return;
    }

    // 获取当前用户的所有仓库权限，然后移除选中的
    $.ajax({
        url: `/api/user-warehouse/user/${currentWarehouseUserId}/warehouses`,
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                const remainingWarehouseIds = response.data.filter(id => !selectedIds.includes(id));

                // 如果移除后没有剩余权限，移除所有权限
                if (remainingWarehouseIds.length === 0) {
                    removeAllWarehousePermissions(selectedIds);
                } else {
                    const requestData = {
                        userIds: [currentWarehouseUserId],
                        warehouseIds: remainingWarehouseIds,
                        assignType: 2 // 覆盖模式
                    };

                    assignWarehousePermissions(requestData, window.userI18n.batchRemoveWarehouseSuccess);
                }
            }
        },
        error: function () {
            alert(window.userI18n.getUserPermissionFailed + '，' + window.userI18n.checkNetworkConnection);
        }
    });
}

// 全选/取消全选仓库权限
$('#selectAllWarehouses').change(function () {
    $('.warehouse-permission-checkbox').prop('checked', $(this).prop('checked'));
});

// 辅助函数：分配仓库权限
function assignWarehousePermissions(requestData, successMessage) {
    $.ajax({
        url: '/api/user-warehouse/assign',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        success: function (response) {
            if (response.code === 10000) {
                alert(successMessage);
                loadWarehousePermissionData();
            } else {
                alert(window.userI18n.operationFailed + ': ' + response.msg);
            }
        },
        error: function () {
            alert(window.userI18n.operationFailed + '，' + window.userI18n.checkNetworkConnection);
        }
    });
}

// 辅助函数：移除所有仓库权限
function removeAllWarehousePermissions(removedIds) {
    // 使用专门的清空权限接口
    $.ajax({
        url: `/api/user-warehouse/user/${currentWarehouseUserId}/warehouses`,
        type: 'DELETE',
        success: function (response) {
            if (response.code === 10000) {
                alert(window.userI18n.warehousePermissionRemoveSuccess);
                // 刷新权限数据显示
                loadWarehousePermissionData();
            } else {
                alert(window.userI18n.permissionRemoveFailed + ': ' + response.msg);
            }
        },
        error: function () {
            alert(window.userI18n.permissionRemoveFailed + '，' + window.userI18n.checkNetworkConnection);
        }
    });
}