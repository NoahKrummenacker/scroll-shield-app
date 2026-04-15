import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'app_colors.dart';

class ThemeProvider extends ChangeNotifier {
  static const _key = 'theme_label';

  AppColors _colors = AppColors.gris;

  AppColors get colors => _colors;

  ThemeProvider(String? savedLabel) {
    if (savedLabel != null) {
      final found = AppColors.allThemes.where((t) => t.label == savedLabel).firstOrNull;
      if (found != null) _colors = found;
    }
  }

  void setTheme(AppColors newColors) {
    _colors = newColors;
    notifyListeners();
    SharedPreferences.getInstance().then((p) => p.setString(_key, newColors.label));
  }

  static Future<String?> loadSavedLabel() async {
    final p = await SharedPreferences.getInstance();
    return p.getString(_key);
  }
}
