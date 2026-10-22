const orderStatusLabels = {
  'EN_ATTENTE_PAIEMENT': 'En attente de paiement',
  'PAYEE': 'Payée',
  'EN_PREPARATION': 'En préparation',
  'PRETE': 'Prête',
  'EXPEDIEE': 'Expédiée',
  'RECUPEREE': 'Récupérée',
  'EN_ROUTE': 'En route',
  'LIVREE': 'Livrée',
  'ANNULEE': 'Annulée',
};

String orderStatusLabel(String status) => orderStatusLabels[status] ?? status;
