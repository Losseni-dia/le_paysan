import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';

class SignaturePadController {
  final GlobalKey repaintKey = GlobalKey();
  final List<Offset?> points = [];
  final ValueNotifier<int> revision = ValueNotifier(0);

  bool get hasSignature => points.any((p) => p != null);

  void addPoint(Offset? point) {
    points.add(point);
    revision.value++;
  }

  void clear() {
    points.clear();
    revision.value++;
  }

  Future<Uint8List> toPngBytes() async {
    final boundary = repaintKey.currentContext!.findRenderObject() as RenderRepaintBoundary;
    final image = await boundary.toImage(pixelRatio: 2.0);
    final byteData = await image.toByteData(format: ui.ImageByteFormat.png);
    return byteData!.buffer.asUint8List();
  }

  void dispose() {
    revision.dispose();
  }
}

/// Zone de signature manuscrite (doigt sur l'ecran). Equivalent du composant
/// web app-signature-pad : exporte en PNG pour l'envoyer comme preuve de remise.
class SignaturePad extends StatelessWidget {
  final SignaturePadController controller;

  const SignaturePad({super.key, required this.controller});

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<int>(
      valueListenable: controller.revision,
      builder: (context, _, _) {
        return RepaintBoundary(
          key: controller.repaintKey,
          child: Container(
            height: 200,
            width: double.infinity,
            decoration: BoxDecoration(
              color: Colors.white,
              border: Border.all(color: const Color(0xFFE1E8E3)),
              borderRadius: BorderRadius.circular(12),
            ),
            child: GestureDetector(
              onPanStart: (details) => controller.addPoint(details.localPosition),
              onPanUpdate: (details) => controller.addPoint(details.localPosition),
              onPanEnd: (_) => controller.addPoint(null),
              child: CustomPaint(
                painter: _SignaturePainter(controller.points),
                size: Size.infinite,
              ),
            ),
          ),
        );
      },
    );
  }
}

class _SignaturePainter extends CustomPainter {
  final List<Offset?> points;

  _SignaturePainter(this.points);

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = Colors.black
      ..strokeWidth = 3
      ..strokeCap = StrokeCap.round;
    for (var i = 0; i < points.length - 1; i++) {
      final current = points[i];
      final next = points[i + 1];
      if (current != null && next != null) {
        canvas.drawLine(current, next, paint);
      }
    }
  }

  @override
  bool shouldRepaint(covariant _SignaturePainter oldDelegate) => true;
}
