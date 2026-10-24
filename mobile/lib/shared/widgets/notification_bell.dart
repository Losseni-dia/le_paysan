import 'dart:async';

import 'package:flutter/material.dart';

import '../../core/notification/app_notification.dart';
import '../../core/notification/notification_service.dart';
import '../../features/livreur/livreur_demandes_screen.dart';
import '../../features/livreur/livreur_home_screen.dart';
import '../../features/orders/order_detail_screen.dart';
import '../../features/vendor/vendor_order_detail_screen.dart';
import '../../features/vendor/vendor_orders_screen.dart';
import '../../features/vendor/vendor_products_screen.dart';

const _pollInterval = Duration(seconds: 30);

/// Cloche de notifications (cf. web shared/ui/notification-bell) : badge avec le nombre de
/// notifications non lues, rafraichi periodiquement, liste complete dans une feuille modale.
class NotificationBell extends StatefulWidget {
  const NotificationBell({super.key});

  @override
  State<NotificationBell> createState() => _NotificationBellState();
}

class _NotificationBellState extends State<NotificationBell> {
  final NotificationService _service = NotificationService();
  Timer? _timer;
  int _unreadCount = 0;

  @override
  void initState() {
    super.initState();
    _refreshUnreadCount();
    _timer = Timer.periodic(_pollInterval, (_) => _refreshUnreadCount());
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _refreshUnreadCount() async {
    try {
      final count = await _service.unreadCount();
      if (mounted) setState(() => _unreadCount = count);
    } catch (_) {
      // Pas bloquant : on retentera au prochain cycle.
    }
  }

  Future<void> _open() async {
    List<AppNotification> notifications = [];
    try {
      notifications = await _service.list();
    } catch (_) {
      // On affiche quand meme la feuille, avec une liste vide.
    }
    if (!mounted) return;

    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (context) => _NotificationSheet(notifications: notifications),
    );

    if (_unreadCount > 0) {
      try {
        await _service.markAllAsRead();
      } catch (_) {
        // Pas bloquant.
      }
      if (mounted) setState(() => _unreadCount = 0);
    }
  }

  @override
  Widget build(BuildContext context) {
    return IconButton(
      onPressed: _open,
      tooltip: 'Notifications',
      icon: Badge(
        label: Text('$_unreadCount'),
        isLabelVisible: _unreadCount > 0,
        child: const Icon(Icons.notifications_outlined),
      ),
    );
  }
}

class _NotificationSheet extends StatelessWidget {
  final List<AppNotification> notifications;

  const _NotificationSheet({required this.notifications});

  /// Mappe les liens web (cf. backend NotificationService.notify(...)) vers l'ecran mobile
  /// equivalent. A garder synchronise avec les chemins "/vendeur/...", "/livreur/..." et
  /// "/mon-espace/..." utilises par le backend.
  void _openLink(BuildContext context, String? linkUrl) {
    Navigator.of(context).pop();
    if (linkUrl == null) return;

    Widget? screen;

    final vendorOrderMatch = RegExp(r'^/vendeur/commandes/(\d+)').firstMatch(linkUrl);
    final buyerOrderMatch = RegExp(r'^/mon-espace/commandes/(\d+)').firstMatch(linkUrl);

    if (vendorOrderMatch != null) {
      screen = VendorOrderDetailScreen(orderId: int.parse(vendorOrderMatch.group(1)!));
    } else if (buyerOrderMatch != null) {
      screen = OrderDetailScreen(orderId: int.parse(buyerOrderMatch.group(1)!));
    } else if (linkUrl.startsWith('/livreur/demandes')) {
      // LivreurDemandesScreen est concu comme contenu d'onglet (sans Scaffold propre, cf.
      // LivreurHomeScreen) : il faut l'envelopper pour un affichage autonome depuis une notif.
      screen = Scaffold(
        appBar: AppBar(title: const Text('Demandes de livraison')),
        body: const LivreurDemandesScreen(),
      );
    } else if (linkUrl.startsWith('/livreur/parametres')) {
      screen = const LivreurHomeScreen();
    } else if (linkUrl.startsWith('/vendeur/commandes')) {
      screen = const VendorOrdersScreen();
    } else if (linkUrl.startsWith('/vendeur/produits') || linkUrl.startsWith('/vendeur/ma-boutique')) {
      screen = const VendorProductsScreen();
    }

    if (screen != null) {
      Navigator.of(context).push(MaterialPageRoute(builder: (_) => screen!));
    }
  }

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      initialChildSize: 0.6,
      minChildSize: 0.3,
      maxChildSize: 0.9,
      expand: false,
      builder: (context, scrollController) {
        return Column(
          children: [
            Padding(
              padding: const EdgeInsets.all(16),
              child: Text('Notifications', style: Theme.of(context).textTheme.titleMedium),
            ),
            const Divider(height: 1),
            Expanded(
              child: notifications.isEmpty
                  ? const Center(child: Text('Aucune notification pour le moment.'))
                  : ListView.separated(
                      controller: scrollController,
                      itemCount: notifications.length,
                      separatorBuilder: (_, _) => const Divider(height: 1),
                      itemBuilder: (context, index) {
                        final n = notifications[index];
                        return ListTile(
                          tileColor: n.isUnread ? const Color(0xFFEAF5EE) : null,
                          title: Text(n.title, style: const TextStyle(fontWeight: FontWeight.w600)),
                          subtitle: Text(n.message),
                          onTap: () => _openLink(context, n.linkUrl),
                        );
                      },
                    ),
            ),
          ],
        );
      },
    );
  }
}
