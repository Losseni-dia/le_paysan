import 'package:flutter/material.dart';

import '../../core/admin/admin_order_service.dart';
import '../../core/livreur/livreur_order.dart';
import '../../core/order/order_status_labels.dart';
import '../../core/product/product_service.dart';
import '../../shared/widgets/authenticated_image.dart';

class AdminOrderDetailScreen extends StatefulWidget {
  final int orderId;

  const AdminOrderDetailScreen({super.key, required this.orderId});

  @override
  State<AdminOrderDetailScreen> createState() => _AdminOrderDetailScreenState();
}

class _AdminOrderDetailScreenState extends State<AdminOrderDetailScreen> {
  final AdminOrderService _adminOrderService = AdminOrderService();

  LivreurOrder? _order;
  bool _loading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _errorMessage = null;
    });
    try {
      final order = await _adminOrderService.getDetail(widget.orderId);
      setState(() => _order = order);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger cette commande.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_order != null ? 'Commande ${_order!.orderNumber}' : 'Commande'),
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_errorMessage != null || _order == null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Text(_errorMessage ?? 'Une erreur est survenue.'),
        ),
      );
    }

    final order = _order!;
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Expanded(
              child: Text(order.shopName, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: const Color(0xFFE1E8E3),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(orderStatusLabel(order.status)),
            ),
          ],
        ),
        const SizedBox(height: 8),
        Text('Acheteur : ${order.buyerFullName}', style: const TextStyle(fontSize: 14)),
        if (order.assignedLivreurName != null) ...[
          const SizedBox(height: 4),
          Text('Livreur assigné : ${order.assignedLivreurName}', style: const TextStyle(fontSize: 14)),
        ],
        const Divider(height: 24),
        const Text('Articles', style: TextStyle(fontWeight: FontWeight.w600)),
        const SizedBox(height: 8),
        for (final item in order.items)
          Padding(
            padding: const EdgeInsets.only(bottom: 10),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('${item.quantity} x ${item.productName}'),
                Text(
                  item.deliveryMode == 'RETRAIT_BOUTIQUE'
                      ? 'Retrait en boutique'
                      : 'Livraison à domicile${item.deliveryAddress != null ? ' - ${item.deliveryAddress}' : ''}',
                  style: const TextStyle(fontSize: 12, color: Colors.black54),
                ),
              ],
            ),
          ),
        const Divider(height: 24),
        Text(
          'Total : ${order.totalAmount.toStringAsFixed(0)} FCFA',
          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
        if (order.historique.isNotEmpty) ...[
          const Divider(height: 24),
          const Text('Historique', style: TextStyle(fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          for (final entry in order.historique)
            Padding(
              padding: const EdgeInsets.only(bottom: 6),
              child: Row(
                children: [
                  Expanded(
                    child: Text(
                      orderStatusLabel(entry.status),
                      style: const TextStyle(fontSize: 13),
                    ),
                  ),
                  if (entry.changedAt != null)
                    Text(
                      formatDateTime(entry.changedAt!),
                      style: const TextStyle(fontSize: 12, color: Colors.black54),
                    ),
                ],
              ),
            ),
        ],
        if (order.deliveryProof != null) ..._buildProofSection(order.deliveryProof!),
      ],
    );
  }

  List<Widget> _buildProofSection(OrderStatusHistoryEntry proof) {
    return [
      const Divider(height: 24),
      Text(
        proof.proofType == 'SIGNATURE' ? 'Signature de remise' : 'Photo de remise',
        style: const TextStyle(fontWeight: FontWeight.w600),
      ),
      const SizedBox(height: 8),
      ClipRRect(
        borderRadius: BorderRadius.circular(12),
        child: AuthenticatedImage(
          url: ProductService.imageUrl(proof.proofUrl!),
          height: 200,
          fit: BoxFit.contain,
        ),
      ),
    ];
  }
}
