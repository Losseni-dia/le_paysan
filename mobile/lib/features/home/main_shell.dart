import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/auth/auth_service.dart';
import '../auth/login/login_screen.dart';
import '../cart/cart_screen.dart';
import '../catalogue/catalogue_screen.dart';
import '../orders/orders_screen.dart';
import '../profile/profile_screen.dart';

/// Barre de navigation principale. Catalogue est accessible sans connexion ;
/// les autres onglets demandent une authentification (redirige vers l'ecran
/// de connexion au lieu de changer d'onglet si non connecte).
class MainShell extends StatefulWidget {
  const MainShell({super.key});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  int _index = 0;

  static const _screens = [
    CatalogueScreen(),
    CartScreen(),
    OrdersScreen(),
    ProfileScreen(),
  ];

  Future<void> _onTap(int index) async {
    if (index == 0) {
      setState(() => _index = index);
      return;
    }
    final auth = context.read<AuthService>();
    if (!auth.isAuthenticated) {
      await Navigator.of(
        context,
      ).push(MaterialPageRoute(builder: (_) => const LoginScreen()));
      if (!mounted || !context.read<AuthService>().isAuthenticated) return;
    }
    setState(() => _index = index);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(index: _index, children: _screens),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: _onTap,
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.storefront_outlined),
            selectedIcon: Icon(Icons.storefront),
            label: 'Catalogue',
          ),
          NavigationDestination(
            icon: Icon(Icons.shopping_cart_outlined),
            selectedIcon: Icon(Icons.shopping_cart),
            label: 'Panier',
          ),
          NavigationDestination(
            icon: Icon(Icons.receipt_long_outlined),
            selectedIcon: Icon(Icons.receipt_long),
            label: 'Commandes',
          ),
          NavigationDestination(
            icon: Icon(Icons.person_outline),
            selectedIcon: Icon(Icons.person),
            label: 'Profil',
          ),
        ],
      ),
    );
  }
}
