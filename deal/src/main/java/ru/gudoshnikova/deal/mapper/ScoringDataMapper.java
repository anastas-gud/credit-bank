package ru.gudoshnikova.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.gudoshnikova.deal.dto.ScoringDataDto;
import ru.gudoshnikova.deal.model.Client;
import ru.gudoshnikova.deal.model.Statement;

@Mapper(componentModel = "spring")
public interface ScoringDataMapper {
    @Mapping(target = "amount", source = "statement.appliedOffer.requestedAmount")
    @Mapping(target = "term", source = "statement.appliedOffer.term")
    @Mapping(target = "firstName", source = "client.firstName")
    @Mapping(target = "lastName", source = "client.lastName")
    @Mapping(target = "middleName", source = "client.middleName")
    @Mapping(target = "gender", source = "client.gender")
    @Mapping(target = "birthdate", source = "client.birthdate")
    @Mapping(target = "passportSeries", source = "client.passport.series")
    @Mapping(target = "passportNumber", source = "client.passport.number")
    @Mapping(target = "passportIssueDate", source = "client.passport.issueDate")
    @Mapping(target = "passportIssueBranch", source = "client.passport.issueBranch")
    @Mapping(target = "maritalStatus", source = "client.maritalStatus")
    @Mapping(target = "dependentAmount", source = "client.dependentAmount")
    @Mapping(target = "employment.workExperienceTotal", source = "client.employment.workExperienceTotal")
    @Mapping(target = "employment.employmentStatus", source = "client.employment.employmentStatus")
    @Mapping(target = "employment.workExperienceCurrent", source = "client.employment.workExperienceCurrent")
    @Mapping(target = "employment.salary", source = "client.employment.salary")
    @Mapping(target = "employment.position", source = "client.employment.position")
    @Mapping(target = "employment.employerINN", source = "client.employment.employerINN")
    @Mapping(target = "accountNumber", source = "client.accountNumber")
    @Mapping(target = "isInsuranceEnabled", source = "statement.appliedOffer.isInsuranceEnabled")
    @Mapping(target = "isSalaryClient", source = "statement.appliedOffer.isSalaryClient")
    ScoringDataDto toScoringDataDto(Client client, Statement statement);
}
