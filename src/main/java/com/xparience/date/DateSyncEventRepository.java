package com.xparience.date;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DateSyncEventRepository extends JpaRepository<DateSyncEvent, Long> {
    long countByInviteId(Long inviteId);
    long countByInviteIdAndAction(Long inviteId, SyncAction action);
    List<DateSyncEvent> findTop1ByInviteIdOrderByOccurredAtDesc(Long inviteId);
}
