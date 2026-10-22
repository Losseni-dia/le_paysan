import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../core/livreur/livreur_order.dart';
import '../../core/livreur/livreur_service.dart';
import '../../core/order/order_status_labels.dart';
import '../../core/product/product_service.dart';
import '../../shared/widgets/authenticated_image.dart';
import '../../shared/widgets/signature_pad.dart';

class LivreurOrderDetailScreen extends StatefulWidget {
  final int orderId;

  const LivreurOrderDetailScreen({super.key, required this.orderId});

  @override
  State<LivreurOrderDetailScreen> createState() => _LivreurOrderDetailScreenState();
}

class _LivreurOrderDetailScreenState extends State<LivreurOrderDetailScreen> {
  final LivreurService _livreurService = LivreurService();
  final ImagePicker _imagePicker = ImagePicker();
  final SignaturePadController _signatureController = SignaturePadController();

  LivreurOrder? _order;
  bool _loading = true;
  bool _actionInProgress = false;
  String? _errorMessage;
  String _proofMode = 'SIGNATURE';
  XFile? _proofPhoto;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _signatureController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _errorMessage = null;
    });
    try {
      final order = await _livreurService.getOrder(widget.orderId);
      setState(() => _order = order);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger cette commande.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _markPickedUp() async {
    setState(() => _actionInProgress = true);
    try {
      final order = await _livreurService.markPickedUp(widget.orderId);
      setState(() => _order = order);
    } catch (_) {
      _showError('Impossible de marquer ce colis comme récupéré.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<void> _markEnRoute() async {
    setState(() => _actionInProgress = true);
    try {
      final order = await _livreurService.markEnRoute(widget.orderId);
      setState(() => _order = order);
    } catch (_) {
      _showError('Impossible de marquer ce colis en route.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  Future<void> _pickProofPhoto() async {
    final photo = await _imagePicker.pickImage(source: ImageSource.camera);
    if (photo != null) setState(() => _proofPhoto = photo);
  }

  Future<void> _confirmDelivery() async {
    Uint8List proofBytes;
    String filename;
    if (_proofMode == 'SIGNATURE') {
      if (!_signatureController.hasSignature) {
        _showError('Faites signer le client avant de valider.');
        return;
      }
      proofBytes = await _signatureController.toPngBytes();
      filename = 'signature.png';
    } else {
      if (_proofPhoto == null) {
        _showError('Prenez une photo du colis avant de valider.');
        return;
      }
      proofBytes = await _proofPhoto!.readAsBytes();
      filename = 'photo.jpg';
    }

    setState(() => _actionInProgress = true);
    try {
      final order = await _livreurService.confirmDelivery(
        widget.orderId,
        proofType: _proofMode,
        proofBytes: proofBytes,
        filename: filename,
      );
      setState(() => _order = order);
    } catch (_) {
      _showError('Impossible de confirmer la remise.');
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  void _showError(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_order != null ? 'Commande ${_order!.orderNumber}' : 'Commande'),
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_errorMessage != null || _order == null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Text(_errorMessage ?? 'Une erreur est survenue.'),
        ),
      );
    }

    final order = _order!;
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(order.shopName, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: const Color(0xFFE1E8E3),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(orderStatusLabel(order.status)),
            ),
          ],
        ),
        const SizedBox(height: 8),
        Text('Client : ${order.buyerFullName}', style: const TextStyle(fontSize: 14)),
        const Divider(height: 24),
        const Text('Articles', style: TextStyle(fontWeight: FontWeight.w600)),
        const SizedBox(height: 8),
        for (final item in order.items)
          Padding(
            padding: const EdgeInsets.only(bottom: 10),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('${item.quantity} x ${item.productName}'),
                if (item.deliveryAddress != null && item.deliveryAddress!.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.only(top: 2),
                    child: Text(
                      'Adresse : ${item.deliveryAddress}',
                      style: const TextStyle(fontSize: 12, color: Colors.black54),
                    ),
                  ),
              ],
            ),
          ),
        const Divider(height: 24),
        Text(
          'Total : ${order.totalAmount.toStringAsFixed(0)} FCFA',
          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
        const SizedBox(height: 24),
        ..._buildActionSection(order.status),
        if (order.deliveryProof != null) ..._buildProofSection(order.deliveryProof!),
      ],
    );
  }

  List<Widget> _buildActionSection(String status) {
    if (status == 'EXPEDIEE') {
      return [_actionButton('Marquer comme récupéré', _markPickedUp)];
    }
    if (status == 'RECUPEREE') {
      return [_actionButton('Marquer en route', _markEnRoute)];
    }
    if (status == 'EN_ROUTE') {
      return [
        const Text('Preuve de remise', style: TextStyle(fontWeight: FontWeight.w600)),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: ChoiceChip(
                label: const Text('Signature'),
                selected: _proofMode == 'SIGNATURE',
                onSelected: (_) => setState(() => _proofMode = 'SIGNATURE'),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: ChoiceChip(
                label: const Text('Photo'),
                selected: _proofMode == 'PHOTO',
                onSelected: (_) => setState(() => _proofMode = 'PHOTO'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        if (_proofMode == 'SIGNATURE') ...[
          const Text(
            "Faites signer l'acheteur directement sur cet écran.",
            style: TextStyle(fontSize: 12, color: Colors.black54),
          ),
          const SizedBox(height: 8),
          SignaturePad(controller: _signatureController),
          Align(
            alignment: Alignment.centerRight,
            child: TextButton(
              onPressed: () => setState(_signatureController.clear),
              child: const Text('Effacer'),
            ),
          ),
        ] else ...[
          OutlinedButton.icon(
            onPressed: _pickProofPhoto,
            icon: const Icon(Icons.camera_alt_outlined),
            label: Text(_proofPhoto == null ? 'Prendre une photo' : 'Reprendre la photo'),
          ),
        ],
        const SizedBox(height: 16),
        _actionButton('Confirmer la remise', _confirmDelivery),
      ];
    }
    return [];
  }

  List<Widget> _buildProofSection(OrderStatusHistoryEntry proof) {
    return [
      const Divider(height: 24),
      Text(
        proof.proofType == 'SIGNATURE' ? 'Signature de remise' : 'Photo de remise',
        style: const TextStyle(fontWeight: FontWeight.w600),
      ),
      if (proof.changedAt != null) ...[
        const SizedBox(height: 4),
        Text(
          'Remise le ${formatDateTime(proof.changedAt!)}',
          style: const TextStyle(fontSize: 12, color: Colors.black54),
        ),
      ],
      const SizedBox(height: 8),
      ClipRRect(
        borderRadius: BorderRadius.circular(12),
        child: AuthenticatedImage(
          url: ProductService.imageUrl(proof.proofUrl!),
          height: 200,
          fit: BoxFit.contain,
        ),
      ),
    ];
  }

  Widget _actionButton(String label, VoidCallback onPressed) {
    return SizedBox(
      height: 52,
      child: FilledButton(
        onPressed: _actionInProgress ? null : onPressed,
        child: _actionInProgress
            ? const SizedBox(
                width: 22,
                height: 22,
                child: CircularProgressIndicator(strokeWidth: 2.5, color: Colors.white),
              )
            : Text(label),
      ),
    );
  }
}
