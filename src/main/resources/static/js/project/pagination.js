// 渲染分页
function renderPagination(pageData) {
    let html = '';
    const totalPages = pageData.pages;
    const currentPage = pageData.current;

    if (totalPages > 1) {
        // 上一页
        html += `<li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0)" onclick="${currentPage === 1 ? '' : `loadData(${currentPage - 1})`}">«</a>
            </li>`;

        // 显示页码（最多显示7个）
        const startPage = Math.max(1, currentPage - 3);
        const endPage = Math.min(totalPages, currentPage + 3);

        if (startPage > 1) {
            html += `<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="loadData(1)">1</a></li>`;
            if (startPage > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = startPage; i <= endPage; i++) {
            html += `<li class="page-item ${currentPage === i ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0)" onclick="loadData(${i})">${i}</a>
                </li>`;
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="loadData(${totalPages})">${totalPages}</a></li>`;
        }

        // 下一页
        html += `<li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0)" onclick="${currentPage === totalPages ? '' : `loadData(${currentPage + 1})`}">»</a>
            </li>`;
    }
    $('#pagination').html(html);
}