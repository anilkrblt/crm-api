package com.anil.crm.repositories;

import com.anil.crm.domain.TicketComment;
import com.anil.crm.web.models.TicketCommentDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findTicketCommentsById(Long id);
}
