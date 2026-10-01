package shop.le_paysan.backend.shop.dto;

import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.shop.ShopCompanyProfile;
import shop.le_paysan.backend.shop.ShopEntreprenantProfile;
import shop.le_paysan.backend.shop.ShopIndividualProfile;

/** Detail complet pour l'admin (infos boutique + documents KYC), cf. docs/02 UC-03. */
public record ShopAdminDetailResponse(
        ShopResponse shop,
        String ownerUsername,
        String ownerEmail,
        IndividualProfile individualProfile,
        EntreprenantProfile entreprenantProfile,
        CompanyProfile companyProfile) {

    public record IndividualProfile(
            String firstName,
            String lastName,
            String birthDate,
            String idDocumentType,
            String idDocumentNumber,
            String idDocumentFrontUrl,
            String idDocumentBackUrl,
            String selfieWithIdUrl) {
        static IndividualProfile from(ShopIndividualProfile p) {
            return new IndividualProfile(
                    p.getFirstName(), p.getLastName(), String.valueOf(p.getBirthDate()), p.getIdDocumentType(),
                    p.getIdDocumentNumber(), p.getIdDocumentFrontUrl(), p.getIdDocumentBackUrl(), p.getSelfieWithIdUrl());
        }
    }

    public record EntreprenantProfile(
            String firstName,
            String lastName,
            String birthDate,
            String idDocumentType,
            String idDocumentNumber,
            String idDocumentFrontUrl,
            String idDocumentBackUrl,
            String selfieWithIdUrl,
            String recepisseNumber,
            String recepisseDate,
            String recepisseDocumentUrl) {
        static EntreprenantProfile from(ShopEntreprenantProfile p) {
            return new EntreprenantProfile(
                    p.getFirstName(), p.getLastName(), String.valueOf(p.getBirthDate()), p.getIdDocumentType(),
                    p.getIdDocumentNumber(), p.getIdDocumentFrontUrl(), p.getIdDocumentBackUrl(), p.getSelfieWithIdUrl(),
                    p.getRecepisseNumber(), String.valueOf(p.getRecepisseDate()), p.getRecepisseDocumentUrl());
        }
    }

    public record CompanyProfile(
            String companyName,
            String legalForm,
            String rccmNumber,
            String nccNumber,
            String legalDocumentUrl,
            String legalRepName,
            String legalRepRole,
            String legalRepIdDocumentType,
            String legalRepIdDocumentNumber,
            String legalRepIdDocumentFrontUrl,
            String legalRepIdDocumentBackUrl,
            String legalRepSelfieWithIdUrl) {
        static CompanyProfile from(ShopCompanyProfile p) {
            return new CompanyProfile(
                    p.getCompanyName(), p.getLegalForm(), p.getRccmNumber(), p.getNccNumber(), p.getLegalDocumentUrl(),
                    p.getLegalRepName(), p.getLegalRepRole(), p.getLegalRepIdDocumentType(), p.getLegalRepIdDocumentNumber(),
                    p.getLegalRepIdDocumentFrontUrl(), p.getLegalRepIdDocumentBackUrl(), p.getLegalRepSelfieWithIdUrl());
        }
    }

    public static ShopAdminDetailResponse from(Shop shop) {
        return new ShopAdminDetailResponse(
                ShopResponse.from(shop),
                shop.getOwner().getUsername(),
                shop.getOwner().getEmail(),
                shop.getIndividualProfile() != null ? IndividualProfile.from(shop.getIndividualProfile()) : null,
                shop.getEntreprenantProfile() != null ? EntreprenantProfile.from(shop.getEntreprenantProfile()) : null,
                shop.getCompanyProfile() != null ? CompanyProfile.from(shop.getCompanyProfile()) : null);
    }
}
