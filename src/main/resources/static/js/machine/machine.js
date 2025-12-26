// 机台管理JavaScript
let currentPage = 1;
let pageSize = 10;
let currentStationId = null;

$(document).ready(function () {
    loadMachines();
    loadMaps();
});

// 加载机台列表
function loadMachines(page = 1) {
    currentPage = page;
    showLoading();

    const searchData = {
        pageNum: currentPage,
        pageSize: pageSize,
        stationName: $('#searchStationName').val(),
        deviceType: $('#searchDeviceType').val() || null,
        status: $('#searchStatus').val() || null
    };

    $.ajax({
        url: '/api/machine/station/page',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(searchData),
        success: function (response) {
            hideLoading();
            if (response.code === 10000) {
                renderMachineTable(response.data.records);
                renderPagination(response.data);
            } else {
                showAlert('加载机台列表失败：' + response.msg, 'danger');
            }
        },
        error: function () {
            hideLoading();
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });
}

// 渲染机台表格
function renderMachineTable(machines) {
    const tbody = $('#machineTableBody');
    tbody.empty();

    if (!machines || machines.length === 0) {
        tbody.append('<tr><td colspan="9" class="text-center">暂无数据</td></tr>');
        return;
    }

    machines.forEach(machine => {
        const row = `
            <tr>
                <td>${machine.stationName || ''}</td>
                <td><span class="device-type-badge">${machine.deviceTypeDesc || ''}</span></td>
                <td>${machine.mapId || ''}</td>
                <td>${machine.areaId || ''}</td>
                <td>${machine.taskChainTemplateId || ''}</td>
                <td>${(machine.firstAddressId === 0 || machine.firstAddressId) ? machine.firstAddressId : ''}</td>
                <td><span class="status-badge ${machine.status === 1 ? 'status-enabled' : 'status-disabled'}">${machine.statusDesc || ''}</span></td>
                <td>${formatDateTime(machine.createdTime)}</td>
                <td class="action-buttons">
                    <button class="btn btn-sm btn-info" onclick="showMachineDetail(${machine.stationId})">
                        <i class="mdi mdi-eye"></i> 详情
                    </button>
                    <button class="btn btn-sm btn-warning" onclick="showEditMachine(${machine.stationId})">
                        <i class="mdi mdi-pencil"></i> 编辑
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deleteMachine(${machine.stationId})">
                        <i class="mdi mdi-delete"></i> 删除
                    </button>
                </td>
            </tr>
        `;
        tbody.append(row);
    });
}

// 渲染分页
function renderPagination(pageData) {
    const pagination = $('#pagination');
    pagination.empty();

    const totalPages = pageData.pages;
    const current = pageData.current;

    if (totalPages <= 1) return;

    // 上一页
    if (current > 1) {
        pagination.append(`<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="loadMachines(${current - 1})">上一页</a></li>`);
    }

    // 页码
    for (let i = Math.max(1, current - 2); i <= Math.min(totalPages, current + 2); i++) {
        const activeClass = i === current ? 'active' : '';
        pagination.append(`<li class="page-item ${activeClass}"><a class="page-link" href="javascript:void(0)" onclick="loadMachines(${i})">${i}</a></li>`);
    }

    // 下一页
    if (current < totalPages) {
        pagination.append(`<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="loadMachines(${current + 1})">下一页</a></li>`);
    }
}

// 搜索机台
function searchMachines() {
    loadMachines(1);
}

// 重置搜索
function resetSearch() {
    $('#searchStationName').val('');
    $('#searchDeviceType').val('');
    $('#searchStatus').val('');
    loadMachines(1);
}

// 添加机台
function addMachine() {
    const formData = {
        stationName: $('#addStationName').val(),
        deviceType: parseInt($('#addDeviceType').val()),
        mapId: parseInt($('#addMapId').val()),
        areaId: $('#addAreaId').val() ? parseInt($('#addAreaId').val()) : null,
        description: $('#addDescription').val(),
        taskChainTemplateId: parseInt($('#addTaskChainTemplateId').val()),
        firstAddressId: $('#addFirstAddressId').val() === '' ? null : parseInt($('#addFirstAddressId').val(), 10),
        status: parseInt($('#addStatus').val())
    };

    if (!formData.stationName || !formData.deviceType || !formData.mapId || !formData.taskChainTemplateId) {
        showAlert('请填写所有必填项（机台名称、设备类型、地图、任务链模板）', 'warning');
        return;
    }

    showLoading();
    $.ajax({
        url: '/api/machine/station/add',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function (response) {
            hideLoading();
            if (response.code === 10000) {
                $('#addMachineModal').modal('hide');
                $('#addMachineForm')[0].reset();
                showAlert('添加机台成功', 'success');
                loadMachines(currentPage);
            } else {
                showAlert('添加机台失败：' + response.msg, 'danger');
            }
        },
        error: function () {
            hideLoading();
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });
}

// 显示编辑机台模态框
function showEditMachine(stationId) {
    showLoading();
    $.ajax({
        url: `/api/machine/station/detail/${stationId}`,
        type: 'GET',
        success: function (response) {
            hideLoading();
            if (response.code === 10000 && response.data) {
                const machine = response.data;
                $('#editStationId').val(machine.stationId);
                $('#editStationName').val(machine.stationName);
                $('#editDeviceType').val(machine.deviceType);
                $('#editMapId').val(machine.mapId || '');
                $('#editTaskChainTemplateId').val(machine.taskChainTemplateId || '');
                $('#editTaskChainTemplateDisplay').val(machine.taskChainTemplateId ? `任务链模板${machine.taskChainTemplateId}` : '');
                $('#editFirstAddressId').val((machine.firstAddressId === 0 || machine.firstAddressId) ? machine.firstAddressId : '');
                $('#editStatus').val(machine.status);
                $('#editDescription').val(machine.description || '');

                // 加载对应的区域列表
                if (machine.mapId) {
                    loadAreas(machine.mapId, 'edit');
                    // 延迟设置区域值，等待区域列表加载完成
                    setTimeout(() => {
                        $('#editAreaId').val(machine.areaId || '');
                    }, 500);
                }

                $('#editMachineModal').modal('show');
            } else {
                showAlert('获取机台信息失败：' + response.msg, 'danger');
            }
        },
        error: function () {
            hideLoading();
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });
}

// 更新机台
function updateMachine() {
    const formData = {
        stationId: parseInt($('#editStationId').val()),
        stationName: $('#editStationName').val(),
        deviceType: parseInt($('#editDeviceType').val()),
        mapId: parseInt($('#editMapId').val()),
        areaId: $('#editAreaId').val() ? parseInt($('#editAreaId').val()) : null,
        description: $('#editDescription').val(),
        taskChainTemplateId: parseInt($('#editTaskChainTemplateId').val()),
        firstAddressId: $('#editFirstAddressId').val() === '' ? null : parseInt($('#editFirstAddressId').val(), 10),
        status: parseInt($('#editStatus').val())
    };

    if (!formData.stationName || !formData.deviceType || !formData.mapId || !formData.taskChainTemplateId) {
        showAlert('请填写所有必填项（机台名称、设备类型、地图、任务链模板）', 'warning');
        return;
    }

    showLoading();
    $.ajax({
        url: '/api/machine/station/update',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function (response) {
            hideLoading();
            if (response.code === 10000) {
                $('#editMachineModal').modal('hide');
                showAlert('更新机台成功', 'success');
                loadMachines(currentPage);
            } else {
                showAlert('更新机台失败：' + response.msg, 'danger');
            }
        },
        error: function () {
            hideLoading();
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });
}

// 删除机台
function deleteMachine(stationId) {
    if (!confirm('确定要删除这个机台吗？删除后无法恢复！')) {
        return;
    }

    showLoading();
    $.ajax({
        url: `/api/machine/station/delete/${stationId}`,
        type: 'POST',
        success: function (response) {
            hideLoading();
            if (response.code === 10000) {
                showAlert('删除机台成功', 'success');
                loadMachines(currentPage);
            } else {
                showAlert('删除机台失败：' + response.msg, 'danger');
            }
        },
        error: function () {
            hideLoading();
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });
}

// 显示机台详情
function showMachineDetail(stationId) {
    currentStationId = stationId;
    showLoading();

    $.ajax({
        url: `/api/machine/station/detail/${stationId}`,
        type: 'GET',
        success: function (response) {
            hideLoading();
            if (response.code === 10000 && response.data) {
                renderMachineDetail(response.data);
                $('#detailMachineModal').modal('show');
            } else {
                showAlert('获取机台详情失败：' + response.msg, 'danger');
            }
        },
        error: function () {
            hideLoading();
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });
}

// 渲染机台详情
function renderMachineDetail(machine) {
    const detailHtml = `
        <div class="row">
            <div class="col-md-6">
                <p><strong>机台ID：</strong>${machine.stationId}</p>
                <p><strong>机台名称：</strong>${machine.stationName}</p>
                <p><strong>设备类型：</strong>${machine.deviceTypeDesc}</p>
                <p><strong>地图ID：</strong>${machine.mapId || '未设置'}</p>
                <p><strong>区域ID：</strong>${machine.areaId || '未设置'}</p>
            </div>
            <div class="col-md-6">
                <p><strong>任务链模板ID：</strong>${machine.taskChainTemplateId || '未设置'}</p>
                <p><strong>通信首地址：</strong>${(machine.firstAddressId === 0 || machine.firstAddressId) ? machine.firstAddressId : '未设置'}</p>
                <p><strong>状态：</strong><span class="status-badge ${machine.status === 1 ? 'status-enabled' : 'status-disabled'}">${machine.statusDesc}</span></p>
                <p><strong>创建时间：</strong>${formatDateTime(machine.createdTime)}</p>
            </div>
        </div>
        <div class="row">
            <div class="col-12">
                <p><strong>机台描述：</strong>${machine.description || '无'}</p>
            </div>
        </div>
    `;
    $('#machineDetailInfo').html(detailHtml);

    // 加载寄存器配置
    loadRegisters(machine.stationId);
}

// 加载寄存器配置
function loadRegisters(stationId) {
    $.ajax({
        url: `/api/machine/station/register/list/station/${stationId}`,
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                renderRegisterTable(response.data);
            } else {
                $('#registerTableBody').html('<tr><td colspan="7" class="text-center">加载寄存器配置失败</td></tr>');
            }
        },
        error: function () {
            $('#registerTableBody').html('<tr><td colspan="7" class="text-center">网络错误</td></tr>');
        }
    });
}

// 渲染寄存器表格
function renderRegisterTable(registers) {
    const tbody = $('#registerTableBody');
    tbody.empty();

    if (!registers || registers.length === 0) {
        tbody.append('<tr><td colspan="7" class="text-center">暂无寄存器配置</td></tr>');
        return;
    }

    registers.forEach(register => {
        const typeClass = getRegisterTypeClass(register.registerType);
        const row = `
            <tr>
                <td><span class="register-type-badge ${typeClass}">${register.registerTypeDesc}</span></td>
                <td>${register.registerAddress}</td>
                <td>${register.protocolType || ''}</td>
                <td>${formatControlType(register)}</td>
                <td>${register.vertexCode || ''}${register.callVertexCode ? ' / ' + register.callVertexCode : ''}</td>
                <td><span class="status-badge ${register.status === 1 ? 'status-enabled' : 'status-disabled'}">${register.statusDesc}</span></td>
                <td class="action-buttons">
                    <button class="btn btn-sm btn-warning" onclick="showEditRegister(${register.registerId})">
                        <i class="mdi mdi-pencil"></i> 编辑
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deleteRegister(${register.registerId})">
                        <i class="mdi mdi-delete"></i> 删除
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="cancelRegister(${register.registerId})">
                        <i class="mdi mdi-delete"></i> 重置调度
                    </button>
                </td>
            </tr>
        `;
        tbody.append(row);
    });
}

function formatControlType(register) {
    if (register.registerType !== 1) return '';
    switch (register.controlType) {
        case 1:
            return '右叫车';
        case 2:
            return '右叫料';
        case 3:
            return '左叫车';
        case 4:
            return '左叫料';
        default:
            return '无';
    }
}


// 获取寄存器类型样式类
function getRegisterTypeClass(registerType) {
    switch (registerType) {
        case 1:
            return 'register-type-control';
        case 2:
            return 'register-type-request';
        case 3:
            return 'register-type-allow';
        case 4:
            return 'register-type-exit';
        default:
            return '';
    }
}

// 控制相关区域显示/隐藏（全局）
function toggleControlSections(prefix) {
    const typeVal = parseInt($(`#${prefix}RegisterType`).val(), 10);
    const isControl = typeVal === 1;
    if (prefix === 'add') {
        $('#addControlSection').toggle(isControl);
    } else if (prefix === 'edit') {
        $('#editControlSection').toggle(isControl);
    }
}

// 事件绑定（委托，确保动态元素也能触发）
$(document).on('change', '#addRegisterType', function () {
    toggleControlSections('add');
});
$(document).on('change', '#editRegisterType', function () {
    toggleControlSections('edit');
});


// 显示添加寄存器模态框
function showAddRegisterModal() {
    $('#addRegisterStationId').val(currentStationId);
    $('#addRegisterForm')[0].reset();
    // 初次打开模态框时初始化显示状态
    toggleControlSections('add');

    $('#addRegisterStatus').val('1');
    $('#addProtocolType').val('MODBUS_TCP'); // 默认选择MODBUS_TCP
    setTimeout(function () {
        toggleControlSections('add');
    }, 0);
    $('#addRegisterModal').modal('show');
}

// 添加寄存器
function addRegister() {
    const formData = {
        stationId: parseInt($('#addRegisterStationId').val()),
        registerType: parseInt($('#addRegisterType').val()),
        registerAddress: parseInt($('#addRegisterAddress').val()),
        protocolType: $('#addProtocolType').val(),
        description: $('#addRegisterDescription').val(),
        status: parseInt($('#addRegisterStatus').val()),
        vertexId: $('#addVertexId').val() === '' ? null : parseInt($('#addVertexId').val(), 10),
        vertexCode: $('#addVertexCode').val() || null,
        controlType: $('#addControlType').val() === '' ? null : parseInt($('#addControlType').val(), 10),
        callVertexId: $('#addCallVertexId').val() === '' ? null : parseInt($('#addCallVertexId').val(), 10),
        callVertexCode: $('#addCallVertexCode').val() || null,
        controlValue: $('#addControlValue').val() || null
    };

    if (!formData.registerType || !formData.registerAddress || !formData.protocolType ||
        formData.status === null || formData.status === undefined) {
        showAlert('请填写所有必填项', 'warning');
        return;
    }

    showLoading();
    $.ajax({
        url: '/api/machine/station/register/add',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function (response) {
            hideLoading();
            console.log('添加寄存器响应:', response); // 调试信息
            if (response.code === 10000) {
                $('#addRegisterModal').modal('hide');
                showAlert('添加寄存器配置成功', 'success');
                loadRegisters(currentStationId);
            } else {
                console.log('添加寄存器失败:', response.msg); // 调试信息
                showAlert('添加寄存器配置失败：' + response.msg, 'danger');
            }
        },
        error: function () {
            hideLoading();
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });
}

// 显示编辑寄存器模态框
function showEditRegister(registerId) {
    showLoading();
    $.ajax({
        url: `/api/machine/station/register/detail/${registerId}`,
        type: 'GET',
        success: function (response) {
            hideLoading();
            if (response.code === 10000 && response.data) {
                const register = response.data;
                $('#editRegisterId').val(register.registerId);
                $('#editRegisterStationId').val(register.stationId);
                $('#editRegisterType').val(register.registerType);
                $('#editRegisterAddress').val(register.registerAddress);
                $('#editProtocolType').val(register.protocolType || 'MODBUS_TCP');
                $('#editRegisterDescription').val(register.description || '');
                $('#editRegisterStatus').val(register.status);
                $('#editVertexId').val(register.vertexId || '');
                $('#editVertexCode').val(register.vertexCode || '');
                $('#editControlType').val(register.controlType != null ? register.controlType : '');
                $('#editCallVertexId').val(register.callVertexId || '');
                $('#editCallVertexCode').val(register.callVertexCode || '');
                $('#editControlValue').val(register.controlValue || '');

                // 根据寄存器类型立即显示/隐藏控制相关区域
                toggleControlSections('edit');

                $('#editRegisterModal').modal('show');
            } else {
                showAlert('获取寄存器信息失败：' + response.msg, 'danger');
            }
        },
        error: function () {
            hideLoading();
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });
}

// 更新寄存器
function updateRegister() {
    const formData = {
        registerId: parseInt($('#editRegisterId').val()),
        stationId: parseInt($('#editRegisterStationId').val()),
        registerType: parseInt($('#editRegisterType').val()),
        registerAddress: parseInt($('#editRegisterAddress').val()),
        protocolType: $('#editProtocolType').val(),
        description: $('#editRegisterDescription').val(),
        status: parseInt($('#editRegisterStatus').val()),
        vertexId: $('#editVertexId').val() === '' ? null : parseInt($('#editVertexId').val(), 10),
        vertexCode: $('#editVertexCode').val() || null,
        controlType: $('#editControlType').val() === '' ? null : parseInt($('#editControlType').val(), 10),
        callVertexId: $('#editCallVertexId').val() === '' ? null : parseInt($('#editCallVertexId').val(), 10),
        callVertexCode: $('#editCallVertexCode').val() || null,
        controlValue: $('#editControlValue').val() || null
    };

    if (!formData.registerType || !formData.registerAddress || !formData.protocolType ||
        formData.status === null || formData.status === undefined) {
        showAlert('请填写所有必填项', 'warning');
        return;
    }

    showLoading();
    $.ajax({
        url: '/api/machine/station/register/update',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function (response) {
            hideLoading();
            if (response.code === 10000) {
                $('#editRegisterModal').modal('hide');
                showAlert('更新寄存器配置成功', 'success');
                loadRegisters(currentStationId);
            } else {
                showAlert('更新寄存器配置失败：' + response.msg, 'danger');
            }
        },
        error: function () {
            hideLoading();
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });
}

// 删除寄存器
function deleteRegister(registerId) {
    if (!confirm('确定要删除这个寄存器配置吗？')) {
        return;
    }

    showLoading();
    $.ajax({
        url: `/api/machine/station/register/delete/${registerId}`,
        type: 'POST',
        success: function (response) {
            hideLoading();
            if (response.code === 10000) {
                showAlert('删除寄存器配置成功', 'success');
                loadRegisters(currentStationId);
            } else {
                showAlert('删除寄存器配置失败：' + response.msg, 'danger');
            }
        },
        error: function () {
            hideLoading();
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });
}

// 重置寄存器
function cancelRegister(registerId) {
    if (!confirm('确定要重置这个寄存器的调度吗？')) {
        return;
    }

    showLoading();
    $.ajax({
        url: `/api/machine/station/register/cancel/${registerId}`,
        type: 'POST',
        success: function (response) {
            hideLoading();
            if (response.code === 10000) {
                showAlert('重置寄存器调度成功', 'success');
                loadRegisters(currentStationId);
            } else {
                showAlert('重置寄存器调度失败：' + response.msg, 'danger');
            }
        },
        error: function () {
            hideLoading();
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });
}

// 格式化日期时间
function formatDateTime(dateTime) {
    if (!dateTime) return '';
    const date = new Date(dateTime);
    return date.getFullYear() + '-' +
        String(date.getMonth() + 1).padStart(2, '0') + '-' +
        String(date.getDate()).padStart(2, '0') + ' ' +
        String(date.getHours()).padStart(2, '0') + ':' +
        String(date.getMinutes()).padStart(2, '0') + ':' +
        String(date.getSeconds()).padStart(2, '0');
}

// 显示加载遮罩
function showLoading() {
    $('#loadingOverlay').show();
}

// 隐藏加载遮罩
function hideLoading() {
    $('#loadingOverlay').hide();
}

// 显示通知信息
function showNotification(message, type) {
    console.log('显示通知:', message, type); // 调试信息
    $.notify({
        message: message
    }, {
        type: type,
        delay: 500,
        z_index: 10060, // 确保在模态框之上 (Bootstrap模态框默认z-index是1050)
        placement: {
            from: 'top',
            align: 'right'
        }
    });
}

// 兼容旧的showAlert函数
function showAlert(message, type = 'info') {
    showNotification(message, type);
}

// 加载地图列表
function loadMaps() {
    $.ajax({
        url: '/api/machine/station/maps',
        type: 'GET',
        success: function (response) {
            if (response.code === 10000 && response.data) {
                const addMapSelect = $('#addMapId');
                const editMapSelect = $('#editMapId');

                addMapSelect.empty().append('<option value="">请选择地图</option>');
                editMapSelect.empty().append('<option value="">请选择地图</option>');

                response.data.forEach(map => {
                    const option = `<option value="${map.mapId}">${map.mapName}</option>`;
                    addMapSelect.append(option);
                    editMapSelect.append(option);
                });
            }
        },
        error: function () {
            console.error('加载地图列表失败');
        }
    });
}

// 根据地图ID加载区域列表
function loadAreas(mapId, formType) {
    if (!mapId) {
        const areaSelect = formType === 'add' ? $('#addAreaId') : $('#editAreaId');
        areaSelect.empty().append('<option value="">请选择区域</option>');
        return;
    }

    $.ajax({
        url: `/api/machine/station/areas/${mapId}`,
        type: 'GET',
        success: function (response) {
            if (response.code === 10000 && response.data) {
                const areaSelect = formType === 'add' ? $('#addAreaId') : $('#editAreaId');
                areaSelect.empty().append('<option value="">请选择区域</option>');

                response.data.forEach(area => {
                    const option = `<option value="${area.areaId}">${area.areaName || area.areaAlias || area.areaId}</option>`;
                    areaSelect.append(option);
                });
            }
        },
        error: function () {
            console.error('加载区域列表失败');
        }
    });
}

// 任务链模板选择器相关变量
let templateSelectorType = 'add'; // 'add' 或 'edit'
let templateCurrentPage = 1;
let templatePageSize = 10;
let selectedTemplateId = null;
let selectedTemplateName = null;

// 显示任务链模板选择器
function showTaskChainTemplateSelector(type) {
    templateSelectorType = type;
    selectedTemplateId = null;
    selectedTemplateName = null;
    $('#taskChainTemplateSelectorModal').modal('show');
    loadTaskChainTemplatesPage(1);
}

// 分页加载任务链模板
function loadTaskChainTemplatesPage(page = 1) {
    templateCurrentPage = page;
    showLoading();

    const searchData = {
        pageNum: templateCurrentPage,
        pageSize: templatePageSize,
        name: $('#templateSearchKeyword').val(),
        alias: $('#templateSearchKeyword').val()
    };

    $.ajax({
        url: '/api/task/chain/list',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(searchData),
        success: function (response) {
            hideLoading();
            if (response.code === 10000) {
                renderTaskChainTemplateTable(response.data.records);
                renderTaskChainTemplatePagination(response.data);
            } else {
                showAlert('加载任务链模板失败：' + response.msg, 'danger');
            }
        },
        error: function () {
            hideLoading();
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });
}

// 渲染任务链模板表格
function renderTaskChainTemplateTable(templates) {
    const tbody = $('#templateTableBody');
    tbody.empty();

    if (!templates || templates.length === 0) {
        tbody.append('<tr><td colspan="6" class="text-center">暂无数据</td></tr>');
        return;
    }

    templates.forEach(template => {
        const displayName = template.alias || template.name || template.id;
        const row = `
            <tr>
                <td>
                    <input type="radio" name="templateSelector" value="${template.id}"
                           data-name="${displayName}"
                           onchange="selectTemplate(${template.id}, '${displayName}')">
                </td>
                <td>${template.id}</td>
                <td>${template.name || ''}</td>
                <td>${template.alias || ''}</td>
                <td><span class="task-chain-type-badge task-chain-type-${template.chainType}">${getChainTypeDesc(template.chainType)}</span></td>
                <td>${formatDateTime(template.createdTime)}</td>
            </tr>
        `;
        tbody.append(row);
    });
}

// 渲染任务链模板分页
function renderTaskChainTemplatePagination(pageData) {
    const pagination = $('#templatePagination');
    pagination.empty();

    const totalPages = pageData.pages;
    const current = pageData.current;

    if (totalPages <= 1) return;

    // 上一页
    if (current > 1) {
        pagination.append(`<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="loadTaskChainTemplatesPage(${current - 1})">上一页</a></li>`);
    }

    // 页码
    for (let i = Math.max(1, current - 2); i <= Math.min(totalPages, current + 2); i++) {
        const activeClass = i === current ? 'active' : '';
        pagination.append(`<li class="page-item ${activeClass}"><a class="page-link" href="javascript:void(0)" onclick="loadTaskChainTemplatesPage(${i})">${i}</a></li>`);
    }

    // 下一页
    if (current < totalPages) {
        pagination.append(`<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="loadTaskChainTemplatesPage(${current + 1})">下一页</a></li>`);
    }
}

// 搜索任务链模板
function searchTaskChainTemplates() {
    loadTaskChainTemplatesPage(1);
}

// 选择模板
function selectTemplate(templateId, templateName) {
    selectedTemplateId = templateId;
    selectedTemplateName = templateName;
}

// 获取任务链类型描述
function getChainTypeDesc(chainType) {
    switch (chainType) {
        case 1:
            return '普通任务链';
        case 2:
            return '入库任务链';
        case 3:
            return '出库任务链';
        case 4:
            return '移库任务链';
        default:
            return '未知类型';
    }
}

// 确认任务链模板选择
function confirmTaskChainTemplateSelection() {
    if (!selectedTemplateId) {
        showAlert('请选择一个任务链模板', 'warning');
        return;
    }

    // 设置选中的模板
    if (templateSelectorType === 'add') {
        $('#addTaskChainTemplateId').val(selectedTemplateId);
        $('#addTaskChainTemplateDisplay').val(selectedTemplateName);
    } else {
        $('#editTaskChainTemplateId').val(selectedTemplateId);
        $('#editTaskChainTemplateDisplay').val(selectedTemplateName);
    }

    $('#taskChainTemplateSelectorModal').modal('hide');
}
