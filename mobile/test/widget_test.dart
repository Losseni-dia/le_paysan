import 'package:flutter_test/flutter_test.dart';

import 'package:le_paysan_mobile/main.dart';

void main() {
  testWidgets('Affiche le catalogue au demarrage (accessible sans connexion)', (WidgetTester tester) async {
    await tester.pumpWidget(const LeePaysanApp());
    await tester.pump();

    expect(find.text('Le Paysan'), findsOneWidget);
    expect(find.text('Rechercher un produit...'), findsOneWidget);

    // Laisse l'appel reseau initial (qui echouera, aucun backend en test) se
    // resoudre pour eviter un timer en suspens a la fin du test. pumpAndSettle
    // ne convient pas ici : l'indicateur de chargement anime en continu tant
    // que l'appel est en cours.
    await tester.pump(const Duration(seconds: 11));
  });
}
