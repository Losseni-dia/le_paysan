import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

import '../../core/review/review_service.dart';

/// Ouvert directement depuis "Mes commandes" (commande livree) : va droit au formulaire de
/// notation, sans passer par la fiche produit (cf. demande utilisateur).
class WriteReviewScreen extends StatefulWidget {
  final int productId;
  final String productName;

  const WriteReviewScreen({super.key, required this.productId, required this.productName});

  @override
  State<WriteReviewScreen> createState() => _WriteReviewScreenState();
}

class _WriteReviewScreenState extends State<WriteReviewScreen> {
  final ReviewService _service = ReviewService();

  bool _loading = true;
  int _rating = 0;
  final _commentController = TextEditingController();
  bool _submitting = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadExisting();
  }

  @override
  void dispose() {
    _commentController.dispose();
    super.dispose();
  }

  Future<void> _loadExisting() async {
    try {
      final existing = await _service.getMyReview(widget.productId);
      if (existing != null) {
        _rating = existing.rating;
        _commentController.text = existing.comment ?? '';
      }
    } catch (_) {
      // Pas d'avis existant ou erreur reseau : on part d'un formulaire vierge.
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _submit() async {
    if (_rating < 1) {
      setState(() => _error = 'Choisissez une note.');
      return;
    }
    setState(() {
      _submitting = true;
      _error = null;
    });
    try {
      final review = await _service.submit(
        widget.productId,
        rating: _rating,
        comment: _commentController.text.trim(),
      );
      if (mounted) Navigator.of(context).pop(review);
    } on DioException catch (e) {
      setState(() {
        _error = (e.response?.data is Map) ? e.response?.data['message'] as String? : null;
        _error ??= "Impossible d'enregistrer votre avis.";
      });
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Donner mon avis')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : ListView(
              padding: const EdgeInsets.all(16),
              children: [
                Text(
                  widget.productName,
                  style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 20),
                if (_error != null) ...[
                  Text(_error!, style: const TextStyle(color: Color(0xFFB3261E))),
                  const SizedBox(height: 16),
                ],
                const Text('Votre note', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
                const SizedBox(height: 8),
                Row(
                  children: List.generate(5, (i) {
                    final filled = i < _rating;
                    return IconButton(
                      onPressed: () => setState(() => _rating = i + 1),
                      icon: Icon(
                        filled ? Icons.star : Icons.star_border,
                        color: const Color(0xFFC98A1E),
                        size: 36,
                      ),
                    );
                  }),
                ),
                const SizedBox(height: 16),
                const Text('Votre commentaire (optionnel)', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
                const SizedBox(height: 8),
                TextField(
                  controller: _commentController,
                  maxLines: 4,
                  decoration: const InputDecoration(hintText: 'Qu\'avez-vous pensé de ce produit ?'),
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
                        : const Text('Envoyer mon avis'),
                  ),
                ),
              ],
            ),
    );
  }
}
