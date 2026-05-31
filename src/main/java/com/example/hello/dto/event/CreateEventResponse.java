package com.example.hello.dto.event;

import java.util.List;

/**
 * POST /api/events 响应 data。
 */
public class CreateEventResponse {

    private Boolean created;
    private EventVO event;
    private List<ConflictEventVO> conflicts;

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }

    public EventVO getEvent() {
        return event;
    }

    public void setEvent(EventVO event) {
        this.event = event;
    }

    public List<ConflictEventVO> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<ConflictEventVO> conflicts) {
        this.conflicts = conflicts;
    }
}
