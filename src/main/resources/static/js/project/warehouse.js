// 仓库管理JavaScript

$(document).ready(function () {
    // 页面加载时获取仓库数据
    loadWarehouses();

    // 搜索框回车事件
    $('#searchInput').on('keypress', function (e) {
        if (e.which === 13) {
            searchWarehouses();
        }
    });

    // 默认显示可视化视图
    showVisualizationView();
});

// 加载仓库数据
function loadWarehouses() {
    $.ajax({
        url: '/api/warehouse/all',
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                renderWarehouseTable(response.data);
                populateWarehouseSelector(response.data);
            } else {
                showNotification(window.warehouseI18n.getDataFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getDataFailed, 'danger');
        }
    });
}

// 渲染仓库表格
function renderWarehouseTable(warehouses) {
    const tbody = $('#warehouseTable');
    tbody.empty();

    if (!warehouses || warehouses.length === 0) {
        tbody.html('<tr><td colspan="5" class="text-center text-muted">' + window.warehouseI18n.noWarehouseData + '</td></tr>');
        return;
    }

    warehouses.forEach(warehouse => {
        const warehouseRow = `
            <tr class="expandable-row" data-warehouse-id="${warehouse.warehouseId}">
                <td>
                    <i class="mdi mdi-chevron-right expand-icon" id="warehouse-icon-${warehouse.warehouseId}"
                       onclick="toggleWarehouse(${warehouse.warehouseId})"></i>
                </td>
                <td><strong>${warehouse.warehouseName}</strong></td>
                <td>${warehouse.description || window.warehouseI18n.noDescription}</td>
                <td><span class="badge badge-info" id="column-count-${warehouse.warehouseId}">-</span></td>
                <td class="action-buttons">
                    <button class="btn btn-sm btn-outline-primary" onclick="editWarehouse(${warehouse.warehouseId})">
                        <i class="mdi mdi-pencil"></i> ${window.warehouseI18n.edit}
                    </button>
                    <button class="btn btn-sm btn-outline-success" onclick="addColumn(${warehouse.warehouseId})">
                        <i class="mdi mdi-plus"></i> ${window.warehouseI18n.addColumn}
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteWarehouse(${warehouse.warehouseId})">
                        <i class="mdi mdi-delete"></i> ${window.warehouseI18n.delete}
                    </button>
                </td>
            </tr>
            <tr id="warehouse-details-${warehouse.warehouseId}" style="display: none;">
                <td colspan="5" style="padding: 0;">
                    <div class="table-responsive">
                        <table class="table sub-table mb-0">
                            <thead>
                                <tr>
                                    <th width="50"><i class="mdi mdi-chevron-right"></i></th>
                                    <th>${window.warehouseI18n.columnName}</th>
                                    <th width="100">${window.warehouseI18n.order}</th>
                                    <th width="100">${window.warehouseI18n.positionCount}</th>
                                    <th width="150">${window.warehouseI18n.operations}</th>
                                </tr>
                            </thead>
                            <tbody id="columns-${warehouse.warehouseId}">
                                <tr><td colspan="5" class="text-center text-muted">${window.warehouseI18n.clickToLoadColumns}</td></tr>
                            </tbody>
                        </table>
                    </div>
                </td>
            </tr>
        `;
        tbody.append(warehouseRow);
    });
}

// 切换仓库展开/收起
function toggleWarehouse(warehouseId) {
    const detailsRow = $(`#warehouse-details-${warehouseId}`);
    const icon = $(`#warehouse-icon-${warehouseId}`);

    if (detailsRow.is(':visible')) {
        detailsRow.hide();
        icon.removeClass('expanded');
    } else {
        detailsRow.show();
        icon.addClass('expanded');
        loadWarehouseColumns(warehouseId);
    }
}

// 加载仓库的库位列
function loadWarehouseColumns(warehouseId) {
    $.ajax({
        url: `/api/warehouse/column/list/${warehouseId}`,
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                renderColumns(warehouseId, response.data);
            } else {
                showNotification(window.warehouseI18n.getColumnDataFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getColumnDataFailed, 'danger');
        }
    });
}

// 渲染库位列
function renderColumns(warehouseId, columns) {
    const container = $(`#columns-${warehouseId}`);
    container.empty();

    // 更新库位列数量
    $(`#column-count-${warehouseId}`).text(columns.length);

    if (!columns || columns.length === 0) {
        container.html('<tr><td colspan="5" class="text-center text-muted">' + window.warehouseI18n.noColumnData + '</td></tr>');
        return;
    }

    columns.forEach(column => {
        const columnRow = `
            <tr class="expandable-row" data-column-id="${column.columnId}">
                <td>
                    <i class="mdi mdi-chevron-right expand-icon" id="column-icon-${column.columnId}"
                       onclick="toggleColumn(${column.columnId})"></i>
                </td>
                <td><strong>${column.columnName}</strong></td>
                <td><span class="badge badge-secondary">${column.columnOrder}</span></td>
                <td><span class="badge badge-info" id="vertex-count-${column.columnId}">-</span></td>
                <td class="action-buttons">
                    <button class="btn btn-sm btn-outline-primary" onclick="editColumn(${column.columnId})">
                        <i class="mdi mdi-pencil"></i> ${window.warehouseI18n.edit}
                    </button>
                    <button class="btn btn-sm btn-outline-success" onclick="addVertexes(${column.columnId})">
                        <i class="mdi mdi-plus"></i> ${window.warehouseI18n.addPosition}
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteColumn(${column.columnId})">
                        <i class="mdi mdi-delete"></i> ${window.warehouseI18n.delete}
                    </button>
                </td>
            </tr>
            <tr id="column-details-${column.columnId}" style="display: none;">
                <td colspan="5" style="padding: 0;">
                    <div class="table-responsive">
                        <table class="table nested-table mb-0">
                            <thead>
                                <tr>
                                    <th width="100">${window.warehouseI18n.positionOrder}</th>
                                    <th>${window.warehouseI18n.positionCode}</th>
                                    <th>${window.warehouseI18n.positionAlias}</th>
                                    <th>${window.warehouseI18n.coordinates}</th>
                                    <th width="100">${window.warehouseI18n.operations}</th>
                                </tr>
                            </thead>
                            <tbody id="vertexes-${column.columnId}">
                                <tr><td colspan="5" class="text-center text-muted">${window.warehouseI18n.clickToLoadPositions}</td></tr>
                            </tbody>
                        </table>
                    </div>
                </td>
            </tr>
        `;
        container.append(columnRow);
    });
}

