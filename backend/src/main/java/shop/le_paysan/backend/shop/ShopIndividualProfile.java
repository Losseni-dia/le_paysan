package shop.le_paysan.backend.shop;

import java.time.LocalDate;

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

/** Cf. docs/03-modele-donnees.md §2.4 (owner_type = PHYSIQUE). */
@Entity
@Table(name = "shop_individual_profile")
@Getter
@Setter
@NoArgsConstructor
public class ShopIndividualProfile {

    @Id
    @Column(name = "shop_id")
    private Long shopId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "id_document_type", nullable = false, length = 20)
    private String idDocumentType;

    @Column(name = "id_document_number", nullable = false, length = 50)
    private String idDocumentNumber;

    @Column(name = "id_document_front_url", nullable = false)
    private String idDocumentFrontUrl;

    @Column(name = "id_document_back_url")
    private String idDocumentBackUrl;

    @Column(name = "selfie_with_id_url", nullable = false)
    private String selfieWithIdUrl;
}
