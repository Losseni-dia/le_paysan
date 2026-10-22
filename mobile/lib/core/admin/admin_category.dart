class AdminCategory {
  final int id;
  final String name;
  final String slug;
  final String status;

  AdminCategory({required this.id, required this.name, required this.slug, required this.status});

  factory AdminCategory.fromJson(Map<String, dynamic> json) {
    return AdminCategory(
      id: json['id'] as int,
      name: json['name'] as String,
      slug: json['slug'] as String,
      status: json['status'] as String,
    );
  }
}
