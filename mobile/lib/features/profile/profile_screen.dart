import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/auth/auth_service.dart';
import '../../core/cart/cart_service.dart';
import '../auth/login/login_screen.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthService>();
    final user = auth.currentUser;

    return Scaffold(
      appBar: AppBar(title: const Text('Profil')),
      body: user == null
          ? _buildLoggedOut(context)
          : _buildLoggedIn(context, user.firstName, user.lastName, user),
    );
  }

  Widget _buildLoggedOut(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Connectez-vous pour accéder à votre profil.'),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: () => Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const LoginScreen()),
              ),
              child: const Text('Se connecter'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildLoggedIn(
    BuildContext context,
    String firstName,
    String lastName,
    dynamic user,
  ) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '$firstName $lastName',
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  '@${user.username}',
                  style: const TextStyle(fontSize: 13, color: Colors.black54),
                ),
                const Divider(height: 24),
                if (user.email != null) _infoRow('Email', user.email),
                if (user.phone != null) _infoRow('Téléphone', user.phone),
                if (user.city != null) _infoRow('Localité', user.city),
              ],
            ),
          ),
        ),
        const SizedBox(height: 20),
        SizedBox(
          height: 52,
          child: OutlinedButton(
            onPressed: () {
              context.read<AuthService>().logout();
              context.read<CartService>().clear();
            },
            child: const Text('Se déconnecter'),
          ),
        ),
      ],
    );
  }

  Widget _infoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          SizedBox(
            width: 90,
            child: Text(label, style: const TextStyle(color: Colors.black54)),
          ),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }
}
