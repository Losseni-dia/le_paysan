import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../../../core/auth/auth_service.dart';
import '../../../shared/widgets/auth_card_scaffold.dart';
import '../../../shared/widgets/selfie_capture_field.dart';

/// Propose a l'utilisateur, juste apres l'inscription s'il n'a pas renseigne d'email,
/// d'enregistrer un selfie de reference. Sans email ni selfie, un compte ne peut pas
/// etre recupere en cas de mot de passe oublie (cf. docs UC-01b) - l'ecran est donc
/// affiche proactivement, mais reste skippable (l'utilisateur peut toujours l'ajouter
/// plus tard depuis son profil).
class SecuritySelfieSetupScreen extends StatefulWidget {
  const SecuritySelfieSetupScreen({super.key});

  @override
  State<SecuritySelfieSetupScreen> createState() =>
      _SecuritySelfieSetupScreenState();
}

class _SecuritySelfieSetupScreenState extends State<SecuritySelfieSetupScreen> {
  XFile? _selfie;
  bool _submitting = false;
  String? _errorMessage;

  Future<void> _submit() async {
    if (_selfie == null) {
      setState(() => _errorMessage = 'Prenez un selfie pour continuer.');
      return;
    }
    setState(() {
      _submitting = true;
      _errorMessage = null;
    });
    try {
      await context.read<AuthService>().updateSecuritySelfie(_selfie!);
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
      title: 'Sécurisez votre compte',
      subtitle: "Vous n'avez pas renseigné d'email",
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text(
            "Sans email, c'est ce selfie qui permettra de récupérer votre compte en cas de mot de passe "
            "oublié. Un administrateur le comparera à celui que vous prendrez le jour de la récupération.",
            style: TextStyle(fontSize: 13, color: Color(0xFF5B6560)),
          ),
          const SizedBox(height: 16),
          if (_errorMessage != null) ...[
            AuthErrorBanner(message: _errorMessage!),
            const SizedBox(height: 16),
          ],
          SelfieCaptureField(
            selfie: _selfie,
            onCaptured: (f) => setState(() => _selfie = f),
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
                      child: CircularProgressIndicator(
                        strokeWidth: 2.5,
                        color: Colors.white,
                      ),
                    )
                  : const Text(
                      'Enregistrer',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
            ),
          ),
          const SizedBox(height: 12),
          TextButton(
            onPressed: _submitting
                ? null
                : () =>
                      Navigator.of(context).popUntil((route) => route.isFirst),
            child: const Text('Plus tard'),
          ),
        ],
      ),
    );
  }
}
