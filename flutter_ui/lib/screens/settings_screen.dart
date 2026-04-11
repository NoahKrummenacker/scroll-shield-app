import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../theme/app_colors.dart';
import '../theme/theme_provider.dart';
import '../services/blocker_channel.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  bool      _scheduleEnabled = false;
  TimeOfDay _startTime       = const TimeOfDay(hour: 9,  minute: 0);
  TimeOfDay _endTime         = const TimeOfDay(hour: 22, minute: 0);
  bool      _pinEnabled      = false;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    final schedule   = await BlockerChannel.getSchedule();
    final pinEnabled = await BlockerChannel.isPinEnabled();
    if (!mounted) return;
    setState(() {
      _scheduleEnabled = schedule['enabled']   as bool;
      _startTime       = TimeOfDay(hour: schedule['startHour'] as int, minute: schedule['startMin'] as int);
      _endTime         = TimeOfDay(hour: schedule['endHour']   as int, minute: schedule['endMin']   as int);
      _pinEnabled      = pinEnabled;
    });
  }

  Future<void> _saveSchedule() async {
    await BlockerChannel.setSchedule(
      enabled:   _scheduleEnabled,
      startHour: _startTime.hour,
      startMin:  _startTime.minute,
      endHour:   _endTime.hour,
      endMin:    _endTime.minute,
    );
  }

  Future<void> _pickTime(bool isStart) async {
    final colors = context.read<ThemeProvider>().colors;
    final picked = await showTimePicker(
      context: context,
      initialTime: isStart ? _startTime : _endTime,
      builder: (ctx, child) => Theme(
        data: ThemeData.dark().copyWith(
          colorScheme: ColorScheme.dark(
            primary:   colors.accent,
            onPrimary: colors.background,
            surface:   colors.card,
            onSurface: colors.text,
          ),
        ),
        child: child!,
      ),
    );
    if (picked == null) return;
    setState(() { if (isStart) _startTime = picked; else _endTime = picked; });
    await _saveSchedule();
  }

  @override
  Widget build(BuildContext context) {
    final colors = context.watch<ThemeProvider>().colors;

    return Scaffold(
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
          children: [
            Text('Paramètres',
              style: TextStyle(color: colors.text, fontSize: 22, fontWeight: FontWeight.bold)),
            const SizedBox(height: 24),

            // ── Thème ──────────────────────────────────────────────────────
            _SectionLabel(text: 'Thème', colors: colors),
            const SizedBox(height: 10),
            _ThemeButton(colors: colors),

            const SizedBox(height: 24),
            Divider(color: colors.surface, thickness: 1),
            const SizedBox(height: 24),

            // ── Horaire ────────────────────────────────────────────────────
            _SectionLabel(text: 'Horaire de blocage', colors: colors),
            const SizedBox(height: 10),
            _ScheduleCard(
              colors:      colors,
              enabled:     _scheduleEnabled,
              startTime:   _startTime,
              endTime:     _endTime,
              onToggle: (v) async {
                setState(() => _scheduleEnabled = v);
                await _saveSchedule();
              },
              onPickStart: () => _pickTime(true),
              onPickEnd:   () => _pickTime(false),
            ),

            const SizedBox(height: 24),
            Divider(color: colors.surface, thickness: 1),
            const SizedBox(height: 24),

            // ── Code PIN ───────────────────────────────────────────────────
            _SectionLabel(text: 'Code PIN', colors: colors),
            const SizedBox(height: 10),
            _PinCard(
              colors:     colors,
              pinEnabled: _pinEnabled,
              onChanged:  (v) => setState(() => _pinEnabled = v),
            ),

            const SizedBox(height: 24),
            Divider(color: colors.surface, thickness: 1),
            const SizedBox(height: 24),

            // ── Infos ──────────────────────────────────────────────────────
            _SectionLabel(text: 'Infos', colors: colors),
            const SizedBox(height: 10),
            _InfoCard(colors: colors),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }
}

// ── Thème ──────────────────────────────────────────────────────────────────────

class _ThemeButton extends StatelessWidget {
  final AppColors colors;
  const _ThemeButton({required this.colors});

  @override
  Widget build(BuildContext context) {
    final current = context.watch<ThemeProvider>().colors;
    return GestureDetector(
      onTap: () => showModalBottomSheet(
        context:            context,
        backgroundColor:    Colors.transparent,
        isScrollControlled: true,
        builder: (_) => _ThemeSheet(colors: colors),
      ),
      child: Container(
        padding:    const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        decoration: BoxDecoration(
          color:        colors.card,
          borderRadius: BorderRadius.circular(16),
          border:       Border.all(color: colors.accent.withOpacity(0.25), width: 1),
        ),
        child: Row(
          children: [
            _ColorDot(color: current.surface),
            const SizedBox(width: 4),
            _ColorDot(color: current.accent),
            const SizedBox(width: 12),
            Expanded(
              child: Text(current.label,
                style: TextStyle(color: colors.text, fontWeight: FontWeight.w600, fontSize: 15)),
            ),
            Icon(Icons.chevron_right, color: colors.textSecondary, size: 20),
          ],
        ),
      ),
    );
  }
}

class _ThemeSheet extends StatelessWidget {
  final AppColors colors;
  const _ThemeSheet({required this.colors});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<ThemeProvider>();
    return Container(
      decoration: BoxDecoration(
        color:        colors.card,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
      ),
      padding: const EdgeInsets.fromLTRB(20, 12, 20, 32),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 36, height: 4,
            margin: const EdgeInsets.only(bottom: 20),
            decoration: BoxDecoration(color: colors.surface, borderRadius: BorderRadius.circular(2)),
          ),
          Text('Choisir un thème',
            style: TextStyle(color: colors.text, fontWeight: FontWeight.bold, fontSize: 16)),
          const SizedBox(height: 16),
          Flexible(
            child: SingleChildScrollView(
              child: Column(
                children: AppColors.allThemes.map((theme) {
                  final isSelected = provider.colors.label == theme.label;
                  return GestureDetector(
                    onTap: () {
                      provider.setTheme(theme);
                      Navigator.pop(context);
                    },
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 200),
                      margin:   const EdgeInsets.only(bottom: 10),
                      padding:  const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                      decoration: BoxDecoration(
                        color:        isSelected ? theme.accent.withOpacity(0.12) : colors.surface,
                        borderRadius: BorderRadius.circular(14),
                        border: Border.all(
                          color: isSelected ? theme.accent.withOpacity(0.5) : Colors.transparent,
                          width: 1.5,
                        ),
                      ),
                      child: Row(
                        children: [
                          _ColorDot(color: theme.surface, size: 16),
                          const SizedBox(width: 4),
                          _ColorDot(color: theme.accent, size: 16),
                          const SizedBox(width: 14),
                          Expanded(
                            child: Text(theme.label, style: TextStyle(
                              color:      isSelected ? theme.accent : colors.text,
                              fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                              fontSize:   15,
                            )),
                          ),
                          if (isSelected) Icon(Icons.check_circle, color: theme.accent, size: 18),
                        ],
                      ),
                    ),
                  );
                }).toList(),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ColorDot extends StatelessWidget {
  final Color  color;
  final double size;
  const _ColorDot({required this.color, this.size = 14});

  @override
  Widget build(BuildContext context) => Container(
    width: size, height: size,
    decoration: BoxDecoration(color: color, shape: BoxShape.circle),
  );
}

// ── Horaire ────────────────────────────────────────────────────────────────────

class _ScheduleCard extends StatelessWidget {
  final AppColors   colors;
  final bool        enabled;
  final TimeOfDay   startTime;
  final TimeOfDay   endTime;
  final ValueChanged<bool> onToggle;
  final VoidCallback onPickStart;
  final VoidCallback onPickEnd;

  const _ScheduleCard({
    required this.colors, required this.enabled, required this.startTime,
    required this.endTime, required this.onToggle,
    required this.onPickStart, required this.onPickEnd,
  });

  String _fmt(TimeOfDay t) =>
      '${t.hour.toString().padLeft(2, '0')}:${t.minute.toString().padLeft(2, '0')}';

  @override
  Widget build(BuildContext context) {
    return AnimatedContainer(
      duration: const Duration(milliseconds: 280),
      decoration: BoxDecoration(
        color:        colors.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: enabled ? colors.accent.withOpacity(0.35) : Colors.transparent,
          width: 1,
        ),
      ),
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Activer l\'horaire',
                        style: TextStyle(color: colors.text, fontWeight: FontWeight.w600, fontSize: 15)),
                      const SizedBox(height: 3),
                      Text('Bloquer uniquement dans la plage horaire définie',
                        style: TextStyle(color: colors.textSecondary, fontSize: 12, height: 1.4)),
                    ],
                  ),
                ),
                Switch(
                  value:            enabled,
                  onChanged:        onToggle,
                  activeColor:      colors.accentForeground,
                  activeTrackColor: colors.accent,
                ),
              ],
            ),
          ),
          AnimatedCrossFade(
            duration:       const Duration(milliseconds: 250),
            crossFadeState: enabled ? CrossFadeState.showFirst : CrossFadeState.showSecond,
            firstChild: Padding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 14),
              child: Row(
                children: [
                  Expanded(child: _TimeButton(label: 'Début', time: _fmt(startTime),
                    colors: colors, onTap: onPickStart)),
                  const SizedBox(width: 12),
                  Expanded(child: _TimeButton(label: 'Fin',   time: _fmt(endTime),
                    colors: colors, onTap: onPickEnd)),
                ],
              ),
            ),
            secondChild: const SizedBox.shrink(),
          ),
        ],
      ),
    );
  }
}

