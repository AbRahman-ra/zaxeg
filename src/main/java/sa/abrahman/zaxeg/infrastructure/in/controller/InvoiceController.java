package sa.abrahman.zaxeg.infrastructure.in.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationCommand;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerator;
import sa.abrahman.zaxeg.infrastructure.in.dto.APIResponse;
import sa.abrahman.zaxeg.infrastructure.in.dto.InvoiceRequest;

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

    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_XML_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Generate UBL 2.1 XML from JSON payload")
    public ResponseEntity<APIResponse<String>> generate(@Valid @RequestBody InvoiceRequest request) {
        InvoiceGenerationCommand command = request.toCommand();
        String invoice = service.handle(command);
        return ResponseEntity.ok(APIResponse.from(HttpStatus.OK.value(), "", invoice));
    }

}
