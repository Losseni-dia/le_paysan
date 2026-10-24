import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/cart/cart.dart';
import '../../core/cart/cart_service.dart';
import '../../core/product/product_service.dart';
import '../../shared/widgets/notification_bell.dart';
import '../checkout/checkout_screen.dart';

class CartScreen extends StatefulWidget {
  const CartScreen({super.key});

  @override
  State<CartScreen> createState() => _CartScreenState();
}

class _CartScreenState extends State<CartScreen> {
  bool _loading = true;
  String? _errorMessage;
  final Set<int> _busyProductIds = {};

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
      await context.read<CartService>().fetch();
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger le panier.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _updateQuantity(CartItem item, int quantity) async {
    if (quantity < 1) return;
    setState(() => _busyProductIds.add(item.productId));
    try {
      await context.read<CartService>().updateItem(
        productId: item.productId,
        quantity: quantity,
        deliveryMode: item.deliveryMode,
        deliveryAddress: item.deliveryAddress,
        deliveryCityName: item.deliveryCityName,
      );
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Impossible de mettre à jour la quantité.'),
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _busyProductIds.remove(item.productId));
    }
  }

  Future<void> _removeItem(CartItem item) async {
    setState(() => _busyProductIds.add(item.productId));
    try {
      await context.read<CartService>().removeItem(item.productId);
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Impossible de retirer ce produit.')),
        );
      }
    } finally {
      if (mounted) setState(() => _busyProductIds.remove(item.productId));
    }
  }

  @override
  Widget build(BuildContext context) {
    final cart = context.watch<CartService>().cart;

    return Scaffold(
      appBar: AppBar(title: const Text('Mon panier'), actions: const [NotificationBell()]),
      body: _buildBody(cart),
    );
  }

  Widget _buildBody(Cart? cart) {
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
    if (cart == null || cart.items.isEmpty) {
      return const Center(child: Text('Votre panier est vide.'));
    }

    return Column(
      children: [
        Expanded(
          child: RefreshIndicator(
            onRefresh: _load,
            child: ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: cart.items.length,
              separatorBuilder: (_, _) => const SizedBox(height: 12),
              itemBuilder: (context, index) => _CartItemCard(
                item: cart.items[index],
                busy: _busyProductIds.contains(cart.items[index].productId),
                onQuantityChanged: (q) => _updateQuantity(cart.items[index], q),
                onRemove: () => _removeItem(cart.items[index]),
              ),
            ),
          ),
        ),
        _buildSummary(cart),
      ],
    );
  }

  Widget _buildSummary(Cart cart) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: const BoxDecoration(
        color: Colors.white,
        border: Border(top: BorderSide(color: Color(0xFFE1E8E3))),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          _summaryRow('Sous-total', cart.subtotalAmount),
          _summaryRow('Livraison', cart.deliveryAmount),
          const Divider(height: 20),
          _summaryRow('Total', cart.totalAmount, bold: true),
          const SizedBox(height: 12),
          SizedBox(
            height: 52,
            child: FilledButton(
              onPressed: cart.items.every((i) => i.available)
                  ? () => Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (_) => CheckoutScreen(cart: cart),
                      ),
                    )
                  : null,
              child: const Text(
                'Commander',
                style: TextStyle(fontWeight: FontWeight.w600),
              ),
            ),
          ),
        ],
      ),
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

class _CartItemCard extends StatelessWidget {
  final CartItem item;
  final bool busy;
  final ValueChanged<int> onQuantityChanged;
  final VoidCallback onRemove;

  const _CartItemCard({
    required this.item,
    required this.busy,
    required this.onQuantityChanged,
    required this.onRemove,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: SizedBox(
                    width: 64,
                    height: 64,
                    child: item.imageUrl != null
                        ? Image.network(
                            ProductService.imageUrl(item.imageUrl!),
                            fit: BoxFit.cover,
                            errorBuilder: (_, _, _) =>
                                const ColoredBox(color: Color(0xFFE1E8E3)),
                          )
                        : const ColoredBox(color: Color(0xFFE1E8E3)),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        item.productName,
                        style: const TextStyle(
                          fontWeight: FontWeight.w600,
                          fontSize: 14,
                        ),
                      ),
                      Text(
                        item.shopName,
                        style: const TextStyle(
                          fontSize: 12,
                          color: Colors.black54,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '${item.unitPrice.toStringAsFixed(0)} FCFA',
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          color: Color(0xFF1B5E3A),
                        ),
                      ),
                    ],
                  ),
                ),
                IconButton(
                  icon: const Icon(
                    Icons.delete_outline,
                    color: Color(0xFFB3261E),
                  ),
                  onPressed: busy ? null : onRemove,
                ),
              ],
            ),
            if (!item.available) ...[
              const SizedBox(height: 8),
              Text(
                item.deliveryOutOfRange
                    ? "La livraison à domicile n'est pas disponible pour votre ville."
                    : 'Ce produit n\'est plus disponible.',
                style: const TextStyle(color: Color(0xFFB3261E), fontSize: 12),
              ),
            ],
            const SizedBox(height: 8),
            Row(
              children: [
                Text(
                  item.deliveryMode == 'RETRAIT_BOUTIQUE'
                      ? 'Retrait en boutique'
                      : 'Livraison à domicile',
                  style: const TextStyle(fontSize: 12, color: Colors.black54),
                ),
                const Spacer(),
                IconButton(
                  icon: const Icon(Icons.remove_circle_outline, size: 20),
                  onPressed: busy || item.quantity <= 1
                      ? null
                      : () => onQuantityChanged(item.quantity - 1),
                ),
                Text('${item.quantity}'),
                IconButton(
                  icon: const Icon(Icons.add_circle_outline, size: 20),
                  onPressed: busy
                      ? null
                      : () => onQuantityChanged(item.quantity + 1),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
