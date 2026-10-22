import 'package:flutter/material.dart';

import '../../core/admin/admin_review.dart';
import '../../core/admin/admin_review_service.dart';

class AdminReviewsScreen extends StatefulWidget {
  const AdminReviewsScreen({super.key});

  @override
  State<AdminReviewsScreen> createState() => _AdminReviewsScreenState();
}

class _AdminReviewsScreenState extends State<AdminReviewsScreen> {
  final AdminReviewService _adminReviewService = AdminReviewService();
  final TextEditingController _searchController = TextEditingController();

  String _tab = 'produits';
  String? _status;
  List<AdminReview> _reviews = [];
  bool _loading = true;
  String? _errorMessage;
  String _query = '';
  int? _moderatingId;

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
      final reviews = _tab == 'produits'
          ? await _adminReviewService.listProductReviews(_status)
          : await _adminReviewService.listShopReviews(_status);
      setState(() => _reviews = reviews);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger les avis.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _moderate(AdminReview review, String newStatus) async {
    String? reason;
    if (newStatus == 'MASQUE') {
      final controller = TextEditingController();
      reason = await showDialog<String>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Motif (optionnel)'),
          content: TextField(controller: controller, maxLines: 3),
          actions: [
            TextButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Annuler')),
            TextButton(
              onPressed: () => Navigator.of(context).pop(controller.text),
              child: const Text('Confirmer'),
            ),
          ],
        ),
      );
      if (reason == null) return;
    }
    setState(() => _moderatingId = review.id);
    try {
      if (_tab == 'produits') {
        await _adminReviewService.moderateProduct(review.id, newStatus, reason?.trim());
      } else {
        await _adminReviewService.moderateShop(review.id, newStatus, reason?.trim());
      }
      await _load();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text("Impossible de mettre à jour cet avis.")),
        );
      }
    } finally {
      if (mounted) setState(() => _moderatingId = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Avis')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
            child: SegmentedButton<String>(
              segments: const [
                ButtonSegment(value: 'produits', label: Text('Produits')),
                ButtonSegment(value: 'boutiques', label: Text('Boutiques')),
              ],
              selected: {_tab},
              onSelectionChanged: (s) {
                setState(() => _tab = s.first);
                _load();
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Wrap(
              spacing: 8,
              children: [
                ChoiceChip(
                  label: const Text('Tous'),
                  selected: _status == null,
                  onSelected: (_) {
                    setState(() => _status = null);
                    _load();
                  },
                ),
                ChoiceChip(
                  label: const Text('Visibles'),
                  selected: _status == 'VISIBLE',
                  onSelected: (_) {
                    setState(() => _status = 'VISIBLE');
                    _load();
                  },
                ),
                ChoiceChip(
                  label: const Text('Masqués'),
                  selected: _status == 'MASQUE',
                  onSelected: (_) {
                    setState(() => _status = 'MASQUE');
                    _load();
                  },
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
            child: TextField(
              controller: _searchController,
              decoration: const InputDecoration(
                hintText: 'Rechercher (produit/boutique, auteur, commentaire)...',
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

    final filtered = _reviews.where((r) => r.matchesSearch(_query)).toList();

    if (_reviews.isEmpty || filtered.isEmpty) {
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 48),
              child: Center(
                child: Text(_reviews.isEmpty ? 'Aucun avis.' : 'Aucun résultat pour cette recherche.'),
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
          final review = filtered[index];
          final busy = _moderatingId == review.id;
          return Card(
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Expanded(
                        child: Text(review.subjectName, style: const TextStyle(fontWeight: FontWeight.bold)),
                      ),
                      Row(
                        children: List.generate(
                          5,
                          (i) => Icon(
                            i < review.rating ? Icons.star : Icons.star_border,
                            size: 16,
                            color: const Color(0xFFC98A1E),
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text('Par ${review.buyerFullName}', style: const TextStyle(fontSize: 12, color: Colors.black54)),
                  if (review.comment != null && review.comment!.isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Text(review.comment!),
                  ],
                  if (review.moderationReason != null) ...[
                    const SizedBox(height: 6),
                    Text(
                      'Motif : ${review.moderationReason}',
                      style: const TextStyle(fontSize: 12, color: Color(0xFFB3261E)),
                    ),
                  ],
                  const SizedBox(height: 10),
                  SizedBox(
                    height: 40,
                    child: OutlinedButton(
                      onPressed: busy ? null : () => _moderate(review, review.status == 'VISIBLE' ? 'MASQUE' : 'VISIBLE'),
                      child: busy
                          ? const SizedBox(
                              width: 16,
                              height: 16,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : Text(review.status == 'VISIBLE' ? 'Masquer' : 'Republier'),
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
