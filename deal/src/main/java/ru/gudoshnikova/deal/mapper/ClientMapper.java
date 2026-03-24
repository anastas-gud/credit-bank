package ru.gudoshnikova.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.gudoshnikova.deal.api.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.deal.api.dto.LoanStatementRequestDto;
import ru.gudoshnikova.deal.model.Client;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClientMapper {
    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "maritalStatus", ignore = true)
    @Mapping(target = "dependentAmount", ignore = true)
    @Mapping(target = "employment", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(source = "passportSeries", target = "passport.series")
    @Mapping(source = "passportNumber", target = "passport.number")
    Client toClient(LoanStatementRequestDto request);

    @Mapping(target = "passport.issueDate", source = "passportIssueDate")
    @Mapping(target = "passport.issueBranch", source = "passportIssueBranch")
    @Mapping(target = "employment.employmentStatus", source = "employment.employmentStatus")
    @Mapping(target = "employment.employerINN", source = "employment.employerINN")
    @Mapping(target = "employment.salary", source = "employment.salary")
    @Mapping(target = "employment.position", source = "employment.position")
    @Mapping(target = "employment.workExperienceTotal", source = "employment.workExperienceTotal")
    @Mapping(target = "employment.workExperienceCurrent", source = "employment.workExperienceCurrent")
    void updateClientFromFinishRegistration(FinishRegistrationRequestDto request, @MappingTarget Client client);
}
