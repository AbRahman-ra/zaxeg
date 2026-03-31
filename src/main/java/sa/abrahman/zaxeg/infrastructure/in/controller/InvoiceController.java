package sa.abrahman.zaxeg.infrastructure.in.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerator;
import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate.InvoiceGenerationRequest;
import sa.abrahman.zaxeg.infrastructure.in.dto.response.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
@Tag(name = "ZATCA E-Invoicing", description = "Endpoints for generating ZATCA XML")
public class InvoiceController {
    private final InvoiceGenerator service;

    @PostMapping(value = "/generate", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Generate UBL 2.1 XML from JSON payload")
    public ResponseEntity<ApiResponse<String>> generate(@Valid @RequestBody InvoiceGenerationRequest request) {
        InvoiceGenerationPayload payload = request.toPayload();
        String invoice = service.handle(payload);
        return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK.value(), "", invoice));
    }

}
