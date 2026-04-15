import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../theme/app_colors.dart';
import '../theme/theme_provider.dart';
import '../services/blocker_channel.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with WidgetsBindingObserver {
  bool _serviceEnabled  = false;
  bool _blockReels      = true;
  bool _blockReelsFeed  = false;
  bool _allowDmReels    = false;
  bool _blockShorts     = true;
  bool _blockShortsFeed = false;
  bool _pinEnabled      = false;
  bool _limitEnabled    = false;
  int  _limitMinutes    = 30;
  int  _usageSeconds    = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _refresh();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) _refresh();
  }

  Future<void> _refresh() async {
    final enabled    = await BlockerChannel.isServiceEnabled();
    final prefs      = await BlockerChannel.getPrefs();
    final pinEnabled = await BlockerChannel.isPinEnabled();
    final limit      = await BlockerChannel.getDailyLimit();
    if (!mounted) return;
    setState(() {
      _serviceEnabled  = enabled;
      _blockReels      = prefs['blockReels']      ?? true;
      _blockReelsFeed  = prefs['blockReelsFeed']  ?? false;
      _allowDmReels    = prefs['allowDmReels']    ?? false;
      _blockShorts     = prefs['blockShorts']     ?? true;
      _blockShortsFeed = prefs['blockShortsFeed'] ?? false;
      _pinEnabled      = pinEnabled;
      _limitEnabled    = limit['enabled']      as bool;
      _limitMinutes    = limit['limitMinutes'] as int;
      _usageSeconds    = limit['usageSeconds'] as int;
    });
  }

  // Demande le PIN si activé et que l'utilisateur veut désactiver (newValue = false)
  Future<void> _onToggle(String key, bool newValue, Function(bool) setter) async {
    if (_pinEnabled && !newValue) {
      final ok = await _askPin();
      if (!ok) return;
    }
    setter(newValue);
    await BlockerChannel.setPref(key, newValue);
  }

  Future<bool> _askPin() async {
    final colors = context.read<ThemeProvider>().colors;
    final result = await showDialog<bool>(
      context:           context,
      barrierDismissible: false,
      builder: (_) => _PinVerifyDialog(colors: colors),
    );
    return result == true;
  }

  @override
  Widget build(BuildContext context) {
    final colors = context.watch<ThemeProvider>().colors;

    return Scaffold(
      body: SafeArea(
        child: RefreshIndicator(
          color:    colors.accent,
          onRefresh: _refresh,
          child: ListView(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
            children: [
              Text('ScrollShield',
                style: TextStyle(color: colors.text, fontSize: 22, fontWeight: FontWeight.bold)),
              const SizedBox(height: 20),

              // ── Statut du service ──────────────────────────────────────
              _StatusCard(colors: colors, enabled: _serviceEnabled),
              if (_limitEnabled) ...[
                const SizedBox(height: 10),
                _DailyProgressCard(
                  colors:       colors,
                  limitMinutes: _limitMinutes,
                  usageSeconds: _usageSeconds,
                ),
              ],
              const SizedBox(height: 24),

              // ── Instagram ──────────────────────────────────────────────
              _SectionLabel(text: 'Instagram', colors: colors),
              const SizedBox(height: 10),
              _ToggleCard(
                colors:    colors,
                title:     'Bloquer l\'onglet Reels',
                subtitle:  'Redirige vers l\'accueil si l\'onglet Reels est sélectionné.',
                value:     _blockReels,
                onChanged: (v) => _onToggle('blockReels', v, (x) => setState(() => _blockReels = x)),
              ),
              const SizedBox(height: 10),
              _ToggleCard(
                colors:    colors,
                title:     'Bloquer les Reels partout',
                subtitle:  'Redirige aussi si un Reel s\'ouvre depuis le fil ou l\'explore.',
                value:     _blockReelsFeed,
                onChanged: (v) => _onToggle('blockReelsFeed', v, (x) => setState(() => _blockReelsFeed = x)),
              ),
              const SizedBox(height: 10),
              _ToggleCard(
                colors:    colors,
                title:     'Autoriser les Reels reçus en DM',
                subtitle:  'Regarde le Reel reçu en message sans pouvoir en voir d\'autres.',
                value:     _allowDmReels,
                onChanged: (v) => _onToggle('allowDmReels', v, (x) => setState(() => _allowDmReels = x)),
              ),
              const SizedBox(height: 24),

              // ── YouTube ────────────────────────────────────────────────
              _SectionLabel(text: 'YouTube', colors: colors),
              const SizedBox(height: 10),
              _ToggleCard(
                colors:    colors,
                title:     'Bloquer l\'onglet Shorts',
                subtitle:  'Redirige vers l\'accueil si l\'onglet Shorts est sélectionné.',
                value:     _blockShorts,
                onChanged: (v) => _onToggle('blockShorts', v, (x) => setState(() => _blockShorts = x)),
              ),
              const SizedBox(height: 10),
              _ToggleCard(
                colors:    colors,
                title:     'Bloquer les Shorts partout',
                subtitle:  'Redirige aussi si un Short s\'ouvre hors de l\'onglet dédié.',
                value:     _blockShortsFeed,
                onChanged: (v) => _onToggle('blockShortsFeed', v, (x) => setState(() => _blockShortsFeed = x)),
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }
}

// ── Statut ─────────────────────────────────────────────────────────────────────

class _StatusCard extends StatefulWidget {
  final AppColors colors;
  final bool      enabled;
  const _StatusCard({required this.colors, required this.enabled});

  @override
  State<_StatusCard> createState() => _StatusCardState();
}

class _StatusCardState extends State<_StatusCard> with SingleTickerProviderStateMixin {
  late AnimationController _pulse;

  @override
  void initState() {
    super.initState();
    _pulse = AnimationController(vsync: this, duration: const Duration(milliseconds: 1200))
      ..repeat(reverse: true);
  }

  @override
  void dispose() {
    _pulse.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final c = widget.colors;
    final enabled = widget.enabled;
    final dotColor = enabled ? c.success : c.error;

    return AnimatedContainer(
      duration: const Duration(milliseconds: 300),
      padding:  const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color:        enabled ? c.success.withOpacity(0.08) : c.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: enabled ? c.success.withOpacity(0.3) : c.surface,
          width: 1,
        ),
      ),
      child: Row(
        children: [
          AnimatedBuilder(
            animation: _pulse,
            builder: (_, __) => Container(
              width: 10, height: 10,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: dotColor.withOpacity(enabled ? 0.4 + 0.6 * _pulse.value : 1.0),
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  enabled ? 'Service actif' : 'Service inactif',
                  style: TextStyle(
                    color:      enabled ? c.success : c.error,
                    fontWeight: FontWeight.w600,
                    fontSize:   15,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  enabled
                      ? 'Le blocage est opérationnel.'
                      : 'Activez le service dans les paramètres d\'accessibilité.',
                  style: TextStyle(color: c.textSecondary, fontSize: 12),
                ),
              ],
            ),
          ),
          if (!enabled)
            GestureDetector(
              onTap: BlockerChannel.openAccessibilitySettings,
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color:        c.accent.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(10),
                  border:       Border.all(color: c.accent.withOpacity(0.4), width: 1),
                ),
                child: Text('Activer', style: TextStyle(color: c.accent, fontWeight: FontWeight.w600, fontSize: 13)),
              ),
            ),
        ],
      ),
    );
  }
}

