import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../core/livreur/livreur_order.dart';
import '../../core/livreur/livreur_service.dart';
import '../../core/order/order_status_labels.dart';

class LivreurOrderDetailScreen extends StatefulWidget {
  final int orderId;

  const LivreurOrderDetailScreen({super.key, required this.orderId});

  @override
  State<LivreurOrderDetailScreen> createState() => _LivreurOrderDetailScreenState();
}

class _LivreurOrderDetailScreenState extends State<LivreurOrderDetailScreen> {
  final LivreurService _livreurService = LivreurService();
  final ImagePicker _imagePicker = ImagePicker();

  LivreurOrder? _order;
  bool _loading = true;
  bool _actionInProgress = false;
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
      final order = await _livreurService.getOrder(widget.orderId);
      setState(() => _order = order);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger cette commande.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _markPickedUp() async {
    setState(() => _actionInProgress = true);
    try {
      final order = await _livreurService.markPickedUp(widget.orderId);
      setState(() => _order = order);
    } catch (_) {
      _showError('Impossible de marquer ce colis comme récupéré.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<void> _markEnRoute() async {
    setState(() => _actionInProgress = true);
    try {
      final order = await _livreurService.markEnRoute(widget.orderId);
      setState(() => _order = order);
    } catch (_) {
      _showError('Impossible de marquer ce colis en route.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<void> _confirmDelivery() async {
    final photo = await _imagePicker.pickImage(source: ImageSource.camera);
    if (photo == null) return;
    setState(() => _actionInProgress = true);
    try {
      final order = await _livreurService.confirmDelivery(widget.orderId, photo);
      setState(() => _order = order);
    } catch (_) {
      _showError('Impossible de confirmer la remise.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  void _showError(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
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
            Text(order.shopName, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
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
        Text('Client : ${order.buyerFullName}', style: const TextStyle(fontSize: 14)),
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
                if (item.deliveryAddress != null && item.deliveryAddress!.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.only(top: 2),
                    child: Text(
                      'Adresse : ${item.deliveryAddress}',
                      style: const TextStyle(fontSize: 12, color: Colors.black54),
                    ),
                  ),
              ],
            ),
          ),
        const Divider(height: 24),
        Text(
          'Total : ${order.totalAmount.toStringAsFixed(0)} FCFA',
          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
        const SizedBox(height: 24),
        _buildActionButton(order.status),
      ],
    );
  }

  Widget _buildActionButton(String status) {
    String? label;
    VoidCallback? action;
    if (status == 'EXPEDIEE') {
      label = 'Marquer comme récupéré';
      action = _markPickedUp;
    } else if (status == 'RECUPEREE') {
      label = 'Marquer en route';
      action = _markEnRoute;
    } else if (status == 'EN_ROUTE') {
      label = 'Confirmer la remise (photo)';
      action = _confirmDelivery;
    }

    if (label == null) {
      return const SizedBox.shrink();
    }

    return SizedBox(
      height: 52,
      child: FilledButton(
        onPressed: _actionInProgress ? null : action,
        child: _actionInProgress
            ? const SizedBox(
                width: 22,
                height: 22,
                child: CircularProgressIndicator(strokeWidth: 2.5, color: Colors.white),
              )
            : Text(label),
      ),
    );
  }
}
