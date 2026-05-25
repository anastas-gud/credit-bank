package ru.gudoshnikova.deal.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.gudoshnikova.deal.dto.LoanOfferDto;
import ru.gudoshnikova.deal.dto.StatementStatusHistoryDto;
import ru.gudoshnikova.deal.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "statements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Statement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "statement_id")
    private UUID statementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Client client;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_id")
    @JsonIgnoreProperties({"statement", "hibernateLazyInitializer", "handler"})
    private Credit credit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;

    @Column(name = "creation_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime creationDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applied_offer", columnDefinition = "jsonb")
    private LoanOfferDto appliedOffer;

    @Column(name = "sign_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime signDate;

    @Column(name = "ses_code")
    private String sesCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "status_history", columnDefinition = "jsonb")
    private List<StatementStatusHistoryDto> statusHistory;
}