class _TimeButton extends StatelessWidget {
  final String label, time;
  final AppColors colors;
  final VoidCallback onTap;
  const _TimeButton({required this.label, required this.time, required this.colors, required this.onTap});

  @override
  Widget build(BuildContext context) => GestureDetector(
    onTap: onTap,
    child: Container(
      padding:    const EdgeInsets.symmetric(vertical: 12),
      decoration: BoxDecoration(
        color:        colors.accent.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
        border:       Border.all(color: colors.accent.withOpacity(0.3), width: 1),
      ),
      child: Column(
        children: [
          Text(label, style: TextStyle(color: colors.textSecondary, fontSize: 11)),
          const SizedBox(height: 4),
          Text(time,  style: TextStyle(color: colors.accent, fontWeight: FontWeight.bold, fontSize: 18)),
        ],
      ),
    ),
  );
}

// ── Code PIN ───────────────────────────────────────────────────────────────────

class _PinCard extends StatelessWidget {
  final AppColors colors;
  final bool      pinEnabled;
  final ValueChanged<bool> onChanged;

  const _PinCard({required this.colors, required this.pinEnabled, required this.onChanged});

  void _showSetDialog(BuildContext context) {
    showDialog(
      context:           context,
      barrierDismissible: false,
      builder: (_) => _PinSetDialog(colors: colors, onSuccess: () => onChanged(true)),
    );
  }

