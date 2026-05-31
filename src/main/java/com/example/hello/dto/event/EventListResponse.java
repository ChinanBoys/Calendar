package com.example.hello.dto.event;

import java.util.List;

/**
 * GET /api/events 分页列表响应 data。
 */
public class EventListResponse {

    private Long total;
    private List<EventVO> rows;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<EventVO> getRows() {
        return rows;
    }

    public void setRows(List<EventVO> rows) {
        this.rows = rows;
    }
}
