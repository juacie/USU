package com.unitech.scanner.utility.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/2/5 上午 09:55
 * 修改人:user
 * 修改時間:2021/2/5 上午 09:55
 * 修改備註:
 */

public interface AllDefaultValue {
//     <!--keys_ScannerConfig-->
    String setting_Chinese_2_of_5= "0";
    String setting_Inverse_1D = "0";
//    <!--keys_UpcEanJan-->
    String setting_UPC_A = "1";
    String setting_UPC_E = "1";
    String setting_UPC_E1 = "0";
    String setting_EAN_8 = "1";
    String setting_EAN_13 = "1";
    String setting_Bookland_EAN = "0";
    String setting_Bookland_ISBN_Format ="0";
    String setting_ISSN_EAN = "0";
    String setting_Decode_UPC_EAN_JAN_Supplementals = "0";
    String setting_Transmit_UPC_A_Check_Digit = "1";
    String setting_Transmit_UPC_E_Check_Digit = "1";
    String setting_Transmit_UPC_E1_Check_Digit = "1";
    String setting_UPC_A_Preamble = "1";
    String setting_UPC_E_Preamble = "1";
    String setting_UPC_E1_Preamble = "1";
    String setting_Convert_UPC_E_to_A = "0";
    String setting_Convert_UPC_E1_to_A = "0";
    String setting_EAN_8_JAN_8_Extend = "0";
    String setting_UCC_Coupon_Extended_Code = "0";
//    <!--keys_Code128-->
    String setting_Code_128 = "1";
    String setting_Code_128_Length_Parameter209 = "0";
    String setting_Code_128_Length_Parameter210 = "0";
    String setting_GS_1_128 = "1";
    String setting_ISBT_128 = "1";
//    <!--keys_Code39-->
    String setting_Code_39 = "1";
    String setting_Trioptic_Code_39 = "0";
    String setting_Convert_Code_39_to_Code_32 = "0";
    String setting_Code_32_Prefix = "0";
    String setting_Code_39_Length_Parameter18 = "2";
    String setting_Code_39_Length_Parameter19 = "55";
    String setting_Code_39_Check_Digit_Verification = "0";
    String setting_Transmit_Code_39_Check_Digit = "0";
    String setting_Code_39_Full_ASCII_Conversion = "0";
//    <!--keys_Code93-->
    String setting_Code_93 = "1";
    String setting_Code_93_Length_Parameter26 = "4";
    String setting_Code_93_Length_Parameter27 = "55";
//    <!--keys_Code11-->
    String setting_Code_11 = "0";
    String setting_Code_11_Length_Parameter28 ="4";
    String setting_Code_11_Length_Parameter29 = "55";
    String setting_Code_11_Check_Digit_Verification = "0";
    String setting_Transmit_Code_11_Check_Digit = "0";
//    <!--keys_Interleaved_2_of_5-->
    String setting_Interleaved_2_of_5 = "0";
    String setting_Interleaved_2_of_5_Length_Parameter22 = "14";
    String setting_Interleaved_2_of_5_Length_Parameter23 = "0";
    String setting_Interleaved_2_of_5_Check_Digit_Verification = "0";
    String setting_Transmit_Interleaved_2_of_5_Check_Digit = "0";
    String setting_Convert_Interleaved_2_of_5_to_EAN_13 = "0";
//    <!--keys_Discrete_2_of_5-->
    String setting_Discrete_2_of_5 = "0";
    String setting_Discrete_2_of_5_Length_Parameter20 = "12";
    String setting_Discrete_2_of_5_Length_Parameter21 = "0";
//    <!--keys_Codabar-->
    String setting_Codabar = "1";
    String setting_Codabar_Length_Parameter24 = "5";
    String setting_Codabar_Length_Parameter25 = "55";
    String setting_CLSI_Editing = "0";
    String setting_NOTIS_Editing = "0";
//    <!--keys_MSI-->
    String setting_MSI = "0";
    String setting_MSI_Length_Parameter30 = "4";
    String setting_MSI_Length_Parameter31 = "55";
    String setting_MSI_Check_Digit = "0";
    String setting_Transmit_MSI_Check_Digit = "0";
    String setting_MSI_Check_Digit_Algorithm = "1";
//    <!--keys_Matrix_2_of_5-->
    String setting_Matrix_2_of_5 = "0";
    String setting_Matrix_2_of_5_Length_Parameter619 = "14";
    String setting_Matrix_2_of_5_Length_Parameter620 = "0";
    String setting_Matrix_2_of_5_Check_Digit = "0";
    String setting_Transmit_Matrix_2_of_5_Check_Digit = "0";
//    <!--keys_GS1_DataBar-->
    String setting_GS1_DataBar = "1";
    String setting_GS1_DataBar_Limited = "1";
    String setting_GS1_DataBar_Expanded = "1";
    String setting_Convert_GS1_DataBar_to_UPC_EAN = "0";
//    <!--keys_Composite-->
    String setting_Composite_CC_C = "0";
    String setting_Composite_CC_A_B = "0";
    String setting_Composite_TLC_39 = "0";
    String setting_UPC_Composite_Mode = "1";
//    <!--keys_TwoD-->
    String setting_PDF417 = "1";
    String setting_MicroPDF417 = "0";
    String setting_Code_128_Emulation = "0";
    String setting_Data_Matrix = "1";
    String setting_Data_Matrix_Inverse = "2";
    String setting_Maxicode = "0";
    String setting_QR_Code = "1";
    String setting_MicroQR = "1";
    String setting_Aztec = "1";
    String setting_Aztec_Inverse = "2";
    String setting_Han_Xin = "0";
    String setting_Han_Xin_Inverse = "0";
//    <!--keys_PostalCodes-->
    String setting_US_Postnet = "0";
    String setting_US_Planet = "0";
    String setting_Transmit_US_Postal_Check_Digit = "1";
    String setting_UK_Postal = "0";
    String setting_Transmit_UK_Postal_Check_Digit = "1";
    String setting_Japan_Postal = "0";
    String setting_Australian_Postal = "0";
    String setting_Netherlands_KIX_Code = "0";
    String setting_USPS_4CB_One_Code_Intelligent_Mail = "0";
    String setting_UPU_FICS_Postal = "0";
//    <!--keys_OtherSettings-->
    String setting_Picklist_Mode = "0";
    String setting_Trigger_Modes = "0";
    String setting_Decode_Session_Timeout = "49";
    String setting_Security_Level = "1";
    String setting_Decode_Aiming_Pattern = "2";
    String setting_Decoding_Illumination = "1";
    String setting_Exposure_Mode = "1";
    String setting_Fixed_Exposure_Time = "100";
    String setting_Low_Light_Motion_Detection_Assist = "0";
    String setting_Transmit_Code_ID_Character = "0";
    String setting_Transmit_No_Read_Message = "0";
    String setting_BT_signal_checking_level = "0";
    String setting_Data_terminator = "0";//*
    String setting_scanned_data_format = "1";//*
//    <!--AppSettings-->
    String setting_Password = "";//*
    String setting_BtAddress = "02:00:00:00:00:00";//*
    boolean setting_StartUp = false;
    String setting_LaunchApp = "";
    boolean setting_AutoEnforceSettings = false;
    boolean setting_FloatingButton = false;
    boolean setting_Scan2key = true;
    boolean setting_Clipboard = false;
    String setting_OutputMethod = "0";
    String setting_InterCharTime = "0";
    boolean setting_Sound = true;
    String setting_Frequency = "1";
    String setting_SoundDuration = "1";
    boolean setting_Vibration = true;
    boolean setting_DataAckWithIndicator = false;
    String setting_IndicatorLed = "none";
    String setting_IndicatorVibrator = "false";
    String setting_IndicatorBeep = "0";
    String setting_DataAction = "USU_INTENT_DATA_ACTION";//*
    String setting_StringData = "data";//*
    String setting_StringDataType = "datatype";//*
    String setting_StringDataLength = "datalength";//*
    String setting_StringDataByte = "databyte";//*
    Set <String> setting_EnableDataIntent = new HashSet <>(Arrays.asList("Type","Length"));
//    <!--keys_LabelFormatting-->
    String setting_Encoding = "0";
    boolean setting_RemoveNonPrintableChar = false;
    boolean setting_UseFormatting = false;
    String setting_Formatting = "{}";//*
    String setting_Prefix = "";
    String setting_Suffix = "";
    String setting_Terminator = "2";
    String setting_Replace = "";//*
}