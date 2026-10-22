import 'package:flutter/material.dart';

import '../../core/product/product_service.dart';
import '../../core/vendor/vendor_product.dart';
import '../../core/vendor/vendor_product_service.dart';

const _statusLabels = {
  'EN_ATTENTE': 'En attente de validation',
  'VALIDE': 'Publié',
  'REJETE': 'Rejeté',
  'DEPUBLIE': 'Dépublié',
};

class VendorProductsScreen extends StatefulWidget {
  const VendorProductsScreen({super.key});

  @override
  State<VendorProductsScreen> createState() => _VendorProductsScreenState();
}

class _VendorProductsScreenState extends State<VendorProductsScreen> {
  final VendorProductService _vendorProductService = VendorProductService();
  final TextEditingController _searchController = TextEditingController();

  List<VendorProduct> _products = [];
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
      final products = await _vendorProductService.listMine();
      setState(() => _products = products);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger vos produits.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _togglePublish(VendorProduct product) async {
    setState(() => _togglingId = product.id);
    try {
      final updated = await _vendorProductService.togglePublishState(product.id);
      setState(() {
        _products = _products.map((p) => p.id == updated.id ? updated : p).toList();
      });
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Impossible de mettre à jour ce produit.')),
        );
      }
    } finally {
      if (mounted) setState(() => _togglingId = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mes produits')),
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

    final filtered = _products.where((p) => p.matchesSearch(_query)).toList();

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(16),
          child: TextField(
            controller: _searchController,
            decoration: const InputDecoration(
              hintText: 'Rechercher un produit...',
              prefixIcon: Icon(Icons.search),
            ),
            onChanged: (value) => setState(() => _query = value),
          ),
        ),
        Expanded(
          child: _products.isEmpty
              ? _emptyState("Vous n'avez pas encore ajouté de produit.")
              : filtered.isEmpty
              ? _emptyState('Aucun résultat pour cette recherche.')
              : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView.separated(
                    padding: const EdgeInsets.all(16),
                    itemCount: filtered.length,
                    separatorBuilder: (_, _) => const SizedBox(height: 12),
                    itemBuilder: (context, index) {
                      final product = filtered[index];
                      return _ProductCard(
                        product: product,
                        busy: _togglingId == product.id,
                        onTogglePublish: () => _togglePublish(product),
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

class _ProductCard extends StatelessWidget {
  final VendorProduct product;
  final bool busy;
  final VoidCallback onTogglePublish;

  const _ProductCard({
    required this.product,
    required this.busy,
    required this.onTogglePublish,
  });

  @override
  Widget build(BuildContext context) {
    final canToggle = product.status == 'VALIDE' || product.status == 'DEPUBLIE';
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: SizedBox(
                width: 64,
                height: 64,
                child: product.imageUrls.isNotEmpty
                    ? Image.network(
                        ProductService.imageUrl(product.imageUrls.first),
                        fit: BoxFit.cover,
                        errorBuilder: (_, _, _) => const ColoredBox(color: Color(0xFFE1E8E3)),
                      )
                    : const ColoredBox(color: Color(0xFFE1E8E3)),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(product.name, style: const TextStyle(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 4),
                  Text(
                    '${product.unitPrice.toStringAsFixed(0)} FCFA / ${product.unit}',
                    style: const TextStyle(fontSize: 13, color: Color(0xFF1B5E3A), fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Stock : ${product.stockQuantity}',
                    style: const TextStyle(fontSize: 12, color: Colors.black54),
                  ),
                  const SizedBox(height: 6),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: const Color(0xFFE1E8E3),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      _statusLabels[product.status] ?? product.status,
                      style: const TextStyle(fontSize: 11),
                    ),
                  ),
                  if (canToggle) ...[
                    const SizedBox(height: 8),
                    SizedBox(
                      height: 36,
                      child: OutlinedButton(
                        onPressed: busy ? null : onTogglePublish,
                        child: busy
                            ? const SizedBox(
                                width: 16,
                                height: 16,
                                child: CircularProgressIndicator(strokeWidth: 2),
                              )
                            : Text(product.status == 'VALIDE' ? 'Dépublier' : 'Republier'),
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
