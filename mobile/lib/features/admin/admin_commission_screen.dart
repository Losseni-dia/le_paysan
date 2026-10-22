import 'package:flutter/material.dart';

import '../../core/admin/admin_commission.dart';
import '../../core/admin/admin_commission_service.dart';
import '../../core/order/order_status_labels.dart';

class AdminCommissionScreen extends StatefulWidget {
  const AdminCommissionScreen({super.key});

  @override
  State<AdminCommissionScreen> createState() => _AdminCommissionScreenState();
}

class _AdminCommissionScreenState extends State<AdminCommissionScreen> {
  final AdminCommissionService _adminCommissionService = AdminCommissionService();
  final TextEditingController _rateController = TextEditingController();

  CommissionRate? _current;
  List<CommissionRate> _history = [];
  bool _loading = true;
  bool _saving = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _rateController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _errorMessage = null;
    });
    try {
      final current = await _adminCommissionService.getCurrent();
      final history = await _adminCommissionService.getHistory();
      setState(() {
        _current = current;
        _history = history;
        _rateController.text = current.rate.toStringAsFixed(2);
      });
    } catch (_) {
      setState(() => _errorMessage = 'Impossible de charger la commission.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _update() async {
    final rate = double.tryParse(_rateController.text.replaceAll(',', '.'));
    if (rate == null || rate < 0 || rate > 100) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Entrez un taux valide entre 0 et 100.')),
      );
      return;
    }
    setState(() => _saving = true);
    try {
      await _adminCommissionService.updateRate(rate);
      await _load();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Impossible de mettre à jour la commission.')),
        );
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Commission')),
      body: _buildBody(),
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

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Taux actuel', style: TextStyle(fontWeight: FontWeight.w600)),
                const SizedBox(height: 4),
                Text(
                  '${_current?.rate.toStringAsFixed(2)} %',
                  style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFF1B5E3A)),
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: _rateController,
                  keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  decoration: const InputDecoration(labelText: 'Nouveau taux (%)'),
                ),
                const SizedBox(height: 16),
                SizedBox(
                  height: 48,
                  child: FilledButton(
                    onPressed: _saving ? null : _update,
                    child: _saving
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                          )
                        : const Text('Mettre à jour'),
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 20),
        const Text('Historique', style: TextStyle(fontWeight: FontWeight.w600)),
        const SizedBox(height: 8),
        for (final entry in _history)
          Card(
            child: ListTile(
              title: Text('${entry.rate.toStringAsFixed(2)} %'),
              subtitle: entry.createdAt != null ? Text(formatDateTime(entry.createdAt!)) : null,
              trailing: entry.active
                  ? Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: const Color(0xFFD7ECDF),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Text('Actif', style: TextStyle(fontSize: 12)),
                    )
                  : null,
            ),
          ),
      ],
    );
  }
}
