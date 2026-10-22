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

String formatDateTime(DateTime dateTime) {
  final local = dateTime.toLocal();
  String pad(int n) => n.toString().padLeft(2, '0');
  return '${pad(local.day)}/${pad(local.month)}/${local.year} à ${pad(local.hour)}:${pad(local.minute)}';
}
