import 'package:flutter/material.dart';

class AppColors {
  final Color background;
  final Color surface;
  final Color card;
  final Color accent;
  final Color accentForeground;
  final Color text;
  final Color textSecondary;
  final Color success;
  final Color error;
  final String label;
  final Brightness brightness;

  const AppColors({
    required this.background,
    required this.surface,
    required this.card,
    required this.accent,
    required this.accentForeground,
    required this.text,
    required this.textSecondary,
    required this.success,
    required this.error,
    required this.label,
    this.brightness = Brightness.dark,
  });

  static const bleuJaune = AppColors(
    background:       Color(0xFF060D15),
    surface:          Color(0xFF0F2035),
    card:             Color(0xFF142844),
    accent:           Color(0xFFFFD60A),
    accentForeground: Color(0xFF060D15),
    text:             Color(0xFFE8EDF5),
    textSecondary:    Color(0xFF8A9BB0),
    success:          Color(0xFF4CAF50),
    error:            Color(0xFFCF6679),
    label:            'Bleu & Jaune',
  );

  static const grisFonce = AppColors(
    background:       Color(0xFF0D0D0D),
    surface:          Color(0xFF1A1A1A),
    card:             Color(0xFF222222),
    accent:           Color(0xFFE0E0E0),
    accentForeground: Color(0xFF0D0D0D),
    text:             Color(0xFFEEEEEE),
    textSecondary:    Color(0xFF888888),
    success:          Color(0xFF4CAF50),
    error:            Color(0xFFCF6679),
    label:            'Gris foncé',
  );

  static const gris = AppColors(
    background:       Color(0xFF1E1E1E),
    surface:          Color(0xFF2A2A2A),
    card:             Color(0xFF333333),
    accent:           Color(0xFFBDBDBD),
    accentForeground: Color(0xFF1E1E1E),
    text:             Color(0xFFE8E8E8),
    textSecondary:    Color(0xFF9E9E9E),
    success:          Color(0xFF4CAF50),
    error:            Color(0xFFCF6679),
    label:            'Gris',
  );

  static const clair = AppColors(
    background:       Color(0xFFF5F5F5),
    surface:          Color(0xFFE0E0E0),
    card:             Color(0xFFFFFFFF),
    accent:           Color(0xFF1A237E),
    accentForeground: Color(0xFFFFFFFF),
    text:             Color(0xFF1A1A1A),
    textSecondary:    Color(0xFF757575),
    success:          Color(0xFF388E3C),
    error:            Color(0xFFC62828),
    label:            'Clair',
    brightness:       Brightness.light,
  );

  static const bossLady = AppColors(
    background:       Color(0xFF090909),
    surface:          Color(0xFF1A0E18),
    card:             Color(0xFF231422),
    accent:           Color(0xFFFF2576),
    accentForeground: Color(0xFF090909),
    text:             Color(0xFFF8EEF2),
    textSecondary:    Color(0xFF9A7585),
    success:          Color(0xFF4CAF50),
    error:            Color(0xFFFF6B9D),
    label:            'Boss Lady',
  );

  static const allThemes = [bleuJaune, grisFonce, gris, clair, bossLady];

  ThemeData toThemeData() {
    if (brightness == Brightness.light) {
      return ThemeData(
        useMaterial3:            true,
        brightness:              Brightness.light,
        scaffoldBackgroundColor: background,
        colorScheme: ColorScheme.light(
          primary: accent, onPrimary: accentForeground,
          secondary: accent, onSecondary: accentForeground,
          surface: surface, onSurface: text, error: error,
        ),
        cardTheme: CardThemeData(color: card, elevation: 0,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16))),
        switchTheme: SwitchThemeData(
          thumbColor: WidgetStateProperty.resolveWith(
            (s) => s.contains(WidgetState.selected) ? accentForeground : Colors.white),
          trackColor: WidgetStateProperty.resolveWith(
            (s) => s.contains(WidgetState.selected) ? accent : surface),
        ),
        dividerColor: surface,
        textTheme: TextTheme(
          bodyMedium: TextStyle(color: text), bodySmall: TextStyle(color: textSecondary)),
      );
    }
    return ThemeData(
      useMaterial3:            true,
      brightness:              Brightness.dark,
      scaffoldBackgroundColor: background,
      colorScheme: ColorScheme.dark(
        primary: accent, onPrimary: accentForeground,
        secondary: accent, onSecondary: accentForeground,
        surface: surface, onSurface: text, error: error,
      ),
      cardTheme: CardThemeData(color: card, elevation: 0,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16))),
      switchTheme: SwitchThemeData(
        thumbColor: WidgetStateProperty.resolveWith(
          (s) => s.contains(WidgetState.selected) ? accentForeground : textSecondary),
        trackColor: WidgetStateProperty.resolveWith(
          (s) => s.contains(WidgetState.selected) ? accent : surface),
      ),
      dividerColor: surface,
      textTheme: TextTheme(
        bodyMedium: TextStyle(color: text), bodySmall: TextStyle(color: textSecondary)),
    );
  }
}
