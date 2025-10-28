package com.anil.crm.web.mappers;

import com.anil.crm.domain.Agent;
import com.anil.crm.domain.Department; // Import Department
import com.anil.crm.web.models.AgentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named; // For custom mapping logic if needed

@Mapper(componentModel = "spring") // Spring Bean olması için
public interface AgentMapper {

    /**
     * Agent Entity'sini AgentDto'ya çevirir.
     * Yanıt (GET) için kullanılır.
     */
    @Mapping(target = "password", ignore = true) // Şifreyi DTO'ya koyma
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    // --- DEĞİŞİKLİK BURADA ---
    // DTO'daki 'departmentName' alanını, Agent'ın içindeki
    // Department nesnesinin 'name' alanından al.
    @Mapping(target = "departmentName", source = "department.name")
    // @Mapping(target = "departmentId", source = "department.id") // Eğer DTO'da ID kullanırsanız
    AgentDto agentToAgentDto(Agent agent);


    /**
     * AgentDto'yu Agent Entity'sine çevirir (Kısmi).
     * İstek (POST/PUT) için kullanılır. Departman gibi ilişkili nesnelerin
     * DTO'dan Entity'ye çevrilmesi genellikle Servis katmanında yapılır.
     */
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.email", source = "email")
    // --- DEĞİŞİKLİK BURADA ---
    // DTO'daki 'departmentName' (String) alanını Entity'deki
    // 'department' (Department nesnesi) alanına DOĞRUDAN map'leyemeyiz.
    // Bu yüzden ignore=true yapıyoruz. Servis katmanı bu ismi kullanarak
    // DepartmentRepository'den ilgili Department nesnesini bulup set etmelidir.
    @Mapping(target = "department", ignore = true)
    // Eğer DTO'da departmentId kullanılsaydı ve DepartmentRepository enjekte edilseydi,
    // MapStruct bunu otomatik bulabilirdi: @Mapping(target = "department", source = "departmentId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user.id", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.role", ignore = true) // Rol serviste atanmalı
    Agent agentDtoToAgent(AgentDto agentDto);

}