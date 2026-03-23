package sa.abrahman.zaxeg.core.model.invoice.wrapper;

import sa.abrahman.zaxeg.core.model.invoice.party.*;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvoiceParties {
    /**
     * <h3>Seller Name</h3>
     * <ul><li>BG-05, BT-27: Seller Name</li></ul>
     *
     * <h3>Seller Address</h3>
     * <ul>
     * <li>BG-05, BT-35: Seller address line 1 - the main address line in an address</li>
     * <li>BG-05, BT-36: Seller address line 2 - an additional address line in an address that can be used to give further details supplementing the main line.</li>
     * <li>BG-05, KSA-17: Seller address building number</li>
     * <li>BG-05, KSA-23: Seller address additional number</li>
     * <li>BG-05, BT-37: The common name of the city, town or village, where the Seller's address is located.</li>
     * <li>BG-05, BT-38: Seller post code</li>
     * <li>BG-05, BT-39: Seller country subdivision</li>
     * <li>BG-05, KSA-03: The name of the subdivision of the Seller city, town, or village in which its address is located, such as the name of its district or borough.</li>
     * <li>BG-05, BT-40: Seller country code</li>
     * </ul>
     *
     * <h3>Seller VAT Identification</h3>
     * <ul>
     * <li>
     * BG-05, BT-31:
     * <ul>
     * <li>Seller VAT identifier - taxpayer entity.</li>
     * <li>Also known as Seller VAT identification number.</li>
     * </ul>
     * </li>
     * </ul>
    *
    *
     * <h3>Other seller IDs</h3>
     * BT-29, BT-29-1: Other seller ID is one of the list:
     *
     * <ol>
     * <li>Commercial registration number with "CRN" as schemeID</li>
     * <li>Momra license with "MOM" as schemeID</li>
     * <li>MLSD license with "MLS" as schemeID</li>
     * <li>Sagia license with "SAG" as schemeID</li>
     * <li>Other OD with "OTH" as schemeID</li>
     * </ol>
     *
     * <p>
     * In case multiple IDs exist then one of the above must be entered following
     * the sequence specified above
     * </p>
     */
    private Party seller;
}
