package com.anil.crm.repositories;

import com.anil.crm.entities.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketComment,Integer> {
}
