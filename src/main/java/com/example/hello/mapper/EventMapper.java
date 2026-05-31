package com.example.hello.mapper;

import com.example.hello.dto.event.ConflictEventVO;
import com.example.hello.dto.event.EventQueryParam;
import com.example.hello.entity.Event;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EventMapper {

    int insert(Event event);

    int updateSelective(Event event);

    int updateStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("status") int status);

    Event selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<ConflictEventVO> selectConflicts(@Param("userId") Long userId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime,
                                          @Param("excludeId") Long excludeId);

    long countByQuery(EventQueryParam param);

    List<Event> selectPageByQuery(EventQueryParam param);
}
