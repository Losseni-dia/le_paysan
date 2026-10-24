import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

import '../../core/delivery/delivery_request.dart';
import '../../core/delivery/delivery_request_service.dart';
import '../../core/order/order_status_labels.dart';

class LivreurDemandesScreen extends StatefulWidget {
  const LivreurDemandesScreen({super.key});

  @override
  State<LivreurDemandesScreen> createState() => _LivreurDemandesScreenState();
}

class _LivreurDemandesScreenState extends State<LivreurDemandesScreen> {
  final DeliveryRequestService _service = DeliveryRequestService();

  List<DeliveryRequest> _requests = [];
  bool _loading = true;
  String? _errorMessage;
  int? _acceptingId;

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
      final requests = await _service.listForLivreur();
      setState(() => _requests = requests);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger les demandes.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _accept(DeliveryRequest request) async {
    setState(() => _acceptingId = request.id);
    try {
      await _service.accept(request.id);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Livraison acceptée.')),
        );
      }
      await _load();
    } on DioException catch (e) {
      final message = e.response?.statusCode == 409
          ? 'Cette commande a déjà été prise par un autre livreur.'
          : "Impossible d'accepter cette demande.";
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
      }
      await _load();
    } finally {
      if (mounted) setState(() => _acceptingId = null);
    }
  }

  @override
  Widget build(BuildContext context) {
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
    if (_requests.isEmpty) {
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          children: const [
            Padding(
              padding: EdgeInsets.symmetric(vertical: 48),
              child: Center(child: Text('Aucune demande de livraison pour le moment.')),
            ),
          ],
        ),
      );
    }
    return RefreshIndicator(
      onRefresh: _load,
      child: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: _requests.length,
        separatorBuilder: (_, _) => const SizedBox(height: 12),
        itemBuilder: (context, index) {
          final request = _requests[index];
          return _DemandeCard(
            request: request,
            busy: _acceptingId == request.id,
            onAccept: () => _accept(request),
          );
        },
      ),
    );
  }
}

class _DemandeCard extends StatelessWidget {
  final DeliveryRequest request;
  final bool busy;
  final VoidCallback onAccept;

  const _DemandeCard({required this.request, required this.busy, required this.onAccept});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Commande ${request.orderNumber}', style: const TextStyle(fontWeight: FontWeight.bold)),
                Text(
                  '${request.totalAmount.toStringAsFixed(0)} FCFA',
                  style: const TextStyle(fontWeight: FontWeight.w600),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(request.shopName, style: const TextStyle(fontSize: 12, color: Colors.black54)),
            const SizedBox(height: 10),
            _infoLine(
              Icons.store_outlined,
              'Récupérer',
              [request.pickupCity, request.pickupAddress].whereType<String>().join(' — '),
            ),
            const SizedBox(height: 6),
            _infoLine(
              Icons.pin_drop_outlined,
              'Livrer à ${request.buyerFullName}',
              request.deliveryAddress ?? '—',
            ),
            if (request.createdAt != null) ...[
              const SizedBox(height: 6),
              Text(
                'Reçue le ${formatDateTime(request.createdAt!)}',
                style: const TextStyle(fontSize: 11, color: Colors.black54),
              ),
            ],
            const SizedBox(height: 12),
            SizedBox(
              height: 44,
              child: FilledButton(
                onPressed: busy ? null : onAccept,
                child: busy
                    ? const SizedBox(
                        width: 18,
                        height: 18,
                        child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                      )
                    : const Text('Accepter'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _infoLine(IconData icon, String title, String subtitle) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, size: 18, color: Colors.black54),
        const SizedBox(width: 8),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
              if (subtitle.isNotEmpty)
                Text(subtitle, style: const TextStyle(fontSize: 12, color: Colors.black54)),
            ],
          ),
        ),
      ],
    );
  }
}
