import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../../../core/auth/auth_service.dart';
import '../../../shared/widgets/auth_card_scaffold.dart';
import '../../../shared/widgets/selfie_capture_field.dart';

/// Deux chemins de recuperation de mot de passe (cf. docs UC-01/UC-01b) :
/// - avec email : lien de reinitialisation classique envoye par email.
/// - sans email (cas majoritaire en Cote d'Ivoire, cf. 01-analyse-besoins.md) : demande
///   de recuperation par selfie, comparee manuellement par un admin au selfie de
///   reference enregistre a l'inscription.
class PasswordRecoveryScreen extends StatefulWidget {
  const PasswordRecoveryScreen({super.key});

  @override
  State<PasswordRecoveryScreen> createState() => _PasswordRecoveryScreenState();
}

enum _RecoveryMode { choice, email, noEmail }

class _PasswordRecoveryScreenState extends State<PasswordRecoveryScreen> {
  _RecoveryMode _mode = _RecoveryMode.choice;

  @override
  Widget build(BuildContext context) {
    final (title, subtitle) = switch (_mode) {
      _RecoveryMode.choice => ('Mot de passe oublié', 'Comment pouvons-nous vous aider ?'),
      _RecoveryMode.email => ('Réinitialisation par email', 'Recevez un lien par email'),
      _RecoveryMode.noEmail => ('Récupération par selfie', 'Vérification par un administrateur'),
    };

    return AuthCardScaffold(
      title: title,
      subtitle: subtitle,
      child: switch (_mode) {
        _RecoveryMode.choice => _ChoiceStep(
            onHasEmail: () => setState(() => _mode = _RecoveryMode.email),
            onNoEmail: () => setState(() => _mode = _RecoveryMode.noEmail),
          ),
        _RecoveryMode.email => _EmailStep(onBack: () => setState(() => _mode = _RecoveryMode.choice)),
        _RecoveryMode.noEmail => _NoEmailStep(onBack: () => setState(() => _mode = _RecoveryMode.choice)),
      },
    );
  }
}

class _ChoiceStep extends StatelessWidget {
  final VoidCallback onHasEmail;
  final VoidCallback onNoEmail;

  const _ChoiceStep({required this.onHasEmail, required this.onNoEmail});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        OutlinedButton.icon(
          onPressed: onHasEmail,
          icon: const Icon(Icons.email_outlined),
          label: const Text("J'ai un email enregistré"),
          style: OutlinedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 16)),
        ),
        const SizedBox(height: 12),
        OutlinedButton.icon(
          onPressed: onNoEmail,
          icon: const Icon(Icons.camera_alt_outlined),
          label: const Text("Je n'ai pas d'email"),
          style: OutlinedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 16)),
        ),
      ],
    );
  }
}

class _EmailStep extends StatefulWidget {
  final VoidCallback onBack;

  const _EmailStep({required this.onBack});

  @override
  State<_EmailStep> createState() => _EmailStepState();
}

class _EmailStepState extends State<_EmailStep> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  bool _submitting = false;
  bool _done = false;
  String? _errorMessage;

  @override
  void dispose() {
    _emailController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _submitting = true;
      _errorMessage = null;
    });
    try {
      await context.read<AuthService>().forgotPassword(_emailController.text.trim());
      if (mounted) setState(() => _done = true);
    } on DioException catch (e) {
      setState(() {
        _errorMessage = (e.response?.data is Map) ? e.response?.data['message'] as String? : null;
        _errorMessage ??= 'Une erreur est survenue, veuillez réessayer.';
      });
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_done) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text(
            'Si cet email correspond à un compte, un lien de réinitialisation vient de lui être envoyé. '
            'Ouvrez-le sur votre navigateur pour choisir un nouveau mot de passe.',
            style: TextStyle(fontSize: 14),
          ),
          const SizedBox(height: 16),
          TextButton(onPressed: widget.onBack, child: const Text('Retour')),
        ],
      );
    }

    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (_errorMessage != null) ...[
            AuthErrorBanner(message: _errorMessage!),
            const SizedBox(height: 16),
          ],
          TextFormField(
            controller: _emailController,
            keyboardType: TextInputType.emailAddress,
            decoration: const InputDecoration(labelText: 'Email', prefixIcon: Icon(Icons.email_outlined)),
            validator: (v) {
              if (v == null || v.isEmpty) return 'Obligatoire';
              if (!RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$').hasMatch(v)) return 'Adresse email invalide';
              return null;
            },
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
                  : const Text('Envoyer le lien', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
            ),
          ),
          const SizedBox(height: 12),
          TextButton(onPressed: widget.onBack, child: const Text('Retour')),
        ],
      ),
    );
  }
}

class _NoEmailStep extends StatefulWidget {
  final VoidCallback onBack;

  const _NoEmailStep({required this.onBack});

  @override
  State<_NoEmailStep> createState() => _NoEmailStepState();
}

class _NoEmailStepState extends State<_NoEmailStep> {
  final _formKey = GlobalKey<FormState>();
  final _usernameController = TextEditingController();
  XFile? _selfie;
  bool _submitting = false;
  bool _done = false;
  String? _errorMessage;

  @override
  void dispose() {
    _usernameController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    if (_selfie == null) {
      setState(() => _errorMessage = 'Veuillez prendre ou sélectionner un selfie.');
      return;
    }
    setState(() {
      _submitting = true;
      _errorMessage = null;
    });
    try {
      await context.read<AuthService>().accountRecoveryRequest(_usernameController.text.trim(), _selfie!);
      if (mounted) setState(() => _done = true);
    } on DioException catch (e) {
      setState(() {
        _errorMessage = (e.response?.data is Map) ? e.response?.data['message'] as String? : null;
        _errorMessage ??= e.response?.statusCode == 404 ? 'Aucun compte trouvé pour ce login.' : 'Une erreur est survenue.';
      });
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_done) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text(
            'Votre demande a été envoyée au support. Un administrateur va comparer votre selfie à votre photo '
            'de référence et vous recontactera (par téléphone si renseigné) pour vous communiquer un mot de '
            'passe temporaire.',
            style: TextStyle(fontSize: 14),
          ),
          const SizedBox(height: 16),
          TextButton(onPressed: widget.onBack, child: const Text('Retour')),
        ],
      );
    }

    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text(
            'Cette procédure est réservée aux comptes sans email, ou en complément du mot de passe oublié '
            'par email.',
            style: TextStyle(fontSize: 13, color: Color(0xFF5B6560)),
          ),
          const SizedBox(height: 16),
          if (_errorMessage != null) ...[
            AuthErrorBanner(message: _errorMessage!),
            const SizedBox(height: 16),
          ],
          TextFormField(
            controller: _usernameController,
            decoration: const InputDecoration(labelText: 'Identifiant', prefixIcon: Icon(Icons.person_outline)),
            validator: (v) => (v == null || v.isEmpty) ? 'Obligatoire' : null,
          ),
          const SizedBox(height: 16),
          SelfieCaptureField(selfie: _selfie, onCaptured: (f) => setState(() => _selfie = f)),
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
                  : const Text('Envoyer ma demande', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
            ),
          ),
          const SizedBox(height: 12),
          TextButton(onPressed: widget.onBack, child: const Text('Retour')),
        ],
      ),
    );
  }
}
