import 'package:flutter/material.dart';

import '../../core/admin/admin_payout.dart';
import '../../core/admin/admin_payout_service.dart';

const _payoutStatusFilters = [
  {'value': 'EN_ATTENTE', 'label': 'En attente'},
  {'value': 'ENVOYE', 'label': 'Envoyés'},
  {'value': 'ECHEC', 'label': 'Échecs'},
];

class AdminPayoutsScreen extends StatefulWidget {
  const AdminPayoutsScreen({super.key});

  @override
  State<AdminPayoutsScreen> createState() => _AdminPayoutsScreenState();
}

class _AdminPayoutsScreenState extends State<AdminPayoutsScreen> {
  final AdminPayoutService _adminPayoutService = AdminPayoutService();
  final TextEditingController _searchController = TextEditingController();

  String _status = 'EN_ATTENTE';
  List<AdminPayout> _payouts = [];
  bool _loading = true;
  String? _errorMessage;
  String _query = '';
  int? _retryingId;

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
      final payouts = await _adminPayoutService.listForAdmin(_status);
      setState(() => _payouts = payouts);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger les reversements.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _retry(AdminPayout payout) async {
    setState(() => _retryingId = payout.id);
    try {
      await _adminPayoutService.retry(payout.id);
      await _load();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Impossible de relancer ce reversement.')),
        );
      }
    } finally {
      if (mounted) setState(() => _retryingId = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Reversements')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
            child: Wrap(
              spacing: 8,
              children: _payoutStatusFilters.map((f) {
                final selected = _status == f['value'];
                return ChoiceChip(
                  label: Text(f['label']!),
                  selected: selected,
                  onSelected: (_) {
                    setState(() => _status = f['value']!);
                    _load();
                  },
                );
              }).toList(),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: TextField(
              controller: _searchController,
              decoration: const InputDecoration(
                hintText: 'Rechercher (n° commande, boutique)...',
                prefixIcon: Icon(Icons.search),
              ),
              onChanged: (value) => setState(() => _query = value),
            ),
          ),
          Expanded(child: _buildBody()),
        ],
      ),
    );
  }

  Widget _buildBody() {
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

    final filtered = _payouts.where((p) => p.matchesSearch(_query)).toList();

    if (_payouts.isEmpty || filtered.isEmpty) {
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 48),
              child: Center(
                child: Text(
                  _payouts.isEmpty ? 'Aucun reversement avec ce statut.' : 'Aucun résultat pour cette recherche.',
                ),
              ),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _load,
      child: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: filtered.length,
        separatorBuilder: (_, _) => const SizedBox(height: 12),
        itemBuilder: (context, index) {
          final payout = filtered[index];
          final busy = _retryingId == payout.id;
          return Card(
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
                          'Commande ${payout.orderNumber}',
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                      ),
                      Text(
                        '${payout.amount.toStringAsFixed(0)} FCFA',
                        style: const TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF1B5E3A)),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(payout.shopName, style: const TextStyle(fontSize: 12, color: Colors.black54)),
                  if (payout.failureReason != null) ...[
                    const SizedBox(height: 6),
                    Text(
                      payout.failureReason!,
                      style: const TextStyle(fontSize: 12, color: Color(0xFFB3261E)),
                    ),
                  ],
                  if (payout.status == 'ECHEC') ...[
                    const SizedBox(height: 10),
                    SizedBox(
                      height: 40,
                      child: OutlinedButton(
                        onPressed: busy ? null : () => _retry(payout),
                        child: busy
                            ? const SizedBox(
                                width: 16,
                                height: 16,
                                child: CircularProgressIndicator(strokeWidth: 2),
                              )
                            : const Text('Relancer'),
                      ),
                    ),
                  ],
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
