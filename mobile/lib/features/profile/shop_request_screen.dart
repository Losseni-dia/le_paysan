import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../../core/auth/auth_service.dart';
import '../../core/catalogue/ci_cities.dart';
import '../../core/shop/shop_request_service.dart';

const List<Map<String, String>> _ownerTypes = [
  {
    'value': 'PHYSIQUE',
    'label': 'Personne physique',
    'hint': "Producteur individuel sans statut formel (pièce d'identité).",
  },
  {
    'value': 'ENTREPRENANT',
    'label': 'Entreprenant',
    'hint': 'Statut OHADA simplifié (récépissé CEPICI).',
  },
  {
    'value': 'MORALE',
    'label': 'Personne morale',
    'hint': 'Société formelle : SARL, SA, SAS... (RCCM, NCC).',
  },
];

const List<String> _idDocumentTypes = ['CNI', 'ATTESTATION_IDENTITE', 'PASSEPORT'];
const List<String> _legalForms = ['SARL', 'SA', 'SAS', 'SUARL', 'AUTRE'];

/// Demande de boutique (cf. web features/shop/shop-request) : les champs requis varient selon
/// le type de detenteur. Verification manuelle par l'equipe apres soumission (statut EN_ATTENTE).
class ShopRequestScreen extends StatefulWidget {
  const ShopRequestScreen({super.key});

  @override
  State<ShopRequestScreen> createState() => _ShopRequestScreenState();
}

class _ShopRequestScreenState extends State<ShopRequestScreen> {
  final ShopRequestService _service = ShopRequestService();
  final ImagePicker _imagePicker = ImagePicker();

  String? _ownerType;

  final _nameController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _categoryController = TextEditingController();
  final _addressDetailController = TextEditingController();
  final _phoneController = TextEditingController();
  CiCity? _city;
  XFile? _logo;
  XFile? _cover;
  bool _acceptCgv = false;

  final _firstNameController = TextEditingController();
  final _lastNameController = TextEditingController();
  DateTime? _birthDate;
  String? _idDocumentType;
  final _idDocumentNumberController = TextEditingController();
  XFile? _idDocumentFront;
  XFile? _idDocumentBack;
  XFile? _selfieWithId;

  final _recepisseNumberController = TextEditingController();
  DateTime? _recepisseDate;
  XFile? _recepisseDocument;

  final _companyNameController = TextEditingController();
  String? _legalForm;
  final _rccmNumberController = TextEditingController();
  final _nccNumberController = TextEditingController();
  XFile? _legalDocument;
  final _legalRepNameController = TextEditingController();
  final _legalRepRoleController = TextEditingController();
  String? _legalRepIdDocumentType;
  final _legalRepIdDocumentNumberController = TextEditingController();
  XFile? _legalRepIdDocumentFront;
  XFile? _legalRepIdDocumentBack;
  XFile? _legalRepSelfieWithId;

  bool _submitting = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    final user = context.read<AuthService>().currentUser;
    if (user != null) {
      _firstNameController.text = user.firstName;
      _lastNameController.text = user.lastName;
      _phoneController.text = user.phone ?? '';
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _descriptionController.dispose();
    _categoryController.dispose();
    _addressDetailController.dispose();
    _phoneController.dispose();
    _firstNameController.dispose();
    _lastNameController.dispose();
    _idDocumentNumberController.dispose();
    _recepisseNumberController.dispose();
    _companyNameController.dispose();
    _rccmNumberController.dispose();
    _nccNumberController.dispose();
    _legalRepNameController.dispose();
    _legalRepRoleController.dispose();
    _legalRepIdDocumentNumberController.dispose();
    super.dispose();
  }

  Future<void> _pickImage(ValueSetter<XFile?> onPicked) async {
    final photo = await _imagePicker.pickImage(source: ImageSource.camera);
    if (photo != null) setState(() => onPicked(photo));
  }

