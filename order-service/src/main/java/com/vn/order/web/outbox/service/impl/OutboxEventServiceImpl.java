package com.vn.order.web.outbox.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vn.order.core.enums.OutboxEventStatus;
import com.vn.order.core.util.JsonUtil;
import com.vn.order.web.outbox.domain.entity.OutboxEvent;
import com.vn.order.web.outbox.mapper.OutboxEventMapper;
import com.vn.order.web.outbox.service.IOutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxEventServiceImpl extends ServiceImpl<OutboxEventMapper, OutboxEvent> implements IOutboxEventService {

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveOutboxEvent(String aggregateType, String aggregateId, String eventType, String topic, String kafkaKey, Object payload) {
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .topic(topic)
                .kafkaKey(kafkaKey)
                .payloadClass(payload.getClass().getName())
                .payload(JsonUtil.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .maxRetries(5)
                .createdAt(LocalDateTime.now())
                .build();

        this.save(event);
    }
}
