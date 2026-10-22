import 'package:flutter/material.dart';

import '../../core/livreur/livreur_order.dart';
import '../../core/order/order_status_labels.dart';
import '../../core/vendor/vendor_order_service.dart';
import 'vendor_order_detail_screen.dart';

const _terminalStatuses = {'LIVREE', 'ANNULEE'};

class VendorOrdersScreen extends StatefulWidget {
  const VendorOrdersScreen({super.key});

  @override
  State<VendorOrdersScreen> createState() => _VendorOrdersScreenState();
}

class _VendorOrdersScreenState extends State<VendorOrdersScreen> {
  final VendorOrderService _vendorOrderService = VendorOrderService();
  final TextEditingController _searchController = TextEditingController();

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
      final orders = await _vendorOrderService.listOrders();
      orders.sort((a, b) {
        if (a.createdAt == null || b.createdAt == null) return 0;
        return b.createdAt!.compareTo(a.createdAt!);
      });
      setState(() => _orders = orders);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger les commandes.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final searched = _orders.where((o) => o.matchesSearch(_query)).toList();
    final active = searched
        .where((o) => !_terminalStatuses.contains(o.status) && o.status != 'EN_ATTENTE_PAIEMENT')
        .toList();
    final history = searched.where((o) => _terminalStatuses.contains(o.status)).toList();

    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Mes commandes'),
          bottom: const TabBar(
            tabs: [
              Tab(text: 'À traiter'),
              Tab(text: 'Historique'),
            ],
          ),
        ),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : _errorMessage != null
            ? Center(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Text(_errorMessage!),
                ),
              )
            : Column(
                children: [
                  Padding(
                    padding: const EdgeInsets.all(16),
                    child: TextField(
                      controller: _searchController,
                      decoration: const InputDecoration(
                        hintText: 'Rechercher (n° commande, client, produit)...',
                        prefixIcon: Icon(Icons.search),
                      ),
                      onChanged: (value) => setState(() => _query = value),
                    ),
                  ),
                  Expanded(
                    child: TabBarView(
                      children: [
                        _buildList(active, 'Aucune commande à traiter.'),
                        _buildList(history, 'Aucune commande dans l\'historique.'),
                      ],
                    ),
                  ),
                ],
              ),
      ),
    );
  }

  Widget _buildList(List<LivreurOrder> orders, String emptyMessage) {
    if (orders.isEmpty) {
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 48),
              child: Center(child: Text(emptyMessage)),
            ),
          ],
        ),
      );
    }
    return RefreshIndicator(
      onRefresh: _load,
      child: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: orders.length,
        separatorBuilder: (_, _) => const SizedBox(height: 12),
        itemBuilder: (context, index) {
          final order = orders[index];
          return Card(
            child: InkWell(
              onTap: () async {
                await Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (_) => VendorOrderDetailScreen(orderId: order.id),
                  ),
                );
                _load();
              },
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
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: const Color(0xFFE1E8E3),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(orderStatusLabel(order.status), style: const TextStyle(fontSize: 12)),
                        ),
                      ],
                    ),
                    const SizedBox(height: 6),
                    Text('Client : ${order.buyerFullName}', style: const TextStyle(fontSize: 13)),
                    const SizedBox(height: 4),
                    Text(
                      '${order.totalAmount.toStringAsFixed(0)} FCFA',
                      style: const TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF1B5E3A)),
                    ),
                    if (order.deliveryProof?.changedAt != null) ...[
                      const SizedBox(height: 4),
                      Text(
                        'Livré le ${formatDateTime(order.deliveryProof!.changedAt!)}',
                        style: const TextStyle(fontSize: 12, color: Colors.black54),
                      ),
                    ],
                  ],
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}
