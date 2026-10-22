import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

import '../../core/admin/admin_category.dart';
import '../../core/admin/admin_category_service.dart';

class AdminCategoriesScreen extends StatefulWidget {
  const AdminCategoriesScreen({super.key});

  @override
  State<AdminCategoriesScreen> createState() => _AdminCategoriesScreenState();
}

class _AdminCategoriesScreenState extends State<AdminCategoriesScreen> {
  final AdminCategoryService _adminCategoryService = AdminCategoryService();
  final TextEditingController _searchController = TextEditingController();

  List<AdminCategory> _categories = [];
  bool _loading = true;
  String? _errorMessage;
  String _query = '';
  int? _validatingId;
  bool _creating = false;
  int? _editingId;
  int? _deletingId;

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
      final categories = await _adminCategoryService.listForAdmin();
      setState(() => _categories = categories);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger les catégories.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _validate(AdminCategory category) async {
    setState(() => _validatingId = category.id);
    try {
      final updated = await _adminCategoryService.validate(category.id);
      setState(() {
        _categories = _categories.map((c) => c.id == updated.id ? updated : c).toList();
      });
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Impossible de valider cette catégorie.')),
        );
      }
    } finally {
      if (mounted) setState(() => _validatingId = null);
    }
  }

  Future<void> _showCreateDialog() async {
    final controller = TextEditingController();
    final name = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Nouvelle catégorie'),
        content: TextField(
          controller: controller,
          decoration: const InputDecoration(hintText: 'Ex. Légumes'),
          autofocus: true,
        ),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Annuler')),
          TextButton(
            onPressed: () => Navigator.of(context).pop(controller.text),
            child: const Text('Ajouter'),
          ),
        ],
      ),
    );
    if (name == null || name.trim().isEmpty) return;
    setState(() => _creating = true);
    try {
      final created = await _adminCategoryService.create(name.trim());
      setState(() => _categories = [..._categories, created]);
    } catch (e) {
      if (mounted) {
        final message = e is DioException && e.response?.statusCode == 409
            ? 'Cette catégorie existe déjà.'
            : "Impossible d'ajouter cette catégorie.";
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
      }
    } finally {
      if (mounted) setState(() => _creating = false);
    }
  }

  Future<void> _showEditDialog(AdminCategory category) async {
    final controller = TextEditingController(text: category.name);
    final name = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Modifier la catégorie'),
        content: TextField(controller: controller, autofocus: true),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Annuler')),
          TextButton(
            onPressed: () => Navigator.of(context).pop(controller.text),
            child: const Text('Enregistrer'),
          ),
        ],
      ),
    );
    if (name == null || name.trim().isEmpty || name.trim() == category.name) return;
    setState(() => _editingId = category.id);
    try {
      final updated = await _adminCategoryService.update(category.id, name.trim());
      setState(() {
        _categories = _categories.map((c) => c.id == updated.id ? updated : c).toList();
      });
    } catch (e) {
      if (mounted) {
        final message = e is DioException && e.response?.statusCode == 409
            ? 'Cette catégorie existe déjà.'
            : "Impossible de modifier cette catégorie.";
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
      }
    } finally {
      if (mounted) setState(() => _editingId = null);
    }
  }

  Future<void> _delete(AdminCategory category) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Supprimer cette catégorie ?'),
        content: Text('"${category.name}" sera définitivement supprimée.'),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Annuler')),
          TextButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('Supprimer')),
        ],
      ),
    );
    if (confirmed != true) return;
    setState(() => _deletingId = category.id);
    try {
      await _adminCategoryService.delete(category.id);
      setState(() => _categories = _categories.where((c) => c.id != category.id).toList());
    } catch (e) {
      if (mounted) {
        final message = e is DioException && e.response?.statusCode == 409
            ? 'Cette catégorie est utilisée par des produits.'
            : 'Impossible de supprimer cette catégorie.';
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
      }
    } finally {
      if (mounted) setState(() => _deletingId = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Catégories'),
        actions: [
          IconButton(
            icon: _creating
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.add),
            onPressed: _creating ? null : _showCreateDialog,
          ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: TextField(
              controller: _searchController,
              decoration: const InputDecoration(
                hintText: 'Rechercher une catégorie...',
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

    final filtered = _categories
        .where((c) => c.name.toLowerCase().contains(_query.trim().toLowerCase()))
        .toList();

    if (filtered.isEmpty) {
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          children: const [
            Padding(
              padding: EdgeInsets.symmetric(vertical: 48),
              child: Center(child: Text('Aucun résultat.')),
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
        separatorBuilder: (_, _) => const SizedBox(height: 8),
        itemBuilder: (context, index) {
          final category = filtered[index];
          final validating = _validatingId == category.id;
          final editing = _editingId == category.id;
          final deleting = _deletingId == category.id;
          return Card(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
              child: Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(category.name, style: const TextStyle(fontWeight: FontWeight.w600)),
                        Text(category.slug, style: const TextStyle(fontSize: 12, color: Colors.black54)),
                        if (category.status == 'EN_ATTENTE') ...[
                          const SizedBox(height: 4),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                            decoration: BoxDecoration(
                              color: const Color(0xFFFFF3E0),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: const Text(
                              'Suggérée par un vendeur',
                              style: TextStyle(fontSize: 11, color: Color(0xFF8A5A00)),
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),
                  if (category.status == 'EN_ATTENTE')
                    IconButton(
                      tooltip: 'Valider',
                      icon: validating
                          ? const SizedBox(
                              width: 18,
                              height: 18,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : const Icon(Icons.check_circle_outline, color: Color(0xFF1B5E3A)),
                      onPressed: validating ? null : () => _validate(category),
                    ),
                  IconButton(
                    tooltip: 'Modifier',
                    icon: editing
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.edit_outlined),
                    onPressed: editing ? null : () => _showEditDialog(category),
                  ),
                  IconButton(
                    tooltip: 'Supprimer',
                    icon: deleting
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Color(0xFFB3261E)),
                          )
                        : const Icon(Icons.delete_outline, color: Color(0xFFB3261E)),
                    onPressed: deleting ? null : () => _delete(category),
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
