package shop.le_paysan.backend.shop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cf. docs/03-modele-donnees.md §2.6 (owner_type = MORALE). */
@Entity
@Table(name = "shop_company_profile")
@Getter
@Setter
@NoArgsConstructor
public class ShopCompanyProfile {

    @Id
    @Column(name = "shop_id")
    private Long shopId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "legal_form", nullable = false, length = 20)
    private String legalForm;

    @Column(name = "rccm_number", nullable = false, length = 30)
    private String rccmNumber;

    @Column(name = "ncc_number", nullable = false, length = 30)
    private String nccNumber;

    @Column(name = "legal_document_url", nullable = false)
    private String legalDocumentUrl;

    @Column(name = "legal_rep_name", nullable = false, length = 150)
    private String legalRepName;

    @Column(name = "legal_rep_role", nullable = false, length = 100)
    private String legalRepRole;

    @Column(name = "legal_rep_id_document_type", nullable = false, length = 20)
    private String legalRepIdDocumentType;

    @Column(name = "legal_rep_id_document_number", nullable = false, length = 50)
    private String legalRepIdDocumentNumber;

    @Column(name = "legal_rep_id_document_front_url", nullable = false)
    private String legalRepIdDocumentFrontUrl;

    @Column(name = "legal_rep_id_document_back_url")
    private String legalRepIdDocumentBackUrl;

    @Column(name = "legal_rep_selfie_with_id_url", nullable = false)
    private String legalRepSelfieWithIdUrl;
}
