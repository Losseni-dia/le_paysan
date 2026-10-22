import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

import '../../core/api/api_client.dart';

/// Affiche une image hebergee par le backend derriere /api/v1/files/** quand la
/// categorie n'est pas publique (ex. delivery-proofs) : necessite le token d'auth,
/// donc pas de simple Image.network(url) possible.
class AuthenticatedImage extends StatelessWidget {
  final String url;
  final double? height;
  final BoxFit fit;

  const AuthenticatedImage({super.key, required this.url, this.height, this.fit = BoxFit.cover});

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<Uint8List>(
      future: _fetch(),
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return SizedBox(
            height: height,
            child: const Center(child: CircularProgressIndicator()),
          );
        }
        if (snapshot.hasError || snapshot.data == null) {
          return SizedBox(
            height: height,
            child: const ColoredBox(color: Color(0xFFE1E8E3)),
          );
        }
        return Image.memory(snapshot.data!, height: height, fit: fit);
      },
    );
  }

  Future<Uint8List> _fetch() async {
    final response = await ApiClient.instance.dio.get<List<int>>(
      url,
      options: Options(responseType: ResponseType.bytes),
    );
    return Uint8List.fromList(response.data!);
  }
}