  Future<void> _pickDate(DateTime? initial, ValueSetter<DateTime?> onPicked) async {
    final picked = await showDatePicker(
      context: context,
      initialDate: initial ?? DateTime(2000),
      firstDate: DateTime(1920),
      lastDate: DateTime.now(),
    );
    if (picked != null) setState(() => onPicked(picked));
  }

  bool get _commonValid =>
      _ownerType != null &&
      _nameController.text.trim().isNotEmpty &&
      _city != null &&
      _addressDetailController.text.trim().isNotEmpty &&
      _phoneController.text.trim().isNotEmpty &&
      _logo != null;

  bool get _justificatifsValid {
    final identityOk = _firstNameController.text.trim().isNotEmpty &&
        _lastNameController.text.trim().isNotEmpty &&
        _birthDate != null &&
        _idDocumentType != null &&
        _idDocumentNumberController.text.trim().isNotEmpty &&
        _idDocumentFront != null &&
        _selfieWithId != null;

    switch (_ownerType) {
      case 'PHYSIQUE':
        return identityOk;
      case 'ENTREPRENANT':
        return identityOk &&
            _recepisseNumberController.text.trim().isNotEmpty &&
            _recepisseDate != null &&
            _recepisseDocument != null;
      case 'MORALE':
        return _companyNameController.text.trim().isNotEmpty &&
            _legalForm != null &&
            _rccmNumberController.text.trim().isNotEmpty &&
            _nccNumberController.text.trim().isNotEmpty &&
            _legalDocument != null &&
            _legalRepNameController.text.trim().isNotEmpty &&
            _legalRepRoleController.text.trim().isNotEmpty &&
            _legalRepIdDocumentType != null &&
            _legalRepIdDocumentNumberController.text.trim().isNotEmpty &&
            _legalRepIdDocumentFront != null &&
            _legalRepSelfieWithId != null;
      default:
        return false;
    }
  }

  bool get _canSubmit => _commonValid && _justificatifsValid && _acceptCgv;

