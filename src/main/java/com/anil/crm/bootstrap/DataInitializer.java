package com.anil.crm.bootstrap;


import com.anil.crm.domain.*;
import com.anil.crm.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {


    private final DepartmentRepository departmentRepository;
    private final CustomerRepository customerRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info(">>>> DataInitializer RUN method started <<<<");
        ticketCommentRepository.deleteAll();
        ticketRepository.deleteAll();
        customerRepository.deleteAll();
        agentRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        Department techSupport = Department.builder()
                .name("Teknik Destek")
                .description("Ürünler ve hizmetlerle ilgili teknik sorunlara yardımcı olan departman.") // Opsiyonel açıklama
                .build();

        Department customerSupport = Department.builder()
                .name("Müşteri Hizmetleri")
                .description("Genel sorular, hesap yönetimi ve şikayetlerle ilgilenen departman.")
                .build();


        departmentRepository.save(techSupport);
        departmentRepository.save(customerSupport);


        User agentUser1 = User.builder()
                .firstName("Anil")
                .lastName("karabulut")
                .email("anil.karabulut@hotmail.com")
                .password(passwordEncoder.encode("123456"))
                .role(Role.ADMIN)
                .build();


        Agent agent1 = Agent.builder()
                .department(techSupport)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(agentUser1)
                .build();

        agentRepository.save(agent1);


        User agentUser2 = User.builder()
                .firstName("Ayşe")
                .lastName("Demir")
                .email("ayse.demir@company.com")
                .password(passwordEncoder.encode("123456"))
                .role(Role.AGENT)
                .build();

        Agent agent2 = Agent.builder()
                .department(customerSupport)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(agentUser2)
                .build();

        agentRepository.save(agent2);


        User customerUser1 = User.builder()
                .firstName("Ali")
                .lastName("Veli")
                .email("ali.veli@email.com")
                .password(passwordEncoder.encode("123456"))
                .role(Role.CUSTOMER)
                .build();


        Customer customer1 = Customer.builder()
                .phone("+905555555555")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(customerUser1)
                .build();

        customerRepository.save(customer1);


        User customerUser2 = User.builder()
                .firstName("Ayşe")
                .lastName("Kara")
                .email("ayse.kara@email.com")
                .password(passwordEncoder.encode("123456"))
                .role(Role.CUSTOMER)
                .build();

        Customer customer2 = Customer.builder()
                .phone("+905554444444")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(customerUser2)
                .build();

        customerRepository.save(customer2);


        Ticket ticket1 = Ticket.builder()
                .customer(customer1)
                .department(customerSupport)
                .subject("Ürün iade talebi")
                .description("Satın aldığım ürünü iade etmek istiyorum")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Ticket ticket2 = Ticket.builder()
                .customer(customer2)
                .department(techSupport)
                .subject("Teknik sorun")
                .description("Uygulama açılmıyor, hata veriyor")
                .status(TicketStatus.IN_PROGRESS)
                .priority(TicketPriority.HIGH)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);


        TicketComment comment1 = TicketComment.builder()
                .ticket(ticket1)
                .author(customer1.getUser())
                .comment("Lütfen talebimi hızlıca işleme alın.")
                .build();

        TicketComment comment2 = TicketComment.builder()
                .ticket(ticket1)
                .author(agent1.getUser())
                .comment("Talebiniz işleme alındı, en kısa sürede dönüş yapılacaktır.")
                .build();

        ticketCommentRepository.save(comment1);
        ticketCommentRepository.save(comment2);
    }
}