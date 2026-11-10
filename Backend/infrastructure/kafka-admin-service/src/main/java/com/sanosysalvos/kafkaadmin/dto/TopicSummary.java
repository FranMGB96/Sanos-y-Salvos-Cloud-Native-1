package com.sanosysalvos.kafkaadmin.dto;

public record TopicSummary(String name, int partitions, int replicationFactor, boolean internal) {
}
