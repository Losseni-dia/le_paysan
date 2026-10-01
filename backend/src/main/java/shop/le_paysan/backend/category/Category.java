package shop.le_paysan.backend.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cf. docs/03-modele-donnees.md §2.8. */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    public static final String STATUS_EN_ATTENTE = "EN_ATTENTE";
    public static final String STATUS_VALIDE = "VALIDE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    // Une categorie suggeree par un vendeur (EN_ATTENTE) n'apparait pas dans la liste
    // publique/vendeur tant qu'un admin ne l'a pas validee (cf. demande produit).
    @Column(nullable = false, length = 20)
    private String status = STATUS_VALIDE;
}