// ── Toggle card ────────────────────────────────────────────────────────────────

class _ToggleCard extends StatelessWidget {
  final AppColors colors;
  final String    title;
  final String    subtitle;
  final bool      value;
  final void Function(bool) onChanged;

  const _ToggleCard({
    required this.colors,
    required this.title,
    required this.subtitle,
    required this.value,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) => AnimatedContainer(
    duration: const Duration(milliseconds: 250),
    padding:  const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    decoration: BoxDecoration(
      color:        value ? colors.accent.withOpacity(0.08) : colors.card,
      borderRadius: BorderRadius.circular(16),
      border: Border.all(
        color: value ? colors.accent.withOpacity(0.3) : Colors.transparent,
        width: 1,
      ),
    ),
    child: Row(
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: TextStyle(color: colors.text, fontWeight: FontWeight.w600, fontSize: 15)),
              const SizedBox(height: 3),
              Text(subtitle, style: TextStyle(color: colors.textSecondary, fontSize: 12, height: 1.4)),
            ],
          ),
        ),
        Switch(
          value:            value,
          onChanged:        onChanged,
          activeColor:      colors.accentForeground,
          activeTrackColor: colors.accent,
        ),
      ],
    ),
  );
}

// ── PIN verify dialog ──────────────────────────────────────────────────────────

class _PinVerifyDialog extends StatefulWidget {
  final AppColors colors;
  const _PinVerifyDialog({required this.colors});

  @override
  State<_PinVerifyDialog> createState() => _PinVerifyDialogState();
}

class _PinVerifyDialogState extends State<_PinVerifyDialog> {
  String _pin   = '';
  String _error = '';

  void _onKey(String digit) async {
    if (_pin.length >= 4) return;
    final next = _pin + digit;
    setState(() { _pin = next; _error = ''; });
    if (next.length < 4) return;

    final ok = await BlockerChannel.verifyPin(next);
    if (ok) {
      if (mounted) Navigator.pop(context, true);
    } else {
      setState(() { _pin = ''; _error = 'Code incorrect'; });
    }
  }

