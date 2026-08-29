package com.payment.repository;

import com.payment.entity.Invoice;
import com.payment.entity.enums.InvoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByOrderIdAndInvoiceType(String orderId, InvoiceType invoiceType);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByOrderId(String orderId);
}
