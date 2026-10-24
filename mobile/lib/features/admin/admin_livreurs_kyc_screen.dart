import 'package:flutter/material.dart';

import '../../core/admin/admin_livreur.dart';
import '../../core/admin/admin_livreur_service.dart';
import 'admin_livreur_kyc_detail_screen.dart';

const _kycStatusFilters = [
  {'value': null, 'label': 'Toutes'},
  {'value': 'EN_ATTENTE', 'label': 'En attente'},
  {'value': 'VALIDE', 'label': 'Validés'},
  {'value': 'REJETE', 'label': 'Rejetés'},
  {'value': 'SUSPENDUS', 'label': 'Suspendus'},
];

class AdminLivreursKycScreen extends StatefulWidget {
  const AdminLivreursKycScreen({super.key});

  @override
  State<AdminLivreursKycScreen> createState() => _AdminLivreursKycScreenState();
}

class _AdminLivreursKycScreenState extends State<AdminLivreursKycScreen> {
  final AdminLivreurService _adminLivreurService = AdminLivreurService();
  final TextEditingController _searchController = TextEditingController();

  String? _kycStatus = 'EN_ATTENTE';
  List<AdminLivreur> _livreurs = [];
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
      // "Suspendus" n'est pas un statut KYC : on recupere tout et on filtre ici sur enabled.
      final isSuspendedFilter = _kycStatus == 'SUSPENDUS';
      final livreurs = await _adminLivreurService.listForAdmin(isSuspendedFilter ? null : _kycStatus);
      setState(() => _livreurs = isSuspendedFilter ? livreurs.where((l) => !l.enabled).toList() : livreurs);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger les livreurs.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('KYC livreurs')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
            child: Wrap(
              spacing: 8,
              children: _kycStatusFilters.map((f) {
                final selected = _kycStatus == f['value'];
                return ChoiceChip(
                  label: Text(f['label'] as String),
                  selected: selected,
                  onSelected: (_) {
                    setState(() => _kycStatus = f['value']);
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
                hintText: 'Rechercher (livreur, identifiant, boutique, ville)...',
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
    final filtered = _livreurs.where((l) => l.matchesSearch(_query)).toList();

    if (_livreurs.isEmpty || filtered.isEmpty) {
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 48),
              child: Center(
                child: Text(
                  _livreurs.isEmpty ? 'Aucun livreur avec ce statut.' : 'Aucun résultat pour cette recherche.',
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
          final livreur = filtered[index];
          return Card(
            child: ListTile(
              title: Row(
                children: [
                  Expanded(
                    child: Text(
                      '${livreur.firstName} ${livreur.lastName}',
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                  ),
                  if (!livreur.enabled)
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                      decoration: BoxDecoration(
                        color: const Color(0xFFFBE9E7),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Text(
                        'Suspendu',
                        style: TextStyle(fontSize: 11, color: Color(0xFFB3261E)),
                      ),
                    ),
                ],
              ),
              subtitle: Text(livreur.city ?? 'Ville inconnue'),
              trailing: const Icon(Icons.chevron_right),
              onTap: () async {
                await Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (_) => AdminLivreurKycDetailScreen(livreurId: livreur.id),
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
