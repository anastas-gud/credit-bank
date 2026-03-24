package ru.gudoshnikova.deal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.gudoshnikova.deal.dto.CreditDto;
import ru.gudoshnikova.deal.model.Credit;
import ru.gudoshnikova.deal.model.Statement;

@Mapper(componentModel = "spring")
public interface CreditMapper {

    @Mapping(target = "creditId", ignore = true)
    @Mapping(target = "statement", source = "statement")
    @Mapping(target = "creditStatus", constant = "CALCULATED")
    Credit toCredit(CreditDto creditDto, Statement statement);
}
