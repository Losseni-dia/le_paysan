/// Vue cote livreur d'une demande de livraison : infos de recuperation et de remise.
class DeliveryRequest {
  final int id;
  final int orderId;
  final String orderNumber;
  final String status;
  final String shopName;
  final String? pickupCity;
  final String? pickupAddress;
  final String buyerFullName;
  final String? deliveryAddress;
  final double totalAmount;
  final DateTime? createdAt;

  DeliveryRequest({
    required this.id,
    required this.orderId,
    required this.orderNumber,
    required this.status,
    required this.shopName,
    required this.pickupCity,
    required this.pickupAddress,
    required this.buyerFullName,
    required this.deliveryAddress,
    required this.totalAmount,
    required this.createdAt,
  });

  factory DeliveryRequest.fromJson(Map<String, dynamic> json) {
    return DeliveryRequest(
      id: json['id'] as int,
      orderId: json['orderId'] as int,
      orderNumber: json['orderNumber'] as String,
      status: json['status'] as String,
      shopName: json['shopName'] as String,
      pickupCity: json['pickupCity'] as String?,
      pickupAddress: json['pickupAddress'] as String?,
      buyerFullName: json['buyerFullName'] as String,
      deliveryAddress: json['deliveryAddress'] as String?,
      totalAmount: (json['totalAmount'] as num).toDouble(),
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'] as String)
          : null,
    );
  }
}
