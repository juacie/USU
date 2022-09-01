package com.unitech.scanner.utility.service.mainUsage;

import android.content.Context;

import com.unitech.scanner.utility.R;
import com.unitech.scanner.utility.config.SupportScanner;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2021/1/5 下午 04:13
 * 修改人:user
 * 修改時間:2021/1/5 下午 04:13
 * 修改備註:
 */

public class PrefCmdTrans {
    private final Context context;
    private final int scanner;

    public PrefCmdTrans(Context context, int scanner) {
        this.context = context;
        this.scanner = scanner;
    }

    public String getCmd(String pref) {
        String moduleCmd = null;

        switch (scanner) {
            case SupportScanner.ms652Plus:
                moduleCmd = getZebraCommand(pref);
                break;
        }
        return moduleCmd;
    }

    private String getZebraCommand(String pref) {
        String cmd = null;
        if (pref.equals(context.getString(R.string.setting_Japan_Postal))) {
            cmd = "Japan_Postal";
        } else if (pref.equals(context.getString(R.string.setting_Australian_Postal))) {
            cmd = "Australia_Post";
        } else if (pref.equals(context.getString(R.string.setting_Data_Matrix))) {
            cmd = "Data_Matrix";
        } else if (pref.equals(context.getString(R.string.setting_QR_Code))) {
            cmd = "QR_Code";
        } else if (pref.equals(context.getString(R.string.setting_Maxicode))) {
            cmd = "Maxicode";
        } else if (pref.equals(context.getString(R.string.setting_Netherlands_KIX_Code))) {
            cmd = "Netherlands_KIX_Code";
        } else if (pref.equals(context.getString(R.string.setting_GS1_DataBar))) {
            cmd = "GS1_DataBar_14";
        } else if (pref.equals(context.getString(R.string.setting_GS1_DataBar_Limited))) {
            cmd = "GS1_DataBar_Limited";
        } else if (pref.equals(context.getString(R.string.setting_GS1_DataBar_Expanded))) {
            cmd = "GS1_DataBar_Expanded";
        } else if (pref.equals(context.getString(R.string.setting_Composite_CC_C))) {
            cmd = "Composite_CC_C";
        } else if (pref.equals(context.getString(R.string.setting_Composite_CC_A_B))) {
            cmd = "Composite_CC_A_B";
        } else if (pref.equals(context.getString(R.string.setting_UPC_Composite_Mode))) {
            cmd = "UPC_Composite_Mode";
        } else if (pref.equals(context.getString(R.string.setting_Composite_TLC_39))) {
            cmd = "Composite_TLC_39";
        } else if (pref.equals(context.getString(R.string.setting_Convert_GS1_DataBar_to_UPC_EAN))) {
            cmd = "Convert_GS1_DataBar_To_UPC_EAN_JAN";
        } else if (pref.equals(context.getString(R.string.setting_Picklist_Mode))) {
            cmd = "Picklist_Mode";
        } else if (pref.equals(context.getString(R.string.setting_Chinese_2_of_5))) {
            cmd = "Chinese_2_of_5";
        }
        //---------------------
        else if (pref.equals(context.getString(R.string.setting_MicroQR))) {
            cmd = "MicroQR";
        } else if (pref.equals(context.getString(R.string.setting_Aztec))) {
            cmd = "Aztec";
        } else if (pref.equals(context.getString(R.string.setting_Bookland_ISBN_Format))) {
            cmd = "Bookland_ISBN_Format";
        } else if (pref.equals(context.getString(R.string.setting_Inverse_1D))) {
            cmd = "Inverse_1D";
        } else if (pref.equals(context.getString(R.string.setting_Data_Matrix_Inverse))) {
            cmd = "Data_Matrix_Inverse";
        } else if (pref.equals(context.getString(R.string.setting_Aztec_Inverse))) {
            cmd = "Aztec_Inverse";
        } else if (pref.equals(context.getString(R.string.setting_USPS_4CB_One_Code_Intelligent_Mail))) {
            cmd = "USPS_4CB_One_Code_Intelligent_Mail";
        } else if (pref.equals(context.getString(R.string.setting_UPU_FICS_Postal))) {
            cmd = "UPU_FICS_Postal";
        } else if (pref.equals(context.getString(R.string.setting_ISSN_EAN))) {
            cmd = "ISSN_EAN";
        } else if (pref.equals(context.getString(R.string.setting_Matrix_2_of_5))) {
            cmd = "Matrix_2_of_5";
        } else if (pref.equals(context.getString(R.string.setting_Matrix_2_of_5_Length_Parameter619))) {
            cmd = "Matrix_2_of_5_Set_Length1";
        } else if (pref.equals(context.getString(R.string.setting_Matrix_2_of_5_Length_Parameter620))) {
            cmd = "Matrix_2_of_5_Set_Length2";
        }  else if (pref.equals(context.getString(R.string.setting_Matrix_2_of_5_Check_Digit))) {
            cmd = "Matrix_2_of_5_Check_Digit";
        } else if (pref.equals(context.getString(R.string.setting_Transmit_Matrix_2_of_5_Check_Digit))) {
            cmd = "Transmit_Matrix_2_of_5_Check_Digit";
        }
        //---------------------
        else if (pref.equals(context.getString(R.string.setting_Low_Light_Motion_Detection_Assist))) {
            cmd = "Low_Light_Scene_Detection";
        }
        //---------------------
        else if (pref.equals(context.getString(R.string.setting_Han_Xin))) {
            cmd = "Han_Xin";
        } else if (pref.equals(context.getString(R.string.setting_Han_Xin_Inverse))) {
            cmd = "Han_Xin_Inverse";
        }
        //---------------------
        else if (pref.equals(context.getString(R.string.setting_Code_39))) {
            cmd = "Code39";
        } else if (pref.equals(context.getString(R.string.setting_UPC_A))) {
            cmd = "UPC_A";
        } else if (pref.equals(context.getString(R.string.setting_UPC_E))) {
            cmd = "UPC_E";
        } else if (pref.equals(context.getString(R.string.setting_EAN_13))) {
            cmd = "EAN_13_JAN_13";
        } else if (pref.equals(context.getString(R.string.setting_EAN_8))) {
            cmd = "EAN_8_JAN_8";
        } else if (pref.equals(context.getString(R.string.setting_Discrete_2_of_5))) {
            cmd = "Discrete_2_of_5";
        } else if (pref.equals(context.getString(R.string.setting_Interleaved_2_of_5))) {
            cmd = "Interleaved_2_of_5";
        } else if (pref.equals(context.getString(R.string.setting_Codabar))) {
            cmd = "Codabar";
        } else if (pref.equals(context.getString(R.string.setting_Code_128))) {
            cmd = "Code128";
        } else if (pref.equals(context.getString(R.string.setting_Code_93))) {
            cmd = "Code93";
        } else if (pref.equals(context.getString(R.string.setting_Code_11))) {
            cmd = "Code11";
        } else if (pref.equals(context.getString(R.string.setting_MSI))) {
            cmd = "MSI";
        } else if (pref.equals(context.getString(R.string.setting_UPC_E1))) {
            cmd = "UPC_E1";
        } else if (pref.equals(context.getString(R.string.setting_Trioptic_Code_39))) {
            cmd = "Trioptic_Code39";
        } else if (pref.equals(context.getString(R.string.setting_GS_1_128))) {
            cmd = "GS1_128";
        } else if (pref.equals(context.getString(R.string.setting_PDF417))) {
            cmd = "PDF417";
        } else if (pref.equals(context.getString(R.string.setting_Decode_UPC_EAN_JAN_Supplementals))) {
            cmd = "Decode_UPC_EAN_JAN_Supplementals";
        } else if (pref.equals(context.getString(R.string.setting_Code_39_Full_ASCII_Conversion))) {
            cmd = "Code39_Full_ASCII_Conversion";
        } else if (pref.equals(context.getString(R.string.setting_Code_39_Length_Parameter18))) {
            cmd = "Code39_Set_Length1";
        } else if (pref.equals(context.getString(R.string.setting_Code_39_Length_Parameter19))) {
            cmd = "Code39_Set_Length2";
        } else if (pref.equals(context.getString(R.string.setting_Discrete_2_of_5_Length_Parameter20))) {
            cmd = "Discrete_2_of_5_Set_Length1";
        } else if (pref.equals(context.getString(R.string.setting_Discrete_2_of_5_Length_Parameter21))) {
            cmd = "Discrete_2_of_5_Set_Length2";
        } else if (pref.equals(context.getString(R.string.setting_Interleaved_2_of_5_Length_Parameter22))) {
            cmd = "Interleaved_2_of_5_Set_Length1";
        } else if (pref.equals(context.getString(R.string.setting_Interleaved_2_of_5_Length_Parameter23))) {
            cmd = "Interleaved_2_of_5_Set_Length2";
        } else if (pref.equals(context.getString(R.string.setting_Codabar_Length_Parameter24))) {
            cmd = "Codabar_Set_Length1";
        } else if (pref.equals(context.getString(R.string.setting_Codabar_Length_Parameter25))) {
            cmd = "Codabar_Set_Length2";
        } else if (pref.equals(context.getString(R.string.setting_Code_93_Length_Parameter26))) {
            cmd = "Code93_Set_Length1";
        } else if (pref.equals(context.getString(R.string.setting_Code_93_Length_Parameter27))) {
            cmd = "Code93_Set_Length2";
        } else if (pref.equals(context.getString(R.string.setting_Code_11_Length_Parameter28))) {
            cmd = "Code11_Set_Length1";
        } else if (pref.equals(context.getString(R.string.setting_Code_11_Length_Parameter29))) {
            cmd = "Code11_Set_Length2";
        } else if (pref.equals(context.getString(R.string.setting_MSI_Length_Parameter30))) {
            cmd = "MSI_Set_Length1";
        } else if (pref.equals(context.getString(R.string.setting_MSI_Length_Parameter31))) {
            cmd = "MSI_Set_Length2";
        } else if (pref.equals(context.getString(R.string.setting_UPC_A_Preamble))) {
            cmd = "UPC_A_Preamble";
        } else if (pref.equals(context.getString(R.string.setting_UPC_E_Preamble))) {
            cmd = "UPC_E_Preamble";
        } else if (pref.equals(context.getString(R.string.setting_UPC_E1_Preamble))) {
            cmd = "UPC_E1_Preamble";
        } else if (pref.equals(context.getString(R.string.setting_Convert_UPC_E_to_A))) {
            cmd = "Convert_UPC_E_to_A";
        } else if (pref.equals(context.getString(R.string.setting_Convert_UPC_E1_to_A))) {
            cmd = "Convert_UPC_E1_to_A";
        } else if (pref.equals(context.getString(R.string.setting_EAN_8_JAN_8_Extend))) {
            cmd = "EAN_8_JAN_8_Extend";
        } else if (pref.equals(context.getString(R.string.setting_Transmit_UPC_A_Check_Digit))) {
            cmd = "Transmit_UPC_A_Check_Digit";
        } else if (pref.equals(context.getString(R.string.setting_Transmit_UPC_E_Check_Digit))) {
            cmd = "Transmit_UPC_E_Check_Digit";
        } else if (pref.equals(context.getString(R.string.setting_Transmit_UPC_E1_Check_Digit))) {
            cmd = "Transmit_UPC_E1_Check_Digit";
        } else if (pref.equals(context.getString(R.string.setting_Transmit_Code_39_Check_Digit))) {
            cmd = "Transmit_Code39_Check_Digit";
        } else if (pref.equals(context.getString(R.string.setting_Transmit_Interleaved_2_of_5_Check_Digit))) {
            cmd = "Transmit_Interleaved_2_of_5_Check_Digit";
        } else if (pref.equals(context.getString(R.string.setting_Transmit_Code_ID_Character))) {
            cmd = "Transmit_Code_ID_Character";
        } else if (pref.equals(context.getString(R.string.setting_Transmit_MSI_Check_Digit))) {
            cmd = "Transmit_MSI_Check_Digit";
        } else if (pref.equals(context.getString(R.string.setting_Transmit_Code_11_Check_Digit))) {
            cmd = "Transmit_Code11_Check_Digit";
        } else if (pref.equals(context.getString(R.string.setting_Code_39_Check_Digit_Verification))) {
            cmd = "Code39_Check_Digit_Verification";
        } else if (pref.equals(context.getString(R.string.setting_Interleaved_2_of_5_Check_Digit_Verification))) {
            cmd = "Interleaved_2_of_5_Check_Digit_Verification";
        } else if (pref.equals(context.getString(R.string.setting_MSI_Check_Digit))) {
            cmd = "MSI_Check_Digits";
        } else if (pref.equals(context.getString(R.string.setting_MSI_Check_Digit_Algorithm))) {
            cmd = "MSI_Check_Digit_Algorithm";
        } else if (pref.equals(context.getString(R.string.setting_Code_11_Check_Digit_Verification))) {
            cmd = "Code11_Check_Digit_Verfication";
        } else if (pref.equals(context.getString(R.string.setting_CLSI_Editing))) {
            cmd = "CLSI_Editing";
        } else if (pref.equals(context.getString(R.string.setting_NOTIS_Editing))) {
            cmd = "NOTIS_Editing";
        } else if (pref.equals(context.getString(R.string.setting_Convert_Interleaved_2_of_5_to_EAN_13))) {
            cmd = "Convert_Interleaved_2_of_5_to_EAN_13";
        } else if (pref.equals(context.getString(R.string.setting_Bookland_EAN))) {
            cmd = "Bookland_EAN";
        } else if (pref.equals(context.getString(R.string.setting_ISBT_128))) {
            cmd = "ISBT_128";
        } else if (pref.equals(context.getString(R.string.setting_UCC_Coupon_Extended_Code))) {
            cmd = "UCC_Coupon_Extended_Code";
        } else if (pref.equals(context.getString(R.string.setting_Convert_Code_39_to_Code_32))) {
            cmd = "Convert_Code39_to_Code32";
        } else if (pref.equals(context.getString(R.string.setting_US_Postnet))) {
            cmd = "US_Postnet";
        } else if (pref.equals(context.getString(R.string.setting_US_Planet))) {
            cmd = "US_Planet";
        } else if (pref.equals(context.getString(R.string.setting_UK_Postal))) {
            cmd = "UK_Postal";
        } else if (pref.equals(context.getString(R.string.setting_Transmit_US_Postal_Check_Digit))) {
            cmd = "Transmit_US_Postal_CheckDigit";
        } else if (pref.equals(context.getString(R.string.setting_Transmit_UK_Postal_Check_Digit))) {
            cmd = "Transmit_UK_Postal_Check_Digit";
        } else if (pref.equals(context.getString(R.string.setting_Code_128_Emulation))) {
            cmd = "Code128_Emulation";
        } else if (pref.equals(context.getString(R.string.setting_Decode_Session_Timeout))) {
            cmd = "Decode_Session_Timeout";
        } else if (pref.equals(context.getString(R.string.setting_Trigger_Modes))) {
            cmd = "Trigger_Mode";
        } else if (pref.equals(context.getString(R.string.setting_Code_128_Length_Parameter209))) {
            cmd = "Code128_Set_Length1";
        } else if (pref.equals(context.getString(R.string.setting_Code_128_Length_Parameter210))) {
            cmd = "Code128_Set_Length2";
        } else if (pref.equals(context.getString(R.string.setting_MicroPDF417))) {
            cmd = "MicroPDF417";
        } else if (pref.equals(context.getString(R.string.setting_Code_32_Prefix))) {
            cmd = "Code32_Prefix";
        } else if(pref.equals(context.getString(R.string.setting_Transmit_No_Read_Message))){
            cmd = "Transmit_No_Read_Message";
        }else if(pref.equals(context.getString(R.string.setting_Security_Level))){
            cmd = "Security_Level";
        }else if(pref.equals(context.getString(R.string.setting_Exposure_Mode))){
            cmd = "Decoding_Autoexposure";
        }else if(pref.equals(context.getString(R.string.setting_Fixed_Exposure_Time))){
            cmd = "Exposure_Time";
        }else if(pref.equals(context.getString(R.string.setting_Decoding_Illumination))){
            cmd = "Decoding_Illumination";
        }else if(pref.equals(context.getString(R.string.setting_Decode_Aiming_Pattern))){
            cmd = "Decode_Aiming_Pattern";
        }
        return cmd;
    }

