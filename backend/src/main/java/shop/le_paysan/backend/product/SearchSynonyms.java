package shop.le_paysan.backend.product;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Cf. demande utilisateur : une recherche generique ("poisson") doit aussi remonter les produits
 * nommes par espece/variete precise ("tilapia", "carpe"...), meme si le mot generique n'apparait
 * nulle part dans le nom du produit. Dictionnaire volontairement simple (catalogue agricole
 * ivoirien) plutot qu'un moteur de synonymes complet.
 */
public final class SearchSynonyms {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private static final Map<String, List<String>> SYNONYMS = Map.ofEntries(
            Map.entry(
                    "poisson",
                    List.of(
                            "tilapia", "carpe", "silure", "machoiron", "capitaine", "thon", "bar", "dorade",
                            "maquereau", "sardine", "crevette", "poisson fume", "poisson braise", "poisson sec")),
            Map.entry(
                    "viande",
                    List.of("boeuf", "poulet", "mouton", "chevre", "porc", "lapin", "pintade", "dinde", "canard",
                            "gibier", "agneau", "chevreau")),
            Map.entry(
                    "volaille",
                    List.of("poulet", "pintade", "dinde", "canard", "pigeon", "caille", "coq", "poule")),
            Map.entry(
                    "legume",
                    List.of(
                            "tomate", "oignon", "gombo", "aubergine", "piment", "chou", "carotte", "poivron",
                            "concombre", "haricot", "epinard", "laitue", "courgette", "salade")),
            Map.entry(
                    "fruit",
                    List.of(
                            "mangue", "banane", "ananas", "orange", "papaye", "avocat", "citron", "pasteque",
                            "pamplemousse", "corossol", "goyave", "coco", "fraise", "mandarine")),
            Map.entry("cereale", List.of("riz", "mais", "mil", "sorgho", "fonio", "ble")),
            Map.entry(
                    "tubercule", List.of("igname", "manioc", "patate douce", "taro", "pomme de terre", "attieke")),
            Map.entry("oeuf", List.of("alveole", "plateau d'oeufs", "oeufs")),
            Map.entry("lait", List.of("lait de vache", "yaourt", "fromage", "beurre")),
            Map.entry(
                    "epice", List.of("gingembre", "ail", "piment", "poivre", "clou de girofle", "cannelle", "basilic")));

    private SearchSynonyms() {
    }

    /**
     * Termes a chercher dans le nom du produit : le terme saisi tel quel (pour matcher le nom
     * exact, accents compris) + les synonymes connus dans leur graphie naturelle (le lookup dans
     * le dictionnaire, lui, ignore accents/casse pour couvrir "poisson"/"Poisson"/"POISSON").
     */
    public static Set<String> expand(String search) {
        List<String> extra = SYNONYMS.getOrDefault(normalize(search), List.of());
        Set<String> terms = new java.util.LinkedHashSet<>();
        terms.add(search.trim());
        terms.addAll(extra);
        return terms;
    }

    public static String normalize(String value) {
        String decomposed = Normalizer.normalize(value.toLowerCase().trim(), Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed).replaceAll("").replace("œ", "oe");
    }
}
