import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../core/delivery/delivery_request_service.dart';
import '../../core/delivery/eligible_livreur.dart';
import '../../core/livreur/livreur_order.dart';
import '../../core/order/order_status_labels.dart';
import '../../core/product/product_service.dart';
import '../../core/vendor/vendor_order_service.dart';
import '../../shared/widgets/authenticated_image.dart';
import '../../shared/widgets/signature_pad.dart';

class VendorOrderDetailScreen extends StatefulWidget {
  final int orderId;

  const VendorOrderDetailScreen({super.key, required this.orderId});

  @override
  State<VendorOrderDetailScreen> createState() => _VendorOrderDetailScreenState();
}

class _VendorOrderDetailScreenState extends State<VendorOrderDetailScreen> {
  final VendorOrderService _vendorOrderService = VendorOrderService();
  final DeliveryRequestService _deliveryRequestService = DeliveryRequestService();
  final ImagePicker _imagePicker = ImagePicker();
  final SignaturePadController _signatureController = SignaturePadController();

  LivreurOrder? _order;
  List<EligibleLivreur> _eligibleLivreurs = [];
  final Set<int> _selectedLivreurIds = {};
  bool _loadingEligible = false;
  bool _broadcasting = false;
  bool _broadcastSent = false;
  String? _proofMode;
  XFile? _proofPhoto;
  bool _loading = true;
  bool _actionInProgress = false;
  String? _errorMessage;

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
      final order = await _vendorOrderService.getOrder(widget.orderId);
      setState(() => _order = order);
      if (_canBroadcast(order)) {
        await _loadEligibleLivreurs();
      }
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger cette commande.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  bool _canBroadcast(LivreurOrder order) {
    return order.status == 'EXPEDIEE' && order.assignedLivreurId == null;
  }

  Future<void> _loadEligibleLivreurs() async {
    setState(() => _loadingEligible = true);
    try {
      final livreurs = await _deliveryRequestService.listEligibleLivreurs(widget.orderId);
      setState(() => _eligibleLivreurs = livreurs);
    } catch (_) {
      // Pas bloquant : la section affichera simplement une liste vide.
    } finally {
      if (mounted) setState(() => _loadingEligible = false);
    }
  }

  Future<void> _pickProofPhoto() async {
    final photo = await _imagePicker.pickImage(source: ImageSource.camera);
    if (photo != null) setState(() => _proofPhoto = photo);
  }

