package cn.gt.msg.mapper;

import cn.gt.msg.entity.ConsumerPushLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消费者推送日志Mapper接口
 *
 * @author message-center
 * @date 2025-11-15
 */
@Mapper
public interface ConsumerPushLogMapper extends BaseMapper<ConsumerPushLog> {
}
