package shop.le_paysan.backend.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

/**
 * Formulaire multipart de demande de boutique (cf. docs/02-specifications-fonctionnelles.md UC-02).
 * Les champs requis varient selon {@link #ownerType} ; validation manuelle dans ShopService
 * (une annotation Bean Validation conditionnelle serait trop rigide ici).
 */
@Getter
@Setter
public class ShopRequestForm {

    // Commun
    private String ownerType; // PHYSIQUE | ENTREPRENANT | MORALE
    private String name;
    private String description;
    private String category;
    private String addressCity;
    private String addressDetail;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private MultipartFile logo;
    private MultipartFile cover;
    private boolean acceptCgv;

    // Personne physique / Entreprenant
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String idDocumentType; // CNI | ATTESTATION_IDENTITE | PASSEPORT
    private String idDocumentNumber;
    private MultipartFile idDocumentFront;
    private MultipartFile idDocumentBack;
    private MultipartFile selfieWithId;

    // Entreprenant uniquement
    private String recepisseNumber;
    private LocalDate recepisseDate;
    private MultipartFile recepisseDocument;

    // Personne morale
    private String companyName;
    private String legalForm; // SARL | SA | SAS | SUARL | AUTRE
    private String rccmNumber;
    private String nccNumber;
    private MultipartFile legalDocument;
    private String legalRepName;
    private String legalRepRole;
    private String legalRepIdDocumentType;
    private String legalRepIdDocumentNumber;
    private MultipartFile legalRepIdDocumentFront;
    private MultipartFile legalRepIdDocumentBack;
    private MultipartFile legalRepSelfieWithId;
}