  Future<void> _advanceStatus() async {
    Uint8List? proofBytes;
    String? filename;
    if (_proofMode == 'SIGNATURE' && _signatureController.hasSignature) {
      proofBytes = await _signatureController.toPngBytes();
      filename = 'signature.png';
    } else if (_proofMode == 'PHOTO' && _proofPhoto != null) {
      proofBytes = await _proofPhoto!.readAsBytes();
      filename = 'photo.jpg';
    }

    setState(() => _actionInProgress = true);
    try {
      final order = await _vendorOrderService.advanceStatus(
        widget.orderId,
        proofType: proofBytes != null ? _proofMode : null,
        proofBytes: proofBytes,
        proofFilename: filename,
      );
      setState(() {
        _order = order;
        _proofMode = null;
        _proofPhoto = null;
        _signatureController.clear();
      });
      if (_canBroadcast(order)) {
        await _loadEligibleLivreurs();
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Impossible de mettre à jour le statut.')),
        );
      }
    } finally {
      if (mounted) setState(() => _actionInProgress = false);
    }
  }

  void _toggleLivreurSelection(int id) {
    setState(() {
      if (_selectedLivreurIds.contains(id)) {
        _selectedLivreurIds.remove(id);
      } else {
        _selectedLivreurIds.add(id);
      }
    });
  }

  Future<void> _confirmBroadcast() async {
    if (_selectedLivreurIds.isEmpty) return;
    setState(() => _broadcasting = true);
    try {
      await _deliveryRequestService.broadcast(widget.orderId, _selectedLivreurIds.toList());
      setState(() {
        _broadcastSent = true;
        _selectedLivreurIds.clear();
      });
      await _loadEligibleLivreurs();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Impossible de diffuser cette demande.')),
        );
      }
    } finally {
      if (mounted) setState(() => _broadcasting = false);
    }
  }

  bool get _hasLivraisonDomicile =>
      _order?.items.any((i) => i.deliveryMode == 'LIVRAISON_DOMICILE') ?? false;

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
            Text('Client : ${order.buyerFullName}', style: const TextStyle(fontWeight: FontWeight.w600)),
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
        if (order.assignedLivreurName != null) ...[
          const SizedBox(height: 8),
          Text(
            'Livreur assigné : ${order.assignedLivreurName}',
            style: const TextStyle(fontSize: 13, color: Colors.black54),
          ),
        ],
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
                Text(
                  item.deliveryMode == 'RETRAIT_BOUTIQUE'
                      ? 'Retrait en boutique'
                      : 'Livraison à domicile${item.deliveryAddress != null ? ' - ${item.deliveryAddress}' : ''}',
                  style: const TextStyle(fontSize: 12, color: Colors.black54),
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
        if (_canBroadcast(order)) ..._buildBroadcastSection(),
        if (order.deliveryProof != null) ..._buildProofSection(order.deliveryProof!),
      ],
    );
  }

  List<Widget> _buildActionSection(String status) {
    if (status == 'PAYEE') {
      return [_actionButton('Commencer la préparation', _advanceStatus)];
    }

    if (status == 'EN_PREPARATION') {
      if (!_hasLivraisonDomicile) {
        return [_actionButton('Marquer comme prête', _advanceStatus)];
      }
      return [
        const Text(
          'Vous pourrez diffuser la demande aux livreurs disponibles juste après ce changement de statut.',
          style: TextStyle(fontSize: 12, color: Colors.black54),
        ),
        const SizedBox(height: 16),
        _actionButton('Marquer comme expédiée', _advanceStatus),
      ];
    }

    if (status == 'PRETE' || status == 'EXPEDIEE') {
      final label = status == 'PRETE' ? 'Confirmer le retrait' : "J'ai livré moi-même";
      return [
        const Text('Preuve de remise (optionnel)', style: TextStyle(fontWeight: FontWeight.w600)),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: ChoiceChip(
                label: const Text('Signature'),
                selected: _proofMode == 'SIGNATURE',
                onSelected: (_) => setState(
                  () => _proofMode = _proofMode == 'SIGNATURE' ? null : 'SIGNATURE',
                ),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: ChoiceChip(
                label: const Text('Photo'),
                selected: _proofMode == 'PHOTO',
                onSelected: (_) => setState(
                  () => _proofMode = _proofMode == 'PHOTO' ? null : 'PHOTO',
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        if (_proofMode == 'SIGNATURE') ...[
          SignaturePad(controller: _signatureController),
          Align(
            alignment: Alignment.centerRight,
            child: TextButton(
              onPressed: () => setState(_signatureController.clear),
              child: const Text('Effacer'),
            ),
          ),
        ] else if (_proofMode == 'PHOTO') ...[
          OutlinedButton.icon(
            onPressed: _pickProofPhoto,
            icon: const Icon(Icons.camera_alt_outlined),
            label: Text(_proofPhoto == null ? 'Prendre une photo' : 'Reprendre la photo'),
          ),
        ],
        const SizedBox(height: 16),
        _actionButton(label, _advanceStatus),
      ];
    }

    return [];
  }

  List<Widget> _buildBroadcastSection() {
    return [
      const Divider(height: 24),
      const Text(
        'Diffuser aux livreurs disponibles',
        style: TextStyle(fontWeight: FontWeight.w600),
      ),
      const SizedBox(height: 4),
      const Text(
        'Sélectionnez un ou plusieurs livreurs disponibles à proximité de la livraison. '
        "Le premier qui accepte devient le livreur assigné.",
        style: TextStyle(fontSize: 12, color: Colors.black54),
      ),
      const SizedBox(height: 12),
      if (_broadcastSent) ...[
        const Text('Demande diffusée.', style: TextStyle(color: Color(0xFF1B5E3A))),
        const SizedBox(height: 8),
      ],
      if (_loadingEligible)
        const Padding(
          padding: EdgeInsets.symmetric(vertical: 12),
          child: Center(child: CircularProgressIndicator()),
        )
      else if (_eligibleLivreurs.isEmpty)
        const Text(
          'Aucun livreur disponible à proximité pour le moment.',
          style: TextStyle(fontSize: 13, color: Colors.black54),
        )
      else ...[
        for (final livreur in _eligibleLivreurs)
          CheckboxListTile(
            contentPadding: EdgeInsets.zero,
            value: _selectedLivreurIds.contains(livreur.id),
            onChanged: (_) => _toggleLivreurSelection(livreur.id),
            title: Text('${livreur.firstName} ${livreur.lastName}'),
            subtitle: Text(
              [
                if (livreur.city != null) livreur.city!,
                if (livreur.distanceKm != null) '${livreur.distanceKm!.toStringAsFixed(1)} km',
              ].join(' — '),
            ),
          ),
        const SizedBox(height: 8),
        _actionButton(
          _broadcasting ? 'Diffusion...' : 'Diffuser',
          _selectedLivreurIds.isEmpty || _broadcasting ? null : _confirmBroadcast,
        ),
      ],
    ];
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

  Widget _actionButton(String label, VoidCallback? onPressed) {
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
