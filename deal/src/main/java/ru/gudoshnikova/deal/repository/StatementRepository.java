package ru.gudoshnikova.deal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gudoshnikova.deal.model.Statement;

import java.util.UUID;

@Repository
public interface StatementRepository extends JpaRepository<Statement, UUID> {
}