// 切换库位列展开/收起
function toggleColumn(columnId) {
    const detailsRow = $(`#column-details-${columnId}`);
    const icon = $(`#column-icon-${columnId}`);

    if (detailsRow.is(':visible')) {
        detailsRow.hide();
        icon.removeClass('expanded');
    } else {
        detailsRow.show();
        icon.addClass('expanded');
        loadColumnPositions(columnId);
    }
}

// 加载库位列的点位关联
function loadColumnPositions(columnId) {
    $.ajax({
        url: `/api/warehouse/vertexes/list/${columnId}`,
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                renderVertexes(columnId, response.data);
            } else {
                showNotification(window.warehouseI18n.getPositionDataFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getPositionDataFailed, 'danger');
        }
    });
}

// 渲染点位关联
function renderVertexes(columnId, vertexes) {
    const container = $(`#vertexes-${columnId}`);
    container.empty();

    // 更新点位数量
    $(`#vertex-count-${columnId}`).text(vertexes.length);

    if (!vertexes || vertexes.length === 0) {
        container.html('<tr><td colspan="5" class="text-center text-muted">' + window.warehouseI18n.noPositionData + '</td></tr>');
        return;
    }

    vertexes.forEach(vertex => {
        const vertexRow = `
            <tr data-position-id="${vertex.positionId}">
                <td><span class="badge badge-secondary">${vertex.positionOrder}</span></td>
                <td>${vertex.mapVertex ? `<span class="badge badge-primary">${vertex.mapVertex.code}</span>` : '<span class="text-danger">-</span>'}</td>
                <td>${vertex.mapVertex ? (vertex.mapVertex.codeAlias || window.warehouseI18n.noAlias) : '-'}</td>
                <td>${vertex.mapVertex ? `(${vertex.mapVertex.x}, ${vertex.mapVertex.y})` : '-'}</td>
                <td>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteVertexes(${vertex.positionId})">
                        <i class="mdi mdi-delete"></i> ${window.warehouseI18n.delete}
                    </button>
                </td>
            </tr>
        `;
        container.append(vertexRow);
    });
}

// 显示添加仓库模态框
function showAddWarehouseModal() {
    $('#addWarehouseForm')[0].reset();
    $('#addWarehouseModal').modal('show');
}

// 添加仓库
function addWarehouse() {
    const formData = {
        warehouseName: $('#warehouseName').val(),
        description: $('#description').val()
    };

    if (!formData.warehouseName) {
        showNotification(window.warehouseI18n.fillRequiredFields, 'warning');
        return;
    }

    $.ajax({
        url: '/api/warehouse/add',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function (response) {
            if (response.code === 10000) {
                showNotification(window.warehouseI18n.addWarehouseSuccess, 'success');
                $('#addWarehouseModal').modal('hide');
                loadWarehouses();
            } else {
                showNotification(window.warehouseI18n.addWarehouseFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.addWarehouseFailed, 'danger');
        }
    });
}

// 编辑仓库
function editWarehouse(warehouseId) {
    $.ajax({
        url: `/api/warehouse/detail/${warehouseId}`,
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                const warehouse = response.data;
                $('#editWarehouseId').val(warehouse.warehouseId);
                $('#editWarehouseName').val(warehouse.warehouseName);
                $('#editDescription').val(warehouse.description);
                $('#editWarehouseModal').modal('show');
            } else {
                showNotification(window.warehouseI18n.getWarehouseInfoFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getWarehouseInfoFailed, 'danger');
        }
    });
}

