package com.unitech.scanner.utility.config;

/**
 * 專案名稱:USU
 * 類描述:
 * 建立人:user
 * 建立時間:2020/9/1 下午 03:15
 * 修改人:user
 * 修改時間:2020/9/1 下午 03:15
 * 修改備註:
 */

public interface ErrorMessage {
    String RECEIVE_NAK = "The return command received is NAK.";
    String NO_PAIRING_BARCODE ="There is no paired barcode.";
    String NO_TARGET_SCANNER = "There is no target scanner.";
    String NO_BUNDLE = "There is no bundle inside the action.";
    String NO_CORRECT_TYPE = "There is no correct input type.";
    String NO_CORRECT_VALUE = "There is no correct input value.";
    String NO_CORRECT_RETURN_FORMAT = "No response in the correct format.";
    String NOT_CORRECT_FORMATTING_TYPE = "Not correct formatting type";
    String NOT_EXIST_FORMATTING_TYPE = "No exist type int the formatting";
    String NO_CORRECT_COMMUNICATION_PROTOCOL="There is no correct communication protocol.";
    String NO_DATA = "There is no data input of formatting.";
    String NOT_CONNECT = "It is not connected to the service.";
}
