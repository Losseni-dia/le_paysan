export interface LegalPageContent {
  title: string;
  sections: { heading: string; paragraphs: string[] }[];
}

/**
 * Contenu condense depuis docs/08-mentions-legales-cgu-rgpd.md.
 * Rappel : base de travail structuree, a faire valider par un juriste
 * ivoirien avant mise en production reelle.
 */
export const LEGAL_CONTENT: Record<string, LegalPageContent> = {
  'mentions-legales': {
    title: 'Mentions légales',
    sections: [
      {
        heading: 'Éditeur',
        paragraphs: [
          "Le Paysan est édité par [Société éditrice — à compléter une fois constituée], Côte d'Ivoire.",
          'Contact : via le formulaire de contact du site.',
        ],
      },
      {
        heading: 'Hébergement',
        paragraphs: ['Hébergeur : [à définir].'],
      },
    ],
  },
  cgu: {
    title: "Conditions Générales d'Utilisation",
    sections: [
      {
        heading: 'Objet',
        paragraphs: [
          'Les présentes CGU régissent l\'accès et l\'utilisation de la plateforme Le Paysan, marketplace mettant en relation des producteurs agricoles et des acheteurs en Côte d\'Ivoire.',
        ],
      },
      {
        heading: 'Rôle de la plateforme',
        paragraphs: [
          "Le Paysan est un intermédiaire technique. Les contrats de vente se forment directement entre le vendeur et l'acheteur ; la plateforme n'est pas elle-même vendeuse des produits.",
        ],
      },
      {
        heading: 'Comptes et comportements interdits',
        paragraphs: [
          "La création d'un compte implique l'exactitude des informations fournies. Toute fraude, fausse déclaration (notamment lors de la vérification d'identité des boutiques) ou contenu illicite peut entraîner la suspension du compte.",
        ],
      },
      {
        heading: 'Droit applicable',
        paragraphs: ["Droit ivoirien. Tribunaux compétents de Côte d'Ivoire."],
      },
    ],
  },
  cgv: {
    title: 'Conditions Générales de Vente',
    sections: [
      {
        heading: 'Champ d\'application',
        paragraphs: [
          'Toute commande passée sur la plateforme entre un acheteur et un vendeur dont la boutique a été validée.',
        ],
      },
      {
        heading: 'Prix et paiement',
        paragraphs: [
          'Prix affichés en FCFA (XOF). Paiement exclusivement par Mobile Money via Fedapay ; aucune donnée de paiement n\'est stockée par Le Paysan.',
        ],
      },
      {
        heading: 'Livraison',
        paragraphs: [
          "Retrait en boutique (avec localisation sur carte) ou livraison à domicile selon les options proposées par le vendeur.",
        ],
      },
      {
        heading: 'Droit de rétractation',
        paragraphs: [
          "Compte tenu de la nature périssable des produits agricoles, le droit de rétractation ne s'applique pas une fois la commande en préparation, sauf non-conformité du produit livré.",
        ],
      },
      {
        heading: 'Réclamations',
        paragraphs: ['Via le formulaire de contact. Médiation par la plateforme avant tout recours judiciaire.'],
      },
    ],
  },
  confidentialite: {
    title: 'Politique de confidentialité',
    sections: [
      {
        heading: 'Cadre juridique',
        paragraphs: [
          "Conformément à la loi ivoirienne n° 2013-450 relative à la protection des données à caractère personnel (autorité de contrôle : ARTCI), Le Paysan applique en complément, de façon volontaire, les standards RGPD.",
        ],
      },
      {
        heading: 'Données collectées',
        paragraphs: [
          "Données de compte (login, email optionnel, téléphone), selfie de sécurité (optionnel, pour la récupération de compte), données KYC vendeur si vous créez une boutique, données de commande et de géolocalisation boutique.",
        ],
      },
      {
        heading: 'Vos droits',
        paragraphs: [
          "Droit d'accès, de rectification, d'effacement, d'opposition, de limitation et de portabilité. Exercice via le formulaire de contact. Réclamation possible auprès de l'ARTCI.",
        ],
      },
      {
        heading: 'Sécurité',
        paragraphs: [
          'Mots de passe chiffrés, accès restreint aux documents sensibles, transmissions sécurisées.',
        ],
      },
    ],
  },
  cookies: {
    title: 'Politique de cookies',
    sections: [
      {
        heading: 'Cookies nécessaires',
        paragraphs: [
          "Utilisés pour la session et l'authentification (ex. jeton de connexion). Indispensables au fonctionnement du site, sans consentement requis.",
        ],
      },
      {
        heading: 'Cookies non essentiels',
        paragraphs: [
          "Si des cookies de mesure d'audience sont ajoutés ultérieurement, un bandeau de consentement vous permettra de les accepter ou refuser.",
        ],
      },
    ],
  },
};
