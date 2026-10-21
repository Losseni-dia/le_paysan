import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../../core/auth/auth_service.dart';
import '../../../core/cart/cart_service.dart';
import '../../../core/catalogue/ci_cities.dart';
import '../../../core/product/product.dart';
import '../../../core/product/product_service.dart';
import '../../auth/login/login_screen.dart';

class ProductDetailScreen extends StatefulWidget {
  final int productId;

  const ProductDetailScreen({super.key, required this.productId});

  @override
  State<ProductDetailScreen> createState() => _ProductDetailScreenState();
}

class _ProductDetailScreenState extends State<ProductDetailScreen> {
  final ProductService _productService = ProductService();
  final _addressController = TextEditingController();

  Product? _product;
  bool _loading = true;
  bool _notFound = false;

  int _activeImage = 0;
  int _quantity = 1;
  String? _deliveryMode;
  CiCity? _deliveryCity;

  bool _addingToCart = false;
  bool _addedToCart = false;
  String? _cartError;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _addressController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    try {
      final product = await _productService.getPublic(widget.productId);
      setState(() {
        _product = product;
        _loading = false;
        if (product.deliveryOptions.length == 1) {
          _deliveryMode = product.deliveryOptions.first.mode;
        }
      });
    } catch (_) {
      setState(() {
        _notFound = true;
        _loading = false;
      });
    }
  }

  bool get _needsAddress => _deliveryMode == 'LIVRAISON_DOMICILE';

  Future<void> _addToCart() async {
    final auth = context.read<AuthService>();
    if (!auth.isAuthenticated) {
      await Navigator.of(
        context,
      ).push(MaterialPageRoute(builder: (_) => const LoginScreen()));
      if (!mounted || !context.read<AuthService>().isAuthenticated) return;
    }

    if (_deliveryMode == null) {
      setState(() => _cartError = 'Choisissez un mode de livraison.');
      return;
    }
    if (_needsAddress && _addressController.text.trim().isEmpty) {
      setState(() => _cartError = "L'adresse de livraison est obligatoire.");
      return;
    }
    if (_needsAddress && _deliveryCity == null) {
      setState(() => _cartError = 'Choisissez une ville de livraison.');
      return;
    }

    setState(() {
      _addingToCart = true;
      _addedToCart = false;
      _cartError = null;
    });

    try {
      await context.read<CartService>().addItem(
        productId: widget.productId,
        quantity: _quantity,
        deliveryMode: _deliveryMode!,
        deliveryAddress: _needsAddress ? _addressController.text.trim() : null,
        deliveryCityName: _needsAddress ? _deliveryCity!.name : null,
        deliveryCityLat: _needsAddress ? _deliveryCity!.lat : null,
        deliveryCityLng: _needsAddress ? _deliveryCity!.lng : null,
      );
      if (mounted) setState(() => _addedToCart = true);
    } on DioException catch (e) {
      setState(() {
        _cartError = (e.response?.data is Map)
            ? e.response?.data['message'] as String?
            : null;
        _cartError ??= "Impossible d'ajouter ce produit au panier.";
      });
    } finally {
      if (mounted) setState(() => _addingToCart = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Produit')),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_notFound || _product == null) {
      return const Center(child: Text('Produit introuvable.'));
    }

    final product = _product!;
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _buildImages(product),
        const SizedBox(height: 16),
        Text(
          product.name,
          style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 4),
        Text(
          product.shopName,
          style: const TextStyle(fontSize: 14, color: Colors.black54),
        ),
        if (product.ratingCount > 0) ...[
          const SizedBox(height: 6),
          Row(
            children: [
              const Icon(Icons.star, size: 16, color: Color(0xFFC98A1E)),
              const SizedBox(width: 4),
              Text(
                '${product.ratingAverage?.toStringAsFixed(1)} (${product.ratingCount})',
                style: const TextStyle(fontSize: 13),
              ),
            ],
          ),
        ],
        const SizedBox(height: 12),
        Text(
          '${product.unitPrice.toStringAsFixed(0)} FCFA / ${product.unit}',
          style: const TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.bold,
            color: Color(0xFF1B5E3A),
          ),
        ),
        const SizedBox(height: 4),
        Text(
          product.stockQuantity > 0
              ? '${product.stockQuantity} en stock'
              : 'Rupture de stock',
          style: TextStyle(
            fontSize: 13,
            color: product.stockQuantity > 0
                ? Colors.black54
                : const Color(0xFFB3261E),
          ),
        ),
        if (product.description != null && product.description!.isNotEmpty) ...[
          const SizedBox(height: 16),
          Text(product.description!, style: const TextStyle(fontSize: 14)),
        ],
        const Divider(height: 32),
        const Text(
          'Livraison',
          style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          children: product.deliveryOptions.map((option) {
            final selected = _deliveryMode == option.mode;
            return ChoiceChip(
              label: Text(
                option.mode == 'RETRAIT_BOUTIQUE'
                    ? 'Retrait en boutique'
                    : 'Livraison à domicile',
              ),
              selected: selected,
              onSelected: (_) => setState(() => _deliveryMode = option.mode),
            );
          }).toList(),
        ),
        if (_deliveryMode == 'RETRAIT_BOUTIQUE') ...[
          Builder(
            builder: (context) {
              final option = product.deliveryOptions.firstWhere(
                (o) => o.mode == 'RETRAIT_BOUTIQUE',
              );
              if (option.pickupLocation == null ||
                  option.pickupLocation!.isEmpty) {
                return const SizedBox.shrink();
              }
              return Padding(
                padding: const EdgeInsets.only(top: 12),
                child: Text(
                  'Adresse de retrait : ${option.pickupLocation}',
                  style: const TextStyle(fontSize: 13),
                ),
              );
            },
          ),
        ],
        if (_needsAddress) ...[
          const SizedBox(height: 16),
          TextField(
            controller: _addressController,
            decoration: const InputDecoration(
              labelText: 'Adresse de livraison',
            ),
          ),
          const SizedBox(height: 16),
          DropdownButtonFormField<CiCity>(
            initialValue: _deliveryCity,
            decoration: const InputDecoration(labelText: 'Ville'),
            items: ciCities
                .map((c) => DropdownMenuItem(value: c, child: Text(c.name)))
                .toList(),
            onChanged: (c) => setState(() => _deliveryCity = c),
          ),
        ],
        const Divider(height: 32),
        Row(
          children: [
            const Text(
              'Quantité',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
            ),
            const Spacer(),
            IconButton(
              icon: const Icon(Icons.remove_circle_outline),
              onPressed: _quantity > 1
                  ? () => setState(() => _quantity--)
                  : null,
            ),
            Text('$_quantity', style: const TextStyle(fontSize: 16)),
            IconButton(
              icon: const Icon(Icons.add_circle_outline),
              onPressed: _quantity < product.stockQuantity
                  ? () => setState(() => _quantity++)
                  : null,
            ),
          ],
        ),
        const SizedBox(height: 16),
        if (_cartError != null) ...[
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: const Color(0xFFFBE9E7),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              _cartError!,
              style: const TextStyle(color: Color(0xFFB3261E)),
            ),
          ),
          const SizedBox(height: 16),
        ],
        if (_addedToCart) ...[
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: const Color(0xFFD7ECDF),
              borderRadius: BorderRadius.circular(12),
            ),
            child: const Text(
              'Produit ajouté au panier.',
              style: TextStyle(color: Color(0xFF0B3D1E)),
            ),
          ),
          const SizedBox(height: 16),
        ],
        SizedBox(
          height: 52,
          child: FilledButton(
            onPressed: (product.stockQuantity == 0 || _addingToCart)
                ? null
                : _addToCart,
            child: _addingToCart
                ? const SizedBox(
                    width: 22,
                    height: 22,
                    child: CircularProgressIndicator(
                      strokeWidth: 2.5,
                      color: Colors.white,
                    ),
                  )
                : const Text(
                    'Ajouter au panier',
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
                  ),
          ),
        ),
      ],
    );
  }

  Widget _buildImages(Product product) {
    if (product.imageUrls.isEmpty) {
      return AspectRatio(
        aspectRatio: 1.3,
        child: Container(
          decoration: BoxDecoration(
            color: const Color(0xFFE1E8E3),
            borderRadius: BorderRadius.circular(12),
          ),
        ),
      );
    }
    return Column(
      children: [
        AspectRatio(
          aspectRatio: 1.3,
          child: ClipRRect(
            borderRadius: BorderRadius.circular(12),
            child: Image.network(
              ProductService.imageUrl(product.imageUrls[_activeImage]),
              fit: BoxFit.cover,
              errorBuilder: (_, _, _) =>
                  const ColoredBox(color: Color(0xFFE1E8E3)),
            ),
          ),
        ),
        if (product.imageUrls.length > 1) ...[
          const SizedBox(height: 8),
          SizedBox(
            height: 64,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              itemCount: product.imageUrls.length,
              separatorBuilder: (_, _) => const SizedBox(width: 8),
              itemBuilder: (context, index) {
                final selected = index == _activeImage;
                return InkWell(
                  onTap: () => setState(() => _activeImage = index),
                  child: Container(
                    width: 64,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(
                        color: selected
                            ? const Color(0xFF1B5E3A)
                            : Colors.transparent,
                        width: 2,
                      ),
                    ),
                    clipBehavior: Clip.antiAlias,
                    child: Image.network(
                      ProductService.imageUrl(product.imageUrls[index]),
                      fit: BoxFit.cover,
                      errorBuilder: (_, _, _) =>
                          const ColoredBox(color: Color(0xFFE1E8E3)),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ],
    );
  }
}
