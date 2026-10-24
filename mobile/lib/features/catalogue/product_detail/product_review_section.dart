import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/auth/auth_service.dart';
import '../../../core/review/review.dart';
import '../../../core/review/review_service.dart';

/// Section avis produit (cf. web shared/ui/review-section) : note moyenne, liste des avis,
/// et formulaire pour l'acheteur ayant reçu ce produit (commande livrée).
class ProductReviewSection extends StatefulWidget {
  final int productId;

  const ProductReviewSection({super.key, required this.productId});

  @override
  State<ProductReviewSection> createState() => _ProductReviewSectionState();
}

class _ProductReviewSectionState extends State<ProductReviewSection> {
  final ReviewService _service = ReviewService();

  RatingSummary? _summary;
  List<ProductReview> _reviews = [];
  ProductReview? _myReview;
  ReviewEligibility? _eligibility;
  bool _loading = true;

  bool _editing = false;
  int _formRating = 0;
  final _commentController = TextEditingController();
  bool _submitting = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _commentController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    final isAuthenticated = context.read<AuthService>().isAuthenticated;
    try {
      final results = await Future.wait([
        _service.getSummary(widget.productId),
        _service.listForProduct(widget.productId),
      ]);
      _summary = results[0] as RatingSummary;
      _reviews = results[1] as List<ProductReview>;

    } catch (_) {
      // Pas bloquant : la section reste affichable avec ce qui a pu etre charge.
    }

    if (isAuthenticated) {
      // Appels independants : un echec sur l'un ne doit jamais masquer le bouton
      // "Laisser un avis" si l'autre a reussi (cf. bug remonte par l'utilisateur).
      try {
        _myReview = await _service.getMyReview(widget.productId);
      } catch (_) {
        _myReview = null;
      }
      try {
        _eligibility = await _service.getEligibility(widget.productId);
      } catch (_) {
        _eligibility = null;
      }
    }

    if (mounted) setState(() => _loading = false);
  }

  void _startEdit() {
    setState(() {
      _formRating = _myReview?.rating ?? 0;
      _commentController.text = _myReview?.comment ?? '';
      _editing = true;
      _error = null;
    });
  }

  Future<void> _submit() async {
    if (_formRating < 1) {
      setState(() => _error = 'Choisissez une note.');
      return;
    }
    setState(() {
      _submitting = true;
      _error = null;
    });
    try {
      await _service.submit(widget.productId, rating: _formRating, comment: _commentController.text.trim());
      setState(() => _editing = false);
      await _load();
    } on DioException catch (e) {
      setState(() {
        _error = (e.response?.data is Map)
            ? e.response?.data['message'] as String?
            : null;
        _error ??= "Impossible d'enregistrer votre avis.";
      });
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  String? get _ineligibleMessage {
    final e = _eligibility;
    if (e == null || e.eligible) return null;
    if (e.reason == 'OWNER') return 'Vous ne pouvez pas noter votre propre produit.';
    return 'Vous devez avoir reçu ce produit (commande livrée) pour pouvoir laisser un avis.';
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 16),
        child: Center(child: CircularProgressIndicator()),
      );
    }

    final auth = context.watch<AuthService>();
    final summary = _summary;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Avis', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        const SizedBox(height: 8),
        if (summary != null && summary.count > 0)
          Row(
            children: [
              const Icon(Icons.star, size: 18, color: Color(0xFFC98A1E)),
              const SizedBox(width: 4),
              Text(
                '${summary.average.toStringAsFixed(1)} (${summary.count} avis)',
                style: const TextStyle(fontSize: 14),
              ),
            ],
          )
        else
          const Text('Aucun avis pour le moment.', style: TextStyle(fontSize: 13, color: Colors.black54)),
        const SizedBox(height: 12),
        if (auth.isAuthenticated) ...[
          if (_editing)
            _buildForm()
          else if (_myReview != null)
            _buildMyReview(_myReview!)
          else if (_ineligibleMessage != null)
            Text(_ineligibleMessage!, style: const TextStyle(fontSize: 13, color: Colors.black54))
          else
            // Par defaut on affiche le bouton (meme si l'appel d'eligibilite a echoue) : le
            // backend reste la source de verite et renverra une erreur claire a la soumission
            // si l'achat n'est pas eligible (cf. bug remonte : le bouton ne devait jamais
            // disparaitre silencieusement sur un simple echec reseau).
            OutlinedButton(onPressed: _startEdit, child: const Text('Laisser un avis')),
          const SizedBox(height: 16),
        ],
        if (_reviews.isNotEmpty) ...[
          const Divider(height: 24),
          for (final review in _reviews) _buildReviewItem(review),
        ],
      ],
    );
  }

  Widget _buildForm() {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFFF4F7F5),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (_error != null) ...[
            Text(_error!, style: const TextStyle(color: Color(0xFFB3261E), fontSize: 13)),
            const SizedBox(height: 8),
          ],
          Row(
            children: List.generate(5, (i) {
              final filled = i < _formRating;
              return IconButton(
                padding: EdgeInsets.zero,
                constraints: const BoxConstraints(),
                onPressed: () => setState(() => _formRating = i + 1),
                icon: Icon(
                  filled ? Icons.star : Icons.star_border,
                  color: const Color(0xFFC98A1E),
                  size: 28,
                ),
              );
            }),
          ),
          const SizedBox(height: 8),
          TextField(
            controller: _commentController,
            maxLines: 3,
            decoration: const InputDecoration(hintText: 'Votre commentaire (optionnel)'),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              FilledButton(
                onPressed: _submitting ? null : _submit,
                child: _submitting
                    ? const SizedBox(
                        width: 18,
                        height: 18,
                        child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                      )
                    : const Text('Envoyer'),
              ),
              const SizedBox(width: 8),
              TextButton(
                onPressed: _submitting ? null : () => setState(() => _editing = false),
                child: const Text('Annuler'),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildMyReview(ProductReview review) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFFF4F7F5),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Votre avis', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
                const SizedBox(height: 4),
                _starsRow(review.rating),
                if (review.comment != null && review.comment!.isNotEmpty) ...[
                  const SizedBox(height: 4),
                  Text(review.comment!, style: const TextStyle(fontSize: 13)),
                ],
              ],
            ),
          ),
          TextButton(onPressed: _startEdit, child: const Text('Modifier')),
        ],
      ),
    );
  }

  Widget _buildReviewItem(ProductReview review) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(review.buyerFullName, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
              ),
              _starsRow(review.rating),
            ],
          ),
          if (review.comment != null && review.comment!.isNotEmpty) ...[
            const SizedBox(height: 4),
            Text(review.comment!, style: const TextStyle(fontSize: 13)),
          ],
        ],
      ),
    );
  }

  Widget _starsRow(int rating) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: List.generate(5, (i) {
        return Icon(
          i < rating ? Icons.star : Icons.star_border,
          size: 14,
          color: const Color(0xFFC98A1E),
        );
      }),
    );
  }
}
