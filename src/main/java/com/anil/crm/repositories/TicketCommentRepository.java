package com.anil.crm.repositories;

import com.anil.crm.domain.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {


    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    List<TicketComment> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

}
