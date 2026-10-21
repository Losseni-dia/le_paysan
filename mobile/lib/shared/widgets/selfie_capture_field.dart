import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

/// Capture d'un selfie en direct (camera frontale uniquement, jamais la galerie, pour
/// limiter la fraude - cf. equivalent web capture="user" dans account-recovery.html).
class SelfieCaptureField extends StatelessWidget {
  final XFile? selfie;
  final ValueChanged<XFile> onCaptured;

  const SelfieCaptureField({
    super.key,
    required this.selfie,
    required this.onCaptured,
  });

  Future<void> _capture() async {
    final picked = await ImagePicker().pickImage(
      source: ImageSource.camera,
      preferredCameraDevice: CameraDevice.front,
    );
    if (picked != null) onCaptured(picked);
  }

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: _capture,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        height: 180,
        decoration: BoxDecoration(
          color: const Color(0xFFF3F8F4),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: const Color(0xFFE1E8E3)),
        ),
        clipBehavior: Clip.antiAlias,
        child: selfie == null
            ? const Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      Icons.camera_alt_outlined,
                      size: 36,
                      color: Color(0xFF5B6560),
                    ),
                    SizedBox(height: 8),
                    Text(
                      'Prendre un selfie',
                      style: TextStyle(color: Color(0xFF5B6560)),
                    ),
                  ],
                ),
              )
            : Stack(
                fit: StackFit.expand,
                children: [
                  Image.file(File(selfie!.path), fit: BoxFit.cover),
                  Positioned(
                    right: 8,
                    bottom: 8,
                    child: FilledButton.tonalIcon(
                      onPressed: _capture,
                      icon: const Icon(Icons.refresh, size: 18),
                      label: const Text('Reprendre'),
                    ),
                  ),
                ],
              ),
      ),
    );
  }
}