  void _showRemoveDialog(BuildContext context) {
    showDialog(
      context:           context,
      barrierDismissible: false,
      builder: (_) => _PinRemoveDialog(colors: colors, onSuccess: () => onChanged(false)),
    );
  }

  @override
  Widget build(BuildContext context) => Container(
    padding:    const EdgeInsets.all(16),
    decoration: BoxDecoration(
      color:        colors.card,
      borderRadius: BorderRadius.circular(16),
      border: Border.all(
        color: pinEnabled ? colors.accent.withOpacity(0.35) : Colors.transparent,
        width: 1,
      ),
    ),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Protéger les paramètres',
          style: TextStyle(color: colors.text, fontWeight: FontWeight.w600, fontSize: 15)),
        const SizedBox(height: 3),
        Text('Un code PIN à 4 chiffres empêche de désactiver le blocage.',
          style: TextStyle(color: colors.textSecondary, fontSize: 12, height: 1.4)),
        const SizedBox(height: 14),
        SizedBox(
          width: double.infinity,
          child: AnimatedSwitcher(
            duration: const Duration(milliseconds: 200),
            child: pinEnabled
                ? OutlinedButton(
                    key:       const ValueKey('remove'),
                    onPressed: () => _showRemoveDialog(context),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: colors.error,
                      minimumSize: const Size(double.infinity, 0),
                      side:  BorderSide(color: colors.error.withOpacity(0.5)),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                    child: const Text('Supprimer le code PIN'),
                  )
                : ElevatedButton(
                    key:       const ValueKey('set'),
                    onPressed: () => _showSetDialog(context),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: colors.accent,
                      foregroundColor: colors.accentForeground,
                      minimumSize: const Size(double.infinity, 0),
                      elevation: 0,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                    child: const Text('Définir un code PIN',
                      style: TextStyle(fontWeight: FontWeight.w600)),
                  ),
          ),
        ),
      ],
    ),
  );
}

// ── Dialogs PIN ────────────────────────────────────────────────────────────────

class _PinSetDialog extends StatefulWidget {
  final AppColors    colors;
  final VoidCallback onSuccess;
  const _PinSetDialog({required this.colors, required this.onSuccess});

  @override
  State<_PinSetDialog> createState() => _PinSetDialogState();
}

class _PinSetDialogState extends State<_PinSetDialog> {
  String _pin        = '';
  String _confirm    = '';
  bool   _confirming = false;
  String _error      = '';

  void _onKey(String digit) async {
    if (!_confirming && _pin.length >= 4) return;
    if (_confirming  && _confirm.length >= 4) return;

    setState(() {
      _error = '';
      if (_confirming) _confirm += digit; else _pin += digit;
    });

    final current = _confirming ? _confirm : _pin;
    if (current.length < 4) return;

    if (!_confirming) {
      setState(() => _confirming = true);
    } else {
      if (_pin == _confirm) {
        await BlockerChannel.setPin(_pin);
        if (mounted) Navigator.pop(context);
        widget.onSuccess();
      } else {
        setState(() { _confirm = ''; _error = 'Les codes ne correspondent pas'; });
      }
    }
  }

