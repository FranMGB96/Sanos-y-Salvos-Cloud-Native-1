package com.sanosysalvos.kafkaadmin.dto;

import java.util.List;
import java.util.Map;

/** Detalle de un topic: particiones (líder, réplicas, ISR) y configuración. */
public record TopicDetail(String name, boolean internal, int replicationFactor,
                          List<PartitionInfo> partitions, Map<String, String> config) {

    public record PartitionInfo(int partition, Integer leader, List<Integer> replicas, List<Integer> isr,
                                long beginningOffset, long endOffset) {
    }
}
