package com.example.hello.mapper;

import com.example.hello.entity.VoiceLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * voice_log 表 Mapper。
 * 语音解析模块只做 INSERT（落解析日志，不创建事件）。
 */
@Mapper
public interface VoiceLogMapper {

    /**
     * 插入一条语音解析日志，回填自增主键到 voiceLog.id。
     *
     * @param voiceLog 待插入的日志
     * @return 影响行数
     */
    int insert(VoiceLog voiceLog);

    /**
     * 按用户 ID 清除全部语音解析日志（F-SET-02 隐私清除）。
     */
    int deleteByUserId(@Param("userId") Long userId);
}