  void _onDelete() {
    setState(() {
      _error = '';
      if (_confirming) {
        if (_confirm.isNotEmpty) _confirm = _confirm.substring(0, _confirm.length - 1);
      } else {
        if (_pin.isNotEmpty) _pin = _pin.substring(0, _pin.length - 1);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final c      = widget.colors;
    final active = _confirming ? _confirm : _pin;
    final title  = _confirming ? 'Confirmer le code PIN' : 'Définir un code PIN';
    return _PinDialogShell(
      colors: c, title: title, pin: active, error: _error,
      onKey: _onKey, onDelete: _onDelete,
      onCancel: () => Navigator.pop(context),
    );
  }
}

class _PinRemoveDialog extends StatefulWidget {
  final AppColors    colors;
  final VoidCallback onSuccess;
  const _PinRemoveDialog({required this.colors, required this.onSuccess});

  @override
  State<_PinRemoveDialog> createState() => _PinRemoveDialogState();
}

class _PinRemoveDialogState extends State<_PinRemoveDialog> {
  String _pin   = '';
  String _error = '';

  void _onKey(String digit) async {
    if (_pin.length >= 4) return;
    final next = _pin + digit;
    setState(() { _pin = next; _error = ''; });
    if (next.length < 4) return;

    final ok = await BlockerChannel.verifyPin(next);
    if (ok) {
      await BlockerChannel.removePin();
      if (mounted) Navigator.pop(context);
      widget.onSuccess();
    } else {
      setState(() { _pin = ''; _error = 'Code incorrect'; });
    }
  }

  void _onDelete() {
    if (_pin.isEmpty) return;
    setState(() { _pin = _pin.substring(0, _pin.length - 1); _error = ''; });
  }

  @override
  Widget build(BuildContext context) => _PinDialogShell(
    colors: widget.colors, title: 'Entrer le code PIN',
    pin: _pin, error: _error,
    onKey: _onKey, onDelete: _onDelete,
    onCancel: () => Navigator.pop(context),
  );
}

// Shell partagé pour les dialogs PIN
class _PinDialogShell extends StatelessWidget {
  final AppColors    colors;
  final String       title;
  final String       pin;
  final String       error;
  final void Function(String) onKey;
  final VoidCallback onDelete;
  final VoidCallback onCancel;

  const _PinDialogShell({
    required this.colors, required this.title, required this.pin,
    required this.error, required this.onKey, required this.onDelete,
    required this.onCancel,
  });

  @override
  Widget build(BuildContext context) {
    final c = colors;
    return Dialog(
      backgroundColor: c.card,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      insetPadding: const EdgeInsets.symmetric(horizontal: 32, vertical: 24),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 28, 16, 20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(title,
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
                  color: i < pin.length ? c.accent : c.surface,
                ),
              )),
            ),
            if (error.isNotEmpty) ...[
              const SizedBox(height: 12),
              Text(error, style: TextStyle(color: c.error, fontSize: 12)),
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
                        onTap: key == '⌫' ? onDelete : () => onKey(key),
                        child: Container(
                          height: 52,
                          margin: const EdgeInsets.symmetric(horizontal: 4),
                          decoration: BoxDecoration(
                            color:        c.surface,
                            borderRadius: BorderRadius.circular(14),
                          ),
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
              onPressed: onCancel,
              child: Text('Annuler', style: TextStyle(color: c.textSecondary)),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Infos ──────────────────────────────────────────────────────────────────────

class _InfoCard extends StatelessWidget {
  final AppColors colors;
  const _InfoCard({required this.colors});

  @override
  Widget build(BuildContext context) => Container(
    padding:    const EdgeInsets.all(16),
    decoration: BoxDecoration(color: colors.card, borderRadius: BorderRadius.circular(16)),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('ScrollShield',
          style: TextStyle(color: colors.text, fontWeight: FontWeight.w600, fontSize: 15)),
        const SizedBox(height: 4),
        Text(
          'Bloque automatiquement les Reels Instagram et Shorts YouTube via le service d\'accessibilité Android.',
          style: TextStyle(color: colors.textSecondary, fontSize: 12, height: 1.5),
        ),
        const SizedBox(height: 12),
        Text('v1.0.0', style: TextStyle(color: colors.textSecondary, fontSize: 12)),
      ],
    ),
  );
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
      Text(text,
        style: TextStyle(color: colors.accent, fontWeight: FontWeight.w600, fontSize: 13)),
    ],
  );
}
