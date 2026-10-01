package shop.le_paysan.backend.product.dto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

/** Formulaire multipart de creation/edition produit (cf. docs/02 UC-04). */
@Getter
@Setter
public class ProductCreateForm {

    private Long categoryId;
    private String name;
    private String description;
    private String unit;
    private BigDecimal unitPrice;
    private Integer stockQuantity;

    private boolean retraitBoutique;
    private String pickupLocation;

    private boolean livraisonDomicile;
    private BigDecimal deliveryPrice;

    private List<MultipartFile> images;
}
