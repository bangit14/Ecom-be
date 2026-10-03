package com.vn.order.web.outbox.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vn.order.web.outbox.domain.entity.OutboxEvent;

public interface IOutboxEventService extends IService<OutboxEvent> {
    void saveOutboxEvent(String aggregateType, String aggregateId, String eventType, String topic, String kafkaKey, Object payload);
}
