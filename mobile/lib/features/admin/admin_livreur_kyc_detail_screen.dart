import 'package:flutter/material.dart';

import '../../core/admin/admin_livreur.dart';
import '../../core/admin/admin_livreur_service.dart';
import '../../core/product/product_service.dart';
import '../../shared/widgets/authenticated_image.dart';

class AdminLivreurKycDetailScreen extends StatefulWidget {
  final int livreurId;

  const AdminLivreurKycDetailScreen({super.key, required this.livreurId});

  @override
  State<AdminLivreurKycDetailScreen> createState() => _AdminLivreurKycDetailScreenState();
}

class _AdminLivreurKycDetailScreenState extends State<AdminLivreurKycDetailScreen> {
  final AdminLivreurService _adminLivreurService = AdminLivreurService();

  AdminLivreur? _livreur;
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
      final livreur = await _adminLivreurService.getDetail(widget.livreurId);
      setState(() => _livreur = livreur);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger ce livreur.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _validate() async {
    setState(() => _actionInProgress = true);
    try {
      await _adminLivreurService.validateKyc(widget.livreurId);
      await _load();
    } catch (_) {
      _showError('Impossible de valider ce livreur.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<void> _reject() async {
    final reason = await _askReason('Motif du rejet');
    if (reason == null || reason.trim().isEmpty) return;
    setState(() => _actionInProgress = true);
    try {
      await _adminLivreurService.rejectKyc(widget.livreurId, reason.trim());
      await _load();
    } catch (_) {
      _showError('Impossible de rejeter ce livreur.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<void> _suspend() async {
    final reason = await _askReason('Motif de la suspension');
    if (reason == null || reason.trim().isEmpty) return;
    setState(() => _actionInProgress = true);
    try {
      await _adminLivreurService.suspend(widget.livreurId, reason.trim());
      await _load();
    } catch (_) {
      _showError('Impossible de suspendre ce livreur.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<void> _restore() async {
    setState(() => _actionInProgress = true);
    try {
      await _adminLivreurService.restore(widget.livreurId);
      await _load();
    } catch (_) {
      _showError('Impossible de réactiver ce livreur.');
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
    final livreur = _livreur;
    return Scaffold(
      appBar: AppBar(
        title: Text(livreur != null ? '${livreur.firstName} ${livreur.lastName}' : 'Livreur'),
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_errorMessage != null || _livreur == null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Text(_errorMessage ?? 'Une erreur est survenue.'),
        ),
      );
    }

    final livreur = _livreur!;
    final documents = <MapEntry<String, String>>[
      if (livreur.photoUrl != null) MapEntry('Photo', livreur.photoUrl!),
      if (livreur.idDocumentFrontUrl != null) MapEntry("Pièce d'identité (recto)", livreur.idDocumentFrontUrl!),
      if (livreur.idDocumentBackUrl != null) MapEntry("Pièce d'identité (verso)", livreur.idDocumentBackUrl!),
      if (livreur.selfieWithIdUrl != null) MapEntry('Selfie avec la pièce', livreur.selfieWithIdUrl!),
    ];

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Expanded(
              child: Text(
                '${livreur.firstName} ${livreur.lastName}',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
              ),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: const Color(0xFFE1E8E3),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(livreur.kycStatus),
            ),
          ],
        ),
        const SizedBox(height: 8),
        _infoRow('Identifiant', '@${livreur.username}'),
        if (livreur.phone != null) _infoRow('Téléphone', livreur.phone!),
        if (livreur.city != null) _infoRow('Ville', livreur.city!),
        const Divider(height: 24),
        const Text('Documents', style: TextStyle(fontWeight: FontWeight.w600)),
        const SizedBox(height: 8),
        if (documents.isEmpty)
          const Text('Aucun document fourni.', style: TextStyle(fontSize: 13, color: Colors.black54))
        else
          for (final doc in documents)
            Padding(
              padding: const EdgeInsets.only(bottom: 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(doc.key, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
                  const SizedBox(height: 6),
                  ClipRRect(
                    borderRadius: BorderRadius.circular(12),
                    child: AuthenticatedImage(
                      url: ProductService.imageUrl(doc.value),
                      height: 180,
                      fit: BoxFit.contain,
                    ),
                  ),
                ],
              ),
            ),
        if (livreur.kycStatus != 'VALIDE') ...[
          const SizedBox(height: 16),
          SizedBox(
            height: 52,
            child: FilledButton(
              onPressed: _actionInProgress ? null : _validate,
              child: _actionInProgress
                  ? const SizedBox(
                      width: 22,
                      height: 22,
                      child: CircularProgressIndicator(strokeWidth: 2.5, color: Colors.white),
                    )
                  : const Text('Valider le KYC'),
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
        ],
        const Divider(height: 24),
        const Text('Compte', style: TextStyle(fontWeight: FontWeight.w600)),
        const SizedBox(height: 8),
        Text(
          livreur.enabled ? 'Actif' : 'Suspendu',
          style: TextStyle(
            fontSize: 13,
            color: livreur.enabled ? const Color(0xFF1B5E3A) : const Color(0xFFB3261E),
          ),
        ),
        const SizedBox(height: 12),
        if (livreur.enabled)
          SizedBox(
            height: 52,
            child: OutlinedButton(
              onPressed: _actionInProgress ? null : _suspend,
              child: const Text('Suspendre le compte'),
            ),
          )
        else
          SizedBox(
            height: 52,
            child: FilledButton(
              onPressed: _actionInProgress ? null : _restore,
              child: const Text('Réactiver le compte'),
            ),
          ),
      ],
    );
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
