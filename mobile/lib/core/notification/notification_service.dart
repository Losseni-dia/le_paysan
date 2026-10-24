import 'package:dio/dio.dart';

import '../api/api_client.dart';
import 'app_notification.dart';

class NotificationService {
  final Dio _dio = ApiClient.instance.dio;

  Future<int> unreadCount() async {
    final response = await _dio.get('/notifications/unread-count');
    return (response.data as Map<String, dynamic>)['count'] as int;
  }

  Future<List<AppNotification>> list() async {
    final response = await _dio.get('/notifications');
    final content = (response.data as Map<String, dynamic>)['content'] as List<dynamic>;
    return content.map((e) => AppNotification.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<void> markAllAsRead() async {
    await _dio.patch('/notifications/read-all');
  }
}
