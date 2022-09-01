package com.unitech.scanner.utility.config;

/**
 * 專案名稱:USU_Jack
 * 類描述:
 * 建立人:user
 * 建立時間:2020/11/2 下午 03:05
 * 修改人:user
 * 修改時間:2020/11/2 下午 03:05
 * 修改備註:
 */

public interface AllUsageAction {
    String apiGetPairingBarcode = "unitech.scanservice.bluetooth.get_pairing_barcode";//2.1
    String apiGetPairingBarcodeReply = "unitech.scanservice.bluetooth.get_pairing_barcode_reply";//2.1
    String apiGetTargetScannerStatus = "unitech.scanservice.bluetooth.get_target_scanner";//2.2
    String apiGetTargetScannerStatusReply = "unitech.scanservice.bluetooth.get_target_scanner_reply";//2.2
    String apiTargetScanner = "unitech.scanservice.bluetooth.target_scanner_callback";//2.3
    String apiUnpaired = "unitech.scanservice.bluetooth.unpair";//2.4
    String apiUnpairedReply = "unitech.scanservice.bluetooth.unpair_reply";//2.4
    String apiGetSN = "unitech.scanservice.bluetooth.get_sn";//2.5
    String apiGetSNReply = "unitech.scanservice.bluetooth.get_sn_reply";//2.5
    String apiGetName = "unitech.scanservice.bluetooth.get_name";//2.6
    String apiGetNameReply = "unitech.scanservice.bluetooth.get_name_reply";//2.6
    String apiGetAddress = "unitech.scanservice.bluetooth.get_address";//2.7
    String apiGetAddressReply = "unitech.scanservice.bluetooth.get_address_reply";//2.7
    String apiGetFW = "unitech.scanservice.bluetooth.get_fw";//2.8
    String apiGetFWReply = "unitech.scanservice.bluetooth.get_fw_reply";//2.8
    String apiGetBattery = "unitech.scanservice.bluetooth.get_battery";//2.9
    String apiGetBatteryReply = "unitech.scanservice.bluetooth.get_battery_reply";//2.9

    String apiGetTrigger = "unitech.scanservice.bluetooth.get_trig";//3.1
    String apiGetTriggerReply = "unitech.scanservice.bluetooth.get_trig_reply";//3.1
    String apiSetTrigger = "unitech.scanservice.bluetooth.set_trig";//3.1
    String apiSetTriggerReply = "unitech.scanservice.bluetooth.set_trig_reply";//3.1
    String apiStartDecode = "unitech.scanservice.bluetooth.start_decode";//3.2
    String apiStartDecodeReply = "unitech.scanservice.bluetooth.start_decode_reply";//3.2
    String apiStopDecode = "unitech.scanservice.bluetooth.stop_decode";//3.3
    String apiStopDecodeReply = "unitech.scanservice.bluetooth.stop_decode_reply";//3.3
    String apiGetAck = "unitech.scanservice.bluetooth.get_ack";//3.4
    String apiGetAckReply = "unitech.scanservice.bluetooth.get_ack_reply";//3.4
    String apiSetAck = "unitech.scanservice.bluetooth.set_ack";//3.4
    String apiSetAckReply = "unitech.scanservice.bluetooth.set_ack_reply";//3.4
    String apiGetAutoConnection = "unitech.scanservice.bluetooth.get_auto_conn";//3.5
    String apiGetAutoConnectionReply = "unitech.scanservice.bluetooth.get_auto_conn_reply";//3.5
    String apiSetAutoConnection = "unitech.scanservice.bluetooth.set_auto_conn";//3.5
    String apiSetAutoConnectionReply = "unitech.scanservice.bluetooth.set_auto_conn_reply";//3.5
    String apiGetConfig = "unitech.scanservice.bluetooth.get_config";//3.6
    String apiGetConfigReply = "unitech.scanservice.bluetooth.get_config_reply";//3.6
    String apiSetConfig = "unitech.scanservice.bluetooth.set_config";//3.6
    String apiSetConfigReply = "unitech.scanservice.bluetooth.set_config_reply";//3.6
    String apiGetBtSignalCheckingLevel = "unitech.scanservice.bluetooth.get_bt_signal_checking_level";//3.7
    String apiGetBtSignalCheckingLevelReply = "unitech.scanservice.bluetooth.get_bt_signal_checking_level_reply";//3.7
    String apiSetBtSignalCheckingLevel = "unitech.scanservice.bluetooth.set_bt_signal_checking_level";//3.7
    String apiSetBtSignalCheckingLevelReply = "unitech.scanservice.bluetooth.set_bt_signal_checking_level_reply";//3.7
    String apiGetDataTerminator = "unitech.scanservice.bluetooth.get_data_terminator";//3.8
    String apiGetDataTerminatorReply = "unitech.scanservice.bluetooth.get_data_terminator_reply";//3.8
    String apiSetDataTerminator = "unitech.scanservice.bluetooth.set_data_terminator";//3.8
    String apiSetDataTerminatorReply = "unitech.scanservice.bluetooth.set_data_terminator_reply";//3.8

