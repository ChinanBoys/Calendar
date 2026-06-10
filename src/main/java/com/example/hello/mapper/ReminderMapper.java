package com.example.hello.mapper;

import com.example.hello.dto.reminder.UpcomingReminderVO;
import com.example.hello.entity.Reminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReminderMapper {

    int insert(Reminder reminder);

    int deleteByEventId(@Param("eventId") Long eventId);

    List<Reminder> selectByEventId(@Param("eventId") Long eventId);

    /**
     * 查询当前正在提醒中的提醒（reminder JOIN event）。
     */
    List<UpcomingReminderVO> selectUpcoming(@Param("userId") Long userId,
                                            @Param("currentTime") LocalDateTime currentTime);

    /**
     * 将提醒标记为已查看。
     */
    int markRead(@Param("id") Long id, @Param("userId") Long userId);
}
