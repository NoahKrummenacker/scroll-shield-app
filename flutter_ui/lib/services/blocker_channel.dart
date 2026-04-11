import 'package:flutter/services.dart';

class BlockerChannel {
  static const _channel = MethodChannel('com.example.contentblocker/blocker');

  static Future<bool> isServiceEnabled() async {
    try {
      return await _channel.invokeMethod<bool>('isServiceEnabled') ?? false;
    } catch (_) {
      return false;
    }
  }

  static Future<Map<String, bool>> getPrefs() async {
    try {
      final result = await _channel.invokeMethod<Map>('getPrefs');
      return {
        'blockReels':      result?['blockReels']      as bool? ?? true,
        'blockReelsFeed':  result?['blockReelsFeed']  as bool? ?? false,
        'blockShorts':     result?['blockShorts']     as bool? ?? true,
        'blockShortsFeed': result?['blockShortsFeed'] as bool? ?? false,
      };
    } catch (_) {
      return {'blockReels': true, 'blockReelsFeed': false, 'blockShorts': true, 'blockShortsFeed': false};
    }
  }

  static Future<void> setPref(String key, bool value) async {
    try {
      await _channel.invokeMethod('setPref', {'key': key, 'value': value});
    } catch (_) {}
  }

  static Future<Map<String, dynamic>> getSchedule() async {
    try {
      final result = await _channel.invokeMethod<Map>('getSchedule');
      return {
        'enabled':   result?['enabled']   as bool? ?? false,
        'startHour': result?['startHour'] as int?  ?? 9,
        'startMin':  result?['startMin']  as int?  ?? 0,
        'endHour':   result?['endHour']   as int?  ?? 22,
        'endMin':    result?['endMin']    as int?  ?? 0,
      };
    } catch (_) {
      return {'enabled': false, 'startHour': 9, 'startMin': 0, 'endHour': 22, 'endMin': 0};
    }
  }

  static Future<void> setSchedule({
    required bool enabled,
    required int startHour,
    required int startMin,
    required int endHour,
    required int endMin,
  }) async {
    try {
      await _channel.invokeMethod('setSchedule', {
        'enabled': enabled, 'startHour': startHour, 'startMin': startMin,
        'endHour': endHour, 'endMin': endMin,
      });
    } catch (_) {}
  }

  static Future<bool> isPinEnabled() async {
    try {
      return await _channel.invokeMethod<bool>('isPinEnabled') ?? false;
    } catch (_) {
      return false;
    }
  }

  static Future<void> setPin(String pin) async {
    try {
      await _channel.invokeMethod('setPin', {'pin': pin});
    } catch (_) {}
  }

  static Future<bool> verifyPin(String pin) async {
    try {
      return await _channel.invokeMethod<bool>('verifyPin', {'pin': pin}) ?? false;
    } catch (_) {
      return false;
    }
  }

  static Future<void> removePin() async {
    try {
      await _channel.invokeMethod('removePin');
    } catch (_) {}
  }

  static Future<void> openAccessibilitySettings() async {
    try {
      await _channel.invokeMethod('openAccessibilitySettings');
    } catch (_) {}
  }

  static Future<Map<String, dynamic>> getDailyLimit() async {
    try {
      final r = await _channel.invokeMethod<Map>('getDailyLimit');
      return {
        'enabled':      r?['enabled']      as bool? ?? false,
        'limitMinutes': r?['limitMinutes'] as int?  ?? 30,
        'usageSeconds': (r?['usageSeconds'] as num?)?.toInt() ?? 0,
      };
    } catch (_) {
      return {'enabled': false, 'limitMinutes': 30, 'usageSeconds': 0};
    }
  }

  static Future<void> setDailyLimit({required bool enabled, required int limitMinutes}) async {
    try {
      await _channel.invokeMethod('setDailyLimit', {'enabled': enabled, 'limitMinutes': limitMinutes});
    } catch (_) {}
  }

  static Future<int> getDailyUsage() async {
    try {
      return (await _channel.invokeMethod<num>('getDailyUsage'))?.toInt() ?? 0;
    } catch (_) {
      return 0;
    }
  }

  static Future<void> resetDailyUsage() async {
    try {
      await _channel.invokeMethod('resetDailyUsage');
    } catch (_) {}
  }
}