    String apiChangeToFormatSsi = "com.unitech.bluetooth.changeToSSI";//4.1
    String apiChangeToFormatSsiReply = "com.unitech.bluetooth.changeToSSI_reply";//4.1
    String apiChangeToFormatRaw = "com.unitech.bluetooth.changeToRAW";//4.1
    String apiChangeToFormatRawReply = "com.unitech.bluetooth.changeToRAW_reply";//4.1
    String apiGetFormat = "com.unitech.bluetooth.getFormat";//4.1
    String apiGetFormatReply = "com.unitech.bluetooth.getFormat_reply";//4.1
    String apiDataCodeType = "com.unitech.bluetooth.dataCodeType";//4.1
    String apiData = "unitech.scanservice.data";//4.1
    String apiDataLength = "unitech.scanservice.datalength";//4.1
    String apiDataByte = "unitech.scanservice.databyte";//4.1
    String apiDataByteLength = "unitech.scanservice.databytelength";//4.1
    String apiDataType = "unitech.scanservice.datatype";//4.1
    String apiDataAll = "unitech.scanservice.dataall";//4.1
    String apiSetIndicator = "unitech.scanservice.bluetooth.set_indicator";//4.2
    String apiSetIndicatorReply = "unitech.scanservice.bluetooth.set_indicator_reply";//4.2

