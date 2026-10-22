import 'package:flutter/material.dart';

import '../../core/admin/admin_shop.dart';
import '../../core/admin/admin_shop_service.dart';
import 'admin_shop_request_detail_screen.dart';

const _shopStatusFilters = [
  {'value': 'EN_ATTENTE', 'label': 'En attente'},
  {'value': 'VALIDEE', 'label': 'Validées'},
  {'value': 'REJETEE', 'label': 'Rejetées'},
  {'value': 'SUSPENDUE', 'label': 'Suspendues'},
];

class AdminShopRequestsScreen extends StatefulWidget {
  const AdminShopRequestsScreen({super.key});

  @override
  State<AdminShopRequestsScreen> createState() => _AdminShopRequestsScreenState();
}

class _AdminShopRequestsScreenState extends State<AdminShopRequestsScreen> {
  final AdminShopService _adminShopService = AdminShopService();

  final TextEditingController _searchController = TextEditingController();

  String _status = 'EN_ATTENTE';
  List<AdminShopDetail> _requests = [];
  bool _loading = true;
  String? _errorMessage;
  String _query = '';

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

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
      final requests = await _adminShopService.listForAdmin(_status);
      setState(() => _requests = requests);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger les boutiques.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Demandes boutique')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
            child: Wrap(
              spacing: 8,
              children: _shopStatusFilters.map((f) {
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
                hintText: 'Rechercher (boutique, vendeur, ville)...',
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
    final filtered = _requests.where((r) => r.matchesSearch(_query)).toList();

    if (_requests.isEmpty || filtered.isEmpty) {
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 48),
              child: Center(
                child: Text(
                  _requests.isEmpty ? 'Aucune boutique avec ce statut.' : 'Aucun résultat pour cette recherche.',
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
          final request = filtered[index];
          return Card(
            child: ListTile(
              title: Text(request.shop.name, style: const TextStyle(fontWeight: FontWeight.bold)),
              subtitle: Text(request.shop.addressCity ?? 'Ville inconnue'),
              trailing: const Icon(Icons.chevron_right),
              onTap: () async {
                await Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (_) => AdminShopRequestDetailScreen(shopId: request.shop.id),
                  ),
                );
                _load();
              },
            ),
          );
        },
      ),
    );
  }
}
