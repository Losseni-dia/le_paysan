import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/auth/auth_service.dart';
import '../../core/auth/user.dart';
import '../../core/cart/cart_service.dart';
import '../admin/admin_livreurs_kyc_screen.dart';
import '../admin/admin_orders_screen.dart';
import '../admin/admin_products_screen.dart';
import '../admin/admin_shop_requests_screen.dart';
import '../admin/admin_users_screen.dart';
import '../auth/login/login_screen.dart';
import '../livreur/livreur_home_screen.dart';
import '../orders/orders_screen.dart';
import '../vendor/vendor_livreurs_screen.dart';
import '../vendor/vendor_orders_screen.dart';
import '../vendor/vendor_products_screen.dart';
import 'change_password_screen.dart';
import 'edit_profile_screen.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthService>();
    final user = auth.currentUser;

    return Scaffold(
      appBar: AppBar(title: const Text('Profil')),
      body: user == null ? _buildLoggedOut(context) : _buildLoggedIn(context, user),
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

  Widget _buildLoggedIn(BuildContext context, AppUser user) {
    final isLivreur = user.hasRole('ROLE_LIVREUR');
    final isVendeur = user.hasRole('ROLE_VENDEUR');
    final isAdmin = user.hasRole('ROLE_ADMIN');

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
                  '${user.firstName} ${user.lastName}',
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
                if (user.email != null) _infoRow('Email', user.email!),
                if (user.phone != null) _infoRow('Téléphone', user.phone!),
                if (user.city != null) _infoRow('Localité', user.city!),
              ],
            ),
          ),
        ),
        const SizedBox(height: 20),
        const Text(
          'Mon espace',
          style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600, color: Colors.black54),
        ),
        const SizedBox(height: 8),
        Card(
          child: ListTile(
            leading: const Icon(Icons.receipt_long_outlined),
            title: const Text('Mes commandes'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => const OrdersScreen()),
            ),
          ),
        ),
        if (isAdmin) ...[
          const SizedBox(height: 20),
          const Text(
            'Espace admin',
            style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600, color: Colors.black54),
          ),
          const SizedBox(height: 8),
          Card(
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.storefront_outlined),
                  title: const Text('Demandes boutique'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const AdminShopRequestsScreen()),
                  ),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.badge_outlined),
                  title: const Text('KYC livreurs'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const AdminLivreursKycScreen()),
                  ),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.receipt_long_outlined),
                  title: const Text('Commandes'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const AdminOrdersScreen()),
                  ),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.inventory_2_outlined),
                  title: const Text('Produits'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const AdminProductsScreen()),
                  ),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.people_outline),
                  title: const Text('Utilisateurs'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const AdminUsersScreen()),
                  ),
                ),
              ],
            ),
          ),
        ],
        if (isLivreur || isVendeur) ...[
          const SizedBox(height: 20),
          const Text(
            'Mes espaces',
            style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600, color: Colors.black54),
          ),
          const SizedBox(height: 8),
          Card(
            child: Column(
              children: [
                if (isVendeur) ...[
                  ListTile(
                    leading: const Icon(Icons.receipt_long_outlined),
                    title: const Text('Ma boutique - Commandes'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => Navigator.of(context).push(
                      MaterialPageRoute(builder: (_) => const VendorOrdersScreen()),
                    ),
                  ),
                  const Divider(height: 1),
                  ListTile(
                    leading: const Icon(Icons.inventory_2_outlined),
                    title: const Text('Ma boutique - Produits'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => Navigator.of(context).push(
                      MaterialPageRoute(builder: (_) => const VendorProductsScreen()),
                    ),
                  ),
                  const Divider(height: 1),
                  ListTile(
                    leading: const Icon(Icons.badge_outlined),
                    title: const Text('Ma boutique - Livreurs'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => Navigator.of(context).push(
                      MaterialPageRoute(builder: (_) => const VendorLivreursScreen()),
                    ),
                  ),
                ],
                if (isVendeur && isLivreur) const Divider(height: 1),
                if (isLivreur)
                  ListTile(
                    leading: const Icon(Icons.delivery_dining_outlined),
                    title: const Text('Espace livreur'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => Navigator.of(context).push(
                      MaterialPageRoute(builder: (_) => const LivreurHomeScreen()),
                    ),
                  ),
              ],
            ),
          ),
        ],
        const SizedBox(height: 20),
        const Text(
          'Paramètres du compte',
          style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600, color: Colors.black54),
        ),
        const SizedBox(height: 8),
        Card(
          child: Column(
            children: [
              ListTile(
                leading: const Icon(Icons.person_outline),
                title: const Text('Modifier mon profil'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const EditProfileScreen()),
                ),
              ),
              const Divider(height: 1),
              ListTile(
                leading: const Icon(Icons.lock_outline),
                title: const Text('Changer mon mot de passe'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const ChangePasswordScreen()),
                ),
              ),
            ],
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
