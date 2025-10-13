package com.anil.crm.bootstrap;

import com.anil.crm.entities.Agent;
import com.anil.crm.entities.Customer;
import com.anil.crm.entities.Ticket;
import com.anil.crm.entities.TicketComment;
import com.anil.crm.repositories.AgentRepository;
import com.anil.crm.repositories.CustomerRepository;
import com.anil.crm.repositories.TicketCommentRepository;
import com.anil.crm.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;
//    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        ticketCommentRepository.deleteAll();
        ticketRepository.deleteAll();
        customerRepository.deleteAll();
        agentRepository.deleteAll();

        Agent agent1 = Agent.builder()
                .fullName("Ahmet Yılmaz")
                .email("ahmet.yilmaz@company.com")
//                .passwordHash(passwordEncoder.encode("123456"))
                .passwordHash("asd")
                .department("Teknik Destek")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Agent agent2 = Agent.builder()
                .fullName("Ayşe Demir")
                .email("ayse.demir@company.com")
//                .passwordHash(passwordEncoder.encode("123456"))
                .passwordHash("asd")
                .department("Müşteri Hizmetleri")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        agentRepository.save(agent1);
        agentRepository.save(agent2);


        Customer customer1 = Customer.builder()
                .fullName("Ali Veli")
                .email("ali.veli@email.com")
//                .passwordHash(passwordEncoder.encode("123456"))
                .passwordHash("asd")
                .phone("+905555555555")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Customer customer2 = Customer.builder()
                .fullName("Ayşe Kara")
                .email("ayse.kara@email.com")
//                .passwordHash(passwordEncoder.encode("123456"))
                .passwordHash("asd")
                .phone("+905554444444")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        customerRepository.save(customer1);
        customerRepository.save(customer2);

        Ticket ticket1 = Ticket.builder()
                .customer(customer1)
                .agent(agent1)
                .subject("Ürün iade talebi")
                .description("Satın aldığım ürünü iade etmek istiyorum")
                .status("OPEN")
                .priority("MEDIUM")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Ticket ticket2 = Ticket.builder()
                .customer(customer2)
                .agent(agent2)
                .subject("Teknik sorun")
                .description("Uygulama açılmıyor, hata veriyor")
                .status("IN_PROGRESS")
                .priority("HIGH")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);


        TicketComment comment1 = TicketComment.builder()
                .ticket(ticket1)
                .authorType("CUSTOMER")
                .customer(customer1)
                .comment("Lütfen talebimi hızlıca işleme alın.")
                .createdAt(LocalDateTime.now())
                .build();

        TicketComment comment2 = TicketComment.builder()
                .ticket(ticket1)
                .authorType("AGENT")
                .agent(agent1)
                .comment("Talebiniz işleme alındı, en kısa sürede dönüş yapılacaktır.")
                .createdAt(LocalDateTime.now())
                .build();

        ticketCommentRepository.save(comment1);
        ticketCommentRepository.save(comment2);


    }
}
