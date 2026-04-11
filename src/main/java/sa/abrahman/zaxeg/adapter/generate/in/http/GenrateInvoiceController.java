package sa.abrahman.zaxeg.adapter.generate.in.http;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.adapter.generate.in.dto.InvoiceGenerationRequest;
import sa.abrahman.zaxeg.core.generate.port.in.InvoiceGenerator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
@Tag(name = "ZATCA E-Invoicing", description = "Endpoints for generating ZATCA XML")
public class GenrateInvoiceController {
    private final InvoiceGenerator service;

    @PostMapping(value = "/generate", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Generate UBL 2.1 XML from JSON payload")
    public ResponseEntity<ApiResponse<?>> generate(@Valid @RequestBody InvoiceGenerationRequest request) {
        InvoiceGenerationPayload payload = request.mapped();
        ApiResponse<?> response = service.handle(payload);
        return ResponseEntity.status(response.status()).body(response);
    }

}
