import 'package:flutter/material.dart';

import '../../core/livreur/livreur_order.dart';
import '../../core/livreur/livreur_profile.dart';
import '../../core/livreur/livreur_service.dart';
import '../../core/order/order_status_labels.dart';
import 'livreur_order_detail_screen.dart';

class LivreurHomeScreen extends StatefulWidget {
  const LivreurHomeScreen({super.key});

  @override
  State<LivreurHomeScreen> createState() => _LivreurHomeScreenState();
}

class _LivreurHomeScreenState extends State<LivreurHomeScreen> {
  final LivreurService _livreurService = LivreurService();

  LivreurProfile? _profile;
  bool _loadingProfile = true;
  bool _togglingAvailability = false;
  String? _profileError;

  @override
  void initState() {
    super.initState();
    _loadProfile();
  }

  Future<void> _loadProfile() async {
    setState(() {
      _loadingProfile = true;
      _profileError = null;
    });
    try {
      final profile = await _livreurService.getSelf();
      setState(() => _profile = profile);
    } catch (_) {
      setState(() => _profileError = 'Impossible de charger votre espace livreur.');
    } finally {
      if (mounted) setState(() => _loadingProfile = false);
    }
  }

  Future<void> _toggleAvailability(bool value) async {
    setState(() => _togglingAvailability = true);
    try {
      final profile = await _livreurService.setAvailability(value);
      setState(() => _profile = profile);
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Impossible de mettre à jour votre disponibilité.')),
        );
      }
    } finally {
      if (mounted) setState(() => _togglingAvailability = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Espace livreur'),
          bottom: const TabBar(
            tabs: [
              Tab(text: 'Mes livraisons'),
              Tab(text: 'Historique'),
            ],
          ),
        ),
        body: Column(
          children: [
            _buildProfileHeader(),
            const Expanded(
              child: TabBarView(
                children: [
                  _LivreurOrdersTab(status: null),
                  _LivreurOrdersTab(status: 'LIVREE'),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildProfileHeader() {
    if (_loadingProfile) {
      return const Padding(
        padding: EdgeInsets.all(16),
        child: LinearProgressIndicator(),
      );
    }
    if (_profileError != null || _profile == null) {
      return Padding(
        padding: const EdgeInsets.all(16),
        child: Text(_profileError ?? 'Une erreur est survenue.'),
      );
    }

    final profile = _profile!;
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          if (profile.kycStatus != 'VALIDE') ...[
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: const Color(0xFFFFF3E0),
                borderRadius: BorderRadius.circular(12),
              ),
              width: double.infinity,
              child: Text(
                profile.kycStatus == 'REJETE'
                    ? "Votre dossier n'a pas été validé. Contactez le support."
                    : 'Votre dossier est en cours de vérification.',
                style: const TextStyle(color: Color(0xFF8A5A00)),
              ),
            ),
            const SizedBox(height: 12),
          ],
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Disponible pour des livraisons',
                        style: TextStyle(fontWeight: FontWeight.w600),
                      ),
                      Switch(
                        value: profile.available,
                        onChanged: _togglingAvailability ? null : _toggleAvailability,
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '${profile.deliveryCount} livraison(s) effectuée(s)',
                    style: const TextStyle(fontSize: 13, color: Colors.black54),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _LivreurOrdersTab extends StatefulWidget {
  final String? status;

  const _LivreurOrdersTab({required this.status});

  @override
  State<_LivreurOrdersTab> createState() => _LivreurOrdersTabState();
}

class _LivreurOrdersTabState extends State<_LivreurOrdersTab> {
  final LivreurService _livreurService = LivreurService();

  List<LivreurOrder> _orders = [];
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
      final orders = await _livreurService.listOrders(status: widget.status);
      setState(() => _orders = orders);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger les livraisons.');
    } finally {
      if (mounted) setState(() => _loading = false);
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
    if (_orders.isEmpty) {
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          children: const [
            Padding(
              padding: EdgeInsets.symmetric(vertical: 48),
              child: Center(child: Text('Aucune livraison pour le moment.')),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _load,
      child: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: _orders.length,
        separatorBuilder: (_, _) => const SizedBox(height: 12),
        itemBuilder: (context, index) {
          final order = _orders[index];
          return _LivreurOrderCard(
            order: order,
            onTap: () async {
              await Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => LivreurOrderDetailScreen(orderId: order.id),
                ),
              );
              _load();
            },
          );
        },
      ),
    );
  }
}

class _LivreurOrderCard extends StatelessWidget {
  final LivreurOrder order;
  final VoidCallback onTap;

  const _LivreurOrderCard({required this.order, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        onTap: onTap,
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
              Text(order.shopName, style: const TextStyle(fontSize: 12, color: Colors.black54)),
              const SizedBox(height: 4),
              Text('Client : ${order.buyerFullName}', style: const TextStyle(fontSize: 13)),
            ],
          ),
        ),
      ),
    );
  }
}
