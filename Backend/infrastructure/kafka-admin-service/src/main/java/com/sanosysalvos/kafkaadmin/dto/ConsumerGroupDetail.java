package com.sanosysalvos.kafkaadmin.dto;

import java.util.List;

/** Detalle de un consumer group: quién consume qué partición y cuánto lag tiene. */
public record ConsumerGroupDetail(String groupId, String state, String coordinator, String assignor,
                                  int members, long totalLag,
                                  List<MemberInfo> memberList, List<PartitionLag> partitions) {

    public record MemberInfo(String consumerId, String clientId, String host, List<String> partitions) {
    }

    /** lag = endOffset (último producido) - committedOffset (último procesado por el grupo). */
    public record PartitionLag(String topic, int partition, Long committedOffset, long endOffset,
                               long lag, String consumerId) {
    }
}
