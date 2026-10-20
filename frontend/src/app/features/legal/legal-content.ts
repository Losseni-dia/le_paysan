export interface LegalPageContent {
  title: string;
  sections: { heading: string; paragraphs: string[] }[];
}

/**
 * Contenu detaille depuis docs/08-mentions-legales-cgu-rgpd.md.
 * Rappel : base de travail structuree et professionnelle, a faire valider par un
 * avocat/juriste ivoirien avant mise en production reelle. Les champs entre
 * crochets [...] sont des placeholders a completer une fois la societe editrice
 * constituee.
 */
export const LEGAL_CONTENT: Record<string, LegalPageContent> = {
  'mentions-legales': {
    title: 'Mentions légales',
    sections: [
      {
        heading: 'Éditeur du site',
        paragraphs: [
          "Raison sociale : [Raison sociale de la société éditrice — à constituer]",
          "Forme juridique : [SARL / SAS... une fois immatriculée]",
          "RCCM : [Numéro RCCM de la société éditrice]",
          "NCC (Numéro de Compte Contribuable) : [à compléter]",
          "Siège social : [Adresse complète, Côte d'Ivoire]",
          "Directeur de publication : [Nom, qualité]",
          "Tant que la société n'est pas juridiquement constituée, le porteur de projet (personne physique) est identifié comme responsable de la publication.",
        ],
      },
      {
        heading: 'Contact',
        paragraphs: ["Via le formulaire de contact du site (page Contact)."],
      },
      {
        heading: 'Hébergement',
        paragraphs: ["Hébergeur : [Nom de l'hébergeur, adresse, contact — à définir]."],
      },
      {
        heading: "Propriété intellectuelle",
        paragraphs: [
          "La marque « Le Paysan », le logo et l'ensemble des éléments éditoriaux du site (hors contenus publiés par les vendeurs) sont la propriété de l'éditeur. Toute reproduction sans autorisation est interdite.",
          "Les vendeurs restent propriétaires des photos et descriptions de leurs produits, mais concèdent à la plateforme un droit d'affichage et de diffusion nécessaire au fonctionnement du service.",
        ],
      },
    ],
  },
  cgu: {
    title: "Conditions Générales d'Utilisation",
    sections: [
      {
        heading: 'Objet',
        paragraphs: [
          "Les présentes CGU régissent l'accès et l'utilisation de la plateforme Le Paysan, marketplace mettant en relation des producteurs agricoles (vendeurs) et des acheteurs en Côte d'Ivoire.",
        ],
      },
      {
        heading: 'Acceptation',
        paragraphs: [
          "L'inscription sur la plateforme vaut acceptation pleine et entière des présentes CGU ainsi que de la Politique de confidentialité, par une case à cocher obligatoire, horodatée et versionnée.",
        ],
      },
      {
        heading: 'Comptes et rôles',
        paragraphs: [
          "La création d'un compte est réservée aux personnes majeures (18 ans en Côte d'Ivoire) capables de contracter.",
          "Trois rôles principaux existent : acheteur, vendeur (après validation de boutique) et livreur. Chaque utilisateur est responsable de la confidentialité de ses identifiants et de toute activité réalisée depuis son compte.",
        ],
      },
      {
        heading: 'Rôle d\'intermédiaire de la plateforme',
        paragraphs: [
          "Le Paysan est un intermédiaire technique mettant en relation vendeurs et acheteurs. Les contrats de vente se forment directement entre le vendeur et l'acheteur ; la plateforme n'est pas elle-même vendeuse des produits, sauf mention contraire explicite.",
        ],
      },
      {
        heading: 'Comportements interdits',
        paragraphs: [
          "Sont interdits : toute fraude, fausse déclaration (notamment lors de la vérification d'identité des boutiques ou livreurs), tout contenu illicite, trompeur ou portant atteinte aux droits de tiers.",
        ],
      },
      {
        heading: 'Modération et sanctions',
        paragraphs: [
          "En cas de non-respect des présentes CGU, la plateforme peut suspendre ou rejeter une demande de boutique, retirer un produit, suspendre un compte, avec motif communiqué à l'utilisateur concerné.",
        ],
      },
      {
        heading: 'Responsabilité et garanties',
        paragraphs: [
          "La plateforme limite sa responsabilité sur la qualité et la conformité des produits, qui relève du vendeur. Elle s'engage sur une obligation de moyens quant à la disponibilité du service, sans garantie de résultat.",
          "La disponibilité du service de paiement tiers (Fedapay) ne relève pas de la responsabilité de la plateforme.",
        ],
      },
      {
        heading: 'Résiliation',
        paragraphs: [
          "Tout utilisateur peut demander la suppression de son compte via le formulaire de contact. Certaines données peuvent être conservées au-delà pour répondre à des obligations légales (facturation, lutte anti-fraude).",
        ],
      },
      {
        heading: 'Droit applicable et juridiction',
        paragraphs: [
          "Les présentes CGU sont soumises au droit ivoirien. Tout litige relève des tribunaux compétents de Côte d'Ivoire, après tentative de résolution amiable via le formulaire de contact.",
        ],
      },
    ],
  },
  cgv: {
    title: 'Conditions Générales de Vente',
    sections: [
      {
        heading: "Champ d'application",
        paragraphs: [
          "Les présentes CGV s'appliquent à toute commande passée sur la plateforme entre un acheteur et un vendeur dont la boutique a été validée par l'administration.",
        ],
      },
      {
        heading: 'Formation de la commande',
        paragraphs: [
          "La commande est formée après récapitulatif, paiement et confirmation. Une notification in-app (et par email si disponible) confirme la commande à l'acheteur et au vendeur.",
        ],
      },
      {
        heading: 'Prix et paiement',
        paragraphs: [
          "Les prix sont affichés en Francs CFA (XOF), toutes taxes applicables mentionnées le cas échéant. Les frais de livraison, lorsqu'ils s'appliquent, sont indiqués de façon transparente avant le paiement.",
          "Le paiement s'effectue exclusivement par Mobile Money via Fedapay, prestataire de paiement agréé. Le Paysan ne collecte ni ne stocke aucune donnée de paiement (numéro, code PIN, etc.).",
        ],
      },
      {
        heading: 'Livraison et retrait',
        paragraphs: [
          "Selon les options proposées par le vendeur : retrait en boutique (adresse et localisation sur carte) ou livraison à domicile (prix fixé par palier de distance ou par ville déclarée par le vendeur).",
          "Les délais indiqués sont donnés à titre indicatif par le vendeur et ne constituent pas un engagement garanti par la plateforme.",
        ],
      },
      {
        heading: 'Droit de rétractation',
        paragraphs: [
          "Compte tenu de la nature périssable des produits agricoles et alimentaires, le droit de rétractation ne s'applique pas une fois la commande confirmée et en préparation, sauf non-conformité ou défaut du produit constaté à la livraison.",
        ],
      },
      {
        heading: 'Réclamations et litiges',
        paragraphs: [
          "Toute réclamation s'effectue via le formulaire de contact. La plateforme assure un rôle de médiation entre acheteur et vendeur avant toute action judiciaire. À défaut de résolution amiable, les juridictions ivoiriennes sont seules compétentes.",
        ],
      },
      {
        heading: 'Avis et notation',
        paragraphs: [
          "Les avis ne peuvent être déposés qu'après réception confirmée d'une commande livrée. La plateforme se réserve le droit de modérer tout avis abusif, injurieux ou manifestement mensonger.",
        ],
      },
    ],
  },
  confidentialite: {
    title: 'Politique de confidentialité',
    sections: [
      {
        heading: 'Cadre juridique applicable',
        paragraphs: [
          "Le Paysan opère en Côte d'Ivoire et applique en premier lieu la loi n° 2013-450 du 19 juin 2013 relative à la protection des données à caractère personnel, dont l'autorité de contrôle est l'ARTCI (Autorité de Régulation des Télécommunications/TIC de Côte d'Ivoire).",
          "Cette loi ivoirienne est structurée de façon proche du Règlement Général sur la Protection des Données (RGPD) européen — voir la section dédiée « Alignement RGPD » ci-dessous.",
        ],
      },
      {
        heading: 'Responsable de traitement',
        paragraphs: [
          "[Société éditrice — à compléter] est responsable du traitement des données personnelles collectées sur la plateforme. Un point de contact dédié à la protection des données est joignable via le formulaire de contact.",
        ],
      },
      {
        heading: 'Données collectées et finalités',
        paragraphs: [
          "Données de compte (identifiant, email optionnel, téléphone, mot de passe haché) : gestion de compte et authentification.",
          "Selfie de sécurité (optionnel) : récupération de compte en l'absence d'email renseigné.",
          "Données KYC vendeur (pièce d'identité, selfie avec pièce, RCCM/NCC/récépissé d'entreprenant) : vérification d'identité, conformité légale et lutte anti-fraude.",
          "Géolocalisation de la boutique (latitude/longitude) : affichage sur carte, retrait en boutique.",
          "Données de commande (produits achetés, adresse de livraison) : exécution de la vente.",
          "Avis et notes : information des autres acheteurs.",
          "Messages envoyés via le formulaire de contact : traitement des demandes de support.",
          "Données techniques (journaux, session) : sécurité et bon fonctionnement du service.",
          "Important : aucune donnée de paiement Mobile Money (numéro, code PIN) n'est collectée ni stockée par Le Paysan — ces données sont traitées exclusivement par Fedapay.",
        ],
      },
      {
        heading: 'Destinataires des données',
        paragraphs: [
          "Fedapay, pour le traitement des paiements.",
          "L'hébergeur du backend et de la base de données.",
          "Le fournisseur de messagerie (SMTP) pour l'envoi d'emails.",
          "Aucune donnée n'est vendue à des tiers à des fins commerciales. Aucun transfert hors de la zone UEMOA/Côte d'Ivoire n'est prévu à ce stade.",
        ],
      },
      {
        heading: 'Durées de conservation',
        paragraphs: [
          "Compte actif : durée de la relation contractuelle, puis délai de prescription légale applicable après clôture.",
          "Documents KYC vendeur : durée de la relation, augmentée des obligations légales et comptables applicables.",
          "Journaux techniques : 6 à 12 mois.",
          "Messages de support traités : conservation raisonnable à des fins de preuve et de qualité de service.",
        ],
      },
      {
        heading: 'Vos droits',
        paragraphs: [
          "Conformément à la loi ivoirienne n° 2013-450, vous disposez d'un droit d'accès, de rectification, d'effacement, d'opposition et de limitation sur vos données personnelles.",
          "Ces droits s'exercent via le formulaire de contact, avec un délai de réponse cible d'un mois. Un droit de réclamation peut être introduit auprès de l'ARTCI, autorité ivoirienne compétente en matière de protection des données.",
        ],
      },
      {
        heading: 'Sécurité',
        paragraphs: [
          "Mots de passe chiffrés (BCrypt), authentification par jeton JWT, accès restreint aux documents sensibles (pièces d'identité, KYC), transmissions sécurisées en production (HTTPS).",
        ],
      },
      {
        heading: 'Alignement RGPD',
        paragraphs: [
          "Le RGPD (règlement européen 2016/679) ne s'applique légalement à Le Paysan que dans des cas précis : utilisateurs résidant dans l'Union Européenne, traitement de données de personnes physiquement présentes dans l'UE, ou implantation future de la société dans l'UE.",
          "Au lancement, la plateforme vise des utilisateurs en Côte d'Ivoire : le texte qui s'applique réellement est donc la loi ivoirienne n° 2013-450 décrite ci-dessus, contrôlée par l'ARTCI.",
          "Le Paysan a néanmoins choisi d'appliquer volontairement, en complément, les standards du RGPD : transparence renforcée, minimisation des données collectées, droit à la portabilité des données (export dans un format structuré), tenue d'un registre interne des traitements, et analyse de risques dédiée pour les traitements les plus sensibles (notamment le KYC vendeur, qui combine pièce d'identité et élément biométrique faible via selfie).",
          "Ce choix vise à offrir un niveau de protection reconnu internationalement, à faciliter une expansion future vers d'autres pays ou vers les utilisateurs de la diaspora, et à anticiper une éventuelle application stricte du RGPD si la plateforme venait à viser explicitement des utilisateurs résidant dans l'UE.",
          "Pour toute personne relevant effectivement du RGPD au sens strict, un droit de réclamation complémentaire existe auprès de l'autorité de protection des données de son pays de résidence dans l'UE, en plus du recours auprès de l'ARTCI.",
        ],
      },
    ],
  },
  cookies: {
    title: 'Politique de cookies',
    sections: [
      {
        heading: 'Cookies strictement nécessaires',
        paragraphs: [
          "Utilisés pour la session et l'authentification (ex. jeton de connexion). Ils sont indispensables au fonctionnement du site et ne nécessitent pas de consentement préalable, conformément aux pratiques standards en la matière.",
        ],
      },
      {
        heading: 'Cookies non essentiels',
        paragraphs: [
          "À ce stade, la plateforme n'utilise aucun cookie de mesure d'audience ou publicitaire. Si de tels cookies étaient ajoutés ultérieurement, un bandeau de consentement vous permettrait de les accepter ou de les refuser avant tout dépôt, avec mémorisation de votre choix.",
        ],
      },
      {
        heading: 'Gestion de vos préférences',
        paragraphs: [
          "Vous pouvez à tout moment configurer votre navigateur pour refuser les cookies non essentiels ou être alerté avant leur dépôt. Le refus des cookies strictement nécessaires peut toutefois empêcher le bon fonctionnement de la connexion au site.",
        ],
      },
    ],
  },
};
