package shop.le_paysan.backend.livreur.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

/** Formulaire multipart de modification d'un compte livreur par le vendeur. */
@Getter
@Setter
public class UpdateLivreurRequest {

    private String firstName;
    private String lastName;
    private String phone;
    private String city;
    private MultipartFile photo;
    private MultipartFile idDocumentFront;
    private MultipartFile idDocumentBack;
    private MultipartFile selfieWithId;
}
