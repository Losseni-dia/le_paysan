class AdminShop {
  final int id;
  final String name;
  final String? description;
  final String ownerType;
  final String? category;
  final String? addressCity;
  final String? addressDetail;
  final String? phone;
  final String? logoUrl;
  final String status;
  final DateTime? createdAt;

  AdminShop({
    required this.id,
    required this.name,
    required this.description,
    required this.ownerType,
    required this.category,
    required this.addressCity,
    required this.addressDetail,
    required this.phone,
    required this.logoUrl,
    required this.status,
    required this.createdAt,
  });

  factory AdminShop.fromJson(Map<String, dynamic> json) {
    return AdminShop(
      id: json['id'] as int,
      name: json['name'] as String,
      description: json['description'] as String?,
      ownerType: json['ownerType'] as String,
      category: json['category'] as String?,
      addressCity: json['addressCity'] as String?,
      addressDetail: json['addressDetail'] as String?,
      phone: json['phone'] as String?,
      logoUrl: json['logoUrl'] as String?,
      status: json['status'] as String,
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'] as String)
          : null,
    );
  }
}

/// Un document KYC a afficher a l'admin (libelle + chemin relatif vers le fichier protege).
class KycDocument {
  final String label;
  final String url;

  KycDocument({required this.label, required this.url});
}

class AdminShopDetail {
  final AdminShop shop;
  final String ownerUsername;
  final String? ownerEmail;
  final Map<String, dynamic>? individualProfile;
  final Map<String, dynamic>? entreprenantProfile;
  final Map<String, dynamic>? companyProfile;

  AdminShopDetail({
    required this.shop,
    required this.ownerUsername,
    required this.ownerEmail,
    required this.individualProfile,
    required this.entreprenantProfile,
    required this.companyProfile,
  });

  factory AdminShopDetail.fromJson(Map<String, dynamic> json) {
    return AdminShopDetail(
      shop: AdminShop.fromJson(json['shop'] as Map<String, dynamic>),
      ownerUsername: json['ownerUsername'] as String,
      ownerEmail: json['ownerEmail'] as String?,
      individualProfile: json['individualProfile'] as Map<String, dynamic>?,
      entreprenantProfile: json['entreprenantProfile'] as Map<String, dynamic>?,
      companyProfile: json['companyProfile'] as Map<String, dynamic>?,
    );
  }

  bool matchesSearch(String query) {
    final q = query.trim().toLowerCase();
    if (q.isEmpty) return true;
    if (shop.name.toLowerCase().contains(q)) return true;
    if (ownerUsername.toLowerCase().contains(q)) return true;
    if ((shop.addressCity ?? '').toLowerCase().contains(q)) return true;
    return (representativeName ?? '').toLowerCase().contains(q);
  }

  /// Nom du represenant (personne physique, entreprenant ou representant legal selon le type).
  String? get representativeName {
    final p = individualProfile ?? entreprenantProfile;
    if (p != null) return '${p['firstName']} ${p['lastName']}';
    if (companyProfile != null) return companyProfile!['legalRepName'] as String?;
    return null;
  }

  List<KycDocument> get documents {
    final docs = <KycDocument>[];
    if (individualProfile != null) {
      final p = individualProfile!;
      _addIfPresent(docs, "Pièce d'identité (recto)", p['idDocumentFrontUrl']);
      _addIfPresent(docs, "Pièce d'identité (verso)", p['idDocumentBackUrl']);
      _addIfPresent(docs, 'Selfie avec la pièce', p['selfieWithIdUrl']);
    }
    if (entreprenantProfile != null) {
      final p = entreprenantProfile!;
      _addIfPresent(docs, "Pièce d'identité (recto)", p['idDocumentFrontUrl']);
      _addIfPresent(docs, "Pièce d'identité (verso)", p['idDocumentBackUrl']);
      _addIfPresent(docs, 'Selfie avec la pièce', p['selfieWithIdUrl']);
      _addIfPresent(docs, 'Récépissé', p['recepisseDocumentUrl']);
    }
    if (companyProfile != null) {
      final p = companyProfile!;
      _addIfPresent(docs, "Document légal de l'entreprise", p['legalDocumentUrl']);
      _addIfPresent(docs, "Pièce d'identité du représentant (recto)", p['legalRepIdDocumentFrontUrl']);
      _addIfPresent(docs, "Pièce d'identité du représentant (verso)", p['legalRepIdDocumentBackUrl']);
      _addIfPresent(docs, 'Selfie du représentant avec la pièce', p['legalRepSelfieWithIdUrl']);
    }
    return docs;
  }

  void _addIfPresent(List<KycDocument> docs, String label, Object? url) {
    if (url is String && url.isNotEmpty) {
      docs.add(KycDocument(label: label, url: url));
    }
  }
}
