import 'package:flutter/material.dart';

import '../../core/livreur/livreur_profile.dart';
import '../../core/vendor/vendor_livreur_service.dart';

class VendorLivreursScreen extends StatefulWidget {
  const VendorLivreursScreen({super.key});

  @override
  State<VendorLivreursScreen> createState() => _VendorLivreursScreenState();
}

class _VendorLivreursScreenState extends State<VendorLivreursScreen> {
  final VendorLivreurService _vendorLivreurService = VendorLivreurService();
  final TextEditingController _searchController = TextEditingController();

  List<LivreurProfile> _livreurs = [];
  bool _loading = true;
  String? _errorMessage;
  String _query = '';
  int? _togglingId;

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
      final livreurs = await _vendorLivreurService.listMine();
      setState(() => _livreurs = livreurs);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger vos livreurs.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _toggleEnabled(LivreurProfile livreur) async {
    setState(() => _togglingId = livreur.id);
    try {
      final updated = await _vendorLivreurService.setEnabled(livreur.id, !livreur.enabled);
      setState(() {
        _livreurs = _livreurs.map((l) => l.id == updated.id ? updated : l).toList();
      });
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Impossible de mettre à jour ce livreur.')),
        );
      }
    } finally {
      if (mounted) setState(() => _togglingId = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mes livreurs')),
      body: _buildBody(),
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

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(16),
          child: TextField(
            controller: _searchController,
            decoration: const InputDecoration(
              hintText: 'Rechercher (nom, identifiant, ville)...',
              prefixIcon: Icon(Icons.search),
            ),
            onChanged: (value) => setState(() => _query = value),
          ),
        ),
        Expanded(
          child: _livreurs.isEmpty
              ? _emptyState('Aucun livreur pour le moment.')
              : filtered.isEmpty
              ? _emptyState('Aucun résultat pour cette recherche.')
              : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView.separated(
                    padding: const EdgeInsets.all(16),
                    itemCount: filtered.length,
                    separatorBuilder: (_, _) => const SizedBox(height: 12),
                    itemBuilder: (context, index) {
                      final livreur = filtered[index];
                      return _LivreurCard(
                        livreur: livreur,
                        busy: _togglingId == livreur.id,
                        onToggleEnabled: () => _toggleEnabled(livreur),
                      );
                    },
                  ),
                ),
        ),
      ],
    );
  }

  Widget _emptyState(String message) {
    return RefreshIndicator(
      onRefresh: _load,
      child: ListView(
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 48),
            child: Center(child: Text(message)),
          ),
        ],
      ),
    );
  }
}

class _LivreurCard extends StatelessWidget {
  final LivreurProfile livreur;
  final bool busy;
  final VoidCallback onToggleEnabled;

  const _LivreurCard({
    required this.livreur,
    required this.busy,
    required this.onToggleEnabled,
  });

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
                Text(
                  '${livreur.firstName} ${livreur.lastName}',
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: livreur.enabled ? const Color(0xFFD7ECDF) : const Color(0xFFE1E8E3),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    livreur.enabled ? 'Actif' : 'Désactivé',
                    style: const TextStyle(fontSize: 12),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 6),
            Text('@${livreur.username}', style: const TextStyle(fontSize: 12, color: Colors.black54)),
            if (livreur.phone != null) ...[
              const SizedBox(height: 2),
              Text(livreur.phone!, style: const TextStyle(fontSize: 12, color: Colors.black54)),
            ],
            if (livreur.city != null) ...[
              const SizedBox(height: 2),
              Text(livreur.city!, style: const TextStyle(fontSize: 12, color: Colors.black54)),
            ],
            const SizedBox(height: 6),
            Row(
              children: [
                Text(
                  livreur.available ? 'Disponible' : 'Indisponible',
                  style: TextStyle(
                    fontSize: 12,
                    color: livreur.available ? const Color(0xFF1B5E3A) : Colors.black54,
                  ),
                ),
                const SizedBox(width: 12),
                Text(
                  '${livreur.deliveryCount} livraison(s)',
                  style: const TextStyle(fontSize: 12, color: Colors.black54),
                ),
              ],
            ),
            const SizedBox(height: 8),
            SizedBox(
              height: 36,
              child: OutlinedButton(
                onPressed: busy ? null : onToggleEnabled,
                child: busy
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : Text(livreur.enabled ? 'Désactiver' : 'Réactiver'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
