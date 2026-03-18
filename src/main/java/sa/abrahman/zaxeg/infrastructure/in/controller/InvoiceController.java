package sa.abrahman.zaxeg.infrastructure.in.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerator;
import sa.abrahman.zaxeg.infrastructure.in.dto.InvoiceRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
@Tag(name = "ZATCA E-Invoicing", description = "Endpoints for generating ZATCA XML")
public class InvoiceController {
    private final InvoiceGenerator invoiceGenerator;

    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_XML_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Generate UBL 2.1 XML from JSON payload")
    public ResponseEntity<String> generate(@Valid @RequestBody InvoiceRequest payload) {
        Invoice invoice = payload.toDomainModel();
        String xmlInvoice = invoiceGenerator.toXML(invoice);
        return ResponseEntity.ok(xmlInvoice);
    }

}