// 更新仓库
function updateWarehouse() {
    const formData = {
        warehouseId: parseInt($('#editWarehouseId').val()),
        warehouseName: $('#editWarehouseName').val(),
        description: $('#editDescription').val()
    };

    if (!formData.warehouseName) {
        showNotification(window.warehouseI18n.fillRequiredFields, 'warning');
        return;
    }

    $.ajax({
        url: '/api/warehouse/update',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function (response) {
            if (response.code === 10000) {
                showNotification(window.warehouseI18n.updateWarehouseSuccess, 'success');
                $('#editWarehouseModal').modal('hide');
                loadWarehouses();
            } else {
                showNotification(window.warehouseI18n.updateWarehouseFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.updateWarehouseFailed, 'danger');
        }
    });
}

// 删除仓库
function deleteWarehouse(warehouseId) {
    if (!confirm(window.warehouseI18n.deleteWarehouseConfirm)) {
        return;
    }

    $.ajax({
        url: `/api/warehouse/delete/${warehouseId}`,
        type: 'POST',
        success: function (response) {
            if (response.code === 10000) {
                showNotification(window.warehouseI18n.deleteWarehouseSuccess, 'success');
                loadWarehouses();
            } else {
                showNotification(window.warehouseI18n.deleteWarehouseFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.deleteWarehouseFailed, 'danger');
        }
    });
}

// 搜索仓库
function searchWarehouses() {
    const keyword = $('#searchInput').val().trim();
    // 这里可以实现搜索逻辑
    loadWarehouses();
}

// 刷新数据
function refreshData() {
    loadWarehouses();
    showNotification(window.warehouseI18n.dataRefreshed, 'info');
}

// 显示通知
function showNotification(message, type) {
    $.notify({
        message: message
    }, {
        type: type,
        delay: 500,
        placement: {
            from: 'top',
            align: 'right'
        }
    });
}

// 显示添加库位列模态框
function addColumn(warehouseId) {
    $('#columnWarehouseId').val(warehouseId);
    $('#addColumnForm')[0].reset();
    $('#columnWarehouseId').val(warehouseId); // 重新设置，因为reset会清空
    $('#addColumnModal').modal('show');
}

// 添加库位列
function addWarehouseColumn() {
    const formData = {
        warehouseId: parseInt($('#columnWarehouseId').val()),
        columnName: $('#columnName').val(),
        columnOrder: parseInt($('#columnOrder').val())
    };

    if (!formData.columnName || !formData.columnOrder) {
        showNotification(window.warehouseI18n.fillRequiredFields, 'warning');
        return;
    }

    $.ajax({
        url: '/api/warehouse/column/add',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function (response) {
            if (response.code === 10000) {
                showNotification(window.warehouseI18n.addColumnSuccess, 'success');
                $('#addColumnModal').modal('hide');
                // 刷新当前展开的库位列数据
                loadWarehouseColumns(formData.warehouseId);
            } else {
                showNotification(window.warehouseI18n.addColumnFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.addColumnFailed, 'danger');
        }
    });
}

// 编辑库位列
function editColumn(columnId) {
    $.ajax({
        url: `/api/warehouse/column/detail/${columnId}`,
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                const column = response.data;
                $('#editColumnId').val(column.columnId);
                $('#editColumnWarehouseId').val(column.warehouseId);
                $('#editColumnName').val(column.columnName);
                $('#editColumnOrder').val(column.columnOrder);
                $('#editColumnModal').modal('show');
            } else {
                showNotification(window.warehouseI18n.getColumnInfoFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getColumnInfoFailed, 'danger');
        }
    });
}

// 更新库位列
function updateWarehouseColumn() {
    const formData = {
        columnId: parseInt($('#editColumnId').val()),
        warehouseId: parseInt($('#editColumnWarehouseId').val()),
        columnName: $('#editColumnName').val(),
        columnOrder: parseInt($('#editColumnOrder').val())
    };

    if (!formData.columnName || !formData.columnOrder) {
        showNotification(window.warehouseI18n.fillRequiredFields, 'warning');
        return;
    }

    $.ajax({
        url: '/api/warehouse/column/update',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function (response) {
            if (response.code === 10000) {
                showNotification(window.warehouseI18n.updateColumnSuccess, 'success');
                $('#editColumnModal').modal('hide');
                // 刷新当前展开的库位列数据
                loadWarehouseColumns(formData.warehouseId);
            } else {
                showNotification(window.warehouseI18n.updateColumnFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.updateColumnFailed, 'danger');
        }
    });
}

// 显示添加点位关联模态框
function addVertexes(columnId) {
    $('#vertexesColumnId').val(columnId);
    $('#addVertexesForm')[0].reset();
    $('#vertexesColumnId').val(columnId); // 重新设置，因为reset会清空

    // 隐藏错误提示
    $('#addVertexesError').hide();

    // 加载地图选项
    loadMapOptions();

    // 清空点位选择器
    $('#mapVertexSelect').empty().append('<option value="">' + window.warehouseI18n.selectMapFirst + '</option>');

    $('#addVertexesModal').modal('show');
}

// 加载地图选项
function loadMapOptions() {
    $.ajax({
        url: '/api/map/select',
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                const select = $('#mapIdSelect');
                select.empty();
                select.append('<option value="">' + window.warehouseI18n.selectMapFirst + '</option>');

                response.data.forEach(mapId => {
                    select.append(`<option value="${mapId}">${window.warehouseI18n.mapIdPrefix} ${mapId}</option>`);
                });
            } else {
                showNotification(window.warehouseI18n.getMapListFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getMapListFailed, 'danger');
        }
    });
}

// 根据选择的地图加载点位
function loadVertexesByMap() {
    const mapId = $('#mapIdSelect').val();
    const vertexSelect = $('#mapVertexSelect');

    if (!mapId) {
        vertexSelect.empty().append('<option value="">' + window.warehouseI18n.selectMapFirst + '</option>');
        return;
    }

    $.ajax({
        url: '/api/map/selectVertexes',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            mapId: parseInt(mapId),
            pageNum: 1,
            pageSize: 1000 // 获取所有点位
        }),
        success: function (response) {
            if (response.code === 10000) {
                vertexSelect.empty();
                vertexSelect.append('<option value="">' + window.warehouseI18n.selectMapPosition + '</option>');

                if (response.data.records && response.data.records.length > 0) {
                    response.data.records.forEach(vertex => {
                        vertexSelect.append(`<option value="${vertex.id}">${vertex.code} - ${vertex.codeAlias || window.warehouseI18n.noAlias}</option>`);
                    });
                } else {
                    vertexSelect.append('<option value="">' + window.warehouseI18n.noPositionsInMap + '</option>');
                }
            } else {
                showNotification(window.warehouseI18n.getMapPositionsFailed + ': ' + response.msg, 'danger');
                vertexSelect.empty().append('<option value="">' + window.warehouseI18n.getMapPositionsFailed + '</option>');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getMapPositionsFailed, 'danger');
            vertexSelect.empty().append('<option value="">' + window.warehouseI18n.getMapPositionsFailed + '</option>');
        }
    });
}

// 加载地图点位选项（保留原有函数，用于兼容）
function loadMapVertexOptions() {
    loadMapOptions();
}

// 添加点位关联
function addWarehouseVertexes() {
    const formData = {
        columnId: parseInt($('#vertexesColumnId').val()),
        hxMapVertexesId: parseInt($('#mapVertexSelect').val()),
        positionOrder: parseInt($('#positionOrder').val()),
        status: parseInt($('#positionStatus').val())
    };

    if (!formData.hxMapVertexesId || !formData.positionOrder || !formData.status) {
        showNotification(window.warehouseI18n.fillRequiredFields, 'warning');
        return;
    }

    $.ajax({
        url: '/api/warehouse/vertexes/add',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function (response) {
            if (response.code === 10000) {
                showNotification(window.warehouseI18n.addPositionAssociationSuccess, 'success');
                $('#addVertexesModal').modal('hide');
                // 刷新当前展开的点位关联数据
                loadColumnPositions(formData.columnId);
            } else {
                showModalError('addVertexesError', 'addVertexesErrorMessage', response.msg);
            }
        },
        error: function (xhr) {
            let errorMessage = window.warehouseI18n.updatePositionAssociationFailed;
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }
            showModalError('addVertexesError', 'addVertexesErrorMessage', errorMessage);
        }
    });
}

// 编辑点位关联
function editVertexes(positionId) {
    $.ajax({
        url: `/api/warehouse/vertexes/detail/${positionId}`,
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                const vertex = response.data;
                $('#editVertexesPositionId').val(vertex.positionId);
                $('#editVertexesColumnId').val(vertex.columnId);
                $('#editPositionOrder').val(vertex.positionOrder);
                $('#editPositionStatus').val(vertex.status || 1);

                // 隐藏错误提示
                $('#editVertexesError').hide();

                // 加载地图选项，然后加载点位选项
                loadMapOptionsForEdit(vertex.hxMapVertexesId);

                $('#editVertexesModal').modal('show');
            } else {
                showNotification(window.warehouseI18n.getPositionDataFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getPositionDataFailed, 'danger');
        }
    });
}

