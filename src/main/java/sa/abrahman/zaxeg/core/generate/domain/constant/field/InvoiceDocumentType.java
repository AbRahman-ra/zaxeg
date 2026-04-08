package sa.abrahman.zaxeg.core.generate.domain.constant.field;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @see <a href= "https://unece.org/fileadmin/DAM/trade/untdid/d16b/tred/tred1001.htm">UN/EDIFACT 1001: Document name
 *      code</a> for the full list
 */
@Getter
@RequiredArgsConstructor
public enum InvoiceDocumentType {
    TAX_INVOICE(388), DEBIT_NOTE(383), CREDIT_NOTE(381), PREPAYMENT_INVOICE(386);

    private final Integer code;
}
