package cn.gt.msg.mapper;

import cn.gt.msg.entity.FailedMessageRetry;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 失败消息重试Mapper接口
 *
 * @author message-center
 * @date 2025-11-15
 */
@Mapper
public interface FailedMessageRetryMapper extends BaseMapper<FailedMessageRetry> {
}