    public String getPref(String cmd) {
        String pref = null;

        switch (scanner) {
            case SupportScanner.ms652Plus:
                pref = getMS652pPref(cmd);
                break;
        }
        return pref;
    }

    private String getMS652pPref(String cmd) {
        String pref = null;
        switch (cmd) {
            case "Japan_Postal":
                pref = context.getString(R.string.setting_Japan_Postal);
                break;
            case "Australia_Post":
                pref = context.getString(R.string.setting_Australian_Postal);
                break;
            case "Data_Matrix":
                pref = context.getString(R.string.setting_Data_Matrix);
                break;
            case "QR_Code":
                pref = context.getString(R.string.setting_QR_Code);
                break;
            case "Maxicode":
                pref = context.getString(R.string.setting_Maxicode);
                break;
            case "Netherlands_KIX_Code":
                pref = context.getString(R.string.setting_Netherlands_KIX_Code);
                break;
            case "GS1_DataBar_14":
                pref = context.getString(R.string.setting_GS1_DataBar);
                break;
            case "GS1_DataBar_Limited":
                pref = context.getString(R.string.setting_GS1_DataBar_Limited);
                break;
            case "GS1_DataBar_Expanded":
                pref = context.getString(R.string.setting_GS1_DataBar_Expanded);
                break;
            case "Composite_CC_C":
                pref = context.getString(R.string.setting_Composite_CC_C);
                break;
            case "Composite_CC_A_B":
                pref = context.getString(R.string.setting_Composite_CC_A_B);
                break;
            case "UPC_Composite_Mode":
                pref = context.getString(R.string.setting_UPC_Composite_Mode);
                break;
            case "Composite_TLC_39":
                pref = context.getString(R.string.setting_Composite_TLC_39);
                break;
            case "Convert_GS1_DataBar_To_UPC_EAN_JAN":
                pref = context.getString(R.string.setting_Convert_GS1_DataBar_to_UPC_EAN);
                break;
            case "Picklist_Mode":
                pref = context.getString(R.string.setting_Picklist_Mode);
                break;
            case "Chinese_2_of_5":
                pref = context.getString(R.string.setting_Chinese_2_of_5);
                break;
            case "MicroQR":
                pref = context.getString(R.string.setting_MicroQR);
                break;
            case "Aztec":
                pref = context.getString(R.string.setting_Aztec);
                break;
            case "Bookland_ISBN_Format":
                pref = context.getString(R.string.setting_Bookland_ISBN_Format);
                break;
            case "Inverse_1D":
                pref = context.getString(R.string.setting_Inverse_1D);
                break;
            case "Data_Matrix_Inverse":
                pref = context.getString(R.string.setting_Data_Matrix_Inverse);
                break;
            case "Aztec_Inverse":
                pref = context.getString(R.string.setting_Aztec_Inverse);
                break;
            case "USPS_4CB_One_Code_Intelligent_Mail":
                pref = context.getString(R.string.setting_USPS_4CB_One_Code_Intelligent_Mail);
                break;
            case "UPU_FICS_Postal":
                pref = context.getString(R.string.setting_UPU_FICS_Postal);
                break;
            case "ISSN_EAN":
                pref = context.getString(R.string.setting_ISSN_EAN);
                break;
            case "Matrix_2_of_5":
                pref = context.getString(R.string.setting_Matrix_2_of_5);
                break;
            case "Matrix_2_of_5_Set_Length1":
                pref = context.getString(R.string.setting_Matrix_2_of_5_Length_Parameter619);
                break;
            case "Matrix_2_of_5_Set_Length2":
                pref = context.getString(R.string.setting_Matrix_2_of_5_Length_Parameter620);
                break;
            case "Matrix_2_of_5_Check_Digit":
                pref = context.getString(R.string.setting_Matrix_2_of_5_Check_Digit);
                break;
            case "Transmit_Matrix_2_of_5_Check_Digit":
                pref = context.getString(R.string.setting_Transmit_Matrix_2_of_5_Check_Digit);
                break;
            case "Low_Light_Scene_Detection":
                pref = context.getString(R.string.setting_Low_Light_Motion_Detection_Assist);
                break;
            case "Han_Xin":
                pref = context.getString(R.string.setting_Han_Xin);
                break;
            case "Han_Xin_Inverse":
                pref = context.getString(R.string.setting_Han_Xin_Inverse);
                break;
            case "Code39":
                pref = context.getString(R.string.setting_Code_39);
                break;
            case "UPC_A":
                pref = context.getString(R.string.setting_UPC_A);
                break;
            case "UPC_E":
                pref = context.getString(R.string.setting_UPC_E);
                break;
            case "EAN_13_JAN_13":
                pref = context.getString(R.string.setting_EAN_13);
                break;
            case "EAN_8_JAN_8":
                pref = context.getString(R.string.setting_EAN_8);
                break;
            case "Discrete_2_of_5":
                pref = context.getString(R.string.setting_Discrete_2_of_5);
                break;
            case "Interleaved_2_of_5":
                pref = context.getString(R.string.setting_Interleaved_2_of_5);
                break;
            case "Codabar":
                pref = context.getString(R.string.setting_Codabar);
                break;
            case "Code128":
                pref = context.getString(R.string.setting_Code_128);
                break;
            case "Code93":
                pref = context.getString(R.string.setting_Code_93);
                break;
            case "Code11":
                pref = context.getString(R.string.setting_Code_11);
                break;
            case "MSI":
                pref = context.getString(R.string.setting_MSI);
                break;
            case "UPC_E1":
                pref = context.getString(R.string.setting_UPC_E1);
                break;
            case "Trioptic_Code39":
                pref = context.getString(R.string.setting_Trioptic_Code_39);
                break;
            case "GS1_128":
                pref = context.getString(R.string.setting_GS_1_128);
                break;
            case "PDF417":
                pref = context.getString(R.string.setting_PDF417);
                break;
            case "Decode_UPC_EAN_JAN_Supplementals":
                pref = context.getString(R.string.setting_Decode_UPC_EAN_JAN_Supplementals);
                break;
            case "Code39_Full_ASCII_Conversion":
                pref = context.getString(R.string.setting_Code_39_Full_ASCII_Conversion);
                break;
            case "Code39_Set_Length1":
                pref = context.getString(R.string.setting_Code_39_Length_Parameter18);
                break;
            case "Code39_Set_Length2":
                pref = context.getString(R.string.setting_Code_39_Length_Parameter19);
                break;
            case "Discrete_2_of_5_Set_Length1":
                pref = context.getString(R.string.setting_Discrete_2_of_5_Length_Parameter20);
                break;
            case "Discrete_2_of_5_Set_Length2":
                pref = context.getString(R.string.setting_Discrete_2_of_5_Length_Parameter21);
                break;
            case "Interleaved_2_of_5_Set_Length1":
                pref = context.getString(R.string.setting_Interleaved_2_of_5_Length_Parameter22);
                break;
            case "Interleaved_2_of_5_Set_Length2":
                pref = context.getString(R.string.setting_Interleaved_2_of_5_Length_Parameter23);
                break;
            case "Codabar_Set_Length1":
                pref = context.getString(R.string.setting_Codabar_Length_Parameter24);
                break;
            case "Codabar_Set_Length2":
                pref = context.getString(R.string.setting_Codabar_Length_Parameter25);
                break;
            case "Code93_Set_Length1":
                pref = context.getString(R.string.setting_Code_93_Length_Parameter26);
                break;
            case "Code93_Set_Length2":
                pref = context.getString(R.string.setting_Code_93_Length_Parameter27);
                break;
            case "Code11_Set_Length1":
                pref = context.getString(R.string.setting_Code_11_Length_Parameter28);
                break;
            case "Code11_Set_Length2":
                pref = context.getString(R.string.setting_Code_11_Length_Parameter29);
                break;
            case "MSI_Set_Length1":
                pref = context.getString(R.string.setting_MSI_Length_Parameter30);
                break;
            case "MSI_Set_Length2":
                pref = context.getString(R.string.setting_MSI_Length_Parameter31);
                break;
            case "UPC_A_Preamble":
                pref = context.getString(R.string.setting_UPC_A_Preamble);
                break;
            case "UPC_E_Preamble":
                pref = context.getString(R.string.setting_UPC_E_Preamble);
                break;
            case "UPC_E1_Preamble":
                pref = context.getString(R.string.setting_UPC_E1_Preamble);
                break;
            case "Convert_UPC_E_to_A":
                pref = context.getString(R.string.setting_Convert_UPC_E_to_A);
                break;
            case "Convert_UPC_E1_to_A":
                pref = context.getString(R.string.setting_Convert_UPC_E1_to_A);
                break;
            case "EAN_8_JAN_8_Extend":
                pref = context.getString(R.string.setting_EAN_8_JAN_8_Extend);
                break;
            case "Transmit_UPC_A_Check_Digit":
                pref = context.getString(R.string.setting_Transmit_UPC_A_Check_Digit);
                break;
            case "Transmit_UPC_E_Check_Digit":
                pref = context.getString(R.string.setting_Transmit_UPC_E_Check_Digit);
                break;
            case "Transmit_UPC_E1_Check_Digit":
                pref = context.getString(R.string.setting_Transmit_UPC_E1_Check_Digit);
                break;
            case "Transmit_Code39_Check_Digit":
                pref = context.getString(R.string.setting_Transmit_Code_39_Check_Digit);
                break;
            case "Transmit_Interleaved_2_of_5_Check_Digit":
                pref = context.getString(R.string.setting_Transmit_Interleaved_2_of_5_Check_Digit);
                break;
            case "Transmit_Code_ID_Character":
                pref = context.getString(R.string.setting_Transmit_Code_ID_Character);
                break;
            case "Transmit_MSI_Check_Digit":
                pref = context.getString(R.string.setting_Transmit_MSI_Check_Digit);
                break;
            case "Transmit_Code11_Check_Digit":
                pref = context.getString(R.string.setting_Transmit_Code_11_Check_Digit);
                break;
            case "Code39_Check_Digit_Verification":
                pref = context.getString(R.string.setting_Code_39_Check_Digit_Verification);
                break;
            case "Interleaved_2_of_5_Check_Digit_Verification":
                pref = context.getString(R.string.setting_Interleaved_2_of_5_Check_Digit_Verification);
                break;
            case "MSI_Check_Digits":
                pref = context.getString(R.string.setting_MSI_Check_Digit);
                break;
            case "MSI_Check_Digit_Algorithm":
                pref = context.getString(R.string.setting_MSI_Check_Digit_Algorithm);
                break;
            case "Code11_Check_Digit_Verfication":
                pref = context.getString(R.string.setting_Code_11_Check_Digit_Verification);
                break;
            case "CLSI_Editing":
                pref = context.getString(R.string.setting_CLSI_Editing);
                break;
            case "NOTIS_Editing":
                pref = context.getString(R.string.setting_NOTIS_Editing);
                break;
            case "Convert_Interleaved_2_of_5_to_EAN_13":
                pref = context.getString(R.string.setting_Convert_Interleaved_2_of_5_to_EAN_13);
                break;
            case "Bookland_EAN":
                pref = context.getString(R.string.setting_Bookland_EAN);
                break;
            case "ISBT_128":
                pref = context.getString(R.string.setting_ISBT_128);
                break;
            case "UCC_Coupon_Extended_Code":
                pref = context.getString(R.string.setting_UCC_Coupon_Extended_Code);
                break;
            case "Convert_Code39_to_Code32":
                pref = context.getString(R.string.setting_Convert_Code_39_to_Code_32);
                break;
            case "US_Postnet":
                pref = context.getString(R.string.setting_US_Postnet);
                break;
            case "US_Planet":
                pref = context.getString(R.string.setting_US_Planet);
                break;
            case "UK_Postal":
                pref = context.getString(R.string.setting_UK_Postal);
                break;
            case "Transmit_US_Postal_CheckDigit":
                pref = context.getString(R.string.setting_Transmit_US_Postal_Check_Digit);
                break;
            case "Transmit_UK_Postal_Check_Digit":
                pref = context.getString(R.string.setting_Transmit_UK_Postal_Check_Digit);
                break;
            case "Code128_Emulation":
                pref = context.getString(R.string.setting_Code_128_Emulation);
                break;
            case "Decode_Session_Timeout":
                pref = context.getString(R.string.setting_Decode_Session_Timeout);
                break;
            case "Trigger_Mode":
                pref = context.getString(R.string.setting_Trigger_Modes);
                break;
            case "Code128_Set_Length1":
                pref = context.getString(R.string.setting_Code_128_Length_Parameter209);
                break;
            case "Code128_Set_Length2":
                pref = context.getString(R.string.setting_Code_128_Length_Parameter210);
                break;
            case "MicroPDF417":
                pref = context.getString(R.string.setting_MicroPDF417);
                break;
            case "Code32_Prefix":
                pref = context.getString(R.string.setting_Code_32_Prefix);
                break;
            case "Transmit_No_Read_Message":
                pref = context.getString(R.string.setting_Transmit_No_Read_Message);
                break;
            case "Security_Level":
                pref = context.getString(R.string.setting_Security_Level);
                break;
            case "Decoding_Autoexposure":
                pref = context.getString(R.string.setting_Exposure_Mode);
                break;
            case "Exposure_Time":
                pref = context.getString(R.string.setting_Fixed_Exposure_Time);
                break;
            case "Decoding_Illumination":
                pref = context.getString(R.string.setting_Decoding_Illumination);
                break;
            case "Decode_Aiming_Pattern":
                pref = context.getString(R.string.setting_Decode_Aiming_Pattern);
                break;
        }
        return pref;
    }

}