    String apiExportSettings = "unitech.scanservice.bluetooth.export_settings";//5.1
    String apiExportSettingsReply = "unitech.scanservice.bluetooth.export_settings_reply";//5.1
    String apiImportSettings = "unitech.scanservice.bluetooth.import_settings";//5.2
    String apiImportSettingsReply = "unitech.scanservice.bluetooth.import_settings_reply";//5.2
    String apiUploadSettings = "unitech.scanservice.bluetooth.upload_all_settings";//5.3
    String apiUploadSettingsReply = "unitech.scanservice.bluetooth.upload_all_settings_reply";//5.3
    //========================================1.3 Add ==============================================
    String apiSetIntentAction = "unitech.scanservice.bluetooth.set_data_action";//4.1 modify
    String apiSetIntentActionReply = "unitech.scanservice.bluetooth.set_data_action_reply";//4.1 modify
    String apiGetIntentAction = "unitech.scanservice.bluetooth.get_data_action";//4.1 modify
    String apiGetIntentActionReply = "unitech.scanservice.bluetooth.get_data_action_reply";//4.1 modify
    String apiResetSettings =  "unitech.scanservice.bluetooth.reset_settings";//5.4
    String apiResetSettingsReply =  "unitech.scanservice.bluetooth.reset_settings_reply";//5.4
    String apiSetScan2key = "unitech.scanservice.bluetooth.set_scan2key";//5.5
    String apiSetScan2keyReply = "unitech.scanservice.bluetooth.set_scan2key_reply";//5.5
    String apiGetScan2key = "unitech.scanservice.bluetooth.get_scan2key";//5.5
    String apiGetScan2keyReply = "unitech.scanservice.bluetooth.get_scan2key_reply";//5.5
    String apiSetOutput = "unitech.scanservice.bluetooth.set_output";//5.6
    String apiSetOutputReply = "unitech.scanservice.bluetooth.set_output_reply";//5.6
    String apiGetOutput = "unitech.scanservice.bluetooth.get_output";//5.6
    String apiGetOutputReply = "unitech.scanservice.bluetooth.get_output_reply";//5.6
    String apiSetEncoding = "unitech.scanservice.bluetooth.set_encoding";//5.7
    String apiSetEncodingReply = "unitech.scanservice.bluetooth.set_encoding_reply";//5.7
    String apiGetEncoding = "unitech.scanservice.bluetooth.get_encoding";//5.7
    String apiGetEncodingReply = "unitech.scanservice.bluetooth.get_encoding_reply";//5.7
    String apiSetFormattingStatus = "unitech.scanservice.bluetooth.set_formatting_status";//5.8
    String apiSetFormattingStatusReply = "unitech.scanservice.bluetooth.set_formatting_status_reply";//5.8
    String apiGetFormattingStatus = "unitech.scanservice.bluetooth.get_formatting_status";//5.8
    String apiGetFormattingStatusReply = "unitech.scanservice.bluetooth.get_formatting_status_reply";//5.8
    String apiSetAppend = "unitech.scanservice.bluetooth.set_append";//5.9
    String apiSetAppendReply = "unitech.scanservice.bluetooth.set_append_reply";//5.9
    String apiGetAppend = "unitech.scanservice.bluetooth.get_append";//5.9
    String apiGetAppendReply = "unitech.scanservice.bluetooth.get_append_reply";//5.9
    String apiSetReplace = "unitech.scanservice.bluetooth.set_replace";//5.10
    String apiSetReplaceReply = "unitech.scanservice.bluetooth.set_replace_reply";//5.10
    String apiGetReplace = "unitech.scanservice.bluetooth.get_replace";//5.10
    String apiGetReplaceReply = "unitech.scanservice.bluetooth.get_replace_reply";//5.10
    String apiSetRemoveNonPrintableChar = "unitech.scanservice.bluetooth.set_remove_non_printable_char";//5.11
    String apiSetRemoveNonPrintableCharReply = "unitech.scanservice.bluetooth.set_remove_non_printable_char_reply";//5.11
    String apiGetRemoveNonPrintableChar = "unitech.scanservice.bluetooth.get_remove_non_printable_char";//5.11
    String apiGetRemoveNonPrintableCharReply = "unitech.scanservice.bluetooth.get_remove_non_printable_char_reply";//5.11
    String apiSetFormatting = "unitech.scanservice.bluetooth.set_formatting";//5.12
    String apiSetFormattingReply = "unitech.scanservice.bluetooth.set_formatting_reply";//5.12
    String apiGetFormatting = "unitech.scanservice.bluetooth.get_formatting";//5.12
    String apiGetFormattingReply = "unitech.scanservice.bluetooth.get_formatting_reply";//5.12
    String apiDeleteFormatting = "unitech.scanservice.bluetooth.delete_formatting";//5.12
    String apiDeleteFormattingReply = "unitech.scanservice.bluetooth.delete_formatting_reply";//5.12
    String apiSetFloatingButton = "unitech.scanservice.bluetooth.set_floating_button";//5.13
    String apiSetFloatingButtonReply = "unitech.scanservice.bluetooth.set_floating_button_reply";//5.13
    String apiGetFloatingButton = "unitech.scanservice.bluetooth.get_floating_button";//5.13
    String apiGetFloatingButtonReply = "unitech.scanservice.bluetooth.get_floating_button_reply";//5.13
    String apiSetSound = "unitech.scanservice.bluetooth.set_sound";//5.14
    String apiSetSoundReply = "unitech.scanservice.bluetooth.set_sound_reply";//5.14
    String apiGetSound = "unitech.scanservice.bluetooth.get_sound";//5.14
    String apiGetSoundReply = "unitech.scanservice.bluetooth.get_sound_reply";//5.14
    String apiSetVibration = "unitech.scanservice.bluetooth.set_vibration";//5.15
    String apiSetVibrationReply = "unitech.scanservice.bluetooth.set_vibration_reply";//5.15
    String apiGetVibration = "unitech.scanservice.bluetooth.get_vibration";//5.15
    String apiGetVibrationReply = "unitech.scanservice.bluetooth.get_vibration_reply";//5.15
    String apiSetStartup = "unitech.scanservice.bluetooth.set_startup";//5.16
    String apiSetStartupReply = "unitech.scanservice.bluetooth.set_startup_reply";//5.16
    String apiGetStartup = "unitech.scanservice.bluetooth.get_startup";//5.16
    String apiGetStartupReply = "unitech.scanservice.bluetooth.get_startup_reply";//5.16
    String apiSetAutoEnforceSettings = "unitech.scanservice.bluetooth.set_auto_enforce_settings";//5.17*
    String apiSetAutoEnforceSettingsReply = "unitech.scanservice.bluetooth.set_auto_enforce_settings_reply";//5.17*
    String apiGetAutoEnforceSettings = "unitech.scanservice.bluetooth.get_auto_enforce_settings";//5.17*
    String apiGetAutoEnforceSettingsReply = "unitech.scanservice.bluetooth.get_auto_enforce_settings_reply";//5.17*

    String floatingServiceStart = "unitech.floatingservice.start";
    //===============================================================================================
    String usuExit = "unitech.scanservice.bluetooth.exit";

    String systemScan2key = "unitech.scanservice.scan2key_setting";
    String serviceExit = "com.unitech.bluetooth.exit";
    String serviceSetScanner = "unitech.scanservice.bluetooth.set_scanner";
    String serviceGetScanner = "unitech.scanservice.bluetooth.get_scanners";
    String serviceGetScannerReply = "unitech.scanservice.bluetooth.get_scanners_reply";
    String serviceKeyboardInput = "unitech.scanservice.keyboard_input";
    String serviceSendAck = "com.unitech.bluetooth.send_ack";
    String serviceImeRespond = "unitech.scanservice.keyboard_respond";
    String serviceImeStatus = "unitech.scanservice.keyboard_status";
    String utcRequestApiData = "com.zlsoft.mobile.bar";

}
