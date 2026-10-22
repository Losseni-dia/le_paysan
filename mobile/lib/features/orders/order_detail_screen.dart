import 'package:flutter/material.dart';

import '../../core/order/order.dart';
import '../../core/order/order_service.dart';
import '../../core/order/order_status_labels.dart';

const _confirmableStatuses = {'PRETE', 'EXPEDIEE', 'RECUPEREE', 'EN_ROUTE'};

class OrderDetailScreen extends StatefulWidget {
  final int orderId;

  const OrderDetailScreen({super.key, required this.orderId});

  @override
  State<OrderDetailScreen> createState() => _OrderDetailScreenState();
}

class _OrderDetailScreenState extends State<OrderDetailScreen> {
  final OrderService _orderService = OrderService();

  Order? _order;
  bool _loading = true;
  bool _confirming = false;
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
      final order = await _orderService.getMine(widget.orderId);
      setState(() => _order = order);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger cette commande.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _confirmReception() async {
    setState(() => _confirming = true);
    try {
      final order = await _orderService.confirmReception(widget.orderId);
      setState(() => _order = order);
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Impossible de confirmer la réception.'),
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _confirming = false);
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
            Text(
              order.shopName,
              style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16),
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
        const Divider(height: 24),
        for (final item in order.items)
          Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        item.productName,
                        style: const TextStyle(fontWeight: FontWeight.w600),
                      ),
                      Text(
                        '${item.quantity} x ${item.unitPrice.toStringAsFixed(0)} FCFA',
                        style: const TextStyle(fontSize: 12, color: Colors.black54),
                      ),
                    ],
                  ),
                ),
                Text('${item.lineSubtotal.toStringAsFixed(0)} FCFA'),
              ],
            ),
          ),
        const Divider(height: 24),
        _summaryRow('Sous-total', order.subtotalAmount),
        _summaryRow('Livraison', order.deliveryAmount),
        const Divider(height: 20),
        _summaryRow('Total', order.totalAmount, bold: true),
        if (_confirmableStatuses.contains(order.status)) ...[
          const SizedBox(height: 24),
          SizedBox(
            height: 52,
            child: FilledButton(
              onPressed: _confirming ? null : _confirmReception,
              child: _confirming
                  ? const SizedBox(
                      width: 22,
                      height: 22,
                      child: CircularProgressIndicator(
                        strokeWidth: 2.5,
                        color: Colors.white,
                      ),
                    )
                  : const Text('Confirmer la réception'),
            ),
          ),
        ],
      ],
    );
  }

  Widget _summaryRow(String label, double amount, {bool bold = false}) {
    final style = TextStyle(
      fontSize: bold ? 16 : 14,
      fontWeight: bold ? FontWeight.bold : FontWeight.normal,
    );
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: style),
          Text('${amount.toStringAsFixed(0)} FCFA', style: style),
        ],
      ),
    );
  }
}
