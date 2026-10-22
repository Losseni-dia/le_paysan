import 'package:flutter/material.dart';

import '../../core/admin/admin_shop.dart';
import '../../core/admin/admin_shop_service.dart';
import '../../core/product/product_service.dart';
import '../../shared/widgets/authenticated_image.dart';

class AdminShopRequestDetailScreen extends StatefulWidget {
  final int shopId;

  const AdminShopRequestDetailScreen({super.key, required this.shopId});

  @override
  State<AdminShopRequestDetailScreen> createState() => _AdminShopRequestDetailScreenState();
}

class _AdminShopRequestDetailScreenState extends State<AdminShopRequestDetailScreen> {
  final AdminShopService _adminShopService = AdminShopService();

  AdminShopDetail? _detail;
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
      final detail = await _adminShopService.getDetail(widget.shopId);
      setState(() => _detail = detail);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger cette boutique.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _validate() async {
    setState(() => _actionInProgress = true);
    try {
      await _adminShopService.validate(widget.shopId);
      await _load();
    } catch (_) {
      _showError('Impossible de valider cette boutique.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<void> _reject() async {
    final reason = await _askReason('Motif du rejet');
    if (reason == null || reason.trim().isEmpty) return;
    setState(() => _actionInProgress = true);
    try {
      await _adminShopService.reject(widget.shopId, reason.trim());
      await _load();
    } catch (_) {
      _showError('Impossible de rejeter cette boutique.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<void> _suspend() async {
    final reason = await _askReason(
      'Motif de la suspension',
      hint: 'Ses produits deviendront invisibles du catalogue.',
    );
    if (reason == null || reason.trim().isEmpty) return;
    setState(() => _actionInProgress = true);
    try {
      await _adminShopService.suspend(widget.shopId, reason.trim());
      await _load();
    } catch (_) {
      _showError('Impossible de suspendre cette boutique.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<void> _restore() async {
    setState(() => _actionInProgress = true);
    try {
      await _adminShopService.restore(widget.shopId);
      await _load();
    } catch (_) {
      _showError('Impossible de réactiver cette boutique.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<String?> _askReason(String title, {String? hint}) async {
    final controller = TextEditingController();
    return showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (hint != null) ...[
              Text(hint, style: const TextStyle(fontSize: 13, color: Colors.black54)),
              const SizedBox(height: 12),
            ],
            TextField(
              controller: controller,
              maxLines: 3,
              decoration: const InputDecoration(hintText: 'Expliquez la raison...'),
            ),
          ],
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
      appBar: AppBar(title: Text(_detail?.shop.name ?? 'Boutique')),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_errorMessage != null || _detail == null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Text(_errorMessage ?? 'Une erreur est survenue.'),
        ),
      );
    }

    final detail = _detail!;
    final shop = detail.shop;
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Expanded(
              child: Text(shop.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: const Color(0xFFE1E8E3),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(shop.status),
            ),
          ],
        ),
        const SizedBox(height: 8),
        if (shop.description != null && shop.description!.isNotEmpty)
          Text(shop.description!, style: const TextStyle(fontSize: 13, color: Colors.black54)),
        const Divider(height: 24),
        _infoRow('Type', shop.ownerType),
        if (detail.representativeName != null) _infoRow('Représentant', detail.representativeName!),
        _infoRow('Propriétaire', '@${detail.ownerUsername}'),
        if (detail.ownerEmail != null) _infoRow('Email', detail.ownerEmail!),
        if (shop.phone != null) _infoRow('Téléphone', shop.phone!),
        if (shop.addressCity != null) _infoRow('Ville', shop.addressCity!),
        if (shop.addressDetail != null) _infoRow('Adresse', shop.addressDetail!),
        const Divider(height: 24),
        const Text('Documents', style: TextStyle(fontWeight: FontWeight.w600)),
        const SizedBox(height: 8),
        if (detail.documents.isEmpty)
          const Text('Aucun document fourni.', style: TextStyle(fontSize: 13, color: Colors.black54))
        else
          for (final doc in detail.documents)
            Padding(
              padding: const EdgeInsets.only(bottom: 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(doc.label, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
                  const SizedBox(height: 6),
                  ClipRRect(
                    borderRadius: BorderRadius.circular(12),
                    child: AuthenticatedImage(
                      url: ProductService.imageUrl(doc.url),
                      height: 180,
                      fit: BoxFit.contain,
                    ),
                  ),
                ],
              ),
            ),
        const SizedBox(height: 16),
        if (shop.status == 'EN_ATTENTE') ...[
          SizedBox(
            height: 52,
            child: FilledButton(
              onPressed: _actionInProgress ? null : _validate,
              child: _buttonChild('Valider la boutique'),
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
        ] else if (shop.status == 'VALIDEE') ...[
          SizedBox(
            height: 52,
            child: OutlinedButton(
              onPressed: _actionInProgress ? null : _suspend,
              child: const Text('Suspendre la boutique'),
            ),
          ),
        ] else if (shop.status == 'SUSPENDUE') ...[
          SizedBox(
            height: 52,
            child: FilledButton(
              onPressed: _actionInProgress ? null : _restore,
              child: _buttonChild('Réactiver la boutique'),
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
            width: 110,
            child: Text(label, style: const TextStyle(color: Colors.black54, fontSize: 13)),
          ),
          Expanded(child: Text(value, style: const TextStyle(fontSize: 13))),
        ],
      ),
    );
  }
}