// 为编辑模态框加载地图选项
function loadMapOptionsForEdit(selectedVertexId) {
    $.ajax({
        url: '/api/map/select',
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                const mapSelect = $('#editMapIdSelect');
                mapSelect.empty();
                mapSelect.append('<option value="">' + window.warehouseI18n.selectMapFirst + '</option>');

                response.data.forEach(mapId => {
                    mapSelect.append(`<option value="${mapId}">${window.warehouseI18n.mapIdPrefix} ${mapId}</option>`);
                });

                // 如果有选中的点位，需要找到它所属的地图
                if (selectedVertexId) {
                    findMapByVertexId(selectedVertexId);
                }
            } else {
                showNotification(window.warehouseI18n.getMapListFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getMapListFailed, 'danger');
        }
    });
}

// 根据点位ID查找所属地图
function findMapByVertexId(vertexId) {
    $.ajax({
        url: '/api/map/vertexes/all',
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                const vertex = response.data.find(v => v.id === vertexId);
                if (vertex && vertex.mapId) {
                    // 设置地图选择器的值
                    $('#editMapIdSelect').val(vertex.mapId);
                    // 加载该地图的点位
                    loadVertexesByMapForEdit(vertexId);
                } else {
                    // 如果找不到地图信息，直接加载所有点位（兼容旧数据）
                    loadMapVertexOptionsForEditFallback(vertexId);
                }
            } else {
                loadMapVertexOptionsForEditFallback(vertexId);
            }
        },
        error: function () {
            loadMapVertexOptionsForEditFallback(vertexId);
        }
    });
}

// 根据选择的地图加载点位（编辑模式）
function loadVertexesByMapForEdit(selectedVertexId = null) {
    const mapId = $('#editMapIdSelect').val();
    const vertexSelect = $('#editMapVertexSelect');

    if (!mapId) {
        vertexSelect.empty().append('<option value="">' + window.warehouseI18n.selectMapFirst + '</option>');
        return;
    }

    $.ajax({
        url: '/api/map/selectVertexes',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            mapId: parseInt(mapId),
            pageNum: 1,
            pageSize: 1000 // 获取所有点位
        }),
        success: function (response) {
            if (response.code === 10000) {
                vertexSelect.empty();
                vertexSelect.append('<option value="">' + window.warehouseI18n.selectMapPosition + '</option>');

                if (response.data.records && response.data.records.length > 0) {
                    response.data.records.forEach(vertex => {
                        const selected = selectedVertexId && vertex.id === selectedVertexId ? 'selected' : '';
                        vertexSelect.append(`<option value="${vertex.id}" ${selected}>${vertex.code} - ${vertex.codeAlias || window.warehouseI18n.noAlias}</option>`);
                    });
                } else {
                    vertexSelect.append('<option value="">' + window.warehouseI18n.noPositionsInMap + '</option>');
                }
            } else {
                showNotification(window.warehouseI18n.getMapPositionsFailed + ': ' + response.msg, 'danger');
                vertexSelect.empty().append('<option value="">' + window.warehouseI18n.getMapPositionsFailed + '</option>');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getMapPositionsFailed, 'danger');
            vertexSelect.empty().append('<option value="">' + window.warehouseI18n.getMapPositionsFailed + '</option>');
        }
    });
}

// 为编辑模态框加载地图点位选项（兼容旧数据的备用方法）
function loadMapVertexOptionsForEditFallback(selectedVertexId) {
    $.ajax({
        url: '/api/map/vertexes/all',
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                const select = $('#editMapVertexSelect');
                select.empty();
                select.append('<option value="">' + window.warehouseI18n.selectMapPosition + '</option>');

                response.data.forEach(vertex => {
                    const selected = vertex.id === selectedVertexId ? 'selected' : '';
                    select.append(`<option value="${vertex.id}" ${selected}>${vertex.code} - ${vertex.codeAlias || window.warehouseI18n.noAlias}</option>`);
                });
            } else {
                showNotification(window.warehouseI18n.getMapPositionsFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getMapPositionsFailed, 'danger');
        }
    });
}

// 为编辑模态框加载地图点位选项（保留原有函数名，用于兼容）
function loadMapVertexOptionsForEdit(selectedVertexId) {
    loadMapOptionsForEdit(selectedVertexId);
}

// 更新点位关联
function updateWarehouseVertexes() {
    const formData = {
        positionId: parseInt($('#editVertexesPositionId').val()),
        columnId: parseInt($('#editVertexesColumnId').val()),
        hxMapVertexesId: parseInt($('#editMapVertexSelect').val()),
        positionOrder: parseInt($('#editPositionOrder').val()),
        status: parseInt($('#editPositionStatus').val())
    };

    if (!formData.hxMapVertexesId || !formData.positionOrder || !formData.status) {
        showNotification(window.warehouseI18n.fillRequiredFields, 'warning');
        return;
    }

    $.ajax({
        url: '/api/warehouse/vertexes/update',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function (response) {
            if (response.code === 10000) {
                showNotification(window.warehouseI18n.updatePositionAssociationSuccess, 'success');
                $('#editVertexesModal').modal('hide');
                // 刷新当前展开的点位关联数据
                loadColumnPositions(formData.columnId);
                // 如果在可视化视图，也刷新可视化
                if ($('#warehouseSelect').val()) {
                    switchWarehouse();
                }
            } else {
                showModalError('editVertexesError', 'editVertexesErrorMessage', response.msg);
            }
        },
        error: function (xhr) {
            let errorMessage = window.warehouseI18n.updatePositionAssociationFailed;
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }
            showModalError('editVertexesError', 'editVertexesErrorMessage', errorMessage);
        }
    });
}

