import 'package:flutter/material.dart';

import '../../core/admin/admin_order_service.dart';
import '../../core/livreur/livreur_order.dart';
import '../../core/order/order_status_labels.dart';
import 'admin_order_detail_screen.dart';

const _orderStatusFilters = [
  {'value': 'PAYEE', 'label': 'Payées'},
  {'value': 'EN_ATTENTE_PAIEMENT', 'label': 'En attente de paiement'},
  {'value': 'EN_PREPARATION', 'label': 'En préparation'},
  {'value': 'PRETE', 'label': 'Prêtes'},
  {'value': 'EXPEDIEE', 'label': 'Expédiées'},
  {'value': 'LIVREE', 'label': 'Livrées'},
  {'value': 'ANNULEE', 'label': 'Annulées'},
  {'value': null, 'label': 'Toutes'},
];

class AdminOrdersScreen extends StatefulWidget {
  const AdminOrdersScreen({super.key});

  @override
  State<AdminOrdersScreen> createState() => _AdminOrdersScreenState();
}

class _AdminOrdersScreenState extends State<AdminOrdersScreen> {
  final AdminOrderService _adminOrderService = AdminOrderService();
  final TextEditingController _searchController = TextEditingController();

  String? _status = 'PAYEE';
  List<LivreurOrder> _orders = [];
  bool _loading = true;
  String? _errorMessage;
  String _query = '';

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _errorMessage = null;
    });
    try {
      final orders = await _adminOrderService.listForAdmin(_status);
      setState(() => _orders = orders);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger les commandes.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Commandes')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
            child: Wrap(
              spacing: 8,
              children: _orderStatusFilters.map((f) {
                final selected = _status == f['value'];
                return ChoiceChip(
                  label: Text(f['label'] as String),
                  selected: selected,
                  onSelected: (_) {
                    setState(() => _status = f['value']);
                    _load();
                  },
                );
              }).toList(),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: TextField(
              controller: _searchController,
              decoration: const InputDecoration(
                hintText: 'Rechercher (n° commande, acheteur, produit)...',
                prefixIcon: Icon(Icons.search),
              ),
              onChanged: (value) => setState(() => _query = value),
            ),
          ),
          Expanded(child: _buildBody()),
        ],
      ),
    );
  }

  Widget _buildBody() {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_errorMessage != null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Text(_errorMessage!),
        ),
      );
    }

    final filtered = _orders.where((o) => o.matchesSearch(_query)).toList();

    if (_orders.isEmpty || filtered.isEmpty) {
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 48),
              child: Center(
                child: Text(
                  _orders.isEmpty ? 'Aucune commande avec ce statut.' : 'Aucun résultat pour cette recherche.',
                ),
              ),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _load,
      child: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: filtered.length,
        separatorBuilder: (_, _) => const SizedBox(height: 12),
        itemBuilder: (context, index) {
          final order = filtered[index];
          return Card(
            child: ListTile(
              title: Text(
                'Commande ${order.orderNumber}',
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
              subtitle: Text('${order.shopName} — ${order.buyerFullName}'),
              trailing: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Text(orderStatusLabel(order.status), style: const TextStyle(fontSize: 12)),
                      Text(
                        '${order.totalAmount.toStringAsFixed(0)} FCFA',
                        style: const TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF1B5E3A)),
                      ),
                    ],
                  ),
                  const SizedBox(width: 4),
                  const Icon(Icons.chevron_right, color: Colors.black38),
                ],
              ),
              onTap: () => Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => AdminOrderDetailScreen(orderId: order.id),
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}
