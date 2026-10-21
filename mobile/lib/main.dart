import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'core/auth/auth_service.dart';
import 'features/auth/login/login_screen.dart';
import 'features/home/home_screen.dart';

void main() {
  runApp(const LeePaysanApp());
}

class LeePaysanApp extends StatelessWidget {
  const LeePaysanApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => AuthService()..tryRestoreSession(),
      child: MaterialApp(
        title: 'Le Paysan',
        theme: _buildTheme(),
        home: const _RootScreen(),
      ),
    );
  }
}

/// Theme partage par toute l'app, aligne sur la charte graphique web
/// (cf. frontend/src/styles.scss : --color-primary #1B5E3A).
ThemeData _buildTheme() {
  const primary = Color(0xFF1B5E3A);
  final colorScheme = ColorScheme.fromSeed(seedColor: primary);

  return ThemeData(
    colorScheme: colorScheme,
    useMaterial3: true,
    scaffoldBackgroundColor: const Color(0xFFFAFCFA),
    appBarTheme: const AppBarTheme(
      backgroundColor: Color(0xFFFAFCFA),
      foregroundColor: Color(0xFF1A1F1C),
      elevation: 0,
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: Colors.white,
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: Color(0xFFE1E8E3)),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: Color(0xFFE1E8E3)),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: primary, width: 1.5),
      ),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: Color(0xFFB3261E)),
      ),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        backgroundColor: primary,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
    ),
  );
}

class _RootScreen extends StatelessWidget {
  const _RootScreen();

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthService>();
    return auth.isAuthenticated ? const HomeScreen() : const LoginScreen();
  }
}