// 删除库位列
function deleteColumn(columnId) {
    if (!confirm(window.warehouseI18n.deleteColumnConfirm)) {
        return;
    }

    $.ajax({
        url: `/api/warehouse/column/delete/${columnId}`,
        type: 'POST',
        success: function (response) {
            if (response.code === 10000) {
                showNotification(window.warehouseI18n.deleteColumnSuccess, 'success');
                // 刷新当前页面数据
                loadWarehouses();
            } else {
                showNotification(window.warehouseI18n.deleteColumnFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.deleteColumnFailed, 'danger');
        }
    });
}

// 删除点位关联
function deleteVertexes(positionId) {
    if (!confirm(window.warehouseI18n.deletePositionConfirm)) {
        return;
    }

    $.ajax({
        url: `/api/warehouse/vertexes/delete/${positionId}`,
        type: 'POST',
        success: function (response) {
            if (response.code === 10000) {
                showNotification(window.warehouseI18n.deletePositionSuccess, 'success');
                // 刷新当前展开的点位关联数据
                const columnId = $(`[data-position-id="${positionId}"]`).closest('table').find('tbody').attr('id').replace('vertexes-', '');
                loadColumnPositions(columnId);
            } else {
                showNotification(window.warehouseI18n.deletePositionFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.deletePositionFailed, 'danger');
        }
    });
}

// ==================== 可视化展示相关功能 ====================

// 填充仓库选择器
function populateWarehouseSelector(warehouses) {
    const selector = $('#warehouseSelect');
    selector.empty();
    selector.append('<option value="">' + window.warehouseI18n.selectWarehouse + '</option>');

    console.log('填充仓库选择器，仓库数量:', warehouses.length); // 调试日志

    warehouses.forEach(warehouse => {
        selector.append(`<option value="${warehouse.warehouseId}">${warehouse.warehouseName}</option>`);
    });
}

// 切换仓库
function switchWarehouse() {
    const warehouseId = $('#warehouseSelect').val();
    if (!warehouseId) {
        $('#warehouseInfo').hide();
        $('#warehouseVisualization').hide();
        return;
    }

    // 获取仓库详情
    $.ajax({
        url: `/api/warehouse/detail/${warehouseId}`,
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                const warehouse = response.data;
                console.log('仓库详情数据:', warehouse); // 调试日志
                displayWarehouseInfo(warehouse);
                loadWarehouseColumnsForVisualization(warehouseId);
            } else {
                showNotification(window.warehouseI18n.getWarehouseDetailFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getWarehouseDetailFailed, 'danger');
        }
    });
}

// 为可视化加载仓库库位列数据
function loadWarehouseColumnsForVisualization(warehouseId) {
    // 开始加载时重置使用率显示
    resetUsageStats();

    $.ajax({
        url: `/api/warehouse/column/list/${warehouseId}`,
        type: 'GET',
        success: function (response) {
            if (response.code === 10000) {
                console.log('库位列数据:', response.data); // 调试日志
                loadColumnsWithVertexes(response.data);
            } else {
                showNotification(window.warehouseI18n.getColumnListFailed + ': ' + response.msg, 'danger');
                // 加载失败时也要更新使用率为0
                updateUsageStats([]);
            }
        },
        error: function () {
            showNotification(window.warehouseI18n.getColumnListFailed, 'danger');
            // 加载失败时也要更新使用率为0
            updateUsageStats([]);
        }
    });
}

// 重置使用率显示
function resetUsageStats() {
    $('#usagePercent').text(window.warehouseI18n.loadingText);
    $('#detailedStats').html('<div class="text-muted small">' + window.warehouseI18n.calculating + '</div>');
}

// 加载库位列及其点位数据
function loadColumnsWithVertexes(columns) {
    if (!columns || columns.length === 0) {
        // 没有库位列数据时，使用率为0%
        updateUsageStats([]);
        generateWarehouseVisualization({columns: []});
        return;
    }

    let loadedCount = 0;
    const totalColumns = columns.length;

    columns.forEach(column => {
        // 为每个库位列加载点位数据
        $.ajax({
            url: `/api/warehouse/vertexes/list/${column.columnId}`,
            type: 'GET',
            success: function (response) {
                if (response.code === 10000) {
                    column.vertexes = response.data;
                    console.log(`库位列 ${column.columnName} 的点位数据:`, response.data); // 调试日志
                } else {
                    column.vertexes = [];
                    console.warn(`获取库位列 ${column.columnName} 点位数据失败:`, response.message);
                }

                loadedCount++;
                if (loadedCount === totalColumns) {
                    // 所有库位列的点位数据都加载完成
                    updateUsageStats(columns);
                    generateWarehouseVisualization({columns: columns});
                }
            },
            error: function () {
                column.vertexes = [];
                loadedCount++;
                if (loadedCount === totalColumns) {
                    updateUsageStats(columns);
                    generateWarehouseVisualization({columns: columns});
                }
            }
        });
    });
}

// 显示仓库信息
function displayWarehouseInfo(warehouse) {
    $('#warehouseTitle').text(warehouse.warehouseName);
    $('#warehouseDesc').text(warehouse.description || window.warehouseI18n.noDescription);

    // 初始化使用率为0%，等待数据加载完成后更新
    $('#usagePercent').text('0%');
    $('#detailedStats').html('<div class="text-muted small">' + window.warehouseI18n.loadingText + '</div>');

    $('#warehouseInfo').show();
    $('#warehouseVisualization').show();
}

// 更新使用率统计（在加载完所有点位数据后调用）
function updateUsageStats(columns) {
    let totalPositions = 0;
    let availablePositions = 0;  // 可用
    let occupiedPositions = 0;   // 占用
    let disabledPositions = 0;   // 禁用

    if (columns) {
        columns.forEach(column => {
            if (column.vertexes) {
                totalPositions += column.vertexes.length;

                // 按状态分类统计
                column.vertexes.forEach(vertex => {
                    switch (vertex.status) {
                        case 1:
                            availablePositions++;
                            break;
                        case 2:
                            occupiedPositions++;
                            break;
                        case 3:
                            disabledPositions++;
                            break;
                        default:
                            availablePositions++; // 默认为可用
                    }
                });
            }
        });
    }

    // 计算使用率（占用数 / 总数）
    const usagePercent = totalPositions > 0 ? Math.round((occupiedPositions / totalPositions) * 100) : 0;
    $('#usagePercent').text(usagePercent + '%');

    // 更新详细统计信息（如果有对应的元素）
    updateDetailedStats(totalPositions, availablePositions, occupiedPositions, disabledPositions, usagePercent);

    console.log(`使用率统计: 总点位${totalPositions}个，可用${availablePositions}个，占用${occupiedPositions}个，禁用${disabledPositions}个，使用率${usagePercent}%`);
}

// 更新详细统计信息
function updateDetailedStats(total, available, occupied, disabled, usagePercent) {
    // 如果页面有详细统计区域，可以在这里更新
    if ($('#detailedStats').length > 0) {
        const statsHtml = `
            <div class="row text-center">
                <div class="col-3">
                    <div class="stat-item">
                        <div class="stat-number text-primary">${total}</div>
                        <div class="stat-label">${window.warehouseI18n.totalPositions}</div>
                    </div>
                </div>
                <div class="col-3">
                    <div class="stat-item">
                        <div class="stat-number text-success">${available}</div>
                        <div class="stat-label">${window.warehouseI18n.statusAvailable}</div>
                    </div>
                </div>
                <div class="col-3">
                    <div class="stat-item">
                        <div class="stat-number text-warning">${occupied}</div>
                        <div class="stat-label">${window.warehouseI18n.statusOccupied}</div>
                    </div>
                </div>
                <div class="col-3">
                    <div class="stat-item">
                        <div class="stat-number text-secondary">${disabled}</div>
                        <div class="stat-label">${window.warehouseI18n.statusDisabled}</div>
                    </div>
                </div>
            </div>
        `;
        $('#detailedStats').html(statsHtml);
    }
}

// 生成仓库可视化展示
function generateWarehouseVisualization(warehouse) {
    const container = $('#warehouseGrid');
    container.empty();

    console.log('开始生成可视化展示，仓库数据:', warehouse); // 调试日志

    if (!warehouse.columns || warehouse.columns.length === 0) {
        container.html('<div class="text-center text-muted p-4">' + window.warehouseI18n.noColumnDataInWarehouse + '</div>');
        return;
    }

    // 按排序生成库位列
    warehouse.columns.sort((a, b) => a.columnOrder - b.columnOrder);

    warehouse.columns.forEach(column => {
        console.log(`处理库位列: ${column.columnName}, 点位数量: ${column.vertexes ? column.vertexes.length : 0}`); // 调试日志

        const columnDiv = $('<div class="warehouse-column"></div>');

        // 添加列标题
        columnDiv.append(`<div class="column-header">${column.columnName}</div>`);

        // 生成点位方块
        if (column.vertexes && column.vertexes.length > 0) {
            // 按排序生成点位
            column.vertexes.sort((a, b) => a.positionOrder - b.positionOrder);

            column.vertexes.forEach(vertex => {
                console.log(`生成点位方块: 排序${vertex.positionOrder}, 地图点位:`, vertex.mapVertex); // 调试日志

                // 优化显示文本
                let displayText = '';
                let vertexCode = '';

                if (vertex.mapVertex) {
                    vertexCode = vertex.mapVertex.code;

                    // 有别名优先展示别名，无别名展示原名（点位编码）
                    if (vertex.mapVertex.codeAlias && vertex.mapVertex.codeAlias.trim() !== '') {
                        // 有别名，优先显示别名
                        if (vertex.mapVertex.codeAlias.length <= 8) {
                            displayText = vertex.mapVertex.codeAlias;
                        } else {
                            // 别名太长，截取显示
                            displayText = vertex.mapVertex.codeAlias.substring(0, 6) + '...';
                        }
                    } else {
                        // 无别名，显示点位编码（原名）
                        if (vertex.mapVertex.code.length <= 8) {
                            displayText = vertex.mapVertex.code;
                        } else {
                            // 编码太长，截取显示
                            displayText = vertex.mapVertex.code.substring(0, 6) + '...';
                        }
                    }
                } else {
                    vertexCode = `P${vertex.positionOrder}`;
                    displayText = window.warehouseI18n.positionPrefix + vertex.positionOrder;
                }

                const vertexTitle = vertex.mapVertex ?
                    window.warehouseI18n.positionTitleWithVertex
                        .replace('{0}', vertex.mapVertex.code)
                        .replace('{1}', vertex.mapVertex.codeAlias || window.warehouseI18n.noAlias)
                        .replace('{2}', vertex.positionOrder)
                        .replace('{3}', getStatusText(vertex.status))
                        .replace('{4}', vertex.mapVertex.x || 0)
                        .replace('{5}', vertex.mapVertex.y || 0) :
                    window.warehouseI18n.positionTitleWithoutVertex
                        .replace('{0}', vertex.positionOrder)
                        .replace('{1}', getStatusText(vertex.status));

                // 根据状态设置样式类
                let statusClass = 'position-available';
                let statusText = window.warehouseI18n.statusAvailable;

                switch (vertex.status) {
                    case 1:
                        statusClass = 'position-available';
                        statusText = window.warehouseI18n.statusAvailable;
                        break;
                    case 2:
                        statusClass = 'position-occupied';
                        statusText = window.warehouseI18n.statusOccupied;
                        break;
                    case 3:
                        statusClass = 'position-disabled';
                        statusText = window.warehouseI18n.statusDisabled;
                        break;
                    default:
                        statusClass = 'position-available';
                        statusText = window.warehouseI18n.statusAvailable;
                }

                // 更新工具提示，包含状态信息
                const enhancedTitle = vertex.mapVertex ?
                    window.warehouseI18n.positionTitleWithVertex
                        .replace('{0}', vertex.mapVertex.code)
                        .replace('{1}', vertex.mapVertex.codeAlias || window.warehouseI18n.noAlias)
                        .replace('{2}', vertex.positionOrder)
                        .replace('{3}', statusText)
                        .replace('{4}', vertex.mapVertex.x || 0)
                        .replace('{5}', vertex.mapVertex.y || 0) :
                    window.warehouseI18n.positionTitleWithoutVertex
                        .replace('{0}', vertex.positionOrder)
                        .replace('{1}', statusText);

                const positionBtn = $(`
                    <div class="position-btn ${statusClass} ${displayText.length > 6 ? 'long-text' : ''}"
                         data-position-id="${vertex.positionId}"
                         data-vertex-code="${vertexCode}"
                         data-status="${vertex.status || 1}"
                         title="${enhancedTitle}">
                        ${displayText}
                    </div>
                `);

                // 添加点击事件
                positionBtn.on('click', function (e) {
                    // 检查是否在批量选择模式下
                    if (handlePositionClick(this, vertex.positionId)) {
                        e.preventDefault();
                        e.stopPropagation();
                        return;
                    }

                    // 正常模式下显示点位详情
                    showPositionDetails(vertex);
                });

                // 添加右键菜单事件
                positionBtn.on('contextmenu', function (e) {
                    e.preventDefault();
                    showPositionContextMenu(e, vertex);
                });

                columnDiv.append(positionBtn);
            });
        } else {
            // 显示空位提示
            columnDiv.append(`
                <div class="position-btn position-empty" title="${window.warehouseI18n.noPositionDataText}">
                    <small>${window.warehouseI18n.noPositionDataText}</small>
                </div>
            `);
        }

        container.append(columnDiv);
    });

}

// 显示点位详情
function showPositionDetails(vertex) {
    // 填充基本信息
    $('#detailPositionId').text(vertex.positionId || '-');
    $('#detailPositionOrder').text(vertex.positionOrder || '-');

    // 设置状态显示
    let statusHtml = '';
    switch (vertex.status) {
        case 1:
            statusHtml = '<span class="badge badge-success">' + window.warehouseI18n.statusAvailable + '</span>';
            break;
        case 2:
            statusHtml = '<span class="badge badge-warning">' + window.warehouseI18n.statusOccupied + '</span>';
            break;
        case 3:
            statusHtml = '<span class="badge badge-secondary">' + window.warehouseI18n.statusDisabled + '</span>';
            break;
        default:
            statusHtml = '<span class="badge badge-secondary">' + window.warehouseI18n.statusUnknown + '</span>';
    }
    $('#detailPositionStatus').html(statusHtml);

    // 填充地图点位信息
    if (vertex.mapVertex) {
        $('#detailVertexCode').text(vertex.mapVertex.code || '-');
        $('#detailVertexAlias').text(vertex.mapVertex.codeAlias || window.warehouseI18n.noAlias);
        $('#detailVertexCoords').text(`(${vertex.mapVertex.x || 0}, ${vertex.mapVertex.y || 0})`);
        $('#detailVertexTheta').text(vertex.mapVertex.theta ? `${vertex.mapVertex.theta}°` : '-');
    } else {
        $('#detailVertexCode').text(window.warehouseI18n.notAssociated);
        $('#detailVertexAlias').text('-');
        $('#detailVertexCoords').text('-');
        $('#detailVertexTheta').text('-');
    }

    // 填充所属信息（需要从当前选择的仓库和库位列获取）
    const selectedWarehouseName = $('#warehouseSelect option:selected').text();
    $('#detailWarehouseName').text(selectedWarehouseName || '-');

    // 查找所属库位列名称
    const columnElement = $(`[data-position-id="${vertex.positionId}"]`).closest('.warehouse-column');
    const columnName = columnElement.find('.column-header').text();
    $('#detailColumnName').text(columnName || '-');

    // 创建时间（模拟数据，实际应该从后端获取）
    $('#detailCreateTime').text(new Date().toLocaleDateString());

    // 设置按钮事件
    $('#editPositionBtn').off('click').on('click', function () {
        $('#positionDetailModal').modal('hide');
        editVertexes(vertex.positionId);
    });

    $('#deletePositionBtn').off('click').on('click', function () {
        $('#positionDetailModal').modal('hide');
        deleteVertexes(vertex.positionId);
    });

    // 显示模态框
    $('#positionDetailModal').modal('show');
}

// 显示点位右键菜单
function showPositionContextMenu(event, vertex) {
    // 创建右键菜单HTML
    const menuHtml = `
        <div class="dropdown-menu show" style="position: fixed; top: ${event.pageY}px; left: ${event.pageX}px; z-index: 9999;">
            <a class="dropdown-item" href="#" data-action="detail">
                <i class="mdi mdi-eye"></i> ${window.warehouseI18n.contextMenuViewDetails}
            </a>
            <a class="dropdown-item" href="#" data-action="edit">
                <i class="mdi mdi-pencil"></i> ${window.warehouseI18n.contextMenuEditPosition}
            </a>
            <div class="dropdown-divider"></div>
            <a class="dropdown-item text-danger" href="#" data-action="delete">
                <i class="mdi mdi-delete"></i> ${window.warehouseI18n.contextMenuDeletePosition}
            </a>
        </div>
    `;

    // 移除已存在的菜单
    $('.context-menu').remove();

    // 添加菜单到页面
    const menu = $(menuHtml).addClass('context-menu');
    $('body').append(menu);

    // 菜单项点击事件
    menu.on('click', 'a', function (e) {
        e.preventDefault();
        e.stopPropagation();

        const action = $(this).data('action');
        switch (action) {
            case 'detail':
                showPositionDetails(vertex);
                break;
            case 'edit':
                editVertexes(vertex.positionId);
                break;
            case 'delete':
                deleteVertexes(vertex.positionId);
                break;
        }

        menu.remove();
    });

    // 点击其他地方关闭菜单
    $(document).one('click', function () {
        menu.remove();
    });
}

// 显示可视化视图
function showVisualizationView() {
    $('#managementView').hide();
    $('.management-panel').show();
    // 如果已选择仓库，显示可视化
    if ($('#warehouseSelect').val()) {
        $('#warehouseVisualization').show();
    }
}

// 显示管理视图
function showManagementView() {
    $('#warehouseVisualization').hide();
    $('.management-panel').hide();
    $('#managementView').show();
    // 加载表格数据
    loadWarehouses();
}

// 显示模态框内的错误信息
function showModalError(errorDivId, errorMessageId, message) {
    $('#' + errorMessageId).text(message);
    $('#' + errorDivId).show();

    // 滚动到错误提示位置
    $('#' + errorDivId)[0].scrollIntoView({behavior: 'smooth', block: 'nearest'});
}

// 显示详细错误通知
function showDetailedErrorNotification(title, message) {
    // 如果消息包含详细的绑定信息，使用更醒目的显示方式
    if (message && (message.includes('已经被绑定到') || message.includes('already bound to'))) {
        // 创建一个更详细的错误提示
        const detailedMessage = `
            <div class="alert alert-danger" role="alert">
                <h6 class="alert-heading"><i class="mdi mdi-alert-circle"></i> ${title}</h6>
                <p class="mb-0">${message}</p>
                <hr>
                <p class="mb-0 small">
                    <i class="mdi mdi-information"></i>
                    ${window.warehouseI18n.selectOtherPosition}
                </p>
            </div>
        `;

        // 如果页面中有专门的错误显示区域，可以在这里显示
        // 否则使用标准通知
        showNotification(message, 'danger', 8000); // 显示8秒
    } else {
        // 普通错误使用标准通知
        showNotification(title + ': ' + message, 'danger');
    }
}

// ==================== 批量操作功能 ====================

// 批量操作相关变量
let batchMode = false;
let selectedPositions = new Set();

// 切换批量选择模式
function toggleBatchMode() {
    batchMode = !batchMode;
    const batchModeBtn = $('#batchModeBtn');
    const batchOperationPanel = $('#batchOperationPanel');

    if (batchMode) {
        batchModeBtn.html('<i class="mdi mdi-close"></i> ' + window.warehouseI18n.exitBatchSelect);
        batchModeBtn.removeClass('btn-primary').addClass('btn-warning');
        batchOperationPanel.show();

        // 为所有点位按钮添加批量选择样式
        $('.position-btn').not('.position-empty').addClass('batch-mode');

    } else {
        batchModeBtn.html('<i class="mdi mdi-checkbox-multiple-marked"></i> ' + window.warehouseI18n.batchSelect);
        batchModeBtn.removeClass('btn-warning').addClass('btn-primary');
        batchOperationPanel.hide();

        // 移除批量选择样式和选中状态
        $('.position-btn').removeClass('batch-mode selected');
        clearSelection();

    }
}

// 清除选择
function clearSelection() {
    selectedPositions.clear();
    $('.position-btn').removeClass('selected');
    updateSelectedCount();
}

// 全选点位
function selectAllPositions() {
    if (!batchMode) {
        showNotification(window.warehouseI18n.enterBatchModeFirst, 'warning');
        return;
    }

    $('.position-btn').not('.position-empty').each(function () {
        const positionId = $(this).data('position-id');
        if (positionId) {
            selectedPositions.add(positionId);
            $(this).addClass('selected');
        }
    });

    updateSelectedCount();
}

// 更新选中数量显示
function updateSelectedCount() {
    $('#selectedCount').text(selectedPositions.size);
    $('#batchSelectedCount').text(selectedPositions.size);

    // 更新批量操作面板的显示状态
    if (selectedPositions.size > 0) {
        $('#batchOperationPanel').show();
    } else if (!batchMode) {
        $('#batchOperationPanel').hide();
    }
}

// 处理点位点击事件（批量选择模式）
function handlePositionClick(element, positionId) {
    if (!batchMode || !positionId) {
        return false; // 返回false表示继续执行原有的点击事件
    }

    const $element = $(element);


    if (selectedPositions.has(positionId)) {
        // 取消选择
        selectedPositions.delete(positionId);
        $element.removeClass('selected');
    } else {
        // 添加选择
        selectedPositions.add(positionId);
        $element.addClass('selected');
    }

    updateSelectedCount();
    return true; // 返回true表示阻止原有的点击事件
}

// 批量更新状态（快速操作）
function batchUpdateStatus(status) {
    if (selectedPositions.size === 0) {
        showNotification(window.warehouseI18n.selectPositionsToModify, 'warning');
        return;
    }

    const statusText = getStatusText(status);
    const confirmMessage = window.warehouseI18n.batchUpdateConfirm.replace('{0}', selectedPositions.size).replace('{1}', statusText);

    if (confirm(confirmMessage)) {
        executeBatchStatusUpdate(Array.from(selectedPositions), status, window.warehouseI18n.batchUpdateReason);
    }
}


// 执行批量状态更新的API调用
function executeBatchStatusUpdate(positionIds, status, reason) {
    const requestData = {
        positionIds: positionIds,
        status: parseInt(status),
        reason: reason || window.warehouseI18n.batchUpdateReason,
        updateMode: 'all'
    };

    console.log('批量更新请求数据:', requestData); // 调试信息

    showNotification(window.warehouseI18n.batchUpdatingStatus, 'info');

    $.ajax({
        url: '/api/warehouse/position/batch-update-status',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        success: function (response) {

            if (response.code === 10000) {
                const result = response.data;

                if (result && result.successCount > 0) {
                    showNotification(window.warehouseI18n.batchUpdateSuccess.replace('{0}', result.successCount), 'success');
                } else {
                    showNotification(window.warehouseI18n.noPositionsUpdated, 'warning');
                }

                // 显示详细结果
                if (result && result.failCount > 0) {
                    showNotification(`${result.summary}`, 'info');
                }

                // 强制刷新当前仓库显示
                const currentWarehouseId = $('#warehouseSelect').val();

                if (currentWarehouseId) {
                    // 延迟一点时间再刷新，确保后端数据已经更新
                    setTimeout(() => {
                        loadWarehouseColumnsForVisualization(currentWarehouseId);
                    }, 500);
                } else {
                    console.warn('没有选中的仓库ID，无法刷新');
                }

                // 清除选择并退出批量模式
                clearSelection();
                if (batchMode) {
                    toggleBatchMode();
                }
            } else {
                showNotification(window.warehouseI18n.batchUpdateFailed + ': ' + response.msg, 'danger');
            }
        },
        error: function (xhr, status, error) {
            showNotification(window.warehouseI18n.batchUpdateFailed, 'danger');
        }
    });
}

// 获取状态文本
function getStatusText(status) {
    switch (parseInt(status)) {
        case 1:
            return window.warehouseI18n.statusAvailable;
        case 2:
            return window.warehouseI18n.statusOccupied;
        case 3:
            return window.warehouseI18n.statusDisabled;
        default:
            return window.warehouseI18n.statusUnknown;
    }
}

// 根据CSS类名获取状态文本
function getStatusTextByClass(className) {
    switch (className) {
        case 'available':
            return window.warehouseI18n.statusAvailable;
        case 'occupied':
            return window.warehouseI18n.statusOccupied;
        case 'disabled':
            return window.warehouseI18n.statusDisabled;
        default:
            return window.warehouseI18n.statusUnknown;
    }
}
