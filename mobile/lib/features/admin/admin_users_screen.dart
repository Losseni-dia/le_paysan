import 'dart:async';

import 'package:flutter/material.dart';

import '../../core/admin/admin_user.dart';
import '../../core/admin/admin_user_service.dart';

const _roleFilters = [
  {'value': null, 'label': 'Tous'},
  {'value': 'ROLE_USER', 'label': 'Utilisateurs'},
  {'value': 'ROLE_VENDEUR', 'label': 'Vendeurs'},
  {'value': 'ROLE_ADMIN', 'label': 'Admins'},
];

const _allRoles = ['ROLE_USER', 'ROLE_VENDEUR', 'ROLE_ADMIN'];

const _roleLabels = {
  'ROLE_USER': 'Utilisateur',
  'ROLE_VENDEUR': 'Vendeur',
  'ROLE_LIVREUR': 'Livreur',
  'ROLE_ADMIN': 'Admin',
};

class AdminUsersScreen extends StatefulWidget {
  const AdminUsersScreen({super.key});

  @override
  State<AdminUsersScreen> createState() => _AdminUsersScreenState();
}

class _AdminUsersScreenState extends State<AdminUsersScreen> {
  final AdminUserService _adminUserService = AdminUserService();
  final TextEditingController _searchController = TextEditingController();

  String? _role;
  List<AdminUser> _users = [];
  bool _loading = true;
  String? _errorMessage;
  Timer? _searchDebounce;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _searchDebounce?.cancel();
    _searchController.dispose();
    super.dispose();
  }

  void _onSearchChanged(String _) {
    _searchDebounce?.cancel();
    _searchDebounce = Timer(const Duration(milliseconds: 400), _load);
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _errorMessage = null;
    });
    try {
      final users = await _adminUserService.list(
        search: _searchController.text.trim(),
        role: _role,
      );
      setState(() => _users = users);
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger les utilisateurs.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _toggleEnabled(AdminUser user) async {
    try {
      final updated = await _adminUserService.setEnabled(user.id, !user.enabled);
      setState(() {
        _users = _users.map((u) => u.id == updated.id ? updated : u).toList();
      });
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text("Impossible d'effectuer cette action.")),
        );
      }
    }
  }

  Future<void> _editRoles(AdminUser user) async {
    final selected = Set<String>.from(user.roles.where(_allRoles.contains));
    final result = await showDialog<Set<String>>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: Text('Rôles de ${user.firstName} ${user.lastName}'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: _allRoles.map((role) {
              return CheckboxListTile(
                title: Text(_roleLabels[role] ?? role),
                value: selected.contains(role),
                onChanged: (checked) {
                  setDialogState(() {
                    if (checked == true) {
                      selected.add(role);
                    } else {
                      selected.remove(role);
                    }
                  });
                },
              );
            }).toList(),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.of(context).pop(), child: const Text('Annuler')),
            TextButton(
              onPressed: () => Navigator.of(context).pop(selected),
              child: const Text('Enregistrer'),
            ),
          ],
        ),
      ),
    );
    if (result == null || result.isEmpty) return;
    try {
      final updated = await _adminUserService.setRoles(user.id, result.toList());
      setState(() {
        _users = _users.map((u) => u.id == updated.id ? updated : u).toList();
      });
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Impossible de mettre à jour les rôles.')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Utilisateurs')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
            child: Wrap(
              spacing: 8,
              children: _roleFilters.map((f) {
                final selected = _role == f['value'];
                return ChoiceChip(
                  label: Text(f['label'] as String),
                  selected: selected,
                  onSelected: (_) {
                    setState(() => _role = f['value']);
                    _load();
                  },
                );
              }).toList(),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: TextField(
              controller: _searchController,
              decoration: const InputDecoration(
                hintText: 'Rechercher (nom, identifiant, email)...',
                prefixIcon: Icon(Icons.search),
              ),
              onChanged: _onSearchChanged,
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
    if (_users.isEmpty) {
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          children: const [
            Padding(
              padding: EdgeInsets.symmetric(vertical: 48),
              child: Center(child: Text('Aucun utilisateur trouvé.')),
            ),
          ],
        ),
      );
    }
    return RefreshIndicator(
      onRefresh: _load,
      child: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: _users.length,
        separatorBuilder: (_, _) => const SizedBox(height: 12),
        itemBuilder: (context, index) {
          final user = _users[index];
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
                        child: Text(
                          '${user.firstName} ${user.lastName}',
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: user.enabled ? const Color(0xFFD7ECDF) : const Color(0xFFE1E8E3),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Text(user.enabled ? 'Actif' : 'Désactivé', style: const TextStyle(fontSize: 12)),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text('@${user.username}', style: const TextStyle(fontSize: 12, color: Colors.black54)),
                  if (user.email != null) Text(user.email!, style: const TextStyle(fontSize: 12, color: Colors.black54)),
                  const SizedBox(height: 8),
                  Wrap(
                    spacing: 6,
                    children: user.roles
                        .map(
                          (r) => Chip(
                            label: Text(_roleLabels[r] ?? r, style: const TextStyle(fontSize: 11)),
                            visualDensity: VisualDensity.compact,
                            materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                          ),
                        )
                        .toList(),
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Expanded(
                        child: OutlinedButton(
                          onPressed: () => _editRoles(user),
                          child: const Text('Modifier les rôles'),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: OutlinedButton(
                          onPressed: () => _toggleEnabled(user),
                          child: Text(user.enabled ? 'Désactiver' : 'Réactiver'),
                        ),
                      ),
                    ],
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
