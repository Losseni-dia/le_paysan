import 'package:flutter/material.dart';

import '../../core/admin/admin_dashboard.dart';
import '../../core/admin/admin_dashboard_service.dart';
import '../../core/order/order_status_labels.dart';

class AdminDashboardScreen extends StatefulWidget {
  const AdminDashboardScreen({super.key});

  @override
  State<AdminDashboardScreen> createState() => _AdminDashboardScreenState();
}

class _AdminDashboardScreenState extends State<AdminDashboardScreen> {
  final AdminDashboardService _adminDashboardService = AdminDashboardService();

  AdminDashboard? _dashboard;
  AdminStats? _stats;
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
      final dashboard = await _adminDashboardService.getDashboard();
      final stats = await _adminDashboardService.getStats();
      setState(() {
        _dashboard = dashboard;
        _stats = stats;
      });
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger le tableau de bord.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Tableau de bord')),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_errorMessage != null || _dashboard == null || _stats == null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Text(_errorMessage ?? 'Une erreur est survenue.'),
        ),
      );
    }

    final dashboard = _dashboard!;
    final stats = _stats!;

    return RefreshIndicator(
      onRefresh: _load,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          GridView.count(
            crossAxisCount: 2,
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            mainAxisSpacing: 12,
            crossAxisSpacing: 12,
            childAspectRatio: 1.6,
            children: [
              _KpiCard(value: '${dashboard.usersTotal}', label: 'Utilisateurs'),
              _KpiCard(value: '${dashboard.shopsValidated}', label: 'Boutiques actives'),
              _KpiCard(value: '${dashboard.shopsPending}', label: 'Boutiques en attente'),
              _KpiCard(value: '${dashboard.productsPending}', label: 'Produits en attente'),
              _KpiCard(value: '${dashboard.ordersPendingPayment}', label: 'Commandes impayées'),
              _KpiCard(value: '${dashboard.payoutsFailed}', label: 'Reversements en échec'),
            ],
          ),
          const SizedBox(height: 24),
          const Text('Statistiques', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _statRow("Chiffre d'affaires total", '${stats.totalRevenue.toStringAsFixed(0)} FCFA'),
                  _statRow('Commission totale', '${stats.totalCommission.toStringAsFixed(0)} FCFA'),
                  _statRow('Boutiques actives', '${stats.activeShops}'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Text('Commandes par statut', style: TextStyle(fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: stats.ordersByStatus.entries
                    .map((e) => _statRow(orderStatusLabel(e.key), '${e.value}'))
                    .toList(),
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Text('Top boutiques', style: TextStyle(fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          if (stats.topShops.isEmpty)
            const Text('Aucune donnée.', style: TextStyle(fontSize: 13, color: Colors.black54))
          else
            Card(
              child: Column(
                children: stats.topShops
                    .map(
                      (s) => ListTile(
                        title: Text(s.shopName),
                        trailing: Text(
                          '${s.revenue.toStringAsFixed(0)} FCFA',
                          style: const TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF1B5E3A)),
                        ),
                      ),
                    )
                    .toList(),
              ),
            ),
        ],
      ),
    );
  }

  Widget _statRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(fontSize: 13, color: Colors.black54)),
          Text(value, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
        ],
      ),
    );
  }
}

class _KpiCard extends StatelessWidget {
  final String value;
  final String label;

  const _KpiCard({required this.value, required this.label});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              value,
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFF1B5E3A)),
            ),
            const SizedBox(height: 4),
            Text(label, style: const TextStyle(fontSize: 12, color: Colors.black54)),
          ],
        ),
      ),
    );
  }
}
