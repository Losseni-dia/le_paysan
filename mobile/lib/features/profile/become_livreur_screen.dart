import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/auth/auth_service.dart';
import '../../core/catalogue/ci_cities.dart';

/// Passage du compte connecte au role livreur : contrairement a l'inscription livreur
/// independante (cf. RegisterLivreurScreen), ici le nom/prenom/telephone sont deja connus
/// (compte existant) - on ne redemande que la localite et les acceptations specifiques.
class BecomeLivreurScreen extends StatefulWidget {
  const BecomeLivreurScreen({super.key});

  @override
  State<BecomeLivreurScreen> createState() => _BecomeLivreurScreenState();
}

class _BecomeLivreurScreenState extends State<BecomeLivreurScreen> {
  CiCity? _city;
  bool _acceptCgu = false;
  bool _acceptCgv = false;
  bool _acceptPrivacy = false;
  bool _submitting = false;
  String? _errorMessage;

  Future<void> _submit() async {
    if (_city == null) {
      setState(() => _errorMessage = 'Veuillez choisir votre localité.');
      return;
    }
    if (!_acceptCgu || !_acceptCgv || !_acceptPrivacy) {
      setState(
        () => _errorMessage =
            "Vous devez accepter les CGU, les CGV et la politique de confidentialité.",
      );
      return;
    }

    setState(() {
      _submitting = true;
      _errorMessage = null;
    });
    try {
      await context.read<AuthService>().becomeLivreur(
        city: _city!.name,
        cityLat: _city!.lat,
        cityLng: _city!.lng,
        acceptCgu: _acceptCgu,
        acceptCgv: _acceptCgv,
        acceptPrivacy: _acceptPrivacy,
      );
      if (mounted) Navigator.of(context).pop(true);
    } on DioException catch (e) {
      setState(() {
        _errorMessage = (e.response?.data is Map)
            ? e.response?.data['message'] as String?
            : null;
        _errorMessage ??= 'Une erreur est survenue, veuillez réessayer.';
      });
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Devenir livreur')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const Text(
            'Indiquez votre localité pour recevoir des demandes de livraison à proximité.',
            style: TextStyle(fontSize: 13, color: Colors.black54),
          ),
          const SizedBox(height: 20),
          if (_errorMessage != null) ...[
            Text(_errorMessage!, style: const TextStyle(color: Color(0xFFB3261E))),
            const SizedBox(height: 12),
          ],
          DropdownButtonFormField<CiCity>(
            initialValue: _city,
            decoration: const InputDecoration(
              labelText: 'Localité',
              prefixIcon: Icon(Icons.location_city_outlined),
            ),
            items: ciCities.map((c) => DropdownMenuItem(value: c, child: Text(c.name))).toList(),
            onChanged: (c) => setState(() => _city = c),
          ),
          const SizedBox(height: 16),
          _ConsentCheckbox(
            label: "J'accepte les CGU livreur",
            value: _acceptCgu,
            onChanged: (v) => setState(() => _acceptCgu = v),
          ),
          _ConsentCheckbox(
            label: "J'accepte les CGV livreur",
            value: _acceptCgv,
            onChanged: (v) => setState(() => _acceptCgv = v),
          ),
          _ConsentCheckbox(
            label: "J'ai pris connaissance de la politique de confidentialité",
            value: _acceptPrivacy,
            onChanged: (v) => setState(() => _acceptPrivacy = v),
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
                  : const Text('Devenir livreur'),
            ),
          ),
        ],
      ),
    );
  }
}

class _ConsentCheckbox extends StatelessWidget {
  final String label;
  final bool value;
  final ValueChanged<bool> onChanged;

  const _ConsentCheckbox({required this.label, required this.value, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () => onChanged(!value),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 4),
        child: Row(
          children: [
            Checkbox(value: value, onChanged: (v) => onChanged(v ?? false)),
            Expanded(child: Text(label, style: const TextStyle(fontSize: 13))),
          ],
        ),
      ),
    );
  }
}