  Future<void> _submit() async {
    if (!_canSubmit) {
      setState(() => _errorMessage = 'Veuillez compléter tous les champs obligatoires (*).');
      return;
    }

    setState(() {
      _submitting = true;
      _errorMessage = null;
    });

    try {
      final map = <String, dynamic>{
        'ownerType': _ownerType,
        'name': _nameController.text.trim(),
        if (_descriptionController.text.trim().isNotEmpty) 'description': _descriptionController.text.trim(),
        if (_categoryController.text.trim().isNotEmpty) 'category': _categoryController.text.trim(),
        'addressCity': _city!.name,
        'addressDetail': _addressDetailController.text.trim(),
        'latitude': _city!.lat,
        'longitude': _city!.lng,
        'phone': _phoneController.text.trim(),
        'logo': await MultipartFile.fromFile(_logo!.path, filename: 'logo.jpg'),
        if (_cover != null) 'cover': await MultipartFile.fromFile(_cover!.path, filename: 'cover.jpg'),
        'acceptCgv': _acceptCgv,
        'firstName': _firstNameController.text.trim(),
        'lastName': _lastNameController.text.trim(),
        'birthDate': _birthDate!.toIso8601String().substring(0, 10),
        'idDocumentType': _idDocumentType,
        'idDocumentNumber': _idDocumentNumberController.text.trim(),
        'idDocumentFront': await MultipartFile.fromFile(_idDocumentFront!.path, filename: 'recto.jpg'),
        if (_idDocumentBack != null)
          'idDocumentBack': await MultipartFile.fromFile(_idDocumentBack!.path, filename: 'verso.jpg'),
        'selfieWithId': await MultipartFile.fromFile(_selfieWithId!.path, filename: 'selfie.jpg'),
      };

      if (_ownerType == 'ENTREPRENANT') {
        map['recepisseNumber'] = _recepisseNumberController.text.trim();
        map['recepisseDate'] = _recepisseDate!.toIso8601String().substring(0, 10);
        map['recepisseDocument'] = await MultipartFile.fromFile(_recepisseDocument!.path, filename: 'recepisse.jpg');
      }

      if (_ownerType == 'MORALE') {
        map['companyName'] = _companyNameController.text.trim();
        map['legalForm'] = _legalForm;
        map['rccmNumber'] = _rccmNumberController.text.trim();
        map['nccNumber'] = _nccNumberController.text.trim();
        map['legalDocument'] = await MultipartFile.fromFile(_legalDocument!.path, filename: 'legal.jpg');
        map['legalRepName'] = _legalRepNameController.text.trim();
        map['legalRepRole'] = _legalRepRoleController.text.trim();
        map['legalRepIdDocumentType'] = _legalRepIdDocumentType;
        map['legalRepIdDocumentNumber'] = _legalRepIdDocumentNumberController.text.trim();
        map['legalRepIdDocumentFront'] =
            await MultipartFile.fromFile(_legalRepIdDocumentFront!.path, filename: 'legalrep-recto.jpg');
        if (_legalRepIdDocumentBack != null) {
          map['legalRepIdDocumentBack'] =
              await MultipartFile.fromFile(_legalRepIdDocumentBack!.path, filename: 'legalrep-verso.jpg');
        }
        map['legalRepSelfieWithId'] =
            await MultipartFile.fromFile(_legalRepSelfieWithId!.path, filename: 'legalrep-selfie.jpg');
      }

      await _service.submitRequest(FormData.fromMap(map));
      if (mounted) Navigator.of(context).pop(true);
    } on DioException catch (e) {
      setState(() {
        _errorMessage = (e.response?.data is Map) ? e.response?.data['message'] as String? : null;
        _errorMessage ??= 'Une erreur est survenue, veuillez réessayer.';
      });
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Créer ma boutique')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          if (_errorMessage != null) ...[
            Text(_errorMessage!, style: const TextStyle(color: Color(0xFFB3261E))),
            const SizedBox(height: 16),
          ],
          const Text('Statut', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 15)),
          const SizedBox(height: 4),
          const Text(
            "Choisissez le statut qui correspond à votre situation, conformément aux statuts juridiques en vigueur "
            "en Côte d'Ivoire.",
            style: TextStyle(fontSize: 12, color: Colors.black54),
          ),
          const SizedBox(height: 12),
          for (final type in _ownerTypes) _ownerTypeCard(type),
          const Divider(height: 32),
          const Text('Informations boutique', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 15)),
          const SizedBox(height: 12),
          _textField(_nameController, 'Nom de la boutique *'),
          const SizedBox(height: 12),
          _textField(_descriptionController, 'Description', maxLines: 3),
          const SizedBox(height: 12),
          _textField(_categoryController, 'Catégorie (ex. maraîchage, élevage...)'),
          const SizedBox(height: 12),
          DropdownButtonFormField<CiCity>(
            initialValue: _city,
            decoration: const InputDecoration(labelText: 'Ville *'),
            items: ciCities.map((c) => DropdownMenuItem(value: c, child: Text(c.name))).toList(),
            onChanged: (c) => setState(() => _city = c),
          ),
          const SizedBox(height: 12),
          _textField(_addressDetailController, 'Adresse détaillée *'),
          const SizedBox(height: 12),
          _textField(_phoneController, 'Téléphone *', keyboardType: TextInputType.phone),
          const SizedBox(height: 16),
          _imageSlot('Logo *', _logo, () => _pickImage((f) => _logo = f)),
          const SizedBox(height: 16),
          _imageSlot('Photo de couverture (optionnel)', _cover, () => _pickImage((f) => _cover = f)),
          const Divider(height: 32),
          const Text('Justificatifs', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 15)),
          const SizedBox(height: 12),
          ..._justificatifsFields(),
          const Divider(height: 32),
          CheckboxListTile(
            contentPadding: EdgeInsets.zero,
            value: _acceptCgv,
            onChanged: (v) => setState(() => _acceptCgv = v ?? false),
            title: const Text("J'accepte les Conditions vendeur *", style: TextStyle(fontSize: 13)),
          ),
          const Text(
            'La vérification de votre dossier (pièces, RCCM/NCC/récépissé) est effectuée manuellement par notre '
            'équipe. Vous recevrez une notification dès que votre boutique sera validée.',
            style: TextStyle(fontSize: 12, color: Colors.black54),
          ),
          const SizedBox(height: 24),
          SizedBox(
            height: 52,
            child: FilledButton(
              onPressed: _submitting ? null : _submit,
              child: _submitting
                  ? const SizedBox(
                      width: 22,
                      height: 22,
                      child: CircularProgressIndicator(strokeWidth: 2.5, color: Colors.white),
                    )
                  : const Text('Envoyer ma demande'),
            ),
          ),
        ],
      ),
    );
  }

  Widget _ownerTypeCard(Map<String, String> type) {
    final selected = _ownerType == type['value'];
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: InkWell(
        onTap: () => setState(() => _ownerType = type['value']),
        borderRadius: BorderRadius.circular(12),
        child: Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            border: Border.all(color: selected ? const Color(0xFF1B5E3A) : const Color(0xFFE1E8E3), width: selected ? 2 : 1),
            borderRadius: BorderRadius.circular(12),
            color: selected ? const Color(0xFFEAF5EE) : null,
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(type['label']!, style: const TextStyle(fontWeight: FontWeight.bold)),
              const SizedBox(height: 4),
              Text(type['hint']!, style: const TextStyle(fontSize: 12, color: Colors.black54)),
            ],
          ),
        ),
      ),
    );
  }

  List<Widget> _justificatifsFields() {
    if (_ownerType == null) {
      return [
        const Text(
          'Choisissez un statut ci-dessus pour afficher les justificatifs à fournir.',
          style: TextStyle(fontSize: 13, color: Colors.black54),
        ),
      ];
    }

    final widgets = <Widget>[];

    if (_ownerType == 'PHYSIQUE' || _ownerType == 'ENTREPRENANT') {
      widgets.addAll([
        _textField(_firstNameController, 'Prénom *'),
        const SizedBox(height: 12),
        _textField(_lastNameController, 'Nom *'),
        const SizedBox(height: 12),
        _dateField('Date de naissance *', _birthDate, (d) => _pickDate(_birthDate, (v) => _birthDate = v)),
        const SizedBox(height: 12),
        DropdownButtonFormField<String>(
          initialValue: _idDocumentType,
          decoration: const InputDecoration(labelText: "Type de pièce d'identité *"),
          items: _idDocumentTypes.map((t) => DropdownMenuItem(value: t, child: Text(t))).toList(),
          onChanged: (v) => setState(() => _idDocumentType = v),
        ),
        const SizedBox(height: 12),
        _textField(_idDocumentNumberController, 'Numéro de pièce *'),
        const SizedBox(height: 16),
        _imageSlot('Photo recto de la pièce *', _idDocumentFront, () => _pickImage((f) => _idDocumentFront = f)),
        const SizedBox(height: 16),
        _imageSlot('Photo verso (optionnel)', _idDocumentBack, () => _pickImage((f) => _idDocumentBack = f)),
        const SizedBox(height: 16),
        _imageSlot("Selfie avec la pièce d'identité *", _selfieWithId, () => _pickImage((f) => _selfieWithId = f)),
      ]);
    }

    if (_ownerType == 'ENTREPRENANT') {
      widgets.addAll([
        const SizedBox(height: 16),
        _textField(_recepisseNumberController, "Numéro de récépissé d'entreprenant *"),
        const SizedBox(height: 12),
        _dateField('Date du récépissé *', _recepisseDate, (d) => _pickDate(_recepisseDate, (v) => _recepisseDate = v)),
        const SizedBox(height: 16),
        _imageSlot('Document de récépissé (CEPICI) *', _recepisseDocument, () => _pickImage((f) => _recepisseDocument = f)),
      ]);
    }

    if (_ownerType == 'MORALE') {
      widgets.addAll([
        _textField(_companyNameController, 'Raison sociale *'),
        const SizedBox(height: 12),
        DropdownButtonFormField<String>(
          initialValue: _legalForm,
          decoration: const InputDecoration(labelText: 'Forme juridique *'),
          items: _legalForms.map((f) => DropdownMenuItem(value: f, child: Text(f))).toList(),
          onChanged: (v) => setState(() => _legalForm = v),
        ),
        const SizedBox(height: 12),
        _textField(_rccmNumberController, 'Numéro RCCM * (ex. CI-ABJ-2024-A-1234)'),
        const SizedBox(height: 12),
        _textField(_nccNumberController, 'NCC (Numéro de Compte Contribuable) *'),
        const SizedBox(height: 16),
        _imageSlot('Document RCCM / statuts *', _legalDocument, () => _pickImage((f) => _legalDocument = f)),
        const SizedBox(height: 16),
        _textField(_legalRepNameController, 'Nom du représentant légal *'),
        const SizedBox(height: 12),
        _textField(_legalRepRoleController, 'Fonction du représentant légal *'),
        const SizedBox(height: 12),
        DropdownButtonFormField<String>(
          initialValue: _legalRepIdDocumentType,
          decoration: const InputDecoration(labelText: 'Type de pièce du représentant légal *'),
          items: _idDocumentTypes.map((t) => DropdownMenuItem(value: t, child: Text(t))).toList(),
          onChanged: (v) => setState(() => _legalRepIdDocumentType = v),
        ),
        const SizedBox(height: 12),
        _textField(_legalRepIdDocumentNumberController, 'Numéro de pièce du représentant légal *'),
        const SizedBox(height: 16),
        _imageSlot(
          'Photo recto de la pièce du représentant légal *',
          _legalRepIdDocumentFront,
          () => _pickImage((f) => _legalRepIdDocumentFront = f),
        ),
        const SizedBox(height: 16),
        _imageSlot(
          'Photo verso (optionnel)',
          _legalRepIdDocumentBack,
          () => _pickImage((f) => _legalRepIdDocumentBack = f),
        ),
        const SizedBox(height: 16),
        _imageSlot(
          'Selfie du représentant légal avec sa pièce *',
          _legalRepSelfieWithId,
          () => _pickImage((f) => _legalRepSelfieWithId = f),
        ),
      ]);
    }

    return widgets;
  }

  Widget _textField(TextEditingController controller, String label, {int maxLines = 1, TextInputType? keyboardType}) {
    return TextField(
      controller: controller,
      maxLines: maxLines,
      keyboardType: keyboardType,
      decoration: InputDecoration(labelText: label),
      onChanged: (_) => setState(() {}),
    );
  }

  Widget _dateField(String label, DateTime? value, ValueChanged<DateTime?> onTap) {
    return InkWell(
      onTap: () => onTap(value),
      child: InputDecorator(
        decoration: InputDecoration(labelText: label),
        child: Text(
          value != null
              ? '${value.day.toString().padLeft(2, '0')}/${value.month.toString().padLeft(2, '0')}/${value.year}'
              : 'Sélectionner une date',
        ),
      ),
    );
  }

  Widget _imageSlot(String label, XFile? file, VoidCallback onPick) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Expanded(child: Text(label, style: const TextStyle(fontSize: 13))),
        OutlinedButton.icon(
          onPressed: onPick,
          icon: const Icon(Icons.camera_alt_outlined, size: 18),
          label: Text(file == null ? 'Prendre' : 'Reprendre'),
        ),
      ],
    );
  }
}
