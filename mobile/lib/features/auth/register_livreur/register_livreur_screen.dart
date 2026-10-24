import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/auth/auth_service.dart';
import '../../../core/cart/cart_service.dart';
import '../../../core/catalogue/ci_cities.dart';
import '../../../shared/widgets/auth_card_scaffold.dart';

class RegisterLivreurScreen extends StatefulWidget {
  const RegisterLivreurScreen({super.key});

  @override
  State<RegisterLivreurScreen> createState() => _RegisterLivreurScreenState();
}

class _RegisterLivreurScreenState extends State<RegisterLivreurScreen> {
  final _formKey = GlobalKey<FormState>();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  final _firstNameController = TextEditingController();
  final _lastNameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _confirmPasswordController = TextEditingController();

  CiCity? _city;
  bool _submitting = false;
  bool _obscurePassword = true;
  bool _obscureConfirmPassword = true;
  String? _errorMessage;

  bool _acceptCgu = false;
  bool _acceptCgv = false;
  bool _acceptPrivacy = false;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    _firstNameController.dispose();
    _lastNameController.dispose();
    _phoneController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
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
      await context.read<AuthService>().registerLivreur(
        username: _usernameController.text.trim(),
        password: _passwordController.text,
        firstName: _firstNameController.text.trim(),
        lastName: _lastNameController.text.trim(),
        phone: _phoneController.text.trim(),
        city: _city!.name,
        cityLat: _city!.lat,
        cityLng: _city!.lng,
        acceptCgu: _acceptCgu,
        acceptCgv: _acceptCgv,
        acceptPrivacy: _acceptPrivacy,
      );
      if (!mounted) return;
      await context.read<CartService>().fetch().catchError((_) {});
      if (mounted) Navigator.of(context).popUntil((route) => route.isFirst);
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
    return AuthCardScaffold(
      title: 'Devenir livreur',
      subtitle: 'Rejoignez Le Paysan comme livreur indépendant',
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            if (_errorMessage != null) ...[
              AuthErrorBanner(message: _errorMessage!),
              const SizedBox(height: 16),
            ],
            Row(
              children: [
                Expanded(
                  child: TextFormField(
                    controller: _firstNameController,
                    decoration: const InputDecoration(labelText: 'Prénom'),
                    validator: (v) => (v == null || v.isEmpty) ? 'Obligatoire' : null,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: TextFormField(
                    controller: _lastNameController,
                    decoration: const InputDecoration(labelText: 'Nom'),
                    validator: (v) => (v == null || v.isEmpty) ? 'Obligatoire' : null,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _usernameController,
              decoration: const InputDecoration(
                labelText: 'Identifiant',
                prefixIcon: Icon(Icons.person_outline),
              ),
              validator: (v) {
                if (v == null || v.isEmpty) return 'Obligatoire';
                if (!RegExp(r'^[a-zA-Z0-9._-]{3,30}$').hasMatch(v)) {
                  return 'Login invalide (3-30 caractères, lettres/chiffres/._-)';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _phoneController,
              keyboardType: TextInputType.phone,
              decoration: const InputDecoration(
                labelText: 'Téléphone (optionnel)',
                prefixIcon: Icon(Icons.phone_outlined),
              ),
            ),
            const SizedBox(height: 16),
            DropdownButtonFormField<CiCity>(
              initialValue: _city,
              decoration: const InputDecoration(
                labelText: 'Localité',
                prefixIcon: Icon(Icons.location_city_outlined),
              ),
              items: ciCities
                  .map((c) => DropdownMenuItem(value: c, child: Text(c.name)))
                  .toList(),
              onChanged: (c) => setState(() => _city = c),
              validator: (v) => v == null ? 'Obligatoire' : null,
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _passwordController,
              obscureText: _obscurePassword,
              decoration: InputDecoration(
                labelText: 'Mot de passe',
                prefixIcon: const Icon(Icons.lock_outline),
                suffixIcon: IconButton(
                  icon: Icon(
                    _obscurePassword ? Icons.visibility_outlined : Icons.visibility_off_outlined,
                  ),
                  onPressed: () => setState(() => _obscurePassword = !_obscurePassword),
                ),
              ),
              validator: (v) {
                if (v == null || v.length < 8) return 'Au moins 8 caractères';
                if (!RegExp(r'(?=.*[0-9])(?=.*[A-Za-z])').hasMatch(v)) {
                  return 'Au moins une lettre et un chiffre';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _confirmPasswordController,
              obscureText: _obscureConfirmPassword,
              decoration: InputDecoration(
                labelText: 'Confirmer le mot de passe',
                prefixIcon: const Icon(Icons.lock_outline),
                suffixIcon: IconButton(
                  icon: Icon(
                    _obscureConfirmPassword
                        ? Icons.visibility_outlined
                        : Icons.visibility_off_outlined,
                  ),
                  onPressed: () =>
                      setState(() => _obscureConfirmPassword = !_obscureConfirmPassword),
                ),
              ),
              validator: (v) {
                if (v == null || v.isEmpty) return 'Obligatoire';
                if (v != _passwordController.text) {
                  return 'Les mots de passe ne correspondent pas';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),
            _ConsentCheckbox(
              label: "J'accepte les CGU",
              value: _acceptCgu,
              onChanged: (v) => setState(() => _acceptCgu = v),
            ),
            _ConsentCheckbox(
              label: "J'accepte les CGV",
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
                    : const Text(
                        "S'inscrire",
                        style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
                      ),
              ),
            ),
          ],
        ),
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
