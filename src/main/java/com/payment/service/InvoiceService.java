package com.payment.service;

import com.payment.dto.invoice.SendInvoiceEmailResponse;
import com.payment.entity.Invoice;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.InvoiceType;

public interface InvoiceService {

    Invoice generateOrGetInvoice(PaymentTransaction transaction, InvoiceType invoiceType);

    byte[] generateInvoicePdf(String orderId, InvoiceType invoiceType);

    SendInvoiceEmailResponse sendInvoiceEmail(String orderId);
}
