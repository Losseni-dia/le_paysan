import 'package:flutter/material.dart';

import '../../core/admin/admin_product.dart';
import '../../core/admin/admin_product_service.dart';
import '../../core/product/product_service.dart';

class AdminProductDetailScreen extends StatefulWidget {
  final int productId;

  const AdminProductDetailScreen({super.key, required this.productId});

  @override
  State<AdminProductDetailScreen> createState() => _AdminProductDetailScreenState();
}

class _AdminProductDetailScreenState extends State<AdminProductDetailScreen> {
  final AdminProductService _adminProductService = AdminProductService();

  AdminProduct? _product;
  bool _loading = true;
  bool _actionInProgress = false;
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
      final product = await _adminProductService.getDetail(widget.productId);
      setState(() => _product = product);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger ce produit.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _validate() async {
    setState(() => _actionInProgress = true);
    try {
      final product = await _adminProductService.validate(widget.productId);
      setState(() => _product = product);
    } catch (_) {
      _showError('Impossible de valider ce produit.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<void> _reject() async {
    final reason = await _askReason('Motif du rejet');
    if (reason == null || reason.trim().isEmpty) return;
    setState(() => _actionInProgress = true);
    try {
      final product = await _adminProductService.reject(widget.productId, reason.trim());
      setState(() => _product = product);
    } catch (_) {
      _showError('Impossible de rejeter ce produit.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<void> _togglePublishState() async {
    setState(() => _actionInProgress = true);
    try {
      final product = await _adminProductService.togglePublishState(widget.productId);
      setState(() => _product = product);
    } catch (_) {
      _showError('Impossible de mettre à jour ce produit.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<String?> _askReason(String title) async {
    final controller = TextEditingController();
    return showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: TextField(
          controller: controller,
          maxLines: 3,
          decoration: const InputDecoration(hintText: 'Expliquez la raison...'),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Annuler')),
          TextButton(
            onPressed: () => Navigator.of(context).pop(controller.text),
            child: const Text('Confirmer'),
          ),
        ],
      ),
    );
  }

  void _showError(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(_product?.name ?? 'Produit')),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_errorMessage != null || _product == null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Text(_errorMessage ?? 'Une erreur est survenue.'),
        ),
      );
    }

    final product = _product!;
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Expanded(
              child: Text(product.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: const Color(0xFFE1E8E3),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(product.status),
            ),
          ],
        ),
        const SizedBox(height: 8),
        if (product.imageUrls.isNotEmpty) ...[
          SizedBox(
            height: 140,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              itemCount: product.imageUrls.length,
              separatorBuilder: (_, _) => const SizedBox(width: 8),
              itemBuilder: (context, index) => ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: Image.network(
                  ProductService.imageUrl(product.imageUrls[index]),
                  width: 140,
                  fit: BoxFit.cover,
                  errorBuilder: (_, _, _) => const ColoredBox(color: Color(0xFFE1E8E3)),
                ),
              ),
            ),
          ),
          const SizedBox(height: 16),
        ],
        _infoRow('Boutique', product.shopName),
        _infoRow('Catégorie', product.categoryName),
        if (product.description != null && product.description!.isNotEmpty)
          _infoRow('Description', product.description!),
        _infoRow('Prix', '${product.unitPrice.toStringAsFixed(0)} FCFA / ${product.unit}'),
        _infoRow('Stock', '${product.stockQuantity}'),
        for (final opt in product.deliveryOptions)
          _infoRow(
            'Livraison',
            opt.mode == 'RETRAIT_BOUTIQUE'
                ? 'Retrait en boutique${opt.pickupLocation != null ? ' (${opt.pickupLocation})' : ''}'
                : 'Livraison à domicile${opt.deliveryPrice != null ? ' (${opt.deliveryPrice!.toStringAsFixed(0)} FCFA)' : ''}',
          ),
        const SizedBox(height: 16),
        if (product.status == 'EN_ATTENTE') ...[
          SizedBox(
            height: 52,
            child: FilledButton(
              onPressed: _actionInProgress ? null : _validate,
              child: _buttonChild('Valider le produit'),
            ),
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: 52,
            child: OutlinedButton(
              onPressed: _actionInProgress ? null : _reject,
              child: const Text('Rejeter'),
            ),
          ),
        ] else if (product.status == 'VALIDE' || product.status == 'DEPUBLIE') ...[
          SizedBox(
            height: 52,
            child: OutlinedButton(
              onPressed: _actionInProgress ? null : _togglePublishState,
              child: _buttonChild(product.status == 'VALIDE' ? 'Masquer du catalogue' : 'Republier'),
            ),
          ),
        ],
      ],
    );
  }

  Widget _buttonChild(String label) {
    return _actionInProgress
        ? const SizedBox(
            width: 22,
            height: 22,
            child: CircularProgressIndicator(strokeWidth: 2.5, color: Colors.white),
          )
        : Text(label);
  }

  Widget _infoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 90,
            child: Text(label, style: const TextStyle(color: Colors.black54, fontSize: 13)),
          ),
          Expanded(child: Text(value, style: const TextStyle(fontSize: 13))),
        ],
      ),
    );
  }
}
