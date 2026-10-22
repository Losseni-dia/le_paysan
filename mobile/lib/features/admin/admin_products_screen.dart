import 'package:flutter/material.dart';

import '../../core/admin/admin_product.dart';
import '../../core/admin/admin_product_service.dart';
import '../../core/product/product_service.dart';
import 'admin_product_detail_screen.dart';

const _productStatusFilters = [
  {'value': 'EN_ATTENTE', 'label': 'En attente'},
  {'value': 'VALIDE', 'label': 'Publiés'},
  {'value': 'REJETE', 'label': 'Rejetés'},
  {'value': 'DEPUBLIE', 'label': 'Dépubliés'},
];

class AdminProductsScreen extends StatefulWidget {
  const AdminProductsScreen({super.key});

  @override
  State<AdminProductsScreen> createState() => _AdminProductsScreenState();
}

class _AdminProductsScreenState extends State<AdminProductsScreen> {
  final AdminProductService _adminProductService = AdminProductService();
  final TextEditingController _searchController = TextEditingController();

  String _status = 'EN_ATTENTE';
  List<AdminProduct> _products = [];
  bool _loading = true;
  String? _errorMessage;
  String _query = '';

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
      final products = await _adminProductService.listForAdmin(_status);
      setState(() => _products = products);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger les produits.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Produits')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
            child: Wrap(
              spacing: 8,
              children: _productStatusFilters.map((f) {
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
                hintText: 'Rechercher (produit, boutique)...',
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

    final filtered = _products.where((p) => p.matchesSearch(_query)).toList();

    if (_products.isEmpty || filtered.isEmpty) {
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 48),
              child: Center(
                child: Text(
                  _products.isEmpty ? 'Aucun produit avec ce statut.' : 'Aucun résultat pour cette recherche.',
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
          final product = filtered[index];
          return Card(
            child: ListTile(
              leading: ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: SizedBox(
                  width: 48,
                  height: 48,
                  child: product.imageUrls.isNotEmpty
                      ? Image.network(
                          ProductService.imageUrl(product.imageUrls.first),
                          fit: BoxFit.cover,
                          errorBuilder: (_, _, _) => const ColoredBox(color: Color(0xFFE1E8E3)),
                        )
                      : const ColoredBox(color: Color(0xFFE1E8E3)),
                ),
              ),
              title: Text(product.name, style: const TextStyle(fontWeight: FontWeight.bold)),
              subtitle: Text(product.shopName),
              trailing: const Icon(Icons.chevron_right),
              onTap: () async {
                await Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (_) => AdminProductDetailScreen(productId: product.id),
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
