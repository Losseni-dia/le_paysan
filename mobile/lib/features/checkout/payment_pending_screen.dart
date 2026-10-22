import 'package:flutter/material.dart';

import '../../core/order/order.dart';
import '../../core/order/order_service.dart';

const _statusLabels = {
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

/// Affiche apres l'ouverture du paiement Fedapay dans le navigateur externe.
/// Le paiement est confirme cote serveur par le webhook Fedapay (source de
/// verite), independamment de ce que fait l'utilisateur dans son navigateur :
/// cet ecran se contente de rafraichir le statut des commandes, au retour dans
/// l'app (cycle de vie) et sur demande manuelle.
class PaymentPendingScreen extends StatefulWidget {
  final List<Order> orders;

  const PaymentPendingScreen({super.key, required this.orders});

  @override
  State<PaymentPendingScreen> createState() => _PaymentPendingScreenState();
}

class _PaymentPendingScreenState extends State<PaymentPendingScreen>
    with WidgetsBindingObserver {
  final OrderService _orderService = OrderService();
  late List<Order> _orders;
  bool _refreshing = false;

  @override
  void initState() {
    super.initState();
    _orders = widget.orders;
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) _refresh();
  }

  Future<void> _refresh() async {
    setState(() => _refreshing = true);
    try {
      final refreshed = await Future.wait(
        _orders.map((o) => _orderService.getMine(o.id)),
      );
      if (mounted) setState(() => _orders = refreshed);
    } catch (_) {
      // Pas de connexion / erreur temporaire : on garde l'etat precedent, l'utilisateur peut reessayer.
    } finally {
      if (mounted) setState(() => _refreshing = false);
    }
  }

  bool get _allPaidOrBeyond => _orders.every(
    (o) => o.status != 'EN_ATTENTE_PAIEMENT' && o.status != 'ANNULEE',
  );

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Suivi du paiement'),
        automaticallyImplyLeading: false,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          if (_allPaidOrBeyond) ...[
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFFD7ECDF),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Row(
                children: [
                  Icon(Icons.check_circle, color: Color(0xFF1B5E3A)),
                  SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'Paiement confirmé. Suivez votre commande ci-dessous.',
                      style: TextStyle(color: Color(0xFF0B3D1E)),
                    ),
                  ),
                ],
              ),
            ),
          ] else ...[
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFFF3F8F4),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Text(
                "Si vous avez terminé le paiement dans votre navigateur, revenez ici et actualisez : "
                "la confirmation peut prendre quelques instants.",
                style: TextStyle(fontSize: 13, color: Color(0xFF5B6560)),
              ),
            ),
          ],
          const SizedBox(height: 20),
          for (final order in _orders)
            Card(
              child: Padding(
                padding: const EdgeInsets.all(14),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'Commande ${order.orderNumber}',
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                        Text(_statusLabels[order.status] ?? order.status),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      order.shopName,
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.black54,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text('${order.totalAmount.toStringAsFixed(0)} FCFA'),
                  ],
                ),
              ),
            ),
          const SizedBox(height: 20),
          SizedBox(
            height: 52,
            child: OutlinedButton(
              onPressed: _refreshing ? null : _refresh,
              child: _refreshing
                  ? const SizedBox(
                      width: 22,
                      height: 22,
                      child: CircularProgressIndicator(strokeWidth: 2.5),
                    )
                  : const Text('Actualiser le statut'),
            ),
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: 52,
            child: FilledButton(
              onPressed: () =>
                  Navigator.of(context).popUntil((route) => route.isFirst),
              child: const Text('Retour au catalogue'),
            ),
          ),
        ],
      ),
    );
  }
}
