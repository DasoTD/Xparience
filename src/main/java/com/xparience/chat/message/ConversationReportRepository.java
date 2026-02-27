package com.xparience.chat.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationReportRepository extends JpaRepository<ConversationReport, Long> {
	List<ConversationReport> findAllByOrderByCreatedAtDesc();
}
