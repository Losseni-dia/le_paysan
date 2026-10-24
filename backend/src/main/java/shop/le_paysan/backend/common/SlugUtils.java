package shop.le_paysan.backend.common;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class SlugUtils {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("^-|-$");

    private SlugUtils() {
    }

    /** Slug de base (sans suffixe), ex. "Tomates fraîches" -&gt; "tomates-fraiches". */
    public static String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutDiacritics = DIACRITICS.matcher(normalized).replaceAll("");
        String slug = NON_ALPHANUMERIC.matcher(withoutDiacritics.toLowerCase()).replaceAll("-");
        return EDGE_DASHES.matcher(slug).replaceAll("");
    }

    /**
     * Slug de base "nom-produit-nom-ferme" (cf. demande utilisateur : pas d'id numerique
     * visible). Genere une fois a la creation, jamais regenere si le nom change ensuite
     * (URL stable). L'appelant doit verifier l'unicite et ajouter un suffixe si necessaire
     * (cf. ProductService.create).
     */
    public static String productSlugBase(String name, String shopName) {
        String productPart = slugify(name);
        String shopPart = slugify(shopName);
        String base = productPart.isEmpty() ? "produit" : productPart;
        return shopPart.isEmpty() ? base : base + "-" + shopPart;
    }
}
