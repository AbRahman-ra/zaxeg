package sa.abrahman.zaxeg.core.model.invoice.wrapper;

import sa.abrahman.zaxeg.core.model.invoice.party.*;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvoiceParties {
    /**
     * <h2>Seller Information</h2>
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

    /**
     * <h2>Buyer Information</h2>
     * <h3>Buyer Name</h3>
     * <ul><li>BG-07, BT-44: The full name of the Buyer.</li></ul>
     *
     * <h3>Buyer Address</h3>
     * <ul>
     * <li>BG-08, BT-50: Buyer address line 1 - the main address line in an address</li>
     * <li>BG-08, BT-51: Buyer address line 2 - an additional address line in an address that can be used to give further details supplementing the main line.</li>
     * <li>BG-08, KSA-18: Buyer address building number</li>
     * <li>BG-08, KSA-19: Buyer address additional number</li>
     * <li>BG-08, BT-52: The common name of the city, town or village, where the Buyer's address is located.</li>
     * <li>BG-08, BT-53: Buyer post code</li>
     * <li>BG-08, BT-54: Buyer country subdivision</li>
     * <li>BG-08, KSA-04: The name of the subdivision of the Buyer city, town, or village in which its address is located, such as the name of its district or borough.</li>
     * <li>BG-08, BT-55: Buyer country code</li>
     * <ul>
     *
     * <h3>Buyer VAT Identification</h3>
     * <ul>
     * <li>BG-07, BT-48, The Buyer's VAT identifier (also known as Buyer VAT identification number).</li>
     * </ul>
    *
    *
     * <h3>Other buyer IDs</h3>
     * BG-07, BT-46, BT-46-1: Other Buyer ID must be one of the following list:
     *
     * <ol>
     * <li>Tax Identification Number "TIN" as schemeID</li>
     * <li>Commercial registration number with "CRN" as schemeID</li>
     * <li>Momra license with "MOM" as schemeID</li>
     * <li>MLSD license with "MLS" as schemeID</li>
     * <li>700 Number with "700" as schemeID</li>
     * <li>Sagia license with "SAG" as schemeID</li>
     * <li>National ID with "NAT" as schemeID</li>
     * <li>GCC ID with "GCC" as schemeID</li>
     * <li>Iqama Number with "IQA" as schemeID</li>
     * <li>Passport ID with "PAS" as schemeID</li>
     * <li>Other ID with "OTH" as schemeID</li>
     * </ol>
     *
     * <p>
     * In case multiple IDs exist then one of the above must be entered following
     * the sequence specified above
     * </p>
     */
    private Party buyer;
}
