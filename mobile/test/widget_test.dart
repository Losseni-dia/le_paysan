import 'package:flutter_test/flutter_test.dart';

import 'package:le_paysan_mobile/main.dart';

void main() {
  testWidgets('Affiche l\'ecran de connexion au demarrage (non authentifie)', (WidgetTester tester) async {
    await tester.pumpWidget(const LeePaysanApp());
    await tester.pump();

    expect(find.text('Le Paysan'), findsOneWidget);
    expect(find.text('Se connecter'), findsOneWidget);
  });
}
