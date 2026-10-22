import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../core/cart/cart.dart';
import '../../core/cart/cart_service.dart';
import '../../core/order/order_service.dart';
import '../../core/payment/payment_service.dart';
import 'payment_pending_screen.dart';

/// Recapitulatif avant paiement : transforme le panier en commande(s) (une par
/// boutique si le panier contient des produits de plusieurs boutiques), puis
/// ouvre le paiement Fedapay dans le navigateur externe.
class CheckoutScreen extends StatefulWidget {
  final Cart cart;

  const CheckoutScreen({super.key, required this.cart});

  @override
  State<CheckoutScreen> createState() => _CheckoutScreenState();
}

class _CheckoutScreenState extends State<CheckoutScreen> {
  final OrderService _orderService = OrderService();
  final PaymentService _paymentService = PaymentService();

  bool _submitting = false;
  String? _errorMessage;

  Future<void> _confirm() async {
    setState(() {
      _submitting = true;
      _errorMessage = null;
    });

    try {
      final orders = await _orderService.checkout();
      if (mounted) context.read<CartService>().clear();

      final paymentUrl = await _paymentService.initiate(
        orders.map((o) => o.id).toList(),
      );
      final launched = await launchUrl(
        Uri.parse(paymentUrl),
        mode: LaunchMode.externalApplication,
      );
      if (!launched) throw Exception('launch_failed');

      if (mounted) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(
            builder: (_) => PaymentPendingScreen(orders: orders),
          ),
        );
      }
    } on DioException catch (e) {
      setState(() {
        _errorMessage = (e.response?.data is Map)
            ? e.response?.data['message'] as String?
            : null;
        _errorMessage ??= 'Une erreur est survenue, veuillez réessayer.';
      });
    } catch (_) {
      setState(
        () => _errorMessage = "Impossible d'ouvrir la page de paiement.",
      );
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final cart = widget.cart;

    return Scaffold(
      appBar: AppBar(title: const Text('Valider la commande')),
      body: Column(
        children: [
          Expanded(
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                for (final item in cart.items)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: Row(
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                item.productName,
                                style: const TextStyle(
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              Text(
                                '${item.quantity} x ${item.unitPrice.toStringAsFixed(0)} FCFA — ${item.shopName}',
                                style: const TextStyle(
                                  fontSize: 12,
                                  color: Colors.black54,
                                ),
                              ),
                            ],
                          ),
                        ),
                        Text('${item.lineSubtotal.toStringAsFixed(0)} FCFA'),
                      ],
                    ),
                  ),
                const Divider(height: 32),
                _summaryRow('Sous-total', cart.subtotalAmount),
                _summaryRow('Livraison', cart.deliveryAmount),
                const Divider(height: 20),
                _summaryRow('Total', cart.totalAmount, bold: true),
                const SizedBox(height: 16),
                const Text(
                  'Le paiement se fait par Mobile Money via Fedapay, dans votre navigateur. '
                  'Revenez ensuite dans l\'application pour suivre votre commande.',
                  style: TextStyle(fontSize: 13, color: Colors.black54),
                ),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.all(16),
            decoration: const BoxDecoration(
              color: Colors.white,
              border: Border(top: BorderSide(color: Color(0xFFE1E8E3))),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                if (_errorMessage != null) ...[
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: const Color(0xFFFBE9E7),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      _errorMessage!,
                      style: const TextStyle(color: Color(0xFFB3261E)),
                    ),
                  ),
                  const SizedBox(height: 12),
                ],
                SizedBox(
                  height: 52,
                  child: FilledButton(
                    onPressed: _submitting ? null : _confirm,
                    child: _submitting
                        ? const SizedBox(
                            width: 22,
                            height: 22,
                            child: CircularProgressIndicator(
                              strokeWidth: 2.5,
                              color: Colors.white,
                            ),
                          )
                        : Text(
                            'Payer ${cart.totalAmount.toStringAsFixed(0)} FCFA',
                            style: const TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                  ),
                ),
              ],
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
