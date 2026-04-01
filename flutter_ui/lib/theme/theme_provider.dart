import 'package:flutter/material.dart';
import 'app_colors.dart';

class ThemeProvider extends ChangeNotifier {
  AppColors _colors = AppColors.gris;

  AppColors get colors => _colors;

  void setTheme(AppColors newColors) {
    _colors = newColors;
    notifyListeners();
  }
}
