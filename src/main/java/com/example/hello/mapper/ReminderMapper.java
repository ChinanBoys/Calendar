package com.example.hello.mapper;

import com.example.hello.entity.Reminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReminderMapper {

    int insert(Reminder reminder);

    int deleteByEventId(@Param("eventId") Long eventId);

    List<Reminder> selectByEventId(@Param("eventId") Long eventId);
}
