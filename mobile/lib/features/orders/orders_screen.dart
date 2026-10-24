import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../core/order/order.dart';
import '../../core/order/order_service.dart';
import '../../core/order/order_status_labels.dart';
import '../../core/payment/payment_service.dart';
import '../../shared/widgets/notification_bell.dart';
import '../checkout/payment_pending_screen.dart';
import 'order_detail_screen.dart';

const _terminalStatuses = {'LIVREE', 'ANNULEE'};
const _historyVisibilityDays = 3;

class OrdersScreen extends StatefulWidget {
  const OrdersScreen({super.key});

  @override
  State<OrdersScreen> createState() => _OrdersScreenState();
}

class _OrdersScreenState extends State<OrdersScreen> {
  final OrderService _orderService = OrderService();

  List<Order> _orders = [];
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
      final orders = await _orderService.listMine();
      orders.sort((a, b) {
        final da = a.createdAt;
        final db = b.createdAt;
        if (da == null || db == null) return 0;
        return db.compareTo(da);
      });
      setState(() => _orders = orders);
    } catch (_) {
      setState(
        () => _errorMessage = 'Impossible de charger vos commandes.',
      );
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  /// Une commande livree disparait de l'historique 3 jours apres la livraison
  /// (reste archivee cote serveur, simplement plus affichee ici).
  bool _visibleInHistory(Order order) {
    if (order.status == 'ANNULEE') return true;
    if (order.status != 'LIVREE') return false;
    final deliveredAt = order.deliveredAt;
    if (deliveredAt == null) return true;
    return DateTime.now().difference(deliveredAt).inDays < _historyVisibilityDays;
  }

  @override
  Widget build(BuildContext context) {
    final ongoing = _orders.where((o) => !_terminalStatuses.contains(o.status)).toList();
    final history = _orders.where((o) => _terminalStatuses.contains(o.status) && _visibleInHistory(o)).toList();

    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Mes commandes'),
          actions: const [NotificationBell()],
          bottom: const TabBar(
            tabs: [
              Tab(text: 'En cours'),
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
            : TabBarView(
                children: [
                  _buildList(ongoing, "Vous n'avez pas de commande en cours."),
                  _buildList(history, "Aucune commande dans l'historique."),
                ],
              ),
      ),
    );
  }

  Widget _buildList(List<Order> orders, String emptyMessage) {
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
        itemBuilder: (context, index) => _OrderCard(order: orders[index], onChanged: _load),
      ),
    );
  }
}

class _OrderCard extends StatefulWidget {
  final Order order;
  final VoidCallback onChanged;

  const _OrderCard({required this.order, required this.onChanged});

  @override
  State<_OrderCard> createState() => _OrderCardState();
}

class _OrderCardState extends State<_OrderCard> {
  final PaymentService _paymentService = PaymentService();
  bool _paying = false;

  Future<void> _pay() async {
    setState(() => _paying = true);
    try {
      final paymentUrl = await _paymentService.initiate([widget.order.id]);
      final launched = await launchUrl(
        Uri.parse(paymentUrl),
        mode: LaunchMode.externalApplication,
      );
      if (!launched) throw Exception('launch_failed');
      if (mounted) {
        await Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => PaymentPendingScreen(orders: [widget.order]),
          ),
        );
        widget.onChanged();
      }
    } on DioException catch (_) {
      _showError('Impossible de démarrer le paiement, réessayez.');
    } catch (_) {
      _showError("Impossible d'ouvrir la page de paiement.");
    } finally {
      if (mounted) setState(() => _paying = false);
    }
  }

  void _showError(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  Widget build(BuildContext context) {
    final order = widget.order;
    return Card(
      child: InkWell(
        onTap: () async {
          await Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => OrderDetailScreen(orderId: order.id),
            ),
          );
          widget.onChanged();
        },
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Text(
                      'Commande ${order.orderNumber}',
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: const Color(0xFFE1E8E3),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      orderStatusLabel(order.status),
                      style: const TextStyle(fontSize: 12),
                    ),
                  ),
                  const SizedBox(width: 4),
                  const Icon(Icons.chevron_right, color: Colors.black38),
                ],
              ),
              const SizedBox(height: 6),
              Text(
                order.shopName,
                style: const TextStyle(fontSize: 12, color: Colors.black54),
              ),
              const SizedBox(height: 6),
              Text(
                '${order.totalAmount.toStringAsFixed(0)} FCFA',
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF1B5E3A),
                ),
              ),
              if (order.status == 'EN_ATTENTE_PAIEMENT') ...[
                const SizedBox(height: 10),
                SizedBox(
                  height: 40,
                  child: FilledButton(
                    onPressed: _paying ? null : _pay,
                    child: _paying
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                          )
                        : const Text('Payer maintenant'),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