  void _onDelete() {
    if (_pin.isEmpty) return;
    setState(() { _pin = _pin.substring(0, _pin.length - 1); _error = ''; });
  }

  @override
  Widget build(BuildContext context) {
    final c = widget.colors;
    return Dialog(
      backgroundColor: c.card,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      insetPadding: const EdgeInsets.symmetric(horizontal: 32, vertical: 24),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 28, 16, 20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('Entrer le code PIN',
              style: TextStyle(color: c.text, fontWeight: FontWeight.bold, fontSize: 17)),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(4, (i) => AnimatedContainer(
                duration: const Duration(milliseconds: 200),
                margin: const EdgeInsets.symmetric(horizontal: 8),
                width: 14, height: 14,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: i < _pin.length ? c.accent : c.surface,
                ),
              )),
            ),
            if (_error.isNotEmpty) ...[
              const SizedBox(height: 12),
              Text(_error, style: TextStyle(color: c.error, fontSize: 12)),
            ],
            const SizedBox(height: 24),
            ...[['1','2','3'],['4','5','6'],['7','8','9'],['','0','⌫']].map((row) =>
              Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: Row(
                  children: row.map((key) {
                    if (key.isEmpty) return const Expanded(child: SizedBox(height: 52));
                    return Expanded(
                      child: GestureDetector(
                        onTap: key == '⌫' ? _onDelete : () => _onKey(key),
                        child: Container(
                          height: 52,
                          margin: const EdgeInsets.symmetric(horizontal: 4),
                          decoration: BoxDecoration(color: c.surface, borderRadius: BorderRadius.circular(14)),
                          child: Center(
                            child: Text(key, style: TextStyle(
                              color:      key == '⌫' ? c.textSecondary : c.text,
                              fontSize:   key == '⌫' ? 18 : 20,
                              fontWeight: FontWeight.w600,
                            )),
                          ),
                        ),
                      ),
                    );
                  }).toList(),
                ),
              ),
            ),
            const SizedBox(height: 8),
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: Text('Annuler', style: TextStyle(color: c.textSecondary)),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Barre de progression quotidienne ─────────────────────────────────────────

class _DailyProgressCard extends StatelessWidget {
  final AppColors colors;
  final int       limitMinutes;
  final int       usageSeconds;
  const _DailyProgressCard({
    required this.colors,
    required this.limitMinutes,
    required this.usageSeconds,
  });

  String _fmtUsage() {
    final m = usageSeconds ~/ 60;
    final s = usageSeconds  % 60;
    if (m == 0) return '${s}s';
    return '${m} min ${s.toString().padLeft(2, '0')} sec';
  }

  String _fmtLimit() {
    if (limitMinutes < 60) return '$limitMinutes min';
    final h = limitMinutes ~/ 60;
    final rem = limitMinutes % 60;
    return rem == 0 ? '${h}h' : '${h}h ${rem}min';
  }

  @override
  Widget build(BuildContext context) {
    final c            = colors;
    final progress     = limitMinutes > 0
        ? (usageSeconds / (limitMinutes * 60)).clamp(0.0, 1.0)
        : 0.0;
    final limitReached = usageSeconds >= limitMinutes * 60;
    final barColor     = limitReached ? c.error : c.accent;

    return Container(
      padding:    const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color:        limitReached ? c.error.withOpacity(0.08) : c.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: limitReached ? c.error.withOpacity(0.3) : c.accent.withOpacity(0.2),
          width: 1,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('Limite quotidienne',
                style: TextStyle(color: c.text, fontWeight: FontWeight.w600, fontSize: 13)),
              Text(
                limitReached ? 'Limite atteinte' : '${_fmtUsage()} / ${_fmtLimit()}',
                style: TextStyle(
                  color:      barColor,
                  fontSize:   12,
                  fontWeight: limitReached ? FontWeight.w600 : FontWeight.normal,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          ClipRRect(
            borderRadius: BorderRadius.circular(4),
            child: LinearProgressIndicator(
              value:           progress,
              minHeight:       5,
              backgroundColor: c.surface,
              valueColor:      AlwaysStoppedAnimation<Color>(barColor),
            ),
          ),
        ],
      ),
    );
  }
}

// ── Utilitaires ───────────────────────────────────────────────────────────────

class _SectionLabel extends StatelessWidget {
  final String    text;
  final AppColors colors;
  const _SectionLabel({required this.text, required this.colors});

  @override
  Widget build(BuildContext context) => Row(
    children: [
      Container(
        width: 3, height: 16,
        decoration: BoxDecoration(color: colors.accent, borderRadius: BorderRadius.circular(2)),
      ),
      const SizedBox(width: 8),
      Text(text, style: TextStyle(color: colors.accent, fontWeight: FontWeight.w600, fontSize: 13)),
    ],
  );
}
