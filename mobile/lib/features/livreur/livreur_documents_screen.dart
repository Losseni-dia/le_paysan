import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../core/livreur/livreur_profile.dart';
import '../../core/livreur/livreur_service.dart';
import '../../core/product/product_service.dart';
import '../../shared/widgets/authenticated_image.dart';

class LivreurDocumentsScreen extends StatefulWidget {
  final LivreurProfile profile;

  const LivreurDocumentsScreen({super.key, required this.profile});

  @override
  State<LivreurDocumentsScreen> createState() => _LivreurDocumentsScreenState();
}

class _LivreurDocumentsScreenState extends State<LivreurDocumentsScreen> {
  final LivreurService _livreurService = LivreurService();
  final ImagePicker _imagePicker = ImagePicker();

  XFile? _photo;
  XFile? _idDocumentFront;
  XFile? _idDocumentBack;
  XFile? _selfieWithId;
  bool _submitting = false;
  String? _errorMessage;

  Future<void> _pick(ValueSetter<XFile?> onPicked) async {
    final photo = await _imagePicker.pickImage(source: ImageSource.camera);
    if (photo != null) setState(() => onPicked(photo));
  }

  Future<void> _submit() async {
    if (_photo == null &&
        _idDocumentFront == null &&
        _idDocumentBack == null &&
        _selfieWithId == null) {
      setState(() => _errorMessage = 'Veuillez fournir au moins un document.');
      return;
    }
    setState(() {
      _submitting = true;
      _errorMessage = null;
    });
    try {
      await _livreurService.uploadDocuments(
        photo: _photo,
        idDocumentFront: _idDocumentFront,
        idDocumentBack: _idDocumentBack,
        selfieWithId: _selfieWithId,
      );
      if (mounted) Navigator.of(context).pop(true);
    } catch (_) {
      setState(() => _errorMessage = "Impossible d'envoyer vos documents.");
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mes documents')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const Text(
            "Ces documents sont nécessaires pour valider votre dossier et commencer à recevoir des demandes de livraison.",
            style: TextStyle(fontSize: 13, color: Colors.black54),
          ),
          const SizedBox(height: 20),
          if (_errorMessage != null) ...[
            Text(_errorMessage!, style: const TextStyle(color: Color(0xFFB3261E))),
            const SizedBox(height: 12),
          ],
          _docSlot(
            label: 'Photo',
            existingUrl: widget.profile.photoUrl,
            picked: _photo,
            onPick: () => _pick((f) => _photo = f),
          ),
          const SizedBox(height: 16),
          _docSlot(
            label: "Pièce d'identité (recto)",
            existingUrl: widget.profile.idDocumentFrontUrl,
            picked: _idDocumentFront,
            onPick: () => _pick((f) => _idDocumentFront = f),
          ),
          const SizedBox(height: 16),
          _docSlot(
            label: "Pièce d'identité (verso)",
            existingUrl: widget.profile.idDocumentBackUrl,
            picked: _idDocumentBack,
            onPick: () => _pick((f) => _idDocumentBack = f),
          ),
          const SizedBox(height: 16),
          _docSlot(
            label: 'Selfie avec la pièce',
            existingUrl: widget.profile.selfieWithIdUrl,
            picked: _selfieWithId,
            onPick: () => _pick((f) => _selfieWithId = f),
          ),
          const SizedBox(height: 24),
          SizedBox(
            height: 52,
            child: FilledButton(
              onPressed: _submitting ? null : _submit,
              child: _submitting
                  ? const SizedBox(
                      width: 22,
                      height: 22,
                      child: CircularProgressIndicator(strokeWidth: 2.5, color: Colors.white),
                    )
                  : const Text('Envoyer'),
            ),
          ),
        ],
      ),
    );
  }

  Widget _docSlot({
    required String label,
    required String? existingUrl,
    required XFile? picked,
    required VoidCallback onPick,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
        const SizedBox(height: 8),
        ClipRRect(
          borderRadius: BorderRadius.circular(12),
          child: picked != null
              ? Image.file(File(picked.path), height: 160, fit: BoxFit.cover)
              : existingUrl != null
              ? AuthenticatedImage(url: ProductService.imageUrl(existingUrl), height: 160, fit: BoxFit.cover)
              : Container(
                  height: 160,
                  color: const Color(0xFFE1E8E3),
                  child: const Center(child: Icon(Icons.image_outlined, color: Colors.black38)),
                ),
        ),
        const SizedBox(height: 8),
        OutlinedButton.icon(
          onPressed: onPick,
          icon: const Icon(Icons.camera_alt_outlined),
          label: Text(picked == null ? 'Prendre une photo' : 'Reprendre la photo'),
        ),
      ],
    );
  }
}
