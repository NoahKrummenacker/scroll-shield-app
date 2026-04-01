import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'theme/theme_provider.dart';
import 'screens/home_screen.dart';
import 'screens/settings_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(
    ChangeNotifierProvider(
      create: (_) => ThemeProvider(),
      child:  const ScrollShieldApp(),
    ),
  );
}

class ScrollShieldApp extends StatelessWidget {
  const ScrollShieldApp({super.key});

  @override
  Widget build(BuildContext context) {
    final themeProvider = context.watch<ThemeProvider>();
    final isLight = themeProvider.colors.brightness == Brightness.light;
    SystemChrome.setSystemUIOverlayStyle(SystemUiOverlayStyle(
      statusBarColor:          Colors.transparent,
      statusBarIconBrightness: isLight ? Brightness.dark : Brightness.light,
    ));
    return MaterialApp(
      title:                      'ScrollShield',
      debugShowCheckedModeBanner: false,
      theme:                      themeProvider.colors.toThemeData(),
      home:                       const _RootScreen(),
    );
  }
}

class _RootScreen extends StatefulWidget {
  const _RootScreen();

  @override
  State<_RootScreen> createState() => _RootScreenState();
}

class _RootScreenState extends State<_RootScreen> {
  int _currentIndex = 0;

  static const _screens = [HomeScreen(), SettingsScreen()];

  @override
  Widget build(BuildContext context) {
    final colors = context.watch<ThemeProvider>().colors;

    return Scaffold(
      backgroundColor: colors.background,
      body: AnimatedSwitcher(
        duration:       const Duration(milliseconds: 250),
        switchInCurve:  Curves.easeOut,
        switchOutCurve: Curves.easeIn,
        transitionBuilder: (child, anim) => FadeTransition(
          opacity: anim,
          child:   SlideTransition(
            position: Tween<Offset>(
              begin: Offset(_currentIndex == 0 ? -0.05 : 0.05, 0),
              end:   Offset.zero,
            ).animate(anim),
            child: child,
          ),
        ),
        child: KeyedSubtree(
          key:   ValueKey(_currentIndex),
          child: _screens[_currentIndex],
        ),
      ),
      bottomNavigationBar: _BottomNav(
        currentIndex: _currentIndex,
        colors:       colors,
        onTap:        (i) => setState(() => _currentIndex = i),
      ),
    );
  }
}

class _BottomNav extends StatelessWidget {
  final int     currentIndex;
  final dynamic colors;
  final ValueChanged<int> onTap;

  const _BottomNav({required this.currentIndex, required this.colors, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color:  colors.surface,
        border: Border(top: BorderSide(color: colors.card, width: 1)),
      ),
      child: SafeArea(
        top: false,
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 8),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _NavItem(icon: Icons.shield_outlined, iconActive: Icons.shield,
                label: 'Accueil', selected: currentIndex == 0, colors: colors, onTap: () => onTap(0)),
              _NavItem(icon: Icons.settings_outlined, iconActive: Icons.settings,
                label: 'Paramètres', selected: currentIndex == 1, colors: colors, onTap: () => onTap(1)),
            ],
          ),
        ),
      ),
    );
  }
}

class _NavItem extends StatelessWidget {
  final IconData icon, iconActive;
  final String label;
  final bool selected;
  final dynamic colors;
  final VoidCallback onTap;

  const _NavItem({required this.icon, required this.iconActive, required this.label,
    required this.selected, required this.colors, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap:    onTap,
      behavior: HitTestBehavior.opaque,
      child: AnimatedContainer(
        duration:   const Duration(milliseconds: 200),
        padding:    const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
        decoration: BoxDecoration(
          color:        selected ? colors.accent.withOpacity(0.12) : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(selected ? iconActive : icon,
              color: selected ? colors.accent : colors.textSecondary, size: 22),
            const SizedBox(height: 3),
            Text(label, style: TextStyle(
              color:      selected ? colors.accent : colors.textSecondary,
              fontSize:   11,
              fontWeight: selected ? FontWeight.w600 : FontWeight.normal,
            )),
          ],
        ),
      ),
    );
  }
}
